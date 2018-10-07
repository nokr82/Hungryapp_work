package com.hdu.hungryapp_work;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    ListView menulist;
    Button btReg;
    ImageView img;
    TextView txFood,txPrice,txScore;
    RatingBar Rbar;

    ArrayList<Mymenudata> arrdata = new ArrayList<>();


    ProgressDialog pDialog;
    MyAdapter mad;
    SharedPreferences appData;
    String id;


    Bitmap mbmp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        appData= getSharedPreferences("appData", MODE_PRIVATE);
        id=appData.getString("user_id", "");
        btReg = findViewById(R.id.btReg);
        menulist = findViewById(R.id.menulist);

        mad = new MyAdapter(this);

        menulist.setAdapter(mad);

        HttpListTask task = new HttpListTask();
        task.execute(id);

        String modi = getIntent().getStringExtra("modi");



        if (modi.equals("수정")){
            btReg.setText("수정할 메뉴를 선택하세요");
            btReg.setEnabled(false);
            menulist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent upit = new Intent(MenuActivity.this,ModifyActivity.class);
                //mTag,mComment,mPerprice
                    String pos = arrdata.get(position).mNo;
                    String ti = arrdata.get(position).mFood;
                    String score = arrdata.get(position).mScore;
                    String price = arrdata.get(position).mPrice;
                    String img = arrdata.get(position).mImg;
                    String tag = arrdata.get(position).mTag;
                    String comment  = arrdata.get(position).mComment;
                    String perprice = arrdata.get(position).mPerprice;

                    upit.putExtra("POS",pos);
                    upit.putExtra("TI",ti);
                    upit.putExtra("PER",score);
                    upit.putExtra("PRICE",price);
                    upit.putExtra("IMG",img);
                    upit.putExtra("TAG",tag);
                    upit.putExtra("COMMENT",comment);
                    upit.putExtra("PERPRICE",perprice);
                    startActivity(upit);



                }
            });

        }else if (modi.equals("등록")){
            btReg.setText("메뉴를 등록하세요");
            btReg.setEnabled(true);
        }


        btReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent it = new Intent(MenuActivity.this,MenuReg.class);
                startActivity(it);
            }
        });



    }//on

    @Override
    protected void onResume() {
        super.onResume();
        HttpListTask task = new HttpListTask();
        task.execute(id);

    }

    class MyAdapter extends BaseAdapter{

        Context con;
        MyAdapter(Context c){
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
                convertView = lif.inflate(R.layout.menu_item,parent,false);
            }
            txFood = convertView.findViewById(R.id.txFood);
            txPrice = convertView.findViewById(R.id.txPrice);
            txScore = convertView.findViewById(R.id.txScore);
            Rbar = convertView.findViewById(R.id.Rbar);
            img = convertView.findViewById(R.id.img);

            Mymenudata md = arrdata.get(position);

            txPrice.setText(String.valueOf(md.mPrice)+"원");
            txFood.setText(md.mFood);
            txScore.setText(md.mScore);
            Rbar.setRating(Integer.parseInt(md.mScore));

            LoadImgTask task = new LoadImgTask();

            task.execute(md.mImg);









            return convertView;
        }
    }
    //통신용 스레드(AsyncTask) 정의
    class HttpListTask extends AsyncTask<String, Void, String>{
        String address;
        String sendMsg, reciveMsg;

        ProgressDialog dlg = new ProgressDialog
                (MenuActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address = "http://00645.net/eat/menu_list.php";

            dlg.setMessage("로딩중...");
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
                        JSONArray jarray = jobj.getJSONArray("menu");

                  for(int i = 0; i < jarray.length(); i++){
                        JSONObject jobj2= jarray.getJSONObject(i);
                         Mymenudata tmp = new Mymenudata();
                         //mTag,mComment,mPerprice
                        tmp.mNo = jobj2.getString("idx");
                        tmp.mFood = jobj2.getString("name");
                        tmp.mScore = jobj2.getString("per");
                        tmp.mPrice = jobj2.getString("price");
                        tmp.mImg = jobj2.getString("img");
                        tmp.mTag = jobj2.getString("tag");
                        tmp.mComment = jobj2.getString("comment");
                        tmp.mPerprice = jobj2.getString("per_price");

                        arrdata.add(tmp);
                  }
                    reciveMsg = "전송성공";


            }catch (Exception e){
                e.printStackTrace();
                reciveMsg = "오류";
            }

            return reciveMsg;
        }
    }//스레드 끝

    class LoadImgTask extends AsyncTask<String,String,Bitmap>{

        ProgressDialog pDialog;


        @Override
        protected void onPreExecute() {
            pDialog  = new ProgressDialog(MenuActivity.this);

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null){
                img.setImageBitmap(bitmap);
                pDialog.dismiss();
            }else {
                pDialog.dismiss();
                Toast.makeText(MenuActivity.this,"이미지가없다",Toast.LENGTH_SHORT).show();
            }


            super.onPostExecute(bitmap);
        }


        @Override
        protected Bitmap doInBackground(String... strings) {

            try {
                URL url = new URL(strings[0]);
                URLConnection uc = url.openConnection();
                InputStream is = uc.getInputStream();

                mbmp = BitmapFactory.decodeStream(is);


            } catch (Exception e) {
                e.printStackTrace();
            }

            return mbmp;
        }
    }


}//클래스
