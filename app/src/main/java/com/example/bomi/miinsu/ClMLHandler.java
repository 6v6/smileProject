package com.example.bomi.miinsu;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.ml.v1.CloudMachineLearningEngine;
import com.google.api.services.ml.v1.CloudMachineLearningEngineScopes;
import com.google.api.services.ml.v1.model.GoogleApiHttpBody;
import com.google.api.services.ml.v1.model.GoogleCloudMlV1PredictRequest;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class ClMLHandler {

    private static final String PROJECT_ID = "sublime-etching-210306";
    private static final String MODEL_NAME = "emotiontrain";
    private Activity mCurrentActivity;

    public ClMLHandler(Activity activity) {
        mCurrentActivity = activity;
        getCMLECredentials();
        setupCMLERequest();

    }
    //sendRequestToCMLE();


    private static final String TAG = "CMLEHANDLER";


    private static final Boolean DEBUG = false;

    long mRequestStartMs;
    long mRequestEndMs;

    private static final String INSTANCES = "instances";
    private static final String PREDICTIONS = "predictions";



    // credentials related to service account
    private GoogleCredential mCredentials = null;

    // CMLE instance for making request
    private CloudMachineLearningEngine mCloudMachineLearningEngine;

    // JSON request for prediction
    private GoogleCloudMlV1PredictRequest mRequestJson;

    // project path string related to project id and model name
    private String mProjectPath;





    // authenticate the service account associated with the CMLE project/model
    public void getCMLECredentials() {
        Log.d(TAG, "getCMLECredentials");
        // get application default credentials from service account json
        InputStream jsonCredentials = mCurrentActivity.getResources().openRawResource(R.raw.myproject9a745a65a876);
        try {
            mCredentials = GoogleCredential.fromStream(jsonCredentials).createScoped(
                    Collections.singleton(CloudMachineLearningEngineScopes.CLOUD_PLATFORM));
        } catch (IOException e) {
            Log.d(TAG, "You need to create service account and associated private key");
        } finally {
            try {
                jsonCredentials.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing input stream", e);
            }
        }
    }


    public void setupCMLERequest() {
        Log.d(TAG, "setupCMLERequest");
        // set project path
        mProjectPath = String.format("projects/%s/models/%s", PROJECT_ID, MODEL_NAME);

        // instantiate predict request
        mRequestJson = new GoogleCloudMlV1PredictRequest();

        // Set up the HTTP transport and JSON factory
        final HttpTransport httpTransport = new ApacheHttpTransport();
        //AndroidHttp.newCompatibleTransport();
        final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        // instantiate CloudMachineLearningEngine instance
        mCloudMachineLearningEngine = new CloudMachineLearningEngine.Builder(
                httpTransport,
                jsonFactory,
                mCredentials)
                .setApplicationName(mCurrentActivity.getPackageName())
                .build();
    }

    public String sendRequestToCMLE(Bitmap bitmap) {

        Bitmap resized = Bitmap.createScaledBitmap(bitmap,90, 90, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedByteArrayString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        String predictions="null";
        PixelStyleJSON imageStylePixels = new PixelStyleJSON();
        imageStylePixels.setImageBytesAndWeights(encodedByteArrayString);

        // set instances input
        mRequestJson.set(INSTANCES, imageStylePixels.objectifyImageStylePixels());


        GoogleApiHttpBody response = null;
        mRequestStartMs = SystemClock.elapsedRealtime();
        try {
            CloudMachineLearningEngine.Projects.Predict predict =
                    mCloudMachineLearningEngine.projects().predict(mProjectPath, mRequestJson);
            response = predict.execute();
            mRequestEndMs = SystemClock.elapsedRealtime();
            long lapseMs = mRequestEndMs - mRequestStartMs;

            Log.d(TAG, "response time: " + lapseMs);
        } catch (java.io.IOException io) {
            Log.d(TAG, "predict execution i/o error: " + io);
        }

        if (response != null) {
            Gson gson = new Gson();
            predictions = gson.toJson(response.get(PREDICTIONS));
            Log.d(TAG, "response.toString() : " + predictions);
        } else {
            Log.d(TAG, "Response body from CMLE is null.");
        }
        return predictions;
    }

}
