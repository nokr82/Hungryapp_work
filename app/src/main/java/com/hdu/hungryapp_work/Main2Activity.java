package com.hdu.hungryapp_work;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main2Activity extends AppCompatActivity {


    EditText edNum,edTitle,edAdmin,edTel,edAdr;
    Button btRes2;


    Spinner spTbl,spMin,spMax;




    SharedPreferences appData;
    String id = null;





    String[] cate = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14"};

    double mapX,mapY;

    ArrayAdapter<String> arrayAdapter;

    String strcate,strcate2,strcate3;
    //지도관련, 1)현재위치
    LocationManager lm;
    String longit, latit;//경도, 위도
    SharedPreferences mapref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        edNum = findViewById(R.id.edNum);
        edTitle = findViewById(R.id.edTitle);
        edTel = findViewById(R.id.edTel);
        edAdmin = findViewById(R.id.edAdmin);
        edAdr  = findViewById(R.id.edAdr);
        btRes2 = findViewById(R.id.btnRes2);
        spTbl = findViewById(R.id.spTbl);
        spMin = findViewById(R.id.spMin);
        spMax = findViewById(R.id.spMax);

        //현재위치 받아오기, GPS, NETWORK값 받아오기
        lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(Build.VERSION.SDK_INT>=23){//마시멜로이상이면 권한요청하기

            //권한 없는 경우
            if(ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION , Manifest.permission.ACCESS_FINE_LOCATION} , 1);
            }
            //권한이 있는 경우
            else{
                //현재 위치 요청
                requestMyLocation();
            }
        }
        else{
            //현재위치요청
            requestMyLocation();
        }
    /*
            mapref=getSharedPreferences("userPos",0);
            //현재 virtualMachine에서 테스트 안되므로 기본값은 부평역으로 설정됨.
            longit = mapref.getString("longt", "37.489473");//경도,
            latit=mapref.getString("latt","126.724765");//위도
            //37.489473, 126.724765
    */

        //네이버지도 표시.
        mapFragment mFrag= new mapFragment();
        mFrag.setArguments(new Bundle());
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fTransaction = fm.beginTransaction();
        fTransaction.add(R.id.mapFrag, mFrag);
        fTransaction.commit();


        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,cate);
        spTbl.setAdapter(arrayAdapter);


        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,cate);
        spMin.setAdapter(arrayAdapter);

        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,cate);
        spMax.setAdapter(arrayAdapter);

        spTbl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strcate = cate[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spMin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strcate2 = cate[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spMax.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strcate3 = cate[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btRes2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpAddTask task = new HttpAddTask();
                String pnum = edNum.getText().toString();
                String shop_name = edTitle.getText().toString();
                String operator = edAdmin.getText().toString();
                String tel = edTel.getText().toString();
                String address = edAdr.getText().toString();
                String cate = strcate;
                String cate2 = strcate2;
                String cate3 = strcate3;
                String mapx2 = String.valueOf(mapX);
                String mapy2 = String.valueOf(mapY);

                task.execute(pnum,shop_name,operator,tel,address,mapx2,mapy2,cate,cate2,cate3);

            }
        });

    }//온

    class HttpAddTask extends AsyncTask<String, Void, String> {
        String address;
        String sendMsg, reciveMsg;
        JSONArray items;

        ProgressDialog dlg = new ProgressDialog(Main2Activity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address = "http://00645.net/eat/register.php";
            dlg.setMessage("등록중...");
            dlg.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dlg.dismiss();

            if (reciveMsg.equals("성공")) {
                Toast.makeText(getApplicationContext(), reciveMsg, Toast.LENGTH_SHORT).show();
                appData= getSharedPreferences("appData", MODE_PRIVATE);
                SharedPreferences.Editor editor = appData.edit();
                editor.putBoolean("save_login", true);//저장여부설정
                editor.putString("user_id", id); //db에서 받아온 아이디 값 저장
                editor.commit();
                Intent it = new Intent(Main2Activity.this,Main3Activity.class);
                startActivity(it);
            } else {
                Toast.makeText(getApplicationContext(), reciveMsg, Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(address);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

                sendMsg = "app=" + "shop"+"&pnum="+strings[0]+"&shop_name="+strings[1]+"&operator="+strings[2]+
                        "&tel="+strings[3]+"&address="+strings[4]+"&map_x="+strings[5]+"&map_y="+strings[6]+"&table_count="+strings[7]+"&table_size_min="+strings[8]+"&table_size_max="+strings[9];
                osw.write(sendMsg);
                osw.flush();
                osw.close();

                //app=shop&
                //pnum=1&
                //shop_name=2&
                //operator=3&
                //tel=4&
                //address=5&
                //map_x=6&
                //map_y=7&
                //table_count=8&
                //table_size_min=9&
                //table_size_max=10&

                int responseCode = conn.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {//stringbuffer에 계속 추가 저장
                    response.append(inputLine);
                }
                br.close();

                //전송받은 완성된 문자열을 JSON 객체에 넣는다.
                JSONObject jobj = new JSONObject(response.toString());//string buffer값을 json객체에 추가
                if(jobj.has("user_id")) {
                    //result = jobj.getJSONObject("list_item");
                    id=jobj.getString("user_id");
                    reciveMsg = "성공";

                }
                else {
                    reciveMsg = "실패";
                    //Toast.makeText(getApplicationContext(), "주소가 확인되지 않습니다. 정확한 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return reciveMsg;
        }
    }

    //나의 위치 요청
    public void requestMyLocation(){
        if(ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        //요청
        //gps검색
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, mlocationListener);
        //네트워크(와이파이)검색
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 10, mlocationListener);
    }

    //권한 요청후 응답 콜백
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //ACCESS_COARSE_LOCATION 권한
        if(requestCode==1){
            //권한받음
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                requestMyLocation();
            }
            //권한못받음
            else{
                Toast.makeText(this, "권한없음", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    //위치정보 구하기 리스너
    private final LocationListener mlocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            String provider = location.getProvider();   //위치제공자
            //if(longitude==0||latitude==0){
            latitude =37.49085971;
            longitude  =126.72073882;

            mapX = latitude;
            mapY = longitude;

            //default부평역
            //}
            longit=String.valueOf(longitude);
            latit=String.valueOf(latitude);
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            Toast.makeText(getApplicationContext(),"위치정보 : " + provider + "\n경도 : " + longitude + "\n위도 : " + latitude,
                    Toast.LENGTH_SHORT).show();
            lm.removeUpdates(mlocationListener);

            //임시저장
            mapref=getSharedPreferences("userPos",MODE_PRIVATE);
            SharedPreferences.Editor mapeditor=mapref.edit();

            mapeditor.putString("longt",longit);
            mapeditor.putString("latt",latit);
            mapeditor.commit();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { Log.d("gps", "onStatusChanged"); }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }
    };



}


