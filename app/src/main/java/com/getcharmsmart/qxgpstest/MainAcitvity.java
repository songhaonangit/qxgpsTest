package com.getcharmsmart.qxgpstest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.getcharmsmart.localsocket.GGAData;
import com.getcharmsmart.localsocket.OnGpsDataListener;
import com.getcharmsmart.localsocket.QxGPSManager;

import net.sf.marineapi.nmea.sentence.GGASentence;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by root on 19-3-27.
 */

public class MainAcitvity extends AppCompatActivity {

    public static final String TAG = "MainAcitvity";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextView tv_result,tv_time,tv_postion,tv_type;
    QxGPSManager qxGPSManager;
    final   SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_time = findViewById(R.id.tv_time);
        tv_result =  findViewById(R.id.tv_result);
        tv_postion =  findViewById(R.id.tv_postion);
        tv_type =  findViewById(R.id.tv_gps_type);
    }
    @Override
    protected void onResume() {
        super.onResume();

        init();
    }

    @Override
    protected void onPause() {
        super.onPause();

        /**
         * 停止使用sdk
         */

        if(qxGPSManager!=null){
            qxGPSManager.closeGps();
        }

    }


    public void init(){

        tv_postion.setText("");
        tv_type.setText("");


        /**
         *检查网络状态
         */

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showSetNetworkUI(this);
        } else {

            if(NetWorkUtils.netCanUse(this)){
                Toast.makeText(this, "网络已连接可用...", Toast.LENGTH_SHORT).show();

                /**
                 * 网络正常时启用SDK
                 */

                initSdk();


            }else {
                Toast.makeText(this, "当前网络不可用，请尝试更换网络连接方式，或者换个地方尝试使用...", Toast.LENGTH_SHORT).show();
            }


        }

        //检查权限
        verifyStoragePermissions(this);

    }

    /**
     * 初始化启用SDK
     */
    public void initSdk(){

        qxGPSManager = new QxGPSManager();
        boolean status;
        status = qxGPSManager.setOnGpsDataListener(new OnGpsDataListener() {
            @Override
            public void onDataReceived(String str) {

            }

            @Override
            public void onDataByteReceived(byte[] str) {
                setdata(str);
            }

            @Override
            public void onDataGgaReceoved(GGASentence ggaData) {
                final  String  Lng = Double.toString(ggaData.getPosition().getLongitude());
                final  String  Lat = Double.toString(ggaData.getPosition().getLatitude());
                final  String  fix = ggaData.getFixQuality().toString();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_postion.setText(Lng+","+Lat);
                        tv_type.setText(fix);
                    }
                });
            }
        }).openGps();


        Log.d(TAG,"------qxGPSManager--open-status-"+status);
    }

    /**
     * 显示时间和NMEA数据
     */

    public void setdata(byte[] str) {
        String rev = "";
        rev =new String(str).trim();
        // new Date()为获取当前系统时间，也可使用当前时间戳
        String date = df.format(new Date());
        tv_time.setText(date);
        tv_result.setText(rev);

    }


    /**
     * 打开设置网络界面
     */
    public void showSetNetworkUI(final Context context) {
        // 提示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("网络设置提示")
                .setMessage("网络连接不可用,是否进行设置?")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Intent intent = null;
                        // 判断手机系统的版本 即API大于10 就是3.0或以上版本
                        if (android.os.Build.VERSION.SDK_INT > 10) {

                            intent = new Intent(Settings.ACTION_SETTINGS);

                        }else{

                            intent = new Intent();
                            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                            intent.setComponent(component);
                            intent.setAction("android.intent.action.VIEW");
                        }
                        context.startActivity(intent);

                    }
                }).create();

        builder.setCancelable(false).show();


    }

    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.READ_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
