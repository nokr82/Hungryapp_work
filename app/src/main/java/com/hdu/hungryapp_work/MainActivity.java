package com.hdu.hungryapp_work;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnRes;
    SharedPreferences appData;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnRes = findViewById(R.id.btnRes);

        appData= getSharedPreferences("appData", MODE_PRIVATE);
        id=appData.getString("user_id", "");


        btnRes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id.equals("")){
                    Intent it = new Intent(MainActivity.this,Main2Activity.class);
                    startActivity(it);
                }else{
                    Intent it = new Intent(MainActivity.this,Main3Activity.class);
                    startActivity(it);
                }
            }
        });


    }
}

