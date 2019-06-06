package com.example.secureEye.Fragment;


import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secureEye.Adapter.SlidingImageAdapter;
import com.example.secureEye.Model.UserMessage;
import com.example.secureEye.R;
import com.example.secureEye.Utils.TypefaceSpan;
import com.github.rtoshiro.view.video.FullscreenVideoLayout;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.secureEye.Utils.SessionManager.getProgressPercentage;
import static com.example.secureEye.Utils.SessionManager.milliSecondsToTimer;
import static com.example.secureEye.Utils.SessionManager.progressToTimer;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminViewMessage extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = "AdminViewMessage";
    private List<String> imgUrlList;
    private ViewPager viewPager;
    private CirclePageIndicator pageIndicator;
    private UserMessage userMessage;
    private EditText etShowMessage;
    private SlidingImageAdapter adapter;
    private String audioPath, videoPath;
    private SeekBar audioSeekbar;
    private ImageView btnAudioPLay, btnAudioStop;
    private TextView tvAudioStartTime, tvAudioTotalTime;
    private MediaPlayer audioPlayer;
    private final Handler mHandler = new Handler();
    private ProgressDialog progressDialog;
    private FullscreenVideoLayout videoView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_view_message, container, false);

        SpannableString str = new SpannableString("Users Message");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        viewPager = view.findViewById(R.id.messageViewPager);
        pageIndicator = view.findViewById(R.id.indicator);
        etShowMessage = view.findViewById(R.id.etShowMessage);
        audioSeekbar = view.findViewById(R.id.audioSeekbar);
        btnAudioPLay = view.findViewById(R.id.audioPlay);
        btnAudioStop = view.findViewById(R.id.audioStop);
        tvAudioStartTime = view.findViewById(R.id.tvAudioStartTime);
        tvAudioTotalTime = view.findViewById(R.id.tvAudioTotalTime);
        videoView=view.findViewById(R.id.videoView);
        etShowMessage.setEnabled(false);


        imgUrlList = new ArrayList<>();
        try {
            userMessage = (UserMessage) getArguments().getSerializable("userMessage");
            String imgUrl = userMessage.getImgUrl();
            if (imgUrl.length() > 0) {

                JSONObject jsonObject = new JSONObject(imgUrl);
                int size = jsonObject.length();
                for (int i = 0; i < size; i++) {
                    imgUrlList.add(jsonObject.getString("imgUrl" + i));
                }
                adapter = new SlidingImageAdapter(getActivity(), imgUrlList);
                viewPager.setAdapter(adapter);
                pageIndicator.setViewPager(viewPager);
                final float density = getResources().getDisplayMetrics().density;
                pageIndicator.setRadius(5 * density);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!userMessage.getMessage().equalsIgnoreCase("")) {
            etShowMessage.setText(userMessage.getMessage());
            Log.d(TAG, "onCreateView:" + userMessage.getMessage());
            Log.d(TAG, "onCreateView: inside message");
        } else {
            etShowMessage.setText("No Message");
            Log.d(TAG, "onCreateView: no message");
        }

        audioPath = userMessage.getAudUrl();
        videoPath=userMessage.getVidUrl();

        if (videoPath!=null&&videoPath.length()>0) {
            try {
                videoView.setVideoURI(Uri.parse(videoPath));
                videoView.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(getActivity(), "No video", Toast.LENGTH_SHORT).show();
            videoView.setVisibility(View.INVISIBLE);
        }


        btnAudioPLay.setOnClickListener(this);
        btnAudioStop.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audioPlay:
                if (audioPath != null && audioPath.length() > 0) {
                    playAudio();
                } else {
                    Toast.makeText(getActivity(), "No audio message.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.audioStop:
                stopAudio();
                break;
        }
    }

    private void playAudio() {
        if (audioPlayer != null) {
            if (audioPlayer.isPlaying()) {
                audioPlayer.pause();
                mHandler.removeCallbacks(mUpdateTimeTask);
                btnAudioPLay.setImageResource(R.drawable.play_icon);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            }else {
                audioPlayer.start();
                btnAudioPLay.setImageResource(R.drawable.pause_icon);
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                /* Updating progressbar while play pause clicked. */
                updateProgressBar();
            }
        }else {

            try {
                audioPlayer = new MediaPlayer();
                audioPlayer.setDataSource(audioPath);
                audioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });

                audioPlayer.prepare();

                // Changing Button Image to pause image
                btnAudioPLay.setImageResource(R.drawable.pause_icon);
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                audioSeekbar.setOnSeekBarChangeListener(this); // Important
                audioPlayer.setOnCompletionListener(this); // Important

                // Updating progress bar
                updateProgressBar();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void stopAudio() {
        if (audioPlayer!=null) {
            audioPlayer.stop();
            audioPlayer.release();
            audioPlayer=null;

            tvAudioTotalTime.setText("00:00");
            // Displaying time completed playing
            tvAudioStartTime.setText("00:00");
            mHandler.removeCallbacks(mUpdateTimeTask);
            // Updating progress bar
            audioSeekbar.setProgress(0);
            btnAudioPLay.setImageResource(R.drawable.play_icon);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            mHandler.removeCallbacks(mUpdateTimeTask);
            int totalDuration = audioPlayer.getDuration();
            int currentPosition = progressToTimer(seekBar.getProgress(), totalDuration);

            // forward or backward to certain seconds
            audioPlayer.seekTo(currentPosition);

            // update timer progress again
            updateProgressBar();
        } catch (Exception e) {
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        audioPlayer.stop();
        audioPlayer.release();
        audioPlayer=null;
        tvAudioTotalTime.setText("00:00");
        // Displaying time completed playing
        tvAudioStartTime.setText("00:00");
        mHandler.removeCallbacks(mUpdateTimeTask);
        // Updating progress bar
        audioSeekbar.setProgress(0);
        btnAudioPLay.setImageResource(R.drawable.play_icon);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    public Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                long totalDuration = audioPlayer.getDuration();
                long currentDuration = audioPlayer.getCurrentPosition();

                // Displaying Total Duration time
                tvAudioTotalTime.setText("" + milliSecondsToTimer(totalDuration));
                // Displaying time completed playing
                tvAudioStartTime.setText("" + milliSecondsToTimer(currentDuration));

                // Updating progress bar
                int progress = (int) (getProgressPercentage(currentDuration, totalDuration));
                //Log.d("Progress", ""+progress);
                audioSeekbar.setProgress(progress);

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
            } catch (Exception e) {
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        btnAudioPLay.setImageResource(R.drawable.play_icon);
        if(audioPlayer!=null) {
            audioPlayer.pause();
            mHandler.removeCallbacks(mUpdateTimeTask);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (audioPlayer != null) {
            audioPlayer.start();
            // Changing button image to pause button
            btnAudioPLay.setImageResource(R.drawable.pause_icon);
            updateProgressBar();
        }
        Log.d("in Resume", "in Resume");
    }
}
