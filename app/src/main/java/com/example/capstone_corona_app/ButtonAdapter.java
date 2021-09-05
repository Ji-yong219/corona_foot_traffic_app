package com.example.capstone_corona_app;


import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.provider.CalendarContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import androidx.annotation.RequiresApi;

public class ButtonAdapter extends BaseAdapter{
    // context는 이 어댑터 객체를 정의하는 액티비티를 참조할 것입니다.
    Context context = null;

    // buttonNames는 액티비티에서 정의된 buttonNames 문자열(버튼 이름들을 담는 배열)을 가리킬 것입니다.
    String[] buttonNames = null;

    public ButtonAdapter(Context context, String[] buttonNames) {
        this.context = context;
        this.buttonNames = buttonNames;
    }

    public int getCount() {
        return (null != buttonNames) ? buttonNames.length : 0;
    }

    public Object getItem(int position) {
        return buttonNames[position];
    }

    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public View getView(int position, View convertView, ViewGroup parent) {
        Button button = null;

        if (null != convertView) {
            button = (Button)convertView;
        }

        else {
            //------------------------------------------------------------
            // 버튼을 생성하고 그것의 이름을 정합니다.
            GridView.LayoutParams params = new GridView.LayoutParams(200, 200);

            button = new Button(context);
            button.setText(buttonNames[position]);
            button.setLayoutParams(params);
            button.setTextSize(18);
            button.setBackgroundColor(button.getContext().getResources().getColor(R.color.colorButton));
//            button.setCornerRadius

            //------------------------------------------------------------

            button.setOnClickListener(new MonthButtonClickListener(context));
        }

        return button;
    }
}