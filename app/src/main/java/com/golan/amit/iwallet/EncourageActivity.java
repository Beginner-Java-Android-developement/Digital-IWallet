package com.golan.amit.iwallet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class EncourageActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView ivMainLiori;
    Button btnBack;
    private int countDownInterval;
    private long timeToRemain;
    CountDownTimer cTimer;
    Animation animation;
    SharedPreferences sp;

    SoundPool soundPool;
    int sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encourage);

        init();

        setListener();

        timerDemo(timeToRemain);

        play();
    }

    private void play() {
        int deposit = -1;
        try {
            deposit = sp.getInt("deposit", -1);
            Log.i(MainActivity.DEBUGTAG, "deposit passed: " + deposit);
            if (deposit >= 100) {
                soundPool.setOnLoadCompleteListener(
                        new SoundPool.OnLoadCompleteListener() {
                            @Override
                            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                                soundPool.play(sampleId, 1, 1, 0, 0, 1);
                            }
                        }
                );
            }
        } catch (Exception e) {
            Log.e(MainActivity.DEBUGTAG, "exception getting passed deposit" + e);
        }
    }

    private void setListener() {
        btnBack.setOnClickListener(this);
    }

    private void init() {
        ivMainLiori = findViewById(R.id.ivLiori);
        btnBack = findViewById(R.id.btnBackId);
        cTimer = null;
        timeToRemain = 10000;
        animation = AnimationUtils.loadAnimation(this, R.anim.anim_slideup);
        ivMainLiori.startAnimation(animation);

        /**
         * Sound
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME).build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10).setAudioAttributes(aa).build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
        }
        sound = soundPool.load(this, R.raw.kolhakavod, 1);
        sp = getSharedPreferences("iwallet", MODE_PRIVATE);
    }

    @Override
    public void onClick(View v) {
        if (v == btnBack) {
            cTimer.cancel();
            soundPool.release();
            finish();
        }
    }

    private void timerDemo(long millisInFuture) {
        countDownInterval = 1000;
        cTimer = new CountDownTimer(millisInFuture, countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                timeToRemain = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                soundPool.release();
                finish();
            }
        }.start();
    }
}