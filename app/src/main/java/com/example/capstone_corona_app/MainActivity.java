package com.example.capstone_corona_app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.capstone_corona_app.ui.path_history.PathHistoryTableFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.Manifest;

import android.content.pm.PackageManager;

import android.net.Uri;

import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import com.opencsv.CSVReader;

import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;


            if (!checkPermissions()) {
                requestPermissions();
            } else {
                mService.requestLocationUpdates();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    public static myDBHelper myDBHelper;
    public static SQLiteDatabase sqlDB;

    public int path_month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
                Log.i("권한", "GPS 권한 없었음");
            }
            else{
                Log.i("권한", "GPS 권한 있었음");
            }
        }

        super.onCreate(savedInstanceState);


        myReceiver = new MyReceiver();

        // 로그인 액티비티 시작
//        setContentView(R.layout.activity_login);
//        Intent intent = new Intent(this, LoginActivity.class);
//        startActivity(intent);

        // 메인 액티비티 시작
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_location, R.id.navigation_path_history, R.id.navigation_data, R.id.navigation_setting)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);


        myDBHelper = new myDBHelper(this);
    }

    public void onFragmentChange(int month){
        path_month = month;
        getSupportActionBar().setTitle(month+"월");

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.action_path_history_to_path_history_table);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    public int getPathMonth(){
        return this.path_month;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home: // 뒤로가기 눌렀을 때
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle("Path history");

                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                navController.navigate(R.id.navigation_path_history);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.container),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }




    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.container),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }



    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(MainActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }


    public static void setSSL() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        // TODO Auto-generated method stub
                        return null;
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                        // TODO Auto-generated method stub

                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                        // TODO Auto-generated method stub
                    }
                }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    public ArrayList<HashMap<String, String>> getConfirmPlacesDaejeon(){
        String url = "https://www.daejeon.go.kr/corona19/index.do?menuId=0008";
        String sPlaceData = getPlaceDataFromSoupDJ( getSoupFromUrl(url) );
        String[] aPlaceData = sPlaceData.split("<newline>");

        ArrayList<HashMap<String, String>> aPlacesCoord = new ArrayList<HashMap<String, String>>();

        for(int i=0 ; i<aPlaceData.length ; i++){
            final String[] aData = aPlaceData[i].split(";");
            String address = "대전광역시 "+aData[0]+" "+aData[3];
            String date = aData[4];

            final String sCoord = getCoordinateFromAddress(address);
            if(sCoord == null){
                continue;
            }

            aPlacesCoord.add(new HashMap<String, String>(){{
                put("date", aData[4]);
                put("address", aData[3]);
                put("name", aData[2]);
                put("coord", sCoord);
            }});
        }
        return aPlacesCoord;
    }

    public ArrayList<HashMap<String, String>> getConfirmPlacesSeJong(){
        String url = "https://www.sejong.go.kr/bbs/R3621/list.do";
        String sPlaceData = getPlaceDataFromSoupSJ( getSoupFromUrl(url) );
        String[] aPlaceData = sPlaceData.split("<newline>");

        ArrayList<HashMap<String, String>> aPlacesCoord = new ArrayList<HashMap<String, String>>();

        for(int i=0 ; i<aPlaceData.length ; i++){
            final String[] aData = aPlaceData[i].split(";");
            String address = aData[0]+"시 "+aData[3];
            String date = aData[4];

            final String sCoord = getCoordinateFromAddress(address);
            if(sCoord == null){
                continue;
            }

            aPlacesCoord.add(new HashMap<String, String>(){{
                put("date", aData[4]);
                put("address", aData[3]);
                put("name", aData[2]);
                put("coord", sCoord);
            }});
        }
        return aPlacesCoord;
    }

    public ArrayList<HashMap<String, String>> getConfirmPlacesCheongJu(){
        String url = "https://corona.cheongju.go.kr/ajax_move.txt";
        String sPlaceData = getPlaceDataFromSoupCJ( getSoupFromUrl(url) );
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
                put("name", aData[3]);
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

    public Boolean getValidDate(String strStart, String strEnd, String strValue) {
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

                    if(((HashMap<String, Object>) map.get("response")).get("status").equals("NOT_FOUND"))
                        return null;

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
                    if( temp == null ){
                        return null;
                    }
                    else {
                        LinkedHashMap<String, String> coord2 = (LinkedHashMap<String, String>) temp.get("point");
                        coord = coord2.get("x") + ";" + coord2.get("y");

                        return coord;
                    }
                }
            };
            result_coord = asyncTask.execute(reverseGeocodeURL).get();
        }
        catch (Exception e) {

        }
        return result_coord;
    }


    public Document getSoupFromUrl(String url) {
        Document doc = null;

        try {
            AsyncTask<String, Void, Document> asyncTask = new AsyncTask<String, Void, Document>() {
                Document doc2;

                @Override
                protected Document doInBackground(String... url) {
                    try {
                        setSSL();
                        doc2 = Jsoup.connect(url[0]).get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (KeyManagementException e) {
                        e.printStackTrace();
                    }
                    return doc2;
                }
            };

            doc = asyncTask.execute(url).get();
        } catch (Exception e) {

        }
        return doc;
    }

    public String getPlaceDataFromSoupCJ(Document doc){
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

    public String getPlaceDataFromSoupSJ(Document doc){
        Elements mElementDatas = doc.select("tr[class='covidnotice']");

        StringBuilder builder_row = new StringBuilder();

        String[] columns = new String[] { "시도", "장소유형", "상호명", "도로명 주소", "노출일시", "소독여부"};
        for (int i=0 ; i<mElementDatas.size() ; i++) {
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
    
    public String getPlaceDataFromSoupDJ(Document doc){
        Elements mElementDatas = doc.select("#content table[class='corona'] tr");

        StringBuilder builder_row = new StringBuilder();

        String[] columns = new String[] { "시군구", "장소유형", "상호명", "도로명 주소", "노출일시", "소독여부", "비고"};
        for (int i=1 ; i < mElementDatas.size() ; i++) {
            Element row = mElementDatas.get(i);

            Iterator<Element> iterElem = row.getElementsByTag("td").iterator();

            StringBuilder builder_col = new StringBuilder();

            int count = 0;

            for (String column : columns) {
                if(iterElem.hasNext()){
                    builder_col.append(iterElem.next().text()+";");
                    count++;
                }
            }
            if(count < 6){
                continue;
            }
            builder_row.append(builder_col.toString().substring(0, builder_col.toString().length()-1) + "<newline>");
        }
        return builder_row.toString();
    }








    public get


    public static void insertGPS(double latitude, double longitude){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        long lNow = Long.parseLong( sdf.format( System.currentTimeMillis() ) );


        sqlDB = myDBHelper.getWritableDatabase();
        sqlDB.execSQL("INSERT INTO userGPS VALUES ( '" + lNow + "' , '" + latitude + "' , '" + longitude +"');");
        sqlDB.close();
    }
    public static void removeAgoGPS(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        long mNow = System.currentTimeMillis();

        Calendar ago_cal = Calendar.getInstance();
        ago_cal.setTime(new Date(mNow));
        ago_cal.add(Calendar.DATE, -15);
        String sAgo = sdf.format( ago_cal.getTime() );

        sqlDB = myDBHelper.getWritableDatabase();
        sqlDB.execSQL("DELETE FROM userGPS WHERE CAST(date AS INT) < CAST('"+sAgo+"' AS INT)");
        sqlDB.close();
    }
    public static List<String> selectAllGPS(){
        sqlDB = myDBHelper.getReadableDatabase();
        Cursor cursor;
        cursor = sqlDB.rawQuery("SELECT * FROM userGPS;",null);

        List<String> sResult = new ArrayList<String>();

        List<Long> date_list = new ArrayList<Long>();
        List<Double> latitude_list = new ArrayList<Double>();
        List<Double> longitude_list = new ArrayList<Double>();

        while (cursor.moveToNext()){
            date_list.add( cursor.getLong(0) );
            latitude_list.add( cursor.getDouble(1) );
            longitude_list.add( cursor.getDouble(2) );
        }

        cursor.close();
        sqlDB.close();


        for(int i=0 ; i<date_list.size() ; i++){
            sResult.add( String.valueOf(date_list.get(i)) +";"+String.valueOf(latitude_list.get(i)) +";"+ String.valueOf(longitude_list.get(i)) );
        }

        return sResult;
    }

    public static ArrayList<String[]> selectDayGPS(String date){
        sqlDB = myDBHelper.getReadableDatabase();
        Cursor cursor;
        cursor = sqlDB.rawQuery("SELECT * FROM userGPS where date like '"+date+"%' order by date;",null);
//        select * from user where address like 'S%';

        ArrayList<String[]> sResult = new ArrayList<String[]>();

        List<Long> date_list = new ArrayList<Long>();
        List<Double> latitude_list = new ArrayList<Double>();
        List<Double> longitude_list = new ArrayList<Double>();

        while (cursor.moveToNext()){
            date_list.add( cursor.getLong(0) );
            latitude_list.add( cursor.getDouble(1) );
            longitude_list.add( cursor.getDouble(2) );
        }

        cursor.close();
        sqlDB.close();


        for(int i=0 ; i<date_list.size() ; i++){
            String[] item = new String[3];
            item[0] = String.valueOf(date_list.get(i));
            item[1] = String.valueOf(latitude_list.get(i));
            item[2] = String.valueOf(longitude_list.get(i));
            sResult.add( item );
        }

        return sResult;
    }


    public class myDBHelper extends SQLiteOpenHelper {
        public myDBHelper(Context context) {
            super(context, "capstoneDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE userGPS ( date INTEGER  PRIMARY KEY, latitude DOUBLE  NOT NULL, longitude DOUBLE  NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS userGPS");
            onCreate(db);

        }
    }
}


