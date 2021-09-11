package com.example.capstone_corona_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity{
    private EditText et_id, et_pass;
    private Button btn_login,btn_register;
    private CheckBox admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_id = findViewById(R.id.et_id);
        et_pass = findViewById(R.id.et_pass);
//        admin = findViewById(R.id.et_admin);
        btn_login=findViewById(R.id.btn_login);
        btn_register=findViewById(R.id.btn_register);

        btn_register.setOnClickListener(new View.OnClickListener() {//회원가입 버튼을 클릭시 수행
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        
        //로그인 버튼 클릭시
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userID = et_id.getText().toString();
                final String userPass = et_pass.getText().toString();
//                final String admin = admin.getText().toString();


                Response.Listener<String> responseListener=new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            System.out.println("response:"+response);
//                            JSONObject jasonObject = new JSONObject(response);
                            JSONObject jasonObject = new JSONObject("{'success':'true'}");

                            boolean success = jasonObject.getBoolean("success");
                            success = true;

                            if (success) {//로그인 성공한 경우
//                                String id = jasonObject.getString("id");
//                                String pasword = jasonObject.getString("password");
//                                String admin = jasonObject.getString("admin");

                                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                intent.putExtra("log", "User");
//                                intent.putExtra("id", id);
//                                intent.putExtra("admin", admin);
                                startActivity(intent);
                            }


                            else{//회원등록 실패한 경우
                                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                                return;

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                LoginRequest loginRequest = new LoginRequest(userID, userPass, responseListener);
                RequestQueue queue= Volley.newRequestQueue(LoginActivity.this);
                queue.add(loginRequest);
            }
        });
    }
}
