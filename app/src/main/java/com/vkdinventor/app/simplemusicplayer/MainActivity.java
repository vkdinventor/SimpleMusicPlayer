package com.vkdinventor.app.simplemusicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, RecyclerViewAdapter.OnItemClickListner{

    private MediaPlayerService player;
    boolean serviceBound = false;
    private ArrayList<Audio> audioList;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ImageView previewThumbnail;
    private ImageView buttonPlayPause;
    private ImageView buttonNext;
    private ImageView buttonPrev;
    private TextView currentTime;
    private TextView totalTime;
    private SeekBar seekBar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    public static int currentIndex;

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
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        previewThumbnail = findViewById(R.id.collapsingImageView);
        buttonNext = findViewById(R.id.nextBtn);
        buttonPlayPause = findViewById(R.id.playPauseBtn);
        buttonPrev = findViewById(R.id.previousBtn);
        totalTime = findViewById(R.id.totalTime);
        seekBar = findViewById(R.id.progressbar);
        currentTime = findViewById(R.id.timePlayed);
        setSupportActionBar(toolbar);
        buttonPrev.setOnClickListener(this);
        buttonPlayPause.setOnClickListener(this);
        buttonNext.setOnClickListener(this);
        loadAudio();
        setupRecycleView();
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.playPauseBtn:
                boolean isPlaying = false;
                if (serviceBound) {
                    isPlaying = player.playPause();
                    if(isPlaying){
                        buttonPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                    }else {
                        buttonPlayPause.setImageResource(android.R.drawable.ic_media_play);
                    }
                } else {
                    playAudio();
                    buttonPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                }

                break;
            case R.id.previousBtn:
                currentIndex--;
                playAudio();
                break;
            case  R.id.nextBtn:
                currentIndex++;
                playAudio();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currentTime.setText(Utils.getTimeString(progress));
        totalTime.setText(Utils.getTimeString(seekBar.getMax()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        player.onSeekComplete(null);
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

    void setupRecycleView(){
        recyclerView = findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(audioList,this);
        recyclerViewAdapter.setOnItemClickListner(this);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void playAudio() {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", audioList);
            playerIntent.putExtra("currentIndex", currentIndex);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Service is active
            //Send media with BroadcastReceiver
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            broadcastIntent.putExtra("currentIndex", currentIndex);
            sendBroadcast(broadcastIntent);
        }
        LogUtil.v("is Service  bound: "+serviceBound);
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
        }
    }

    @Override
    public void onItemClicked(int position) {
        currentIndex = position;
        playAudio();
        Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
        toolbar.setTitle(audioList.get(position).getTitle());
        collapsingToolbarLayout.setTitle(audioList.get(position).getTitle());
        previewThumbnail.setImageBitmap(Utils.getThumbnail(audioList.get(position).getData()));
    }
}
