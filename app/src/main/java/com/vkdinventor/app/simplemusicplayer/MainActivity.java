package com.vkdinventor.app.simplemusicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaDataSource;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MediaPlayerService player;
    boolean serviceBound = false;
    ArrayList<Audio> audioList;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.vkdinventor.app.simplemusicplayer.PlayNewAudeo";

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder localBinder = (MediaPlayerService.LocalBinder) service;
            player = localBinder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Vikash","Service Disconnected ComponentName: "+name);
            serviceBound = false;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadAudio();
        playAudio(6);
    }

    private void loadAudio() {
        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String [] projection = new String[]{MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.ARTIST};
        String sortOrder = MediaStore.Audio.Media.TITLE;

        Cursor cursor = resolver.query(uri,projection,null,null,sortOrder+" ASC");
        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                LogUtil.v("audio title :"+title);

                // Save to audioList
                audioList.add(new Audio(data, title, album, artist));
            }
            cursor.close();
        }
    }

    private void playAudio(int index) {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", audioList);
            playerIntent.putExtra("currentIndex", index);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            LogUtil.v("Service is bound: "+index);
        } else {
            //Service is active
            //Send media with BroadcastReceiver
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
            LogUtil.v("Service is not bound: "+index);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        LogUtil.v("onSaveInstanceState - serviceBound :"+serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
        LogUtil.v("onRestoreInstanceState - serviceBound :"+serviceBound);
    }

    @Override
    protected void onDestroy() {
        LogUtil.v("onDestroy: unbind service");
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

}
