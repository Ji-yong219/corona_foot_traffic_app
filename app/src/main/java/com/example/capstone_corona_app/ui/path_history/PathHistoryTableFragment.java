package com.example.capstone_corona_app.ui.path_history;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.capstone_corona_app.ButtonAdapter;
import com.example.capstone_corona_app.JsonReader;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

public class PathHistoryTableFragment extends Fragment {

    private PathHistoryViewModel pathHistoryViewModel;
    private LocationViewModel locationViewModel;
    private TextView contactText;
    private ImageView emoticon;
    private LinearLayout topLayout;
    private LinearLayout bottomLayout;

    private TableLayout tableLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        pathHistoryViewModel =
                ViewModelProviders.of(this).get(PathHistoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_path_history_table, container, false);

        super.onCreate(savedInstanceState);


        tableLayout = (TableLayout) root.findViewById(R.id.tablelayout);

        int month = ((MainActivity)getActivity()).getPathMonth();
//            isContact()

        ArrayList<String> user_LL_arr = getMonthRoutes(month);

        for(int i = 0 ; i < user_LL_arr.size() ; i++) {
            TableRow tableRow = new TableRow(container.getContext());
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 6
            ));

            String sUser_LL = user_LL_arr.get(i);
            String[] aUser_LL = sUser_LL.split(";");


            TextView textViewDate = new TextView(container.getContext());
            String tempDate = aUser_LL[0].substring(4, 6) + "." + aUser_LL[0].substring(6, 8);
            textViewDate.setText(tempDate);
            textViewDate.setGravity(Gravity.CENTER);
            textViewDate.setTextSize(18);
            textViewDate.setBackgroundResource(R.drawable.table_border);

            textViewDate.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1f
            ));


            TextView textViewTime = new TextView(container.getContext());
            textViewTime.setText(String.valueOf(aUser_LL[1].substring(0, 4)));
            textViewTime.setGravity(Gravity.CENTER);
            textViewTime.setTextSize(18);
            textViewTime.setBackgroundResource(R.drawable.table_border);

            textViewTime.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 2f
            ));


            TextView textViewPlace = new TextView(container.getContext());
            String address = getAddressFromCoordinate(aUser_LL[2].split(",")[0], aUser_LL[2].split(",")[1]);
            textViewPlace.setText(address);
            textViewPlace.setGravity(Gravity.CENTER);
            textViewPlace.setTextSize(18);
            textViewPlace.setBackgroundResource(R.drawable.table_border);

            textViewPlace.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 3f
            ));

            tableRow.addView(textViewDate);
            tableRow.addView(textViewTime);
            tableRow.addView(textViewPlace);


//
//            for (int j = 0; j < aUser_LL.length ; j++) {
//                TextView textView = new TextView(container.getContext());
//                textView.setText(String.valueOf(aUser_LL[j]));
//                textView.setGravity(Gravity.CENTER);
//                textView.setTextSize(18);
//                textView.setBackgroundResource(R.drawable.table_border);
//
//                textView.setLayoutParams(new TableRow.LayoutParams(
//                        0, TableRow.LayoutParams.WRAP_CONTENT, j+1
//                ));
//
//                tableRow.addView(textView);
//            }

            tableLayout.addView(tableRow);
        }

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


    public ArrayList<String> getMonthRoutes(int month){
        ArrayList userRoutes = new ArrayList<>();
        for(String item : MainActivity.selectAllGPS()){
            userRoutes.add( item.split(";") );
        }

        ArrayList<String> LL_arr = new ArrayList<String>();

        for(int j=0 ; j<userRoutes.size() ; j++){
            String[] temp = (String[]) userRoutes.get(j);
            Long lUserDate = Long.parseLong(temp[0]);
            Double userLat = Double.parseDouble(temp[1]);
            Double userLng = Double.parseDouble(temp[2]);

            String sUserDate = String.valueOf(lUserDate);
            String user_month = sUserDate.substring(4, 6);
            if(Integer.parseInt(user_month) == month){
                LL_arr.add(sUserDate.substring(0, 8)+";"+sUserDate.substring(8)+";"+userLat+","+userLng);
            }
        }
        return LL_arr;
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


    public String getAddressFromCoordinate(String latitude, String longitude){


        final JsonReader jsonReader = new JsonReader();

        String key = "C1452070-7C02-3307-9F40-C9FAD0213169";

        final String reverseGeocodeURL = "http://api.vworld.kr/req/address?"
            + "service=address&request=getAddress&version=2.0&crs=epsg:4326&point="
            + longitude + "," + latitude
            + "&format=json"
            + "&type=both&zipcode=true"
            + "&simple=false&"
            + "key="+key;

        new Thread(){
            public void run(){
                String getJson = jsonReader.callURL(reverseGeocodeURL);
                Map<String, Object> map = jsonReader.string2Map(getJson);

                // 지도 결과 확인하기
                ArrayList reverseGeocodeResultArr = (ArrayList)((HashMap< String, Object >) map.get("response")).get("result");
                String parcel_address = "";
                String road_address = "";
                for (int counter = 0; counter < reverseGeocodeResultArr.size(); counter++) {
                    HashMap < String, Object > tmp = (HashMap < String, Object > ) reverseGeocodeResultArr.get(counter);
                    String level0 = (String)((HashMap < String, Object > ) tmp.get("structure")).get("level0");
                    String level1 = (String)((HashMap < String, Object > ) tmp.get("structure")).get("level1");
                    String level2 = (String)((HashMap < String, Object > ) tmp.get("structure")).get("level2");
                    if (tmp.get("type").equals("parcel")) {
                        parcel_address = (String) tmp.get("text");
                        parcel_address = parcel_address.replace(level0, "").replace(level1, "").replace(level2, "").trim();
                    } else {
                        road_address = "도로 주소:" + (String) tmp.get("text");
                        road_address = road_address.replace(level0, "").replace(level1, "").replace(level2, "").trim();
                    }
                }
//                System.out.println("parcel_address = > " + parcel_address);
//                System.out.println("road_address = > " + road_address);
            }
        }.start();

//        String getJson = jsonReader.callURL(reverseGeocodeURL);
//        Map<String, Object> map = jsonReader.string2Map(getJson);

        return "gg";
//        return getJson;
    }

//    Handler handler = new Handler(){
//        public void handleMessage(Message msg){
//            Bundle bun = msg.getData();
//            String text = bun.getString("address");
//        }
//    }
}