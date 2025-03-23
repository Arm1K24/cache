package com.example.cache;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import com.tbruyelle.rxpermissions3.RxPermissions;

import java.util.HashMap;

public class CachePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    private MethodChannel channel;
    private Activity activity;
    private RxPermissions rxPermissions;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        // Инициализация MethodChannel с использованием BinaryMessenger
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "cache");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getSystemCache":
                try {
                    result.success(DataCleanUtil.getTotalCacheSize(activity));
                } catch (Exception e) {
                    e.printStackTrace();
                    result.success(e.toString());
                }
                break;
            case "clearCache":
                DataCleanUtil.cleanTotalCache(activity);
                result.success(true);
                break;
            case "availableSpace":
                rxPermissions = new RxPermissions(activity);
                rxPermissions
                        .request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                result.success(PhoneUtil.getInstance().getSDFreeSize() + "MB");
                            } else {
                                result.success("无访问权限");
                            }
                        });
                break;
            case "deviceCacheSpace":
                rxPermissions = new RxPermissions(activity);
                rxPermissions
                        .request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                result.success(PhoneUtil.getInstance().getSDAllSize() + "MB");
                            } else {
                                result.success("无访问权限");
                            }
                        });
                break;
            case "saveImage":
                HashMap hashMap = (HashMap) call.arguments;
                String path = (String) hashMap.get("path");
                byte[] data = (byte[]) hashMap.get("data");
                rxPermissions = new RxPermissions(activity);
                rxPermissions
                        .request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                FileUtil.saveBitmap(path, Bytes2Bimap(data));
                                result.success("Image saved");
                            } else {
                                result.success("无访问权限");
                            }
                        });
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null); // Очистка обработчика при отключении
        Log.d("CachePlugin", "onDetachedFromEngine");
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        Log.d("CachePlugin", "onAttachedToActivity: " + (activity != null));
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.d("CachePlugin", "onDetachedFromActivityForConfigChanges");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        Log.d("CachePlugin", "onReattachedToActivityForConfigChanges");
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
        Log.d("CachePlugin", "onDetachedFromActivity");
    }
}
