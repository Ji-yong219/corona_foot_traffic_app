package com.example.capstone_corona_app;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest{

    //서버 url 설정(php파일 연동)
    final static  private String URL = "http://203.252.240.74:20909/login.php";
    private Map<String,String> map;

    public LoginRequest(String userID, String userPassword, Response.Listener<String>listener){
        super(Method.POST, URL, listener,null);

        map =new HashMap<>();
        map.put("id", userID);
        map.put("password", userPassword);

    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
