package com.example.secureEye.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;

import com.example.secureEye.Activity.AdminViewMessage;
import com.example.secureEye.Interface.StartMyActivity;
import com.example.secureEye.R;

public class FullScreenMediaController extends MediaController {

    private ImageButton fullScreen;
    private String isFullScreen="normal";
    private StartMyActivity startMyActivity;

    public FullScreenMediaController(Context context, StartMyActivity startMyActivity) {
        super(context);
        this.startMyActivity=startMyActivity;
    }

    @Override
    public void setAnchorView(View view) {

        super.setAnchorView(view);

        //image button for full screen to be added to media controller
        fullScreen = new ImageButton (super.getContext());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        fullScreen.setImageResource(R.drawable.ic_fullscreen);
        params.gravity = Gravity.RIGHT;
        params.rightMargin = 80;
        addView(fullScreen, params);

        //add listener to image button to handle full screen and exit full screen events
        fullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isFullScreen.equals("normal")) {
                    startMyActivity.startThisActivity("fullscreen");
                    isFullScreen="fullscreen";
                }else {
                    startMyActivity.startThisActivity("normal");
                    isFullScreen="normal";
                    fullScreen.setImageResource(R.drawable.ic_fullscreen);
                }
            }
        });
    }
}