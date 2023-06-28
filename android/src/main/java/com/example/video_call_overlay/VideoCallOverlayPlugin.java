package com.example.video_call_overlay;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import android.content.Context;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;

import java.util.Random;

public class VideoCallOverlayPlugin implements FlutterPlugin, MethodCallHandler {
    private MethodChannel channel;
    private Context applicationContext;
    private static final String CHANNEL_ID = "video_call_overlay_channel";
    private static final int NOTIFICATION_ID = 123;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
        applicationContext = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "video_call_overlay");
        channel.setMethodCallHandler(this);
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
        if (applicationContext != null) {
            createNotificationChannel();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_call_black)
                    .setContentTitle("En video llamada")
                    .setContentText(notificationText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOngoing(true);
            ;

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
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void closeNotificationOverlay() {
        if (applicationContext != null) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        applicationContext = null;
    }
}
