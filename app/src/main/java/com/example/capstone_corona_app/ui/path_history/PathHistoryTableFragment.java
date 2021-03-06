package com.example.capstone_corona_app.ui.path_history;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
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

import com.example.capstone_corona_app.MainActivity;
import com.example.capstone_corona_app.R;
import com.example.capstone_corona_app.ui.location.LocationViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

        ArrayList<String> user_LL_arr = ((MainActivity) getActivity()).getMonthRoutes(month);


        SimpleDateFormat fDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//        ProgressDialog dialog = ProgressDialog.show(getContext(), "",
//                "접촉을 파악하고 있어요..", true);

        ArrayList<HashMap<String, String>> confirmPlacesCJ = ((MainActivity) getActivity()).getConfirmPlacesCheongJu();
        ArrayList<HashMap<String, String>> confirmPlacesSJ = ((MainActivity)getActivity()).getConfirmPlacesSeJong();
        ArrayList<HashMap<String, String>> confirmPlacesDJ = ((MainActivity)getActivity()).getConfirmPlacesDaejeon();

        for (int i = 0; i < user_LL_arr.size(); i++) {
            TableRow tableRow = new TableRow(container.getContext());
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 6
            ));

            String sUser_LL = user_LL_arr.get(i);
            String[] aUser_LL = sUser_LL.split(";");


            String sUserLatitude = aUser_LL[2].split(",")[0];
            String sUserLongitude = aUser_LL[2].split(",")[1];


            confirmPlacesCJ.addAll(confirmPlacesDJ);

            boolean is_visit = false;

            for(HashMap<String, String> place : confirmPlacesCJ){
//                String sOpenDate = place.get("open_date");
                String sOpenDate = "2021.00000";
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
                        if(aPlaceDate.size()==1){
                            aPlaceDate.add(aPlaceDate.get(0));
                        }

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
                    System.out.println(sPlaceDate);
                    continue;
                }

                String sPlaceLongitude = place.get("coord").split(";")[0];
                String sPlaceLatitude = place.get("coord").split(";")[1];

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

                if(aPlaceDate.size() < 4) continue;

                if( ((MainActivity) getActivity()).getValidDate(aPlaceDate.get(aPlaceDate.size()-2), aPlaceDate.get(aPlaceDate.size()-1), sUserDate) ){
                    if(((MainActivity) getActivity()).isContact(
                            Double.parseDouble(sPlaceLatitude),
                            Double.parseDouble(sPlaceLongitude),
                            Double.parseDouble(sUserLatitude),
                            Double.parseDouble(sUserLongitude)
                    )){
                        System.out.println("접촉");
                        is_visit = true;
                    }
                    else{
//                        System.out.println("비접촉");
                    }
                }
                else{
//                    System.out.println("비접촉");
                }
            }


            for(HashMap<String, String> place : confirmPlacesSJ){
//                String sOpenDate = place.get("open_date");
                String sOpenDate = "2021.00000";
                String sPlaceDate = place.get("date");

                Pattern pattern = Pattern.compile("\\d{1,2}/\\d{1,2} ");
                Matcher matcher = pattern.matcher(sPlaceDate);

                ArrayList<String> aPlaceDate = new ArrayList<String>();

                boolean found = false;

                while(matcher.find()){
                    aPlaceDate.add( matcher.group() );
                    found = true;
                }
                if(found){
                    try {
                        if(aPlaceDate.size()==1){
                            aPlaceDate.add(aPlaceDate.get(0));
                        }

                        Date start = fDate.parse(
                                sOpenDate.split("\\.")[0]
                                        +"-"+aPlaceDate.get(0).split("/")[0].trim()
                                        +"-"+aPlaceDate.get(0).split("/")[1].trim()
                                        +" "
                                        +"00:00:00"
                        );

                        String to = fDate.format(start);
                        aPlaceDate.add( to );

                        Date end = fDate.parse(
                                sOpenDate.split("\\.")[0]
                                        +"-"+aPlaceDate.get(1).split("/")[0].trim()
                                        +"-"+aPlaceDate.get(1).split("/")[1].trim()
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
                    System.out.println(sPlaceDate);
                    continue;
                }

                String sPlaceLongitude = place.get("coord").split(";")[0];
                String sPlaceLatitude = place.get("coord").split(";")[1];

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

                if(aPlaceDate.size() < 4) continue;

                if( ((MainActivity) getActivity()).getValidDate(aPlaceDate.get(aPlaceDate.size()-2), aPlaceDate.get(aPlaceDate.size()-1), sUserDate) ){
                    if(((MainActivity) getActivity()).isContact(
                            Double.parseDouble(sPlaceLatitude),
                            Double.parseDouble(sPlaceLongitude),
                            Double.parseDouble(sUserLatitude),
                            Double.parseDouble(sUserLongitude)
                    )){
                        System.out.println("접촉");
                        is_visit = true;
                    }
                    else{
//                        System.out.println("비접촉");
                    }
                }
                else{
//                    System.out.println("비접촉");
                }
            }



            TextView textViewDate = new TextView(container.getContext());
            String tempDate = Integer.parseInt(aUser_LL[0].substring(4, 6)) + "." + Integer.parseInt(aUser_LL[0].substring(6, 8));

            if(is_visit){
                Spannable spanna = new SpannableString(tempDate);
                textViewDate.setTextColor(Color.RED);
                spanna.setSpan(new BackgroundColorSpan(Color.YELLOW), 0, tempDate.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                textViewDate.setText(spanna);
            }
            else textViewDate.setText(tempDate);

            textViewDate.setGravity(Gravity.CENTER);
            textViewDate.setTextSize(18);
            textViewDate.setBackgroundResource(R.drawable.table_border);

            textViewDate.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1f
            ));


            TextView textViewTime = new TextView(container.getContext());
            String tempTime = String.valueOf(aUser_LL[1].substring(0, 2)) + ":" + String.valueOf(aUser_LL[1].substring(2, 4));

            if(is_visit){
                Spannable spanna = new SpannableString(tempTime);
                textViewTime.setTextColor(Color.RED);
                spanna.setSpan(new BackgroundColorSpan(Color.YELLOW), 0, tempTime.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                textViewTime.setText(spanna);
            }
            else textViewTime.setText(tempTime);

            textViewTime.setGravity(Gravity.CENTER);
            textViewTime.setTextSize(18);
            textViewTime.setBackgroundResource(R.drawable.table_border);

            textViewTime.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f
            ));



            TextView textViewPlace = new TextView(container.getContext());
            String address = ((MainActivity) getActivity()).getAddressFromCoordinate(sUserLatitude, sUserLongitude);

            if(is_visit){
                Spannable spanna = new SpannableString(address);
                textViewPlace.setTextColor(Color.RED);
                spanna.setSpan(new BackgroundColorSpan(Color.YELLOW), 0, address.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                textViewPlace.setText(spanna);
            }
            else textViewPlace.setText(address);

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

//        dialog.dismiss();

        return root;
    }
}

