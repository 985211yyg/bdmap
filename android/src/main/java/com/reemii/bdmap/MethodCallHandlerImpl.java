package com.reemii.bdmap;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * Author: yyg
 * Date: 2019-12-18 17:03
 * Description:
 */
public class MethodCallHandlerImpl implements MethodChannel.MethodCallHandler {
    public static final String TAG = MethodCallHandlerImpl.class.getSimpleName();
    private Context mContext;

    public MethodCallHandlerImpl(Context context) {
        Log.e(TAG, "MethodCallHandlerImpl: ");
        mContext = context;
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("init")) {
            //实例化
            BDLocationClient.init(mContext);
        } else {
            result.notImplemented();

        }
    }
}
