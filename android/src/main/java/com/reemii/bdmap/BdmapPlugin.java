package com.reemii.bdmap;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.service.ServiceAware;
import io.flutter.embedding.engine.plugins.service.ServicePluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * BdmapPlugin
 */
public class BdmapPlugin implements FlutterPlugin, ActivityAware, ServiceAware {
    public static final String TAG = BdmapPlugin.class.getSimpleName();
    public static final String CHANNEL_NAME = "com.reemii.driver.channel.bamap";
    private static Registrar mRegistrar;
    private MethodChannel mMethodChannel;


    //FlutterEngine调用
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        Log.e(TAG, "onAttachedToEngine: " );
        setupChannel(flutterPluginBinding.getBinaryMessenger(), flutterPluginBinding.getApplicationContext());
    }

    //
    public static void registerWith(Registrar registrar) {
        Log.e(TAG, "registerWith: " );
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
        mMethodChannel = new MethodChannel(messenger, CHANNEL_NAME);
        MethodCallHandlerImpl handler = new MethodCallHandlerImpl(context);
        mMethodChannel.setMethodCallHandler(handler);

    }

    private void teardownChannel() {
        mMethodChannel.setMethodCallHandler(null);
        mMethodChannel = null;
    }


    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
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
        Log.e(TAG, "onDetachedFromActivity: " );

    }


    @Override
    public void onAttachedToService(ServicePluginBinding binding) {

    }


    @Override
    public void onDetachedFromService() {

    }
}
