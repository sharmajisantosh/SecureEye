package com.example.secureEye.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.secureEye.R;

public class FullscreenVideo extends AppCompatActivity {

    private VideoView videoView = null;
    private int currenttime = 0;
    private String Url = "";
    private static ProgressDialog progressDialog;
    private Button btnNormalScreenVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen_video);
        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            currenttime = extras.getInt("currenttime", 0);
            Url = extras.getString("Url");
        }
        progressDialog = ProgressDialog.show(this, "", "Loading...", true);
        videoView = (VideoView) findViewById(R.id.VideoViewfull);
        btnNormalScreenVideo=findViewById(R.id.btnNormalScreenVideo);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        Uri video = Uri.parse(Url);
        videoView.setVideoURI(video);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer arg0) {
                progressDialog.dismiss();
                videoView.start();
                videoView.seekTo(currenttime);

            }
        });

        btnNormalScreenVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("currenttime", videoView.getCurrentPosition());
        setResult(RESULT_OK, data);
        super.finish();
    }
}
