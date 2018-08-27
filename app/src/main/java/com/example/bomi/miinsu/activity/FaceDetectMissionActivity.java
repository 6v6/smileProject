package com.example.bomi.miinsu.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.bomi.miinsu.ClMLHandler;
import com.example.bomi.miinsu.MissionList;
import com.example.bomi.miinsu.activity.ui.FaceOverlayView;
import com.example.bomi.miinsu.adapter.ImagePreviewAdapter;
import com.example.bomi.miinsu.model.FaceResult;
import com.example.bomi.miinsu.utils.CameraErrorCallback;
import com.example.bomi.miinsu.R;
import com.example.bomi.miinsu.utils.ImageUtils;
import com.example.bomi.miinsu.utils.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class FaceDetectMissionActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    // Number of Cameras in device.
    private int numberOfCameras;
    private final static int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;
    public static final String TAG = FaceDetectGrayActivity.class.getSimpleName();

    private Camera mCamera;
    private int cameraId = 0;

    // Let's keep track of the display rotation and orientation also:
    private int mDisplayRotation;
    private int mDisplayOrientation;

    private int previewWidth;
    private int previewHeight;

    // The surface view for the camera data
    private SurfaceView mView;
    private SurfaceHolder holder;
    private Paint paint = new Paint();

    // Draw rectangles and other fancy stuff:
    private FaceOverlayView mFaceView;

    // Log all errors:
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();


    private static final int MAX_FACE = 1;
    private boolean isThreadWorking = false;
    private Handler handler;
    private FaceDetectThread detectThread = null;
    private int prevSettingWidth;
    private int prevSettingHeight;
    private android.media.FaceDetector fdet;

    private byte[] grayBuff;
    private int bufflen;
    private int[] rgbs;

    private FaceResult faces[];
    private FaceResult faces_previous[];
    private int Id = 0;

    private String BUNDLE_CAMERA_ID = "camera";

    //미션 확인 및 일차바꾸기
    private static int MISSION_PASS = 1;
    private int smileCount=0;
    private FirebaseDatabase Database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = Database.getReference();
    private DatabaseReference userdb = Database.getReference("users");
    private FirebaseAuth mAuth;
    String email, ruser;
    String day;


    //RecylerView face image
    private HashMap<Integer, Integer> facesCount = new HashMap<>();
    private ImagePreviewAdapter imagePreviewAdapter;
    private ArrayList<Bitmap> facesBitmap;

    private  ClMLHandler clml;
    private String  happy;
    private TextView smileTv;
    private Button button;
    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_camera_viewer);
        button = (Button)findViewById(R.id.btnCapture);

        //cloud ml 연결
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        clml = new ClMLHandler(this);

        //미션 횟수 보여주기
        Intent mission = getIntent();
        Bundle extras = mission.getExtras();
        String total = extras.getString("Mission");

        smileTv = (TextView)findViewById(R.id.smileText);
        smileTv.setText(smileCount+"/"+total.substring(0,2));

        mView = (SurfaceView) findViewById(R.id.surfaceview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Now create the OverlayView:
        mFaceView = new FaceOverlayView(this);
        addContentView(mFaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // Create and Start the OrientationListener:
        handler = new Handler();
        faces = new FaceResult[MAX_FACE];
        faces_previous = new FaceResult[MAX_FACE];
        for (int i = 0; i < MAX_FACE; i++) {
            faces[i] = new FaceResult();
            faces_previous[i] = new FaceResult();
        }


        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("웃음확인");

        if (icicle != null)
            cameraId = icicle.getInt(BUNDLE_CAMERA_ID, 0);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        SurfaceHolder holder = mView.getHolder();
        holder.addCallback(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.switchCam:
                if (numberOfCameras == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Switch Camera").setMessage("Your device have one camera").setNeutralButton("Close", null);
                    AlertDialog alert = builder.create();
                    alert.show();
                    return true;
                }
                cameraId = (cameraId + 1) % numberOfCameras;
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");
        startPreview();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetData();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_CAMERA_ID, cameraId);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //Find the total number of cameras available
        resetData();

        numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                if (cameraId == 0) cameraId = i;
            }
        }

        mCamera = Camera.open(cameraId);

        Camera.getCameraInfo(cameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mFaceView.setFront(true);
        }

        try {
            mCamera.setPreviewDisplay(mView.getHolder());
        } catch (Exception e) {
            Log.e(TAG, "Could not preview the image.", e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        // We have no surface, return immediately:
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        // Try to stop the current preview:
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // Ignore...
        }

        configureCamera(width, height);
        setDisplayOrientation();
        setErrorCallback();

        // Create media.FaceDetector
        float aspect = (float) previewHeight / (float) previewWidth;
        fdet = new android.media.FaceDetector(prevSettingWidth, (int) (prevSettingWidth * aspect), MAX_FACE);

        bufflen = previewWidth * previewHeight;
        grayBuff = new byte[bufflen];
        rgbs = new int[bufflen];

        // Everything is configured! Finally start the camera preview again:
        startPreview();
    }

    private void setErrorCallback() {
        mCamera.setErrorCallback(mErrorCallback);
    }

    private void setDisplayOrientation() {
        // Now set the display orientation:
        mDisplayRotation = Util.getDisplayRotation(FaceDetectMissionActivity.this);
        mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, cameraId);

        mCamera.setDisplayOrientation(mDisplayOrientation);

        if (mFaceView != null) {
            mFaceView.setDisplayOrientation(mDisplayOrientation);
        }
    }

    private void configureCamera(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        // Set the PreviewSize and AutoFocus:
        setOptimalPreviewSize(parameters, width, height);
        setAutoFocus(parameters);
        // And set the parameters:
        mCamera.setParameters(parameters);
    }

    private void setOptimalPreviewSize(Camera.Parameters cameraParameters, int width, int height) {
        List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
        float targetRatio = (float) width / height;
        Camera.Size previewSize = Util.getOptimalPreviewSize(this, previewSizes, targetRatio);
        previewWidth = previewSize.width;
        previewHeight = previewSize.height;

        Log.e(TAG, "previewWidth" + previewWidth);
        Log.e(TAG, "previewHeight" + previewHeight);

        /**
         * Calculate size to scale full frame bitmap to smaller bitmap
         * Detect face in scaled bitmap have high performance than full bitmap.
         * The smaller image size -> detect faster, but distance to detect face shorter,
         * so calculate the size follow your purpose
         */
        if (previewWidth / 4 > 360) {
            prevSettingWidth = 360;
            prevSettingHeight = 270;
        } else if (previewWidth / 4 > 320) {
            prevSettingWidth = 320;
            prevSettingHeight = 240;
        } else if (previewWidth / 4 > 240) {
            prevSettingWidth = 240;
            prevSettingHeight = 160;
        } else {
            prevSettingWidth = 160;
            prevSettingHeight = 120;
        }

        cameraParameters.setPreviewSize(previewSize.width, previewSize.height);

        mFaceView.setPreviewWidth(previewWidth);
        mFaceView.setPreviewHeight(previewHeight);
    }

    private void setAutoFocus(Camera.Parameters cameraParameters) {
        List<String> focusModes = cameraParameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    private void startPreview() {
        if (mCamera != null) {
            isThreadWorking = false;
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.setPreviewCallbackWithBuffer(null);
        mCamera.setErrorCallback(null);
        mCamera.release();
        mCamera = null;
    }





    @Override
    public void onPreviewFrame(byte[] _data, Camera _camera) {
        if (!isThreadWorking) {
            isThreadWorking = true;
            waitForFdetThreadComplete();
            detectThread = new FaceDetectThread(handler, this);
            detectThread.setData(_data);
            detectThread.start();
        }
    }

    private void waitForFdetThreadComplete() {
        if (detectThread == null) {
            return;
        }

        if (detectThread.isAlive()) {
            try {
                detectThread.join();
                detectThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }




    /**
     * Do face detect in thread
     */
    private class FaceDetectThread extends Thread {
        private Handler handler;
        private byte[] data = null;
        private Context ctx;
        private Bitmap faceCroped;

        public FaceDetectThread(Handler handler, Context ctx) {
            this.ctx = ctx;
            this.handler = handler;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public void run() {
            float aspect = (float) previewHeight / (float) previewWidth;
            int w = prevSettingWidth;
            int h = (int) (prevSettingWidth * aspect);
            int orientation = setCameraDisplayOrientation(FaceDetectMissionActivity.this,
                    CAMERA_FACING, mCamera);

            //ByteBuffer bbuffer = ByteBuffer.wrap(data);
            //bbuffer.get(grayBuff, 0, bufflen);

            Bitmap bitmap = Bitmap.createBitmap(rgbs, previewWidth, previewHeight, Bitmap.Config.RGB_565);
            Bitmap bitmap2 = Bitmap.createBitmap(rgbs, previewWidth, previewHeight, Bitmap.Config.ARGB_4444);

            //이미지 저장
            YuvImage yuv = new YuvImage(data, ImageFormat.NV21,
                    bitmap.getWidth(), bitmap.getHeight(), null);
            Rect rectImage = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (!yuv.compressToJpeg(rectImage, 100, stream)) {
                Log.e("CreateBitmap", "compressToJpeg failed");
            }
            //위에 내용 안쓰면'int android.graphics.Bitmap.getWidth()' on a null object reference 오류

            BitmapFactory.Options bfo = new BitmapFactory.Options();
            bfo.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeStream(
                    new ByteArrayInputStream(stream.toByteArray()), null, bfo);
            Bitmap bmp = Bitmap.createScaledBitmap(bitmap, w, h, false);


            //bitmap을 byte array로 변환
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            final byte[] currentData = stream.toByteArray();

            //gray8toRGB32(grayBuff, previewWidth, previewHeight, rgbs);

            //버튼눌러 값 전송
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SaveImageTask().execute(currentData);
                    Intent intent=new Intent(getApplicationContext(),MissionList.class);
                    startActivity(intent);
                    finish();
                }
            });

            float xScale = (float) previewWidth / (float) prevSettingWidth;
            float yScale = (float) previewHeight / (float) h;

            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            int rotate = mDisplayOrientation;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mDisplayRotation % 180 == 0) {
                if (rotate + 180 > 360) {
                    rotate = rotate - 180;
                } else
                    rotate = rotate + 180;
            }

            switch (rotate) {
                case 90:
                    bmp = ImageUtils.rotate(bmp, 90);
                    xScale = (float) previewHeight / bmp.getWidth();
                    yScale = (float) previewWidth / bmp.getHeight();
                    break;
                case 180:
                    bmp = ImageUtils.rotate(bmp, 180);
                    break;
                case 270:
                    bmp = ImageUtils.rotate(bmp, 270);
                    xScale = (float) previewHeight / (float) h;
                    yScale = (float) previewWidth / (float) prevSettingWidth;
                    break;
            }

            fdet = new android.media.FaceDetector(bmp.getWidth(), bmp.getHeight(), MAX_FACE);
            android.media.FaceDetector.Face[] fullResults = new android.media.FaceDetector.Face[MAX_FACE];
            fdet.findFaces(bmp, fullResults);

            for (int i = 0; i < MAX_FACE; i++) {
                if (fullResults[i] == null) {
                    faces[i].clear();
                } else {
                    PointF mid = new PointF();
                    fullResults[i].getMidPoint(mid);
                    mid.x *= xScale;
                    mid.y *= yScale;

                    float eyesDis = fullResults[i].eyesDistance() * xScale;
                    //  float confidence = fullResults[i].confidence();
                    float pose = fullResults[i].pose(android.media.FaceDetector.Face.EULER_Y);
                    int idFace = Id;
                    Rect rect = new Rect(
                            (int) (mid.x - eyesDis * 1.20f),
                            (int) (mid.y - eyesDis * 0.55f),
                            (int) (mid.x + eyesDis * 1.20f),
                            (int) (mid.y + eyesDis * 1.85f));

                    /**
                     * Only detect face size > 100x100
                     */
                    if(rect.height() * rect.width() > 100 * 100) {
                        // Check this face and previous face have same ID?
                        for (int j = 0; j < MAX_FACE; j++) {
                            float eyesDisPre = faces_previous[j].eyesDistance();
                            PointF midPre = new PointF();
                            faces_previous[j].getMidPoint(midPre);

                            RectF rectCheck = new RectF(
                                    (midPre.x - eyesDisPre * 1.5f),
                                    (midPre.y - eyesDisPre * 1.15f),
                                    (midPre.x + eyesDisPre * 1.5f),
                                    (midPre.y + eyesDisPre * 1.85f));

                            if (rectCheck.contains(mid.x, mid.y) && (System.currentTimeMillis() - faces_previous[j].getTime()) < 1000) {
                                idFace = faces_previous[j].getId();
                                break;
                            }
                        }

                        if (idFace == Id) Id++;

                        faces[i].setFace(idFace, mid, eyesDis, pose, System.currentTimeMillis());

                        faces_previous[i].set(faces[i].getId(), faces[i].getMidEye(), faces[i].eyesDistance(), faces[i].getPose(), faces[i].getTime());

                        //
                        // if focus in a face 5 frame -> take picture face display in RecyclerView
                        // because of some first frame have low quality
                        //
                        if (facesCount.get(idFace) == null) {
                            facesCount.put(idFace, 0);
                        } else {
                            int count = facesCount.get(idFace) + 1;
                            if (count <= 5)
                                facesCount.put(idFace, count);
                            //
                            // Crop Face to display in RecylerView
                            //
                            if (count == 5) {
                                faceCroped = ImageUtils.cropFace(faces[i], bitmap, rotate);
                                if (faceCroped != null) {
                                    happy = clml.sendRequestToCMLE(faceCroped);
                                    happy = happy.substring(happy.indexOf(",")+1, happy.indexOf("]")-1);
                                    Log.e("response::",happy);

                                    final Handler handler = new Handler(Looper.getMainLooper());

                                    Intent mission = getIntent();
                                    Bundle extras = mission.getExtras();
                                    String clickMission = extras.getString("Mission");
                                    final String total = clickMission.substring(0, 2);
                                    final int[] time = {0};
                                    //Integer.parseInt(total)/10

                                    /*//초 미션
                                    if(clickMission.contains("초")){
                                       handler.post(new Runnable() {
                                           @Override
                                           public void run() {
                                               new CountDownTimer(10 * 1000, 1000) {
                                                   @Override
                                                   public void onTick(long millisUntilFinished) {
                                                       time[0]++;
                                                       smileTv.setText(time[0] +"/"+ total);
                                                   }

                                                   @Override
                                                   public void onFinish() {

                                                   }
                                               }.start();
                                           }
                                       });

                                        if(Double.parseDouble(happy)>0.3){
                                            onNextDay();
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    showDialog("통과하셨습니다!");
                                                }
                                            });
                                        }

                                    }*/

                                    //횟수 미션
                                    if(Double.parseDouble(happy)>0&&MISSION_PASS!=101) {
                                        numberMission();
                                    }

                                    handler.post(new Runnable() {
                                        public void run() {
                                            imagePreviewAdapter.add(faceCroped);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

            handler.post(new Runnable() {
                public void run() {
                    //send face to FaceView to draw rect
                    mFaceView.setFaces(faces);
                    isThreadWorking = false;
                }
            });
        }
        //날짜 바꾸기
        private void onNextDay(){
            //레퍼런스 정보 가져오기
            //db에서 users읽기
            mAuth=FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            email = user.getEmail();
            //내 이메일
            ruser = email.substring(0,email.indexOf("."));
            userdb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    day = dataSnapshot.child(ruser).child("challenge").getValue(String.class);

                    int newDay=Integer.parseInt(day);
                    newDay++;
                    myRef.child("users").child(ruser).child("challenge").setValue(String.valueOf(newDay));
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        //횟수 미션
        private void numberMission(){

            Intent mission = getIntent();
            Bundle extras = mission.getExtras();
            String clickMission = extras.getString("Mission");
            String total=clickMission.substring(0,1);

            smileCount++;
            if(smileCount==Integer.parseInt(total))
            {
                setText(smileTv,smileCount+"/"+total);
                onNextDay();
                MISSION_PASS=101;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showDialog("통과하셨습니다!");

                    }
                });
            }

            else{
                setText(smileTv,smileCount+"/"+total);
            }
        }


        private void showDialog(String msg) {

            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(FaceDetectMissionActivity.this);
            builder.setTitle("알림");
            builder.setMessage(msg);
            builder.setCancelable(true);
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    MISSION_PASS=1;
                    Intent intent=new Intent(getApplicationContext(),MissionList.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.create().show();
        }
    }

    /**
     * Release Memory
     */
    private void resetData() {
        if (imagePreviewAdapter == null) {
            facesBitmap = new ArrayList<>();
            imagePreviewAdapter = new ImagePreviewAdapter(FaceDetectMissionActivity.this, facesBitmap, new ImagePreviewAdapter.ViewHolder.OnItemClickListener() {
                @Override
                public void onClick(View v, int position) {
                    imagePreviewAdapter.setCheck(position);
                    imagePreviewAdapter.notifyDataSetChanged();
                }
            });
        } else {
            imagePreviewAdapter.clearAll();
        }
    }

    //사진 저장
    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/Testtest");
                dir.mkdirs();

                //fileName : 측정 값
                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to "
                        + outFile.getAbsolutePath());

                refreshGallery(outFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }
        private void refreshGallery(File file) {
            Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(file));
            sendBroadcast(mediaScanIntent);
        }

    }

    public static int setCameraDisplayOrientation(Activity activity,
                                                  int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private void setText(final TextView text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }
}
