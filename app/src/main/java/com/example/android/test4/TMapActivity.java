package com.example.android.test4;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import java.util.ArrayList;
import java.util.logging.LogManager;


import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;




public class TMapActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {


    TMapView mapView;

    LocationManager mLM;
    String mProvider = LocationManager.NETWORK_PROVIDER;
    TMapGpsManager tmapgps;
    private boolean m_bTrackingMode = true;
    private BluetoothSPP bt;


    EditText keywordView;
    //    ListView listView;
    ArrayAdapter<POI> mAdapter;

    TMapPoint start, end;
    ArrayList pass;
    RadioGroup typeView;
    TextView textview;
    Location cacheLocation = null;
    TMapPolyLine tMapPolyLine = new TMapPolyLine();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        typeView = (RadioGroup) findViewById(R.id.group_type);
        keywordView = (EditText) findViewById(R.id.edit_keyword);
        mAdapter = new ArrayAdapter<POI>(this, android.R.layout.simple_list_item_1);

        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapView = (TMapView) findViewById(R.id.map_view);


        mapView.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupMap();
                    }
                });
            }

            @Override
            public void SKTMapApikeyFailed(String s) {

            }
        });
        mapView.setSKTMapApiKey("2a33df12-85f2-490c-ac98-fd26abb771d1");
        mapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        Button btn = (Button) findViewById(R.id.btn_add_marker);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TMapPoint point = mapView.getCenterPoint();
                addMarker(point.getLatitude(), point.getLongitude(), "My Marker");
            }
        });

        final Context context = this;

        btn = (Button) findViewById(R.id.btn_search);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchPOI();
            }
        });


        btn = (Button) findViewById(R.id.search_load_start);
        btn.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       if (end != null) {
                                           switch (view.getId()) {
                                               case R.id.search_load_start:
                                                   AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                                   // 제목셋팅
                                                   alertDialogBuilder.setTitle("안내 시작");
                                                   // AlertDialog 셋팅
                                                   alertDialogBuilder.setMessage("GLASSES로 안내를 시작합니다.").setCancelable(false)
                                                           .setPositiveButton("안내종료", new DialogInterface.OnClickListener() {
                                                               public void onClick(
                                                                       DialogInterface dialog, int id) {
                                                                   // 프로그램을 종료한다
                                                                   TMapActivity.this.finish();
                                                               }
                                                           })
                                                           .setNegativeButton("창 닫기",
                                                                   new DialogInterface.OnClickListener() {
                                                                       public void onClick(
                                                                               DialogInterface dialog, int id) {
                                                                           // 다이얼로그를 취소한다
                                                                           dialog.cancel();
                                                                       }
                                                                   });
                                                   // 다이얼로그 생성
                                                   AlertDialog alertDialog = alertDialogBuilder.create();
                                                   // 다이얼로그 보여주기
                                                   alertDialog.show();
                                                   break;
                                           }
                                           Thread threadDis = new Thread(new Runnable() {
                                               String straight_distance = calcDistance(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());

                                               //                                           TMapPoint end = null;
                                               @Override
                                               public void run() {
                                                   while (true) {
                                                       Log.i("Thread", "" + 2);
                                                       Log.i("point", "" + start);

                                                       if (straight_distance != "0") {
                                                           setup(straight_distance);
                                                           Log.i("distance", "" + straight_distance);
                                                       }
                                                       try {
                                                           Thread.sleep(1000);
                                                       } catch (InterruptedException e) {
                                                           e.printStackTrace();
                                                       }
                                                   }
                                               }
                                           });
                                           threadDis.start();
                                       } else {
                                           Toast.makeText(TMapActivity.this, "도착지를 설정하지 않았습니다.", Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               });





        btn = (Button)findViewById(R.id.btn_route);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (end != null) {
                    Thread setloc = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(true) {
//                                tmapgps = new TMapGpsManager(TMapActivity.this);
//                                tmapgps.setMinTime(1000);//현재 위치를 찾을 최소 시간 (밀리초)
//                                tmapgps.setMinDistance(1);//현재 위치를 갱신할 최소 거리
                                tmapgps.setProvider(tmapgps.GPS_PROVIDER);//현재위치를 찾을 방법(네트워크)
                                tmapgps.OpenGps();//네트워크 위치 탐색 허용
                                start = mapView.getLocationPoint();
                                Log.i("Thread","" + 1);
                                    if (end != null)
                                        searchRoute(start, end);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    setloc.start();
                }
                else{
                    Toast.makeText(TMapActivity.this,"도착지를 설정하지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

                tmapgps = new TMapGpsManager(TMapActivity.this);
//                tmapgps.setMinTime(1000);//현재 위치를 찾을 최소 시간 (밀리초)
//                tmapgps.setMinDistance(1);//현재 위치를 갱신할 최소 거리
                tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);//현재위치를 찾을 방법(네트워크)
                tmapgps.OpenGps();//네트워크 위치 탐색 허용
                mapView.setIconVisibility(true);
                mapView.setCompassMode(true);







        //블루투스 통신-----------------------------------------------------------------------------
        bt = new BluetoothSPP(this); //Initializing

        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(TMapActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });


        Button btnConnect = findViewById(R.id.connection); //연결시도
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
    }


    int i = 0;
    //경로 탐색
    private void searchRoute(final TMapPoint start,final TMapPoint end){
        TMapData tMapData = new TMapData();

        tMapData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, start, end,pass,0,new TMapData.FindPathDataListenerCallback()  {
            String temp_distance;
            @Override
            public void onFindPathData(final TMapPolyLine tMapPolyLine) {
                mapView.addTMapPath(tMapPolyLine);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        for (; i < 1; i++){
                            tMapPolyLine.setLineWidth(5);
                        tMapPolyLine.setLineColor(Color.RED);

                        mapView.addTMapPath(tMapPolyLine);
                        Bitmap s = ((BitmapDrawable) ContextCompat.getDrawable(TMapActivity.this, android.R.drawable.ic_menu_myplaces)).getBitmap();
                        Bitmap e = ((BitmapDrawable) ContextCompat.getDrawable(TMapActivity.this, android.R.drawable.star_big_on)).getBitmap();
                        mapView.setTMapPathIcon(s, e);
                    }

                    // 데이터 확인용 ---------------------------------------------------------------------
//                        int Distance = (int)tMapPolyLine.getDistance();
//                        String distance1 = Integer.toString(Distance);
//                        textview = (TextView)findViewById(R.id.text1);
//                        textview.setText("총 거리:"+ distance1 +"M");



//                        double sLat = start.getLatitude();
//                        double sLng = start.getLongitude();
//                        double eLat = end.getLatitude();
//                        double eLng = end.getLongitude();
//
//
//
//                        temp_distance = calcDistance(sLat, sLng, eLat, eLng);
//                        textview = (TextView)findViewById(R.id.text6);
//                        textview.setText("도착지점과의 거리 :"+ temp_distance );
//                        Log.i("distance1",""+ temp_distance);
//                        setup(temp_distance);


//
//                        double Latitude_s = start.getLatitude();
//                        double Longitude_s = start.getLongitude();
//                        double Latitude_e = end.getLatitude();
//                        double Longitude_e = end.getLongitude();
//
//
//                        String start_lat = Double.toString(Latitude_s);
//                        String start_long = Double.toString(Longitude_s);
//                        String end_lat = Double.toString(Latitude_e);
//                        String end_long = Double.toString(Longitude_e);
//
//                        textview = (TextView)findViewById(R.id.text2);
//                        textview.setText("출발지 위도:"+start_lat );
//                        textview = (TextView)findViewById(R.id.text3);
//                        textview.setText("출발지 경도:"+start_long);
//                        textview = (TextView)findViewById(R.id.text4);
//                        textview.setText("도착지 위도:"+end_lat);
//                        textview = (TextView)findViewById(R.id.text5);
//                        textview.setText("도착지 위도:"+end_long);

                    }

                });

            }
        });

    }



    private void searchPOI() {
        TMapData data = new TMapData();
        String keyword = keywordView.getText().toString();
        if (!TextUtils.isEmpty(keyword)) {
            data.findAllPOI(keyword, new TMapData.FindAllPOIListenerCallback() {
                @Override
                public void onFindAllPOI(final ArrayList<TMapPOIItem> arrayList) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapView.removeAllMarkerItem();
                            mAdapter.clear();

                            for (TMapPOIItem poi : arrayList) {
                                addMarker(poi);
                                mAdapter.add(new POI(poi));
                            }

                            if (arrayList.size() > 0) {
                                TMapPOIItem poi = arrayList.get(0);
                                moveMap(poi.getPOIPoint().getLatitude(), poi.getPOIPoint().getLongitude());
                            }
                        }
                    });
                }
            });
        }
    }


    public void addMarker(TMapPOIItem poi) {
        TMapMarkerItem item = new TMapMarkerItem();
        item.setTMapPoint(poi.getPOIPoint());
        Bitmap icon = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_input_add)).getBitmap();
        item.setIcon(icon);
        item.setPosition(0.5f, 1);
        item.setCalloutTitle(poi.getPOIName());
        item.setCalloutSubTitle(poi.getPOIContent());
        Bitmap left = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_info)).getBitmap();
        item.setCalloutLeftImage(left);
        Bitmap right = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_menu_add)).getBitmap();
        item.setCalloutRightButtonImage(right);
        item.setCanShowCallout(true);
        mapView.addMarkerItem(poi.getPOIID(), item);
    }

    private void addMarker(double lat, double lng, String title) {
        TMapMarkerItem item = new TMapMarkerItem();
        TMapPoint point = new TMapPoint(lat, lng);
        item.setTMapPoint(point);
        Bitmap icon = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_input_add)).getBitmap();
        item.setIcon(icon);
        item.setPosition(0.5f, 1);
        item.setCalloutTitle(title);
        item.setCalloutSubTitle("sub " + title);
        Bitmap left = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_info)).getBitmap();
        item.setCalloutLeftImage(left);
        Bitmap right = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_menu_add)).getBitmap();
        item.setCalloutRightButtonImage(right);
        item.setCanShowCallout(true);
        mapView.addMarkerItem("m" + id, item);
        id++;
    }

    int id = 0;

    boolean isInitialized = false;

    private void setupMap() {
        isInitialized = true;
        mapView.setMapType(TMapView.MAPTYPE_STANDARD);
                mapView.setSightVisible(true);
                mapView.setCompassMode(true);
//                mapView.setTrafficInfo(true);
                mapView.setTrackingMode(true);

        if (cacheLocation != null) {
            moveMap(cacheLocation.getLatitude(), cacheLocation.getLongitude());
            setMyLocation(cacheLocation.getLatitude(), cacheLocation.getLongitude());
        }
        mapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem tMapMarkerItem) {
                String message = null;
//                switch (typeView.getCheckedRadioButtonId()){
//
//                    case R.id.radio_end:
//                        start = tmapgps.getLocation();
                        end = tMapMarkerItem.getTMapPoint();
                        message = "도착지";

//                        break;
//                }


                Toast.makeText(TMapActivity.this,message + " 설정 완료",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = mLM.getLastKnownLocation(mProvider);
        if (location != null) {
            mListener.onLocationChanged(location);
        }
        mLM.requestSingleUpdate(mProvider, mListener, null);

        if (!bt.isBluetoothEnabled()) { //
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
    } else {
        if (!bt.isServiceAvailable()) {
            bt.setupService();
            bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리

        }
    }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.removeUpdates(mListener);
    }


    //뷰에 보이는 맵의 위치 변경
    private void moveMap(double lat, double lng) {
        mapView.setCenterPoint(lng, lat);
    }

    //현재 위치 설정
    private void setMyLocation(double lat, double lng) {
        Bitmap icon = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_menu_mylocation)).getBitmap();
        mapView.setIcon(icon);
        mapView.setLocationPoint(lng, lat);
        mapView.setIconVisibility(true);
    }


    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (isInitialized) {
                moveMap(location.getLatitude(),location.getLongitude());
                setMyLocation(location.getLatitude(),location.getLongitude());
            } else {
                cacheLocation = location;
            }
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
    };



    @Override
    public void onLocationChange(Location location) {
//           mapView.setLocationPoint(location.getLatitude(), location.getLongitude());

    }

//    public void setGps() {
//        final LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {}
//        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자(실내에선 NETWORK_PROVIDER 권장)
//                1000, // 통지사이의 최소 시간간격 (miliSecond)
//                1, // 통지사이의 최소 변경거리 (m)
//                mListener);
//    }


    //두 좌표간 거리 계산
    public static String calcDistance(double lat1, double lon1, double lat2, double lon2){
        double EARTH_R, Rad, radLat1, radLat2, radDist;
        double distance, ret;

        EARTH_R = 6371000.0;
        Rad = Math.PI/180;
        radLat1 = Rad * lat1;
        radLat2 = Rad * lat2;
        radDist = Rad * (lon1 - lon2);

        distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        ret = EARTH_R * Math.acos(distance);


        String result =  Math.round(ret) +" m/";

        return result;
    }

    //아두이노로 블루투스 데이터 전송
    public void setup(final String  distance) {
//        Button btnSend = findViewById(R.id.search_load_start); //데이터 전송
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
                String sendDistance = distance;
                bt.send( sendDistance, true);
            }
//        });
//    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);

            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}


