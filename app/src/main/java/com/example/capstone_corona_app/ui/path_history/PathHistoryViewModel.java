package com.example.capstone_corona_app.ui.path_history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PathHistoryViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PathHistoryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is PathHistory fragment");
    }



    public LiveData<String> getText() {
        return mText;
    }
}