package com.example.androidmain;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HealthCare extends AppCompatActivity {
    String f_happy= "happy";
    String f_smile= "smile";
    String f_confused= "confused";
    String f_disappointed = "disappointed";
    String f_sad = "sad";
    String f_dead = "dead";

    ImageView iv_happy;
    ImageView iv_smile;
    ImageView iv_confused;
    ImageView iv_disappointed;
    ImageView iv_sad ;
    ImageView iv_dead;

    List preicon = new ArrayList();

    private TextView tv_result;
    private EditText et_sleeptimehour;//수면시간(시간)
    private EditText et_sleeptimeminute;//수면시간(분)
    private EditText et_healthcare_memo;//그날의 메모

    Intent IntentListener_healthCare;

    DailyData mytodaydata = new DailyData();//오늘 하루의 데이터를 저장하는 클래스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_care);
        Log.i("HealthCare", "oncreate");

        IntentListener_healthCare = getIntent();

        //getWeather();

        try {
            iv_happy = findViewById(R.id.iv_happy);
            iv_smile = findViewById(R.id.iv_smile);
            iv_confused = findViewById(R.id.iv_confused);
            iv_disappointed  = findViewById(R.id.iv_disappointed);
            iv_sad = findViewById(R.id.iv_sad);
            iv_dead = findViewById(R.id.iv_dead);

            iv_happy.setImageBitmap(getFileImage(f_happy));
            iv_smile.setImageBitmap(getFileImage(f_smile));
            iv_confused.setImageBitmap(getFileImage(f_confused));
            iv_disappointed.setImageBitmap(getFileImage(f_disappointed));
            iv_sad.setImageBitmap(getFileImage(f_sad));
            iv_dead.setImageBitmap(getFileImage(f_dead));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //기분 이미지 화면 출력
    public Bitmap getFileImage(String str) throws IOException {
        AssetManager am = getResources().getAssets();
        InputStream is = null;
        Bitmap bm = null;
        try{
            is = am.open(str+".png");
            bm = BitmapFactory.decodeStream(is);
            is.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(is!=null){
            is.close();
        }
        return bm;
    }
/*
    public ImageView.OnClickListener ivClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.iv_happy:
                    //LinearLayout layout = findViewById(R.id.llayout);
                    Toast.makeText(view.getContext(),f_happy, Toast.LENGTH_LONG).show();

            }

        }
    };
*/

    //기분 데이터를 DB에 삽입
    public void insertFData(View view){

        if(preicon.isEmpty()){//이전에 눌렀던 아이콘이 없을때 실행
            setDailyicon(view);//id값을 찾아서 기분 아이콘 이름을 mytodaydata클래스에 저장한다.
        }
        else{//이전에 눌렀던 아이콘이 있을 때
            switch (preicon.get(0).toString()){//이전에 눌렀던 아이콘을 저장하는 리스트에 있는 아이콘 이름을 받아온다.
                case "smile"://만약 "smile"이면
                    iv_smile.setBackgroundResource(0);//iv_smile 이미지뷰의 노란색 박스를 제거한다.
                    break;
                case "happy":
                    iv_happy.setBackgroundResource(0);
                    break;
                case "confused":
                    iv_confused.setBackgroundResource(0);
                    break;
                case "sad":
                    iv_sad.setBackgroundResource(0);
                    break;
                case "dead":
                    iv_dead.setBackgroundResource(0);
                    break;
            }
            preicon.remove(0);//늘 이전 아이콘 저장하는 리스트의 0값을 받아오게 하기 위해 노란색 박스를 삭제한 후에는 값을 삭제해준다.
            setDailyicon(view);//이제 눌러진 이미지 뷰의 아이콘을 저장한다.
        }

    }

    /*view의 아이콘 값으로 mytodaydata클래스에 기분 아이콘이름을 저장하고 노란색 박스를 띄우는 함수.*/
    public void setDailyicon(View newview){

        switch (newview.getId()){
            case R.id.iv_smile:
                mytodaydata.setDaily_icon("smile");//mytodaydata클래스에 기분 아이콘 이름 저장
                newview.setBackgroundResource(R.drawable.tableclick);//클릭한 기분 아이콘에 클릭되었다는 표시로 노란색 박스를 띄운다.
                preicon.add("smile");//눌러진 아이콘 이름을 저장하기 위해 리스트에 추가
                break;
            case R.id.iv_happy:
                mytodaydata.setDaily_icon("happy");
                newview.setBackgroundResource(R.drawable.tableclick);
                preicon.add("happy");
                break;
            case R.id.iv_confused:
                mytodaydata.setDaily_icon("confused");
                newview.setBackgroundResource(R.drawable.tableclick);
                preicon.add("confused");
                break;
            case R.id.iv_sad:
                mytodaydata.setDaily_icon("sad");
                newview.setBackgroundResource(R.drawable.tableclick);
                preicon.add("sad");
                break;
            case R.id.iv_dead:
                mytodaydata.setDaily_icon("dead");
                newview.setBackgroundResource(R.drawable.tableclick);
                preicon.add("dead");
                break;
        }
    }

    /*헬스케어 데이터를 데이터 베이스에 저장한다.*/
    public void saveHealthData(View view){
        et_sleeptimehour = findViewById(R.id.et_sleeptimehour);
        et_sleeptimeminute = findViewById(R.id.et_sleeptimeminute);
        et_healthcare_memo = findViewById(R.id.et_healthcare_memo);

        // 수면시간, 분, 메모 에디트 텍스트에서 값 받아오기
        String hc_sleephour = et_sleeptimehour.getText().toString();
        String hc_sleepminute = et_sleeptimeminute.getText().toString();
        String hc_memo = et_healthcare_memo.getText().toString();

        //DailyData클래스에 수면시간, 분, 메모를 저장한다.
        mytodaydata.setDaily_sleep_hour(hc_sleephour);
        mytodaydata.setDaily_sleep_minute(hc_sleepminute);
        mytodaydata.setDaily_memo(hc_memo);


        /*//값을 제대로 받아오는지 확인하기 위해
        String myicon = mytodaydata.getDaily_icon();
        String mysleep_hour = mytodaydata.getDaily_sleep_hour();
        String mysleep_minute = mytodaydata.getDaily_sleep_minute();
        String mymemo = mytodaydata.getDaily_memo();

        CharSequence text = myicon + mysleep_hour+ mysleep_minute + mymemo;
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        */
        InsertHealthDataTask healthdatatask = new InsertHealthDataTask(this);//헬스케어 데이터를 저장하기 위한 asynctask
        healthdatatask.execute(mytodaydata);//mytodaydata 클래스를 인자값으로 보낸다.
    }



    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location){
            tv_result = findViewById(R.id.txv_result);
            ImageView iv_icon = findViewById(R.id.iv_icon);

            String provider = location.getProvider();
            int lon = (int)location.getLongitude();
            int lat = (int)location.getLatitude();

            OpenWeatherApiTask t = new OpenWeatherApiTask();
            GetWeatherIconTask wt = new GetWeatherIconTask(iv_icon);

            try{
                Weather w = t.execute(lat, lon).get();

                int kel_to_cel = w.getTemperature() - 273;//절대 온도->섭씨 온도. 받아오는 값이 절대온도이다.
                String temperature = String.valueOf(kel_to_cel);
                String mcity = w.getCity();
                String micon = w.getIcon();

                wt.execute(micon);

                tv_result.setText("위치 정보 " + provider+" 경도 : " + lat + " 위도 : "+lon + "\n" +mcity + " 기온 : "+ temperature + "도 " + "아이콘 넘버" + micon );
                //"위도 : "+lon + " 경도 : " + lat + "\n" +
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

        public void onStatusChanged(String provider, int status, Bundle extras){
        }

        public void onProviderEnabled(String provider){
        }

        public void onProviderDisabled(String provider){
        }
    };

    public void getWeather(){
        tv_result = findViewById(R.id.txv_result);
        ImageView iv_icon = findViewById(R.id.iv_icon);

        int lon, lat;
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( HealthCare.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },0 );
        }
        else{
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            String provider = location.getProvider();
            lon = (int)location.getLongitude();//lon = 위도
            lat = (int)location.getLatitude();//lat = 경도

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);

            OpenWeatherApiTask t = new OpenWeatherApiTask();
            GetWeatherIconTask wt = new GetWeatherIconTask(iv_icon);

            try{
                Weather w = t.execute(lat, lon).get();

                int kel_to_cel = w.getTemperature() - 273;//절대 온도->섭씨 온도. 받아오는 값이 절대온도이다.
                String temperature = String.valueOf(kel_to_cel);
                String mcity = w.getCity();
                String micon = w.getIcon();

                wt.execute(micon);

                tv_result.setText(mcity + " 기온 : " + temperature + "도 ");
                //"위치 정보 " + provider+"위도 : "+lon + " 경도 : " + lat + "\n" +
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }
}


