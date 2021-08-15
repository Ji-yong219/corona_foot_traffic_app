package com.example.capstone_corona_app.ui.location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class LocationViewModel extends ViewModel {
    long mNow;
    Date mDate;
    int target_day;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
    SimpleDateFormat mFormatMonth = new SimpleDateFormat("MM월 dd일");
    Calendar cal = Calendar.getInstance();

    private MutableLiveData<String> mText;
    private MutableLiveData<String> mResult;

    public LocationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is location fragment");

    }

    public String getMapText(){
        String sResult;
        Calendar today_cal = Calendar.getInstance();
        Calendar start_cal = Calendar.getInstance();

        mNow = System.currentTimeMillis();

        mDate = new Date(mNow);
        today_cal.setTime(mDate);
        start_cal.add(Calendar.DATE, -14);

        sResult  = mFormatMonth.format(start_cal.getTime()) + "부터 ";
        sResult += mFormatMonth.format(today_cal.getTime()) + "까지\n";

        return sResult;
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getDate(){
        mResult = new MutableLiveData<>();
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        mResult.setValue( mFormat.format(mDate) );
        return mResult;
    }

    public String getDate(int num){
        Date resultDate;
        Calendar today_cal = Calendar.getInstance();
        Calendar ago_cal = Calendar.getInstance();

        target_day += num;

        mResult = new MutableLiveData<>();
        mNow = System.currentTimeMillis();

        mDate = new Date(mNow);
        today_cal.setTime(mDate);
        ago_cal.setTime(mDate);
        ago_cal.add(Calendar.DATE, -15);

        cal.setTime(mDate);

        // 날짜 이동 (화살표로 이동한 만큼)
        cal.add(Calendar.DATE, target_day);

        // 이동한 날짜가 현재보다 미래일 경우, 현재 값 반환

        if( today_cal.compareTo(cal) < 0){
            resultDate = today_cal.getTime();
            target_day = 0;
        }
        else if( ago_cal.compareTo(cal) > -1){
            ago_cal.add(Calendar.DATE, 1);
            resultDate = ago_cal.getTime();
            target_day = -14;
        }
        else{
            resultDate = cal.getTime();
        }

        return mFormat.format(resultDate);
    }
}