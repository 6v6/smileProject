package com.example.bomi.miinsu;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Geocoder geocoder;
    private Button button;
    private EditText editText;
    String addr=null;
    double longitude;
    double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        editText = (EditText)findViewById(R.id.editText);
        button = (Button)findViewById(R.id.button3);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions mOptions = new MarkerOptions();
                //마커 타이틀
                mOptions.title("현재 위치");
                Double latitude = point.latitude; //위도
                Double longitude = point.longitude; //경도
                //마커 스니펫 설정
                mOptions.snippet(latitude.toString() + ", "+ longitude.toString());
                //LatLng 위도 경도 쌍 나타냄
                mOptions.position(new LatLng(latitude,longitude));
                //마커 추가
                googleMap.addMarker(mOptions);
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void searchClick(View view) {

        geocoder = new Geocoder(this);
        String str = editText.getText().toString();
        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocationName(
                    str,
                    10);
            latitude = addressList.get(0).getLatitude();
            longitude = addressList.get(0).getLongitude();
            addr = addressList.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("lati: "+latitude+" / long: "+longitude+" / addr: "+addr);
        //, 기준으로 나누기

        //좌표 생성
        LatLng point = new LatLng(latitude,longitude);
        //마커 생성
        MarkerOptions mOptions2 = new MarkerOptions();
        mOptions2.title("검색 결과");
        mOptions2.snippet(addr);
        mOptions2.position(point);
        //마커 추가
        mMap.addMarker(mOptions2);
        //화면 줌
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));
    }

    public void onSetClicked(View view) {
        Intent intent = new Intent();
        intent.putExtra("place",addr);
        intent.putExtra("latitude",latitude); //위도
        intent.putExtra("longitude",longitude); //경도
        setResult(RESULT_OK,intent);
        finish();
    }
}
