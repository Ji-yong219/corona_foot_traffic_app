package com.example.capstone_corona_app.ui.location;

import android.app.FragmentManager;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;


import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.capstone_corona_app.MainActivity;
import com.example.capstone_corona_app.R;


import com.example.capstone_corona_app.ui.path_history.PathHistoryFragment;
import com.example.capstone_corona_app.ui.path_history.PathHistoryTableFragment;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.MultipartPathOverlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.MarkerIcons;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = LocationFragment.class.getSimpleName();
    private LocationViewModel locationViewModel;

    //지도 객체 변수
    private MapView mapView;
    private TextView dateView;
    private NaverMap naverMap;

    ArrayList<PathOverlay> path_arr = new ArrayList<PathOverlay>();
    ArrayList<Marker> marker_arr = new ArrayList<>();

    ImageView imageview = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        locationViewModel =
                ViewModelProviders.of(this).get(LocationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_location, container, false);


        final TextView mapTextView = root.findViewById(R.id.mapTextView);

//        mapTextView.setText( locationViewModel.getMapText()+"기록된 위치 정보를 확인합니다." );



        final TextView textView = root.findViewById(R.id.text_location);
        locationViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        dateView = root.findViewById(R.id.dateView);
        locationViewModel.getDate().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                dateView.setText(s);
            }
        });

        final ImageView btnDateViewY = root.findViewById(R.id.yesterday);
        final ImageView btnDateViewT = root.findViewById(R.id.tomorrow);

        btnDateViewY.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String date_result = (String) locationViewModel.getDate(-1);
                if(date_result.equals("-1")){
                    btnDateViewY.setImageResource(R.drawable.arrow_left_gray);
                }
                else{
                    btnDateViewY.setImageResource(R.drawable.arrow_left);

                    MainActivity.removeAgoGPS();
                    dateView.setText( date_result );
                    String date = (String) dateView.getText();
                    setRoute(naverMap, date);
                }
                btnDateViewT.setImageResource(R.drawable.arrow_right);
            }
        });

        btnDateViewT.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String date_result = (String) locationViewModel.getDate(1);

                if(date_result.equals("1")){
                    btnDateViewT.setImageResource(R.drawable.arrow_right_gray);
                }
                else{
                    btnDateViewT.setImageResource(R.drawable.arrow_right);

                    MainActivity.removeAgoGPS();
                    dateView.setText( date_result );
                    String date = (String) dateView.getText();
                    setRoute(naverMap, date);
                }
                btnDateViewY.setImageResource(R.drawable.arrow_left);
            }
        });

        mapView = (MapView) root.findViewById(R.id.map_fragment);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return root;
    }

    public void setConfirmPlace(@NonNull NaverMap naverMap){
        System.out.println();
        ArrayList<HashMap<String, String>> confirmPlacesCJ = ((MainActivity)getActivity()).getConfirmPlacesCheongJu();

        for(final HashMap<String, String> place : confirmPlacesCJ) {
            Double dPlaceLatitude = Double.parseDouble( place.get("coord").split(";")[0] );
            Double dPlaceLongitude = Double.parseDouble( place.get("coord").split(";")[1] );

            InfoWindow infoWindow = new InfoWindow();
            infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
                @NonNull
                @Override
                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                    return place.get("name");
                }
            });

            infoWindow.setPosition(new LatLng(dPlaceLongitude, dPlaceLatitude));
            infoWindow.setMap(naverMap);

            CircleOverlay circle = new CircleOverlay();
            circle.setCenter(new LatLng(dPlaceLongitude, dPlaceLatitude));
            circle.setRadius(50);
            circle.setColor(Color.argb(100, 255, 0, 0));
            circle.setMap(naverMap);
        }
    }

    public void setRoute(@NonNull NaverMap naverMap, String date){
        ArrayList<String[]> routes = new ArrayList<String[]>();
        date = date.replaceAll(" ", "").replace("년", "").replace("월", "").replace("일", "");

        routes = MainActivity.selectDayGPS(date);

        for(PathOverlay i : path_arr ){
            i.setMap(null);
        }
        for(Marker i : marker_arr ){
            i.setMap(null);
        }
        path_arr.clear();
        marker_arr.clear();

        PathOverlay path = new PathOverlay();

        ArrayList arr_arr = new ArrayList<>();
        ArrayList<LatLng> LL_arr = new ArrayList<LatLng>();

        for (int i = 0; i < routes.size(); i++) {
            long route_date = Long.valueOf( routes.get(i)[0] );
            double latitude = Double.valueOf( routes.get(i)[1] );
            double longitude = Double.valueOf( routes.get(i)[2] );
            LL_arr.add(new LatLng(latitude, longitude));
        }

        if(routes.size() > 0 && LL_arr.size()>1){
            path.setCoords(LL_arr);
            path.setWidth(7);
            path.setOutlineWidth(3);
//        path.setPatternImage(OverlayImage.fromResource(R.drawable.path_pattern));
//        path.setPatternInterval(10);
            path.setColor(Color.argb(127, 0, 255, 255));
            path.setOutlineColor(Color.WHITE);
            path.setMap(naverMap);

            LatLngBounds.Builder LL_b = new LatLngBounds.Builder();
            LL_b.include(LL_arr);
            LatLngBounds bounds = LL_b.build();

            CameraUpdate cu = CameraUpdate.fitBounds(bounds, 100);
            naverMap.moveCamera(cu);


            for (int i = 0; i < routes.size(); i++) {
                Marker marker = new Marker();

                if(i==0){
                    marker.setWidth(50);
                    marker.setHeight(80);

                    marker.setPosition(LL_arr.get(0));
                    marker.setIcon(MarkerIcons.BLACK);
                    marker.setIconTintColor(Color.YELLOW);
                    marker.setMap(naverMap);
                }
                else if(i == LL_arr.size()-1){
                    marker.setWidth(50);
                    marker.setHeight(80);

                    marker.setPosition(LL_arr.get(LL_arr.size()-1));
                    marker.setIcon(MarkerIcons.BLACK);
                    marker.setIconTintColor(Color.RED);
                    marker.setMap(naverMap);
                }
                else{
                    marker.setWidth(40);
                    marker.setHeight(70);

                    marker.setPosition(LL_arr.get(i));
                    marker.setIconTintColor(Color.argb(255, 0, 0, 255));
                    marker.setMap(naverMap);
                }

                marker_arr.add(marker);
            }


            path_arr.add(path);
        }

    }

    public void onMapReady(@NonNull NaverMap naverMap){
        //배경 지도 선택
        naverMap.setMapType(NaverMap.MapType.Basic);

        //건물 표시
        naverMap.setLayerGroupEnabled(naverMap.LAYER_GROUP_BUILDING, true);

        //위치 및 각도 조정
        CameraPosition cameraPosition = new CameraPosition(
                new LatLng(36.64, 127.49),   // 위치 지정
                14,                                     // 줌 레벨
                0,                                       // 기울임 각도
                0                                     // 방향
        );
        naverMap.setCameraPosition(cameraPosition);

        setConfirmPlace(naverMap);

        String date = (String) dateView.getText();
        setRoute(naverMap, date);
        this.naverMap = naverMap;
    }

}