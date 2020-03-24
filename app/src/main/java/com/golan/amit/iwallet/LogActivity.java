package com.golan.amit.iwallet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;

public class LogActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemLongClickListener {

    Button btnBackToMainPage;
    WalletDbHelper wdh;
    ArrayList<WalletAction> listOfWalletActions;
    ListView lv;
    WalletActionAdapter walletActionAdapter;

    /**
     * Timer
     * @param savedInstanceState
     */
    private int countDownInterval;
    private long timeToRemain;
    CountDownTimer cTimer;

    /**
     * background Sound
     * @param savedInstanceState
     */
    MediaPlayer mp;
    SeekBar sb;
    AudioManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        init();

        setListeners();

        timerDemo(timeToRemain);

    }

    private void setListeners() {
        btnBackToMainPage.setOnClickListener(this);
        lv.setOnItemLongClickListener(this);
    }

    private void init() {
        btnBackToMainPage = findViewById(R.id.btnBackToMainPageId);
        lv = findViewById(R.id.lv);
        wdh = new WalletDbHelper(this);
        listOfWalletActions = new ArrayList<WalletAction>();

        wdh.open();
        listOfWalletActions = wdh.getAllWalletActions();
        wdh.close();

        walletActionAdapter = new WalletActionAdapter(this, 0, listOfWalletActions);
        lv.setAdapter(walletActionAdapter);

        cTimer = null;
        timeToRemain = 360000;

        sb = findViewById(R.id.sb);
        mp = MediaPlayer.create(this, R.raw.money_pink_floyd);
        mp.start();

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        sb.setMax(max);
        sb.setProgress(max / 4);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, max / 4, 0);
        sb.setOnSeekBarChangeListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v == btnBackToMainPage) {
            cTimer.cancel();
            Intent i = new Intent(this, WalletMainActivity.class);
            startActivity(i);
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

                if(MainActivity.DEBUG) {
                    Log.i(MainActivity.DEBUGTAG, "count doun finished, going to main page");
                }
                Intent i = new Intent(LogActivity.this, WalletMainActivity.class);
                startActivity(i);
            }
        }.start();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    @Override
    protected void onPause() {
        super.onPause();
        mp.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mp != null) {
            try {
                mp.start();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        wdh.open();
        switch (item.getItemId()) {
            case R.id.menu_date_asc:
                listOfWalletActions = wdh.getAllWalletActionsByFilter(null, "curr_datetime ASC");
                refreshMyAdapter();
                break;
            case R.id.menu_date_desc:
                listOfWalletActions = wdh.getAllWalletActionsByFilter(null, "curr_datetime DESC");
                refreshMyAdapter();
                break;
            case R.id.menu_deposit:
                listOfWalletActions = wdh.getAllWalletActionsByFilter("deposit > 0", "deposit ASC");
                refreshMyAdapter();
                break;
            case R.id.menu_draw:
                listOfWalletActions = wdh.getAllWalletActionsByFilter("draw > 0", "draw ASC");
                refreshMyAdapter();
                break;
        }
        wdh.close();
        return super.onOptionsItemSelected(item);
    }

    public void refreshMyAdapter() {
        walletActionAdapter = new WalletActionAdapter(this, 0, listOfWalletActions);
        lv.setAdapter(walletActionAdapter);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (MainActivity.DEBUG) {
            Log.v(MainActivity.DEBUGTAG, "raw details, position: " + position + ", id: " + id);
        }
        WalletAction wa = walletActionAdapter.getItem(position);
        if (wa == null) {
            Log.e(MainActivity.DEBUGTAG, "picked a null object");
            return true;
        }
        final int realId = wa.getId();
        if (MainActivity.DEBUG) {
            Log.i(MainActivity.DEBUGTAG, "details: " + wa.toString());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("כן", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                wdh.open();
                wdh.deleteRecordById(realId);
                listOfWalletActions = wdh.getAllWalletActions();
                wdh.close();
                refreshMyAdapter();
            }
        });
        builder.setNegativeButton("לא", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setTitle("מחיקת רשומה");
        builder.setMessage("האם למחוק את הרשומה?");
        AlertDialog dlg = builder.create();
        dlg.show();

        return true;
    }
}
