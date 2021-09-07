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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


        SimpleDateFormat fDate = new SimpleDateFormat("yyyy-MM-DD hh:mm:ss");

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
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f
            ));


            String sUserLatitude = aUser_LL[2].split(",")[0];
            String sUserLongitude = aUser_LL[2].split(",")[1];

            ArrayList<HashMap<String, String>> confirmPlacesCJ = getConfirmPlacesCheongJu();


            for(HashMap<String, String> place : confirmPlacesCJ){
                String sOpenDate = place.get("open_date");
                String sPlaceDate = place.get("date");

                Pattern pattern = Pattern.compile("\\d{1,2}.\\d{1,2}.\\([월화수목금토일]\\)");
                Matcher matcher = pattern.matcher(sPlaceDate);

                ArrayList<String> aPlaceDate = new ArrayList<String>();

                boolean found = false;

                while(matcher.find()){
                    aPlaceDate.add( matcher.group() );
                    found = true;
                }
                if(found){
                    try {
                        Date start = fDate.parse(
                        sOpenDate.split("\\.")[0]
                                +"-"+aPlaceDate.get(0).split("\\.")[0]
                                +"-"+aPlaceDate.get(0).split("\\.")[1]
                                +" "
                                +"00:00:00"
                        );
                        String to = fDate.format(start);
                        aPlaceDate.add( to );

                        Date end = fDate.parse(
                            sOpenDate.split("\\.")[0]
                                    +"-"+aPlaceDate.get(1).split("\\.")[0]
                                    +"-"+aPlaceDate.get(1).split("\\.")[1]
                                    +" "
                                    +"23:59:59"
                        );

                        to = fDate.format(end);
                        aPlaceDate.add( to );


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    System.out.println("날짜 찾기 에러");
                }

                String sPlaceLatitude = place.get("coord").split(";")[0];
                String sPlaceLongitude = place.get("coord").split(";")[1];

                String sUserDate = aUser_LL[0].substring(0, 4)
                    + "-"
                    + aUser_LL[0].substring(4, 6)
                    + "-"
                    + aUser_LL[0].substring(6, 8)
                    + " "
                    + aUser_LL[1].substring(0, 2)
                    + ":"
                    + aUser_LL[1].substring(2, 4)
                    + ":"
                    + aUser_LL[1].substring(4, 6);

                if( getValidDate(aPlaceDate.get(2), aPlaceDate.get(3), sUserDate) ){
                    if(isContact(
                            Double.parseDouble(sPlaceLatitude),
                            Double.parseDouble(sPlaceLongitude),
                            Double.parseDouble(sUserLatitude),
                            Double.parseDouble(sUserLongitude)
                    )){
                        System.out.println("접촉");
                    }
                    else{
                        System.out.println("비접촉");
                    }
                }
                else{
                    System.out.println("비접촉");
                }
            }

            TextView textViewPlace = new TextView(container.getContext());
            String address = getAddressFromCoordinate(sUserLatitude, sUserLongitude);
            textViewPlace.setText(address);
            textViewPlace.setGravity(Gravity.CENTER);
            textViewPlace.setTextSize(12);
            textViewPlace.setBackgroundResource(R.drawable.table_border);

            textViewPlace.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.MATCH_PARENT, 3.5f
            ));

            tableRow.addView(textViewDate);
            tableRow.addView(textViewTime);
            tableRow.addView(textViewPlace);

            tableLayout.addView(tableRow);
        }

        return root;
    }

    public ArrayList<HashMap<String, String>> getConfirmPlacesCheongJu(){
        String url = "https://corona.cheongju.go.kr/ajax_move.txt";
        String sPlaceData = getSoupFromUrl(url);
        String[] aPlaceData = sPlaceData.split("<newline>");

        ArrayList<HashMap<String, String>> aPlacesCoord = new ArrayList<HashMap<String, String>>();

        for(int i=0 ; i<aPlaceData.length ; i++){
            final String[] aData = aPlaceData[i].split(";");
            String address = aData[4];
            String date = aData[5];

            final String sCoord = getCoordinateFromAddress(address);

            aPlacesCoord.add(new HashMap<String, String>(){{
                put("open_date", aData[0]);
                put("date", aData[5]);
                put("address", aData[4]);
                put("coord", sCoord);
            }});
        }
        return aPlacesCoord;
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

    public boolean isContact(double venueLat, double venueLng, double userLat, double userLng){
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

        return false;
    }


    private Calendar getDateTime(String strDatetime) {
        Calendar cal = Calendar.getInstance();
        String[] strSplitDateTime = strDatetime.split(" ");
        String[] strSplitDate = strSplitDateTime[0].split("-");
        String[] strSplitTime = strSplitDateTime[1].split(":");

        cal.set(Integer.parseInt(strSplitDate[0]), Integer.parseInt(strSplitDate[1]) - 1,
                Integer.parseInt(strSplitDate[2]), Integer.parseInt(strSplitTime[0]), Integer.parseInt(strSplitTime[1]),
                Integer.parseInt(strSplitTime[2]));

        return cal;
    }

    private Boolean getValidDate(String strStart, String strEnd, String strValue) {
        Calendar calStart = getDateTime(strStart);
        Calendar calEnd = getDateTime(strEnd);
        Calendar calValue = getDateTime(strValue);

        Boolean bValid = false;

        if (calStart.before (calValue) && calEnd.after(calValue)) {
            bValid = true;
        }

        return bValid;
    }

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
                            parcel_address = parcel_address.replace(level0, "").trim();
                        } else {
                            road_address = (String) tmp.get("text");
                            road_address = road_address.replace(level0, "").trim();
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

    public String getCoordinateFromAddress(String address) {
        final JsonReader jsonReader = new JsonReader();
        String key = "C1452070-7C02-3307-9F40-C9FAD0213169";
        String result_coord = "";

        final String reverseGeocodeURL = "http://api.vworld.kr/req/address?"
                + "service=address&request=getcoord&version=2.0&crs=epsg:4326&address="
                + address
                + "refine=true"
                + "&simple=false&"
                + "&format=json"
                + "&type=road"
                + "&key=" + key;


        try {
            AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... url) {
                    String latitude = "";
                    String longitude = "";
                    String coord = "";

                    String getJson = jsonReader.callURL(url[0]);
                    Map<String, Object> map = jsonReader.string2Map(getJson);


                    LinkedHashMap<String, Object> temp = (LinkedHashMap<String, Object>) ((HashMap<String, Object>) map.get("response")).get("result");
                    LinkedHashMap<String, String> coord2 = (LinkedHashMap<String, String>) temp.get("point");
                    coord = coord2.get("x")+";"+coord2.get("y");

                    return coord;
                }
            };
            result_coord = asyncTask.execute(reverseGeocodeURL).get();
        }
        catch (Exception e) {

        }
        return result_coord;
    }


    public String getSoupFromUrl(String url) {
        Document doc = null;

        try {
            AsyncTask<String, Void, Document> asyncTask = new AsyncTask<String, Void, Document>() {
                Document doc2;
                @Override
                protected Document doInBackground(String... url) {
                    try {
                        doc2 = Jsoup.connect(url[0]).get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return doc2;
                }
            };

            doc = asyncTask.execute(url).get();
        }
        catch (Exception e) {

        }

        Elements mElementDatas = doc.select("tr");

        StringBuilder builder_row = new StringBuilder();

        String[] columns = new String[] { "공개일", "시/군/구", "장소유형", "상호명", "도로명 주소", "노출일시", "소독여부"};
        for (int i=1 ; i<mElementDatas.size() ; i++) {
            Element row = mElementDatas.get(i);

            Iterator<Element> iterElem = row.getElementsByTag("td").iterator();

            StringBuilder builder_col = new StringBuilder();

            for (String column : columns) {
                builder_col.append(iterElem.next().text()+";");
            }
            builder_row.append(builder_col.toString().substring(0, builder_col.toString().length()-1) + "<newline>");
        }
        return builder_row.toString();
    }
}