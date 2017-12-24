package com.vkdinventor.app.simplemusicplayer;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import java.util.Locale;

/**
 * Created by vikash on 24-12-2017.
 * Utility Function
 */

public class Utils {

    public static String getTimeString(int duration) {
        final StringBuilder sb = new StringBuilder(8);
        final int hours = duration / (60 * 60);
        final int minutes = (duration % (60 * 60)) / 60;
        final int seconds = ((duration % (60 * 60)) % 60);

        if (duration > 3600) {
            sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":");
        }

        sb.append(String.format(Locale.getDefault(), "%02d", minutes));
        sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds));

        return sb.toString();
    }

    public static Bitmap getThumbnail(String audioFilePath){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(audioFilePath);
        byte[] art = mediaMetadataRetriever.getEmbeddedPicture();
        Bitmap thumbnail = null;
        if (art == null){
            thumbnail = BitmapFactory.decodeResource(Resources.getSystem(),R.drawable.ic_launcher_foreground);
        }else {
            thumbnail = BitmapFactory.decodeByteArray(art,0,art.length);
        }
        return thumbnail;
    }
}
