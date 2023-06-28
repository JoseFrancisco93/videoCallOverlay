package com.example.video_call_overlay;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class VideoCallOverlayPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, ActivityResultListener {
    private MethodChannel channel;
    private Context applicationContext;
    private static final String CHANNEL_ID = "video_call_overlay_channel";
    private static final int NOTIFICATION_ID = 123;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private Activity activity;
    private WindowManager.LayoutParams params;
    private View overlayView;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
        applicationContext = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "video_call_overlay");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Version de Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("showOverlay")) {
            String notificationText = call.argument("notificationText");
            showNotificationOverlay(notificationText);
            result.success(true);
        } else if (call.method.equals("closeOverlay")) {
            closeNotificationOverlay();
            result.success(true);
        } else {
            result.notImplemented();
        }
    }

    private void showNotificationOverlay(String notificationText) {
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(applicationContext)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + applicationContext.getPackageName()));
                activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                return;
            }

            params = new WindowManager.LayoutParams(
                    120,
                    60,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                            : WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    PixelFormat.TRANSPARENT);

            overlayView = new View(applicationContext);
            overlayView.setBackgroundColor(Color.BLACK);

            WindowManager windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                windowManager.addView(overlayView, params);
            }

            createNotificationChannel();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_call_black)
                    .setContentTitle("En video llamada")
                    .setContentText(notificationText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOngoing(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Video llamada";
            String description = "Estás en una llamada con";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            NotificationManager notificationManager = applicationContext.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void closeNotificationOverlay() {
        if (activity != null) {
            WindowManager windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null && overlayView != null) {
                windowManager.removeView(overlayView);
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        applicationContext = null;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(activity)) {
                // Permiso concedido, puedes continuar con la lógica para mostrar la
                // superposición
                // ...
            } else {
                // Permiso no concedido, puedes mostrar un mensaje al usuario o realizar alguna
                // acción adicional
                // ...
            }
            return true;
        }
        return false;
    }
}
