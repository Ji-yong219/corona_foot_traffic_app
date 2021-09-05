package com.example.capstone_corona_app;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MonthButtonClickListener implements View.OnClickListener {

    // context는 버튼을 갖는 액티비티를 참조
    protected Context context = null;

    public MonthButtonClickListener(Context context) {
        this.context = context;
    }


    public void onClick(View v) {
        Button buttonClicked = (Button)v;

        //--------------------------------------------------------------

        String STR = buttonClicked.getText().toString();
        int month = Integer.parseInt(STR.substring(0, STR.length()-1));

        MainActivity main_activity = (MainActivity)context;
        main_activity.onFragmentChange(month);
    }
}
