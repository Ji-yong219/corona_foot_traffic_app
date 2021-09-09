package com.example.capstone_corona_app;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class RegisterRequest extends StringRequest{

    //서버 url 설정(php파일 연동)
    final static  private String URL="http://203.252.240.74:20909/signup.php";
    private Map<String,String>map;

    public RegisterRequest(String userID, String password, String gender, String name, int birth, String email, String phone, String admin, Response.Listener<String>listener){
        super(Method.POST,URL,listener,null);//위 url에 post방식으로 값을 전송

        map=new HashMap<>();
        map.put("id", userID);
        map.put("gender",gender);
        map.put("birth", birth+"");
        map.put("email", email);
        map.put("phone",phone);
        map.put("name", name);
        map.put("password", password);
        map.put("admin", admin+"");
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
