package com.reemii.bdmap;

import android.app.Notification;
import android.content.Context;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by yyg on 2018/10/19 ,10:29
 */
public class BDLocationClient {

    public static final String TAG = BDLocationClient.class.getSimpleName();
    private LocationClient mLocationClient;
    private LocationUploadCallback mLocationUploadCallback;
    private static BDLocationClient instance;

    public static BDLocationClient getInstance() {
        if (instance == null) {
            throw new RuntimeException("请初始化BDLocationClient");
        }
        return instance;
    }

    public static void init(Context context) {
        if (instance == null) {
            synchronized (BDLocationClient.class) {
                instance = new BDLocationClient(context);
            }
        }
    }

    private BDLocationClient(Context context) {
        mLocationClient = new LocationClient(context);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");  //坐标类型
        option.setScanSpan(4000); //定位时间间隔
        option.setOpenGps(true); //使用gps
        option.setLocationNotify(false); //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        option.setIgnoreKillProcess(false);  //是否杀死服务
        option.SetIgnoreCacheException(false);
        option.setNeedDeviceDirect(true); //需要设备方向
//        option.setWifiCacheTimeOut(5 * 60 * 1000);
        option.setEnableSimulateGps(false); //可选，设置是否收集Crash信息，默认收集，即参数为false
        mLocationClient.setLocOption(option);
        //定位会回调监听
        mLocationUploadCallback = new LocationUploadCallback();
    }

    public void register(final onLocationCallBack locationCallBack) {
        Log.e(TAG, "register: ");
        //定位回掉
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                RMLocation rmLocation = new RMLocation();
                rmLocation.lat = bdLocation.getLatitude();
                rmLocation.lng = bdLocation.getLongitude();
                rmLocation.direction = bdLocation.getDirection();
                int errorCode = bdLocation.getLocType();
                if (locationCallBack != null
                        && rmLocation != null
                        && (errorCode == 61 || errorCode == 66 || errorCode == 161)) {
                    locationCallBack.onLocation(rmLocation);
                }
            }
        });
    }

    public void removeLocationUploadCallback() {
        mLocationClient.unRegisterLocationListener(mLocationUploadCallback);
    }

    public void registerLocationUploadCallback() {
        mLocationClient.registerLocationListener(mLocationUploadCallback);
    }

    public void enableLocInForeground(Notification notification) {
        if (mLocationClient == null) {
            return;
        }
        mLocationClient.enableLocInForeground(10001, notification);
    }

    public void start() {
        if (mLocationClient != null) {
            Log.e("百度SDK定位", "start: ");
            mLocationClient.start();
            registerLocationUploadCallback();
        }
    }

    public void reStart() {
        if (mLocationClient != null) {
            Log.e("百度SDK定位", "重启: ");
            removeLocationUploadCallback();
            mLocationClient.restart();
            registerLocationUploadCallback();
        }
    }

    public void stop() {
        if (mLocationClient != null && mLocationClient.isStarted()) {
            Log.e("百度SDK定位", "stop: ");
            mLocationClient.stop();
            removeLocationUploadCallback();
        }
    }


    //定位回调
    public interface onLocationCallBack {
        void onLocation(RMLocation rmLocation);
    }


    //定位回调
    public static class LocationUploadCallback extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            int code = bdLocation.getLocType();
            //上传位置
            if (code == 61 || code == 66 || code == 161) {
                RMLocation rmLocation = new RMLocation();
                rmLocation.lat = bdLocation.getLatitude();
                rmLocation.lng = bdLocation.getLongitude();
                rmLocation.direction = bdLocation.getDirection();
                rmLocation.alt = (float) bdLocation.getAltitude();
                rmLocation.speed = bdLocation.getSpeed();
                //存储位置
//                LocalStorage.getInstance().putLocation(rmLocation);
                //上传位置
                postLocation(rmLocation);
            }
        }

        //上传位置
        public void postLocation(RMLocation rmLocation) {
            Map<String, Object> params = new HashMap<>();
            params.put("lat", rmLocation.lat);
            params.put("lng", rmLocation.lng);
            params.put("direction", rmLocation.direction);
            params.put("alt", rmLocation.alt);
            params.put("speed", rmLocation.speed);


        }

    }

}

