package com.example.lks33.googlemap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.kgitbank.a6_googlemapexam.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements LocationListener
        , OnMapReadyCallback{ // 구글맵 등치가 크기 때문에 OnMapReadyCallback을 등록하여 준비 시켜야 한다.

    GoogleMap googleMap;
    MapFragment fragment;
    LocationManager lm;     // 위치 정보 객체
    Marker marker;          // 지도에 표시할 마커
    boolean permissionCK = false; // 퍼미션 허락 여부

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = (MapFragment)getFragmentManager().findFragmentById(R.id.fragment); // fragment 초기화
        fragment.getMapAsync(this); // 해당 프래그먼트에 구글맵을 깔라는 준비 과정

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); // GPS기능 사용을 위해 시스템 서비스를 불러와야 한다.
        // lm을 사용하기 전에 permission을 확인해야 한다.
        permissionCheck();
    }

    public void permissionCheck() {
        // 퍼미션이 시스템에 등록 되었는지 확인
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                // 퍼미션을 시스템에 등록할 지 여부를 확인한다. (마시멜로 버전)
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},100);
            }
        }   else {
            permissionCK = true;
        }
    }

    // 위치 수신을 시작하기 위한 메소드
    public void readyMap() {
        // 현재 사용 가능한 하드웨어 이름 얻기
        // => LocationManager.GPS_PROVIDER / LocationMananger.NETWORK_PROVIDER
        String provider = lm.getBestProvider(new Criteria(), true); // GPS 쓸건지, 데이터 쓸건지

        if(provider == null) {
            Toast.makeText(this, "위치 정보를 사용 가능한 상태가 아닙니다.",Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // 해당 장치가 마지막으로 수신한 위치 얻기
        Location location = lm.getLastKnownLocation(provider); // 퍼미션 체크 위에 메소드 잇기 때문에 추가 안해도 됨. 이거 빨간줄 무시
        if(location != null) {
            onLocationChanged(location);        // 이벤트 강제 호출
        }

        // 위치정보 취득 시작
        // 하드웨어이름, 갱신시간주기, 갱신거리주기
        lm.requestLocationUpdates(provider,1000,1,this); // 1초에 한번씩 gps값 가져옴
    }

    // 위치 수신을 종료하기 위한 메소드 재정의


    @Override
    protected void onPause() {
        super.onPause(); //화면이 꺼질 때 설정 사용, 화면을 끄고 있는데도 gps를 쓰고 있으면 안되니까...
        //위치정보 수신 종료
        lm.removeUpdates(this); // 화면 꺼져 있을 시 gps 기능 사용 중지
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        readyMap();
    }

    // 지정된 시간, 거리마다 한번씩 호출된다.
    public void onLocationChanged(Location location) {
        // 위도 경도 얻기
        double lat = location.getLatitude(); // 위도
        double lng = location.getLongitude(); // 경도

        // 구글맵에 위치 설정하기
        // 현재 위치 객체
        LatLng position = new LatLng(lat, lng);

        if(marker == null) {
            // 마커가 없을 경우, 새로 생성하여 지도에 추가
            MarkerOptions options = new MarkerOptions();
            options.position(position);
            options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
            marker = googleMap.addMarker(options);
        } else {
            // 이미 있는 경우 위치만 갱신
            marker.setPosition(position);
        }

        // zoom : 1~21 값이 커질수록 확대
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(position,19);
        // 현재 위치로 맵의 카메라 이동
        googleMap.animateCamera(camera);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap; //전역변수에 해당 매개변수를 세팅시키고

        if(permissionCK) readyMap(); // readyMap 메소드 호출하자
    }
}
