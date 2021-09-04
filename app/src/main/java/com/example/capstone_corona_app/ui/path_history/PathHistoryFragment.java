package com.example.capstone_corona_app.ui.path_history;

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


    public String[] getMonthListWithNow(){
        List<String> monthList = new ArrayList<String>();

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH)+1;

        for(int i=1 ; i<month+1 ; i++){
            monthList.add(String.valueOf(i)+"월");
        }

        return monthList.toArray(new String[0]);
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


//        locationViewModel =
//                ViewModelProviders.of(this).get(LocationViewModel.class);

//        final TextView textView = root.findViewById(R.id.text_path_history);
//        pathHistoryViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });


//        final TextView mapTextView = root.findViewById(R.id.textView1);
//        contactText = root.findViewById(R.id.textView2);
//        emoticon = root.findViewById(R.id.emoticon);
//        topLayout = root.findViewById(R.id.topLayout);
//        bottomLayout = root.findViewById(R.id.bottomLayout);
//
//        mapTextView.setText( locationViewModel.getMapText() );
//
//
//        Button btnCheckContact = (Button) root.findViewById(R.id.todayWriteButton);
//        btnCheckContact.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(isContact()){
//                    System.out.println("접촉");
//                    contactText.setText( "확진자 동선과 겹치셨어요" );
//                    topLayout.setBackgroundResource(R.drawable.round_border_sad_top);
//                    bottomLayout.setBackgroundResource(R.drawable.round_border_sad_bottom);
//                    emoticon.setImageResource(R.drawable.sadicon);
//                }
//                else{
//                    System.out.println("안전");
//                    contactText.setText( "안전하게 지내셨어요" );
//                    topLayout.setBackgroundResource(R.drawable.round_border_smile_top);
//                    bottomLayout.setBackgroundResource(R.drawable.round_border_smile_bottom);
//                    emoticon.setImageResource(R.drawable.smileicon);
//                }
//            }
//        });

        return root;
    }
    private ArrayList<String[]> getDataFromCSV(InputStreamReader target_csv){
        String[] nextLine = null;
        ArrayList<String[]> csv_arr = new ArrayList<String[]>();

        try {
            InputStreamReader is = target_csv;
            BufferedReader read = new BufferedReader(is);
            CSVReader reader = new CSVReader(read);
            while ((nextLine = reader.readNext()) != null) {
                csv_arr.add(nextLine);
            }
        } catch (IOException e) {

        }
        return csv_arr;
    }

    public boolean isContact(){
        ArrayList userRoutes = new ArrayList<>();
        for(String item : MainActivity.selectAllGPS()){
            userRoutes.add( item.split(";") );
        }

        ArrayList<String[]> routes = new ArrayList<String[]>();

        routes = getDataFromCSV( new InputStreamReader(getResources().openRawResource(R.raw.cb_routes)) );

        ArrayList<LatLng> LL_arr = new ArrayList<LatLng>();


        for(int j=0 ; j<userRoutes.size() ; j++){
            String[] temp = (String[]) userRoutes.get(j);
            Long userDate = Long.parseLong(temp[0]);
            Double userLat = Double.parseDouble(temp[1]);
            Double userLng = Double.parseDouble(temp[2]);


            for (int i = 1; i < routes.size(); i++) {
                if (routes.get(i)[5]!=null && !routes.get(i)[5].equals("")) {
                    Long date = Long.parseLong( routes.get(i)[1]+routes.get(i)[2] );
                    double venueLat = Double.valueOf( routes.get(i)[5] );
                    double venueLng = Double.valueOf( routes.get(i)[6] );

                    if(date <= userDate){
                        double latDistance = Math.toRadians(userLat - venueLat);
                        double lngDistance = Math.toRadians(userLng - venueLng);
                        double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
                                (Math.cos(Math.toRadians(userLat))) *
                                        (Math.cos(Math.toRadians(venueLat))) *
                                        (Math.sin(lngDistance / 2)) *
                                        (Math.sin(lngDistance / 2));

                        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

                        double dist = 6371 * c;
                        if (dist<0.05){ //(in km, you can use 0.1 for metres etc.)
                            /* If it's within 10m, we assume we're not moving */

                            return true;
                        }
                    }
//                        LL_arr.add(new LatLng(latitude, longitude));

//                    System.out.println( date+"\t:\t"+latitude +"\t" + longitude );


                }
            }
        }


        return false;
    }
}