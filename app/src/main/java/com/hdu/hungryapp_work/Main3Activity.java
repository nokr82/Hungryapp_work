package com.hdu.hungryapp_work;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Main3Activity extends AppCompatActivity {

    Button btMenu,btModi;
    ListView listReview;

    TextView txReview,txWdate;

    ArrayList<Reviewdata> arrdata = new ArrayList<>();

    SharedPreferences appData;
    String id;

    Myadapter mad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        btMenu = findViewById(R.id.btMenu);
        btModi = findViewById(R.id.btModi);

        listReview = findViewById(R.id.listReview);
        appData= getSharedPreferences("appData", MODE_PRIVATE);
        id=appData.getString("user_id", "");


        mad = new Myadapter(this);

        listReview.setAdapter(mad);



        listReview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent reit = new Intent(Main3Activity.this,ReviewActivity.class);
               //   rd.mReview = jobj2.getString("comment");
                //                   rd.mWdate = jobj2.getString("wdate");
                //                   rd.midx = jobj2.getString("idx");
                String pos = arrdata.get(position).midx;
                String comment = arrdata.get(position).mReview;
                String wdate  = arrdata.get(position).mWdate;

                reit.putExtra("pos",pos);
                reit.putExtra("comment",comment);
                reit.putExtra("wdate",wdate);

                startActivity(reit);

            }
        });



        btModi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(Main3Activity.this,MenuActivity.class);
                it.putExtra("modi","수정");

                startActivity(it);

            }
        });


        btMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(Main3Activity.this,MenuActivity.class);
                it.putExtra("modi","등록");
                startActivity(it);
            }
        });


    }//온

    @Override
    protected void onResume() {
        super.onResume();
        HttplistTask task = new HttplistTask();
        task.execute(id);
    }

    class Myadapter extends BaseAdapter{
        Context con;
        Myadapter(Context c){
            con = c;
        }
        @Override
        public int getCount() {
            return arrdata.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                LayoutInflater lif = LayoutInflater.from(con);
                convertView = lif.inflate(R.layout.review_item,parent,false);
            }
            txReview = convertView.findViewById(R.id.txReview);
            txWdate = convertView.findViewById(R.id.txWdate);

            Reviewdata rd = arrdata.get(position);


            txReview.setText(rd.mReview);
            txWdate.setText(rd.mWdate);



            return convertView;
        }
    }

    //리뷰리스트
    //http://00645.net/eat/review_list.php?app=shop&user_id=1

    class HttplistTask extends AsyncTask<String, Void, String>{
        String address;
        String sendMsg, reciveMsg;

        ProgressDialog dlg = new ProgressDialog
                (Main3Activity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address = "http://00645.net/eat/review_list.php";

            dlg.setMessage("리뷰준비중...");
            dlg.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dlg.dismiss();//프로그래스 종료

            if(reciveMsg != null && reciveMsg.equals("전송성공")){
                mad.notifyDataSetChanged();
            }
            else{
                Toast.makeText(getApplicationContext(),
                        reciveMsg,
                        Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                URL url = new URL(address);

                HttpURLConnection conn =
                        (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset:UTF-8");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

                sendMsg = "app=shop&user_id=" + strings[0];

                osw.write(sendMsg);
                osw.flush();
                osw.close();

                int responseCode = conn.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                if ((inputLine = br.readLine()) != null) {//stringbuffer에 계속 추가 저장
                    response.append(inputLine);
                }
                br.close();

                //String receiveStr = buffer.toString();

                //JSON 배열에 최종 완성된 문자열 입력
                JSONObject jobj = new JSONObject(response.toString());

                //리스트 데이터 원본 비우기
                arrdata.clear();




                //JSONArray 내부에 저장된 JSONObject를
                //하나씩 가져와서 값을 얻는다.
                //String num= String.valueOf(i);
                JSONArray jarray = jobj.getJSONArray("review");

                for(int i = 0; i < jarray.length(); i++){
                    JSONObject jobj2= jarray.getJSONObject(i);
                    Reviewdata rd = new Reviewdata();
                    //mTag,mComment,mPerprice
                   rd.mReview = jobj2.getString("comment");
                   rd.mWdate = jobj2.getString("wdate");
                   rd.midx = jobj2.getString("idx");





                    arrdata.add(rd);
                }
                reciveMsg = "전송성공";


            }catch (Exception e){
                e.printStackTrace();
                reciveMsg = "오류";
            }
            return reciveMsg;
        }
    }



}//클래스
