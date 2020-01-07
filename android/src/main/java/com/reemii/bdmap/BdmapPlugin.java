package com.reemii.bdmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.baidu.trace.api.track.DistanceResponse;
import com.google.gson.Gson;
import com.reemii.bdmap.trace.TraceConfig;
import com.rxjava.rxlife.RxLife;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.OkHttpClient;
import rxhttp.wrapper.param.RxHttp;

/**
 * BdmapPlugin 实现了ActivityAware和ServiceAware注册时实现了这两个接口注册之后会自动实例化
 */
public class BdmapPlugin implements MethodChannel.MethodCallHandler, FlutterPlugin, ActivityAware {
    public static final String TAG = BdmapPlugin.class.getSimpleName();
    public static final String CHANNEL_NAME = "com.reemii.driver.channel.bamap";
    public static final String EVENT_CHANNEL = "com.reemii.driver.channel.bamap.event";
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
        mFlutterPluginBinding = flutterPluginBinding;
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
        new EventChannel(messenger, EVENT_CHANNEL).setStreamHandler(new EventChannel.StreamHandler() {
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
        mActivityPluginBinding = binding;
        Log.e(TAG, "onAttachedToActivity: ");

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
        BDLocationClient.getInstance().stop();
        TraceManager.getInstance().stopTrace();
    }


    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("init")) {
            Log.e(TAG, "onMethodCall: 初始化百度定位，鹰眼轨迹");
            Map<String, String> params = (Map<String, String>) call.arguments;
            String staffId = params.get("staffId");
            String traceId = params.get("traceId");
            //初始化位置服务
            BDLocationClient.init(mFlutterPluginBinding.getApplicationContext());
            BDLocationClient.getInstance().start();
            BDLocationClient.getInstance().register(new BDLocationClient.onLocationCallBack() {
                @Override
                public void onLocation(RMLocation rmLocation) {
                    //传递位置数据给flutter保存
                    Map<String, String> data = new HashMap<>();
                    data.put("location", new Gson().toJson(rmLocation));
                    mEventSink.success(data);
                }
            });
            //初始化鹰眼轨迹服务
            TraceManager.getInstance().initTrace(new TraceConfig(staffId,
                    Long.parseLong(traceId),
                    4,
                    8,
                    mFlutterPluginBinding.getApplicationContext()));
            //开始服务的回调
            TraceManager.getInstance().startTrace(new TraceConfig.ITraceService.TraceCallback() {
                @Override
                public void onTraceOpenSuccess() {
                    Log.e(TAG, "onTraceOpenSuccess: 轨迹服务开启成功！");
                    //查询一次轨迹
                    queryTraceMile();
                }

                @Override
                public void onTraceOpenFailed(int code, String msg) {
                    Log.e(TAG, "onTraceOpenSuccess: 轨迹服务开启失败！");
                }
            });
        } else if (call.method.equals("startGather")) {
            TraceManager.getInstance().startGather(new TraceConfig.ITraceService.GatherCallback() {
                @Override
                public void onGatherSuccess() {
                    Map<String, Object> data = new HashMap<>();
                    data.put("trace", "success");
                    mEventSink.success(data);
                }

                @Override
                public void onGatherFailed(int code, String msg) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("trace", "failed");
                    mEventSink.success(data);
                }
            });
        } else if (call.method.equals("stopGather")) {
            TraceManager.getInstance().stopGather();
        } else if (call.method.equals("query")) {
            queryTraceMile();
        } else {
            result.notImplemented();
        }
    }

    private void queryTraceMile() {
        //查询轨迹
        TraceManager.getInstance().queryMile(new TraceConfig.ITraceService.QueryCallback() {
            @Override
            public void onQueryResult(DistanceResponse distanceResponse) {
                Map<String, Object> data = new HashMap<>();
                data.put("miles", distanceResponse.getDistance());
                mEventSink.success(data);
            }
        });
    }

    //销毁
    private void teardownChannel() {
        mMethodChannel.setMethodCallHandler(null);
        mMethodChannel = null;
    }


}
