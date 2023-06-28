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
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.RectF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

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
    private boolean isOverlayVisible = false;
    private float initialX;
    private float initialY;

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
            String initials = call.argument("initials");
            showNotificationOverlay(notificationText, initials);
            result.success(true);
        } else if (call.method.equals("closeOverlay")) {
            closeNotificationOverlay();
            result.success(true);
        } else {
            result.notImplemented();
        }
    }

    private void showNotificationOverlay(String notificationText, String initials) {
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(applicationContext)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + applicationContext.getPackageName()));
                activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                return;
            }

            if (!isOverlayVisible) {
                int squareWidth = 170;
                int squareHeight = 92;
                int borderRadius = 8;
                int circleSize = 34;

                params = new WindowManager.LayoutParams(
                        squareWidth,
                        squareHeight,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                                : WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        PixelFormat.TRANSPARENT);

                overlayView = new View(applicationContext) {
                    @Override
                    protected void onDraw(Canvas canvas) {
                        super.onDraw(canvas);

                        int squareLeft = 0;
                        int squareTop = 0;
                        int squareRight = getWidth();
                        int squareBottom = getHeight();

                        Paint squarePaint = new Paint();
                        squarePaint.setColor(Color.BLACK);
                        squarePaint.setStyle(Paint.Style.FILL);
                        squarePaint.setAntiAlias(true);

                        RectF squareRect = new RectF(squareLeft, squareTop, squareRight, squareBottom);
                        canvas.drawRoundRect(squareRect, borderRadius, borderRadius, squarePaint);

                        int circleRadius = circleSize;
                        int circleCenterX = squareWidth / 2;
                        int circleCenterY = squareHeight / 2;

                        Paint circlePaint = new Paint();
                        circlePaint.setColor(Color.WHITE);
                        circlePaint.setStyle(Paint.Style.FILL);
                        circlePaint.setAntiAlias(true);

                        canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, circlePaint);

                        Paint textPaint = new Paint();
                        textPaint.setColor(Color.BLACK);
                        textPaint.setTextSize(32);
                        textPaint.setTypeface(null);

                        String initialsText = initials.substring(0, Math.min(initials.length(), 2));

                        Rect textBounds = new Rect();
                        textPaint.getTextBounds(initialsText, 0, initialsText.length(), textBounds);
                        float textWidth = textBounds.width();
                        float textHeight = textBounds.height();

                        float textX = circleCenterX - (textWidth / 2) - textBounds.left;
                        float textY = circleCenterY + (textHeight / 2) - textBounds.bottom;

                        // Dibujar el primer icono en la parte superior derecha
                        Drawable icon1 = getResources().getDrawable(R.drawable.ic_expand);
                        int icon1Size = 30; // Tamaño del icono en píxeles
                        int icon1Padding = 4; // Espaciado del icono en píxeles

                        int icon1Left = getWidth() - icon1Size - icon1Padding;
                        int icon1Top = icon1Padding;
                        int icon1Right = getWidth() - icon1Padding;
                        int icon1Bottom = icon1Size + icon1Padding;

                        Rect icon1Rect = new Rect(icon1Left, icon1Top, icon1Right, icon1Bottom);
                        icon1.setBounds(icon1Rect);
                        icon1.draw(canvas);

                        // Dibujar el segundo icono en la parte inferior izquierda
                        Drawable icon2 = getResources().getDrawable(R.drawable.ic_mic);
                        int icon2Size = 30; // Tamaño del icono en píxeles
                        int icon2Padding = 4; // Espaciado del icono en píxeles

                        int icon2Left = icon2Padding;
                        int icon2Top = getHeight() - icon2Size - icon2Padding;
                        int icon2Right = icon2Size + icon2Padding;
                        int icon2Bottom = getHeight() - icon2Padding;

                        Rect icon2Rect = new Rect(icon2Left, icon2Top, icon2Right, icon2Bottom);
                        icon2.setBounds(icon2Rect);
                        icon2.draw(canvas);

                        canvas.drawText(initialsText, textX, textY, textPaint);
                    }
                };

                overlayView.setBackgroundColor(Color.TRANSPARENT);

                WindowManager windowManager = (WindowManager) applicationContext
                        .getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null) {
                    windowManager.addView(overlayView, params);
                }

                initialX = 0;
                initialY = 0;

                overlayView.setOnTouchListener(new View.OnTouchListener() {
                    private float offsetX;
                    private float offsetY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) overlayView
                                .getLayoutParams();

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                // Obtener las coordenadas iniciales del toque
                                initialX = event.getRawX();
                                initialY = event.getRawY();
                                offsetX = layoutParams.x;
                                offsetY = layoutParams.y;
                                break;
                            case MotionEvent.ACTION_MOVE:
                                // Calcular la diferencia entre las coordenadas actuales y las iniciales
                                float dx = event.getRawX() - initialX;
                                float dy = event.getRawY() - initialY;
                                // Actualizar la posición del widget
                                layoutParams.x = (int) (offsetX + dx);
                                layoutParams.y = (int) (offsetY + dy);
                                // Actualizar la vista del widget
                                WindowManager windowManager = (WindowManager) applicationContext
                                        .getSystemService(Context.WINDOW_SERVICE);
                                if (windowManager != null) {
                                    windowManager.updateViewLayout(overlayView, layoutParams);
                                }
                                break;
                        }
                        return true;
                    }
                });
                isOverlayVisible = true;
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
            if (isOverlayVisible) {
                WindowManager windowManager = (WindowManager) applicationContext
                        .getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null && overlayView != null) {
                    windowManager.removeView(overlayView);
                }
                isOverlayVisible = false;
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
