package com.hdu.hungryapp_work;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ModifyActivity extends AppCompatActivity {
    private static final int PICK_FROM_CAMERA = 0;

    private static final int PICK_FROM_ALBUM = 1;

    private static final int CROP_FROM_CAMERA = 2;

    Bitmap mbmp;

    private Uri mImageCaptureUri;

    private ImageView mPhotoImageView;

    private Button mButton;

    EditText edName,edPrice,edPer,edPerPrice,edTag,edCom;
    Button btReg2,btDel;
    SharedPreferences appData;
    String id = null;
    byte[] data;


    String strNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);
        edName = findViewById(R.id.edName);
        edPrice = findViewById(R.id.edPrice);
        edPer = findViewById(R.id.edPer);
        edPerPrice = findViewById(R.id.edPerPrice);
        edTag = findViewById(R.id.edTag);
        edCom = findViewById(R.id.edCom);
        btReg2 = findViewById(R.id.btReg2);
        mButton = findViewById(R.id.button);
        mPhotoImageView = findViewById(R.id.image);
        btDel = findViewById(R.id.btDel);

//upit.putExtra("POS",pos);
//                    upit.putExtra("TI",ti);
//                    upit.putExtra("SCORE",score);
//                    upit.putExtra("PRICE",price);
//                    upit.putExtra("IMG",img);
//                    upit.putExtra("TAG",tag);
//                    upit.putExtra("COMMENT",comment);
//                    upit.putExtra("PERPRICE",perprice);
        Intent upit = getIntent();
        String ti = upit.getStringExtra("TI");
        edName.setText(ti);
        String pr = upit.getStringExtra("PRICE");
        edPrice.setText(pr);
        String per = upit.getStringExtra("PER");
        edPer.setText(per);
        String img = upit.getStringExtra("IMG");
        LoadImgTask task = new LoadImgTask();
        task.execute(img);
        String tag = upit.getStringExtra("TAG");
        edTag.setText(tag);
        String comment = upit.getStringExtra("COMMENT");
        edCom.setText(comment);
        String perprice = upit.getStringExtra("PERPRICE");
        edPerPrice.setText(perprice);
        String pos = upit.getStringExtra("POS");
        strNum = pos;

        appData= getSharedPreferences("appData", MODE_PRIVATE);
        id=appData.getString("user_id", "");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModifyActivity.this.onClick(v);
            }
        });


        btDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpDeltask task = new HttpDeltask();
                String user_id = id;
                String idx = strNum;

                task.execute(user_id,idx);


            }
        });


        btReg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpTask task = new HttpTask();
                String idx = strNum;
                String user_id = id;
                String name = edName.getText().toString();
                String price = edPrice.getText().toString();
                String per = edPer.getText().toString();
                String per_price = edPerPrice.getText().toString();
                String tag = edTag.getText().toString();
                String comment = edCom.getText().toString();


//
//                sendMsg ="app=shop" +"&user_id="+strings[0]+"&name="+strings[1]+"&fileToUpload="+"aaa.jpg"+
//                        "&price="+strings[2]+"&per="+strings[3]+"&per_price="+strings[4]+"&tag="+strings[5]+"&comment="+strings[6];
//
                task.execute(idx,user_id,name,price,per,per_price,tag,comment);
            }
        });

    }//온

    //------------------------------
    //   Http Post로 주고 받기
    //------------------------------
    private class HttpTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        ProgressDialog dlg = new ProgressDialog(ModifyActivity.this);
        JSONArray items;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dlg.setMessage("전송 중");
            dlg.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dlg.dismiss();
            if (receiveMsg.equals("성공")) {
                Toast.makeText(getApplicationContext(), "전송 성공", Toast.LENGTH_SHORT).show();
                Intent it = new Intent(ModifyActivity.this,MenuActivity.class);
                it.putExtra("modi","수정");
                startActivity(it);
            } else {
                Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";

                //--------------------------
                //   이미지뷰에 로드된 이미지를 바이트로 만들어서 전송 준비
                //--------------------------
                BitmapDrawable bmpDrw = (BitmapDrawable)mPhotoImageView.getDrawable();
                Bitmap bmp = bmpDrw.getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                data = stream.toByteArray();
                stream.close();

                //--------------------------
                //   URL 설정하고 접속하기
                //--------------------------
                URL url = new URL("http://00645.net/eat/menu_admin.php");       // URL 설정
                HttpURLConnection http = (HttpURLConnection) url.openConnection();   // 접속
                //--------------------------
                //   전송 모드 설정 - 기본적인 설정이다
                //--------------------------
                http.setDefaultUseCaches(false);
                http.setDoInput(true);                         // 서버에서 읽기 모드 지정
                http.setDoOutput(true);                       // 서버로 쓰기 모드 지정
                http.setRequestMethod("POST");         // 전송 방식은 POST

                // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
                http.setRequestProperty("Connection", "Keep-Alive");
                http.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                //--------------------------
                //   서버로 값 전송
                //--------------------------
                DataOutputStream dos = new DataOutputStream(http.getOutputStream());

                String[] paramNames = {"app", "idx","user_id", "name","price","per","per_price","tag","comment"}; // 쿼리스트링 변수명
                String[] paramValues = {"shop", strings[0], strings[1],strings[2],strings[3],strings[4],strings[5],strings[6],strings[7]}; // 쿼리스트링 값
//sendMsg ="app=" + "shop" +"&save=new"+"&user_id="+strings[0]+"&name="+strings[1]+"&fileToUpload="+"aaa.jpg"+
//                        "&price="+strings[2]+"&per="+strings[3]+"&per_price="+strings[4]+"&tag="+strings[5]+
//                        "&comment="+strings[6];
                for(int i =0; i<paramNames.length;i++){
                    dos.writeBytes(twoHyphens + boundary + lineEnd); //필드 구분자 시작
                    dos.writeBytes("Content-Disposition: form-data; name=\""+paramNames[i]+"\""+ lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(paramValues[i]);
                    dos.writeBytes(lineEnd);
                }

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\";filename=\"menu_img.jpg\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.write(data,0,data.length);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dos.flush(); // finish upload...
                dos.close();

                //--------------------------
                //   서버에서 전송받기
                //--------------------------
                int responseCode = http.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(http.getInputStream()));
                    receiveMsg = "성공";
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(http.getErrorStream()));
                    receiveMsg = "실패";
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                    //Toast.makeText(getApplicationContext(), inputLine, Toast.LENGTH_SHORT).show();
                }
                br.close();

            } catch (Exception e) {
                //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                //
            } // try
            return receiveMsg;
        }
    }

    //메뉴삭제
    class HttpDeltask extends AsyncTask<String,Void,String>{
        String address;
        String sendMsg, reciveMsg;

        ProgressDialog dlg = new ProgressDialog(ModifyActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address = "http://00645.net/eat/menu_del.php";
            dlg.setMessage("삭제중...");
            dlg.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dlg.dismiss();
            if (reciveMsg.equals("성공")){
                Toast.makeText(getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
                Intent it = new Intent(ModifyActivity.this,MenuActivity.class);
                it.putExtra("modi","수정");
                startActivity(it);
            }else {
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

                sendMsg ="app=shop"+"&user_id="+strings[0]+"&idx="+strings[1];

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
                    reciveMsg = "성공";
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    reciveMsg = "실패";
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {//stringbuffer에 계속 추가 저장
                    response.append(inputLine);
                }
                br.close();


            } catch (Exception e) {
                e.printStackTrace();
            }
            return reciveMsg;
        }
    }






//사진가져오기
    class LoadImgTask extends AsyncTask<String,String,Bitmap>{

        ProgressDialog pDialog;


        @Override
        protected void onPreExecute() {
            pDialog  = new ProgressDialog(ModifyActivity.this);

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null){
                mPhotoImageView.setImageBitmap(bitmap);
                pDialog.dismiss();
            }else {
                pDialog.dismiss();
                Toast.makeText(ModifyActivity.this,"이미지가없다",Toast.LENGTH_SHORT).show();
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

    //카메라
    private void doTakePhotoAction()

    {





        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);



        // 임시로 사용할 파일의 경로를 생성

        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";

        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));



        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

        // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.

        //intent.putExtra("return-data", true);

        startActivityForResult(intent, PICK_FROM_CAMERA);

    }



    /**

     * 앨범에서 이미지 가져오기

     */

    private void doTakeAlbumAction()

    {

        // 앨범 호출

        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);

        startActivityForResult(intent, PICK_FROM_ALBUM);

    }



    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data)

    {

        if(resultCode != RESULT_OK)

        {

            return;

        }



        switch(requestCode)

        {

            case CROP_FROM_CAMERA:

            {

                // 크롭이 된 이후의 이미지를 넘겨 받습니다.

                // 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에

                // 임시 파일을 삭제합니다.

                final Bundle extras = data.getExtras();



                if(extras != null)

                {

                    Bitmap photo = extras.getParcelable("data");

                    mPhotoImageView.setImageBitmap(photo);

                }



                // 임시 파일 삭제

                File f = new File(mImageCaptureUri.getPath());

                if(f.exists())

                {

                    f.delete();

                }



                break;

            }



            case PICK_FROM_ALBUM:

            {

                // 이후의 처리가 카메라와 같으므로 일단  break없이 진행합니다.

                // 실제 코드에서는 좀더 합리적인 방법을 선택하시기 바랍니다.



                mImageCaptureUri = data.getData();

            }



            case PICK_FROM_CAMERA:

            {

                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정합니다.

                // 이후에 이미지 크롭 어플리케이션을 호출하게 됩니다.



                Intent intent = new Intent("com.android.camera.action.CROP");

                intent.setDataAndType(mImageCaptureUri, "image/*");



                intent.putExtra("outputX", 90);

                intent.putExtra("outputY", 90);

                intent.putExtra("aspectX", 1);

                intent.putExtra("aspectY", 1);

                intent.putExtra("scale", true);

                intent.putExtra("return-data", true);

                startActivityForResult(intent, CROP_FROM_CAMERA);



                break;

            }

        }

    }


    public void onClick(View v)

    {

        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener()

        {

            @Override

            public void onClick(DialogInterface dialog, int which)

            {

                doTakePhotoAction();

            }

        };



        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener()

        {

            @Override

            public void onClick(DialogInterface dialog, int which)

            {

                doTakeAlbumAction();

            }

        };



        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener()

        {

            @Override

            public void onClick(DialogInterface dialog, int which)

            {

                dialog.dismiss();

            }

        };



        new AlertDialog.Builder(this)

                .setTitle("업로드할 이미지 선택")

                .setPositiveButton("사진촬영", cameraListener)

                .setNeutralButton("앨범선택", albumListener)

                .setNegativeButton("취소", cancelListener)

                .show();

    }



}//클래스
