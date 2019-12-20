package com.reemii.bdmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import javax.security.auth.login.LoginException;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.service.ServiceAware;
import io.flutter.embedding.engine.plugins.service.ServicePluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * BdmapPlugin 实现了ActivityAware和ServiceAware注册时实现了这两个接口注册之后会自动实例化
 */
public class BdmapPlugin implements MethodChannel.MethodCallHandler, FlutterPlugin, ActivityAware, ServiceAware {
    public static final String TAG = BdmapPlugin.class.getSimpleName();
    public static final String CHANNEL_NAME = "com.reemii.driver.channel.bamap";
    public static final String LOCATION_EVENT_CHANNEL = "com.reemii.driver.channel.bamap.event";
    private static Registrar mRegistrar;
    private MethodChannel mMethodChannel;
    private EventChannel.EventSink mEventSink;
    private FlutterPluginBinding mFlutterPluginBinding;
    private ActivityPluginBinding mActivityPluginBinding;

    public BdmapPlugin() {
        Log.e(TAG, "BdmapPlugin:初始化 ");
    }

    //新版本使用:注入到插件表中是自动调用
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        Log.e(TAG, "onAttachedToEngine: ");
        setupChannel(flutterPluginBinding.getBinaryMessenger(), flutterPluginBinding.getApplicationContext());
    }

    //老版本使用
    public static void registerWith(Registrar registrar) {
        Log.e(TAG, "registerWith: ");
        final BdmapPlugin bdmapPlugin = new BdmapPlugin();
        mRegistrar = registrar;
        bdmapPlugin.setupChannel(registrar.messenger(), registrar.context());

    }

    //FlutterEngine调用
    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        teardownChannel();
    }

    private void setupChannel(BinaryMessenger messenger, Context context) {
        new EventChannel(messenger, LOCATION_EVENT_CHANNEL).setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                mEventSink = events;
            }

            @Override
            public void onCancel(Object arguments) {

            }
        });
        mMethodChannel = new MethodChannel(messenger, CHANNEL_NAME);
        mMethodChannel.setMethodCallHandler(this::onMethodCall);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        Log.e(TAG, "onAttachedToActivity: ");
        binding.addRequestPermissionsResultListener(new PluginRegistry.RequestPermissionsResultListener() {
            @Override
            public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                if (requestCode == 123 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.e(TAG, "onDetachedFromActivityForConfigChanges: ");

    }


    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        Log.e(TAG, "onReattachedToActivityForConfigChanges: ");

    }

    @Override
    public void onDetachedFromActivity() {
        Log.e(TAG, "onDetachedFromActivity: ");

    }


    @Override
    public void onAttachedToService(ServicePluginBinding binding) {

    }


    @Override
    public void onDetachedFromService() {

    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("init")) {
            //实例化
            BDLocationClient.init(mFlutterPluginBinding.getApplicationContext());
            BDLocationClient.getInstance().start();
            BDLocationClient.getInstance().register(new BDLocationClient.onLocationCallBack() {
                @Override
                public void onLocation(RMLocation rmLocation) {
                    mEventSink.success("初始化定成功！");
                    Log.e(TAG, "onLocation: " + rmLocation.toString());
                }
            });
        } else {
            result.notImplemented();
        }
    }

    //销毁
    private void teardownChannel() {
        BDLocationClient.getInstance().stop();
        mMethodChannel.setMethodCallHandler(null);
        mMethodChannel = null;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void check() {
        if (mActivityPluginBinding.getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            mActivityPluginBinding.getActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        } else {

        }

    }
}
