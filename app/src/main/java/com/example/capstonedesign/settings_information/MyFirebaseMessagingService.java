package com.example.capstonedesign.settings_information;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.capstonedesign.MainActivity;
import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_Service";
    private static final String CHANNEL_ID = "inquiry_channel"; // 알림 채널 ID

    /**
     * 앱이 실행 중일 때 포그라운드 상태에서 FCM 메시지를 수신하면 호출됩니다.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 푸시 알림 설정이 꺼져있다면 알림을 표시하지 않습니다.
        SharedPreferences prefs = getSharedPreferences("PushSettingsPrefs", MODE_PRIVATE);
        boolean isNoticeOn = prefs.getBoolean("notice_on", true);
        if (!isNoticeOn) {
            Log.d(TAG, "Notification setting is off. Notification not shown.");
            return;
        }

        // 알림 메시지가 있을 경우 화면에 표시합니다.
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            sendNotification(title, body);
        }
    }

    /**
     * FCM 토큰이 새로 생성되거나 갱신될 때마다 호출됩니다.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // 새로 생성된 토큰을 Firestore 서버에 저장합니다.
        sendRegistrationToServer(token);
    }

    /**
     * 수신된 메시지를 바탕으로 사용자에게 푸시 알림을 표시합니다.
     */
    private void sendNotification(String title, String messageBody) {
        // 알림을 클릭했을 때 MainActivity를 열도록 설정합니다.
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 알림의 모양과 내용을 설정합니다.
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stamp) // 알림 아이콘 (ic_stamp 또는 다른 아이콘으로 변경)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true) // 알림 클릭 시 자동으로 사라지도록 설정
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // 안드로이드 8.0 (Oreo) 이상에서는 알림 채널을 생성해야 합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "문의 답변 알림", // 사용자에게 보여질 채널 이름
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // 알림을 표시합니다.
        notificationManager.notify(0, notificationBuilder.build());
    }

    /**
     * FCM 토큰을 Firestore의 현재 사용자 문서에 저장/업데이트합니다.
     * 이 메서드는 public static으로 선언하여 다른 곳(예: 로그인 시)에서도 호출할 수 있습니다.
     * @param token 저장할 새로운 FCM 토큰
     */
    public static void sendRegistrationToServer(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && token != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token successfully updated for user: " + userId))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating FCM token", e));
        }
    }
}