package com.example.capstone_corona_app;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

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

    public View getView(int position, View convertView, ViewGroup parent) {
        Button button = null;

        if (null != convertView) {
            button = (Button)convertView;
        }

        else {
            //------------------------------------------------------------
            // 버튼을 생성하고 그것의 이름을 정합니다.
            button = new Button(context);
            button.setText(buttonNames[position]);

            //------------------------------------------------------------
            // 버튼 클릭에 대한 처리는 추후 구현 예정입니다.
        }

        return button;
    }
}
