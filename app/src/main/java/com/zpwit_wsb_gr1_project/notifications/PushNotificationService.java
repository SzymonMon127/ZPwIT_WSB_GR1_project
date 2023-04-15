package com.zpwit_wsb_gr1_project.notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.VibrationEffect;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zpwit_wsb_gr1_project.MainActivity;
import com.zpwit_wsb_gr1_project.R;
import com.zpwit_wsb_gr1_project.chat.ChatUsersActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PushNotificationService extends FirebaseMessagingService
{

    @SuppressLint("NewApi")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Context context = getApplicationContext();
        String title = remoteMessage.getNotification().getTitle();
        String text = remoteMessage.getNotification().getBody();

        if (text.equals(context.getResources().getString(R.string.followedyou)))
        {
            String CHANNEL_ID = "Follow";
            long[] pattern = { 500, 500, 1000 };

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Follow Notification",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert_message), null); // Set the sound for the channel
            channel.setVibrationPattern(pattern);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("data", 3);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);


            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.mipmap.app_icon_one)
                    .setAutoCancel(true)
                    .setGroupSummary(true)
                    .setContentIntent(pendingIntent);
            int notificationId = convert(title);
            NotificationManagerCompat.from(this).notify(notificationId, notification.build()); // nadajemy unikalny numer powiadomieniu
        }
        else if (text.equals(context.getResources().getString(R.string.newMessage)))
        {
            String CHANNEL_ID = "Messages";
            long[] pattern = { 500, 500, 1000 };
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Messages Notification",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert_not), null); // Set the sound for the channel
            channel.setVibrationPattern(pattern);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);

            Intent intent = new Intent(this, ChatUsersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);


            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.mipmap.app_icon_one)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setGroupSummary(true);
            int notificationId = convert(title)+1;
            NotificationManagerCompat.from(this).notify(notificationId, notification.build()); // nadajemy unikalny numer powiadomieniu
        }

        super.onMessageReceived(remoteMessage);
    }


    public static int convert(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5"); // Wybór algorytmu skrótu
            byte[] bytes = md.digest(input.getBytes()); // Obliczenie funkcji skrótu dla ciągu znaków
            int result = 0;
            for (int i = 0; i < 4; i++) { // Konwersja 4-bajtowej wartości skrótu na int
                result <<= 8;
                result |= (bytes[i] & 0xFF);
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
