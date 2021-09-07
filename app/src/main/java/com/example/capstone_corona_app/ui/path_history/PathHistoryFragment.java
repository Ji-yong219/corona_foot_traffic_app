package com.example.capstone_corona_app.ui.path_history;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.capstone_corona_app.ButtonAdapter;
import com.example.capstone_corona_app.MainActivity;
import com.example.capstone_corona_app.R;
import com.example.capstone_corona_app.ui.location.LocationViewModel;
import com.naver.maps.geometry.LatLng;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PathHistoryFragment extends Fragment {

    private PathHistoryViewModel pathHistoryViewModel;
    private LocationViewModel locationViewModel;
    private TextView contactText;
    private ImageView emoticon;
    private LinearLayout topLayout;
    private LinearLayout bottomLayout;

    MainActivity activity;


    public String[] getMonthListWithNow(){
        List<String> monthList = new ArrayList<String>();

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH)+1;

        for(int i=1 ; i<month+1 ; i++){
            monthList.add(String.valueOf(i)+"월");
        }

        return monthList.toArray(new String[0]);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        activity = (MainActivity)getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        pathHistoryViewModel =
                ViewModelProviders.of(this).get(PathHistoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_path_history, container, false);

        GridView gridViewPathHistory = (GridView) root.findViewById(R.id.gridViewPathHistory);
        ButtonAdapter buttonAdapter = new ButtonAdapter(getActivity(), getMonthListWithNow());
        gridViewPathHistory.setAdapter(buttonAdapter);
        super.onCreate(savedInstanceState);

        return root;
    }
}