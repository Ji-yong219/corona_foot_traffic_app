package com.example.capstone_corona_app.ui.path_history;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.toolbox.HttpResponse;
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
import java.util.HashMap;
import java.util.Map;

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
        root.setTag("path_history_table_tag");

        super.onCreate(savedInstanceState);


        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true); // actionbar 좌측에 뒤로가기 화살표 표시

        tableLayout = (TableLayout) root.findViewById(R.id.tablelayout);

        int month = ((MainActivity) getActivity()).getPathMonth();
//            isContact()

        ArrayList<String> user_LL_arr = getMonthRoutes(month);

        for (int i = 0; i < user_LL_arr.size(); i++) {
            TableRow tableRow = new TableRow(container.getContext());
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 6
            ));

            String sUser_LL = user_LL_arr.get(i);
            String[] aUser_LL = sUser_LL.split(";");


            TextView textViewDate = new TextView(container.getContext());
            String tempDate = Integer.parseInt(aUser_LL[0].substring(4, 6)) + "." + Integer.parseInt(aUser_LL[0].substring(6, 8));
            textViewDate.setText(tempDate);
            textViewDate.setGravity(Gravity.CENTER);
            textViewDate.setTextSize(18);
            textViewDate.setBackgroundResource(R.drawable.table_border);

            textViewDate.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1f
            ));


            TextView textViewTime = new TextView(container.getContext());
            String tempTime = String.valueOf(aUser_LL[1].substring(0, 2)) + ":" + String.valueOf(aUser_LL[1].substring(2, 4));
            textViewTime.setText(tempTime);
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

            tableLayout.addView(tableRow);
        }

        return root;
    }

    private ArrayList<String[]> getDataFromCSV(InputStreamReader target_csv) {
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


    public ArrayList<String> getMonthRoutes(int month) {
        ArrayList userRoutes = new ArrayList<>();
        for (String item : MainActivity.selectAllGPS()) {
            userRoutes.add(item.split(";"));
        }

        ArrayList<String> LL_arr = new ArrayList<String>();

        for (int j = 0; j < userRoutes.size(); j++) {
            String[] temp = (String[]) userRoutes.get(j);
            Long lUserDate = Long.parseLong(temp[0]);
            Double userLat = Double.parseDouble(temp[1]);
            Double userLng = Double.parseDouble(temp[2]);

            String sUserDate = String.valueOf(lUserDate);
            String user_month = sUserDate.substring(4, 6);
            if (Integer.parseInt(user_month) == month) {
                LL_arr.add(sUserDate.substring(0, 8) + ";" + sUserDate.substring(8) + ";" + userLat + "," + userLng);
            }
        }
        return LL_arr;
    }

    public boolean isContact() {
        ArrayList userRoutes = new ArrayList<>();
        for (String item : MainActivity.selectAllGPS()) {
            userRoutes.add(item.split(";"));
        }

        ArrayList<String[]> routes = new ArrayList<String[]>();

        routes = getDataFromCSV(new InputStreamReader(getResources().openRawResource(R.raw.cb_routes)));

        ArrayList<LatLng> LL_arr = new ArrayList<LatLng>();


        for (int j = 0; j < userRoutes.size(); j++) {
            String[] temp = (String[]) userRoutes.get(j);
            Long userDate = Long.parseLong(temp[0]);
            Double userLat = Double.parseDouble(temp[1]);
            Double userLng = Double.parseDouble(temp[2]);


            for (int i = 1; i < routes.size(); i++) {
                if (routes.get(i)[5] != null && !routes.get(i)[5].equals("")) {
                    Long date = Long.parseLong(routes.get(i)[1] + routes.get(i)[2]);
                    double venueLat = Double.valueOf(routes.get(i)[5]);
                    double venueLng = Double.valueOf(routes.get(i)[6]);

                    if (date <= userDate) {
                        double latDistance = Math.toRadians(userLat - venueLat);
                        double lngDistance = Math.toRadians(userLng - venueLng);
                        double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
                                (Math.cos(Math.toRadians(userLat))) *
                                        (Math.cos(Math.toRadians(venueLat))) *
                                        (Math.sin(lngDistance / 2)) *
                                        (Math.sin(lngDistance / 2));

                        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

                        double dist = 6371 * c;
                        if (dist < 0.05) { //(in km, you can use 0.1 for metres etc.)
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


//    @SuppressLint("StaticFieldLeak")
    public String getAddressFromCoordinate(String latitude, String longitude) {
        final JsonReader jsonReader = new JsonReader();
        String key = "C1452070-7C02-3307-9F40-C9FAD0213169";
        String result_address = "";

        final String reverseGeocodeURL = "http://api.vworld.kr/req/address?"
                + "service=address&request=getAddress&version=2.0&crs=epsg:4326&point="
                + longitude + "," + latitude
                + "&format=json"
                + "&type=both&zipcode=true"
                + "&simple=false&"
                + "key=" + key;


        try {
            AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... url) {
                    String parcel_address = "";
                    String road_address = "";

                    String getJson = jsonReader.callURL(url[0]);
                    Map<String, Object> map = jsonReader.string2Map(getJson);


                    // 지도 결과 확인하기
                    ArrayList reverseGeocodeResultArr = (ArrayList) ((HashMap<String, Object>) map.get("response")).get("result");

                    for (int counter = 0; counter < reverseGeocodeResultArr.size(); counter++) {
                        HashMap<String, Object> tmp = (HashMap<String, Object>) reverseGeocodeResultArr.get(counter);
                        String level0 = (String) ((HashMap<String, Object>) tmp.get("structure")).get("level0");
                        String level1 = (String) ((HashMap<String, Object>) tmp.get("structure")).get("level1");
                        String level2 = (String) ((HashMap<String, Object>) tmp.get("structure")).get("level2");

                        if (tmp.get("type").equals("parcel")) {
                            parcel_address = (String) tmp.get("text");
                            parcel_address = parcel_address.replace(level0, "").replace(level1, "").replace(level2, "").trim();
                        } else {
                            road_address = (String) tmp.get("text");
                            road_address = road_address.replace(level0, "").replace(level1, "").replace(level2, "").trim();
                        }
                    }
                    return parcel_address;
                }
            };

            result_address = asyncTask.execute(reverseGeocodeURL).get();
        }
        catch (Exception e) {

        }
        return result_address;
    }
}