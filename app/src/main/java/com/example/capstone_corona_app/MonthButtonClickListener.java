package com.example.capstone_corona_app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MonthButtonClickListener implements View.OnClickListener {

    // context는 버튼을 갖는 액티비티를 참조할 것입니다.
    protected Context context = null;

    public MonthButtonClickListener(Context context) {
        this.context = context;
    }

    // 계산기의 버튼을 클릭하면,
    // 버튼의 내용(숫자, 연산자, 소수점 등)이 계산기의 화면에 입력되는 것으로 가정합니다.

    public void onClick(View v) {
        Button buttonClicked = (Button)v;

        //--------------------------------------------------------------

        String STR = buttonClicked.getText().toString();

        Log.i("click", STR.substring(0, STR.length()-1));
    }

    //--------------------------------------------------------------
    // editText 뷰의 텍스트에 str 문자열을 추가하는 메소드입니다.

    protected void appendString(EditText editText, String str) {
        StringBuilder textFromEditText = new StringBuilder(editText.getText().toString());

        if (null != textFromEditText) {
            textFromEditText.append(str);
            editText.setText(textFromEditText);
        }
    }
}
