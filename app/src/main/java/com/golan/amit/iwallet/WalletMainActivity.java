package com.golan.amit.iwallet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class WalletMainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnLongClickListener {

    EditText etDeposit, etDraw;
    Button btnDeposit, btnDraw, btnResetDb;
    TextView tvMoney, tvInfo;
    WalletDbHelper wdbh;
    ImageView ivWalletDisplay;

    /**
     * background Sound
     * @param savedInstanceState
     */
    MediaPlayer mp;
    SeekBar sb;
    AudioManager am;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_main);

        init();

        setListeners();

        goOn();
    }

    private void goOn() {

        if (wdbh != null) {
            wdbh.open();

            int currentMoneyState = wdbh.currentMoneyAmount();
            tvMoney.setText(String.valueOf(currentMoneyState));

            String lastDate = wdbh.lastActivityDate();
            wdbh.close();
            if(lastDate == null) {
                tvInfo.setText("no last known activity date");
            } else {
                tvInfo.setText(lastDate);
            }
        }
    }

    private void setListeners() {
        btnDeposit.setOnClickListener(this);
        btnDraw.setOnClickListener(this);
        btnResetDb.setOnClickListener(this);
        ivWalletDisplay.setOnLongClickListener(this);
    }

    private void init() {
        etDeposit = findViewById(R.id.etDepositId);
        etDeposit.requestFocus();
        etDraw = findViewById(R.id.etDrawId);

        btnDeposit = findViewById(R.id.btnDepositId);
        btnDraw = findViewById(R.id.btnDrawId);
        btnResetDb = findViewById(R.id.btnDbResetId);

        tvMoney = findViewById(R.id.tvMoneyId);
        tvInfo = findViewById(R.id.tvInfoId);
        ivWalletDisplay = findViewById(R.id.ivWalletDisplayId);
        wdbh = new WalletDbHelper(this);

        sb = findViewById(R.id.sb);
        mp = MediaPlayer.create(this, R.raw.money_abba);
        mp.start();

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        sb.setMax(max);
        sb.setProgress(max / 4);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, max / 4, 0);
        sb.setOnSeekBarChangeListener(this);

        sp = getSharedPreferences("iwallet", MODE_PRIVATE);

    }

    @Override
    public void onClick(View v) {
        if(v == btnDeposit) {
            if(etDeposit.getText() == null || etDeposit.getText().toString().equalsIgnoreCase("")) {
                Toast.makeText(WalletMainActivity.this, "לא הוכנס ערך", Toast.LENGTH_SHORT).show();
                clearEdittexts();
                return;
            }
            String tmpDepositStr = etDeposit.getText().toString();
            int tmpDepositInt = -1;
            try {
                tmpDepositInt = Integer.parseInt(tmpDepositStr);
            } catch (Exception e) {
                Log.e(MainActivity.DEBUGTAG, "Illegal value in deposit");
                Toast.makeText(WalletMainActivity.this, "הערך אינו חוקי", Toast.LENGTH_SHORT).show();
                clearEdittexts();
                return;
            }
            if(tmpDepositInt == -1) {
                Log.e(MainActivity.DEBUGTAG, "value in deposit is still -1");
                Toast.makeText(WalletMainActivity.this, "הערך אינו חוקי", Toast.LENGTH_SHORT).show();
                clearEdittexts();
                return;
            }

            /**
             * Deposit was done
             */
            wdbh.open();
            wdbh.insertDeposit(tmpDepositStr);
            clearEdittexts();

            tvMoney.setText(String.valueOf(wdbh.currentMoneyAmount()));
            tvInfo.setText(wdbh.lastActivityDate());
            wdbh.close();

            //  shared preferences -> move amount (sum) to EncourageActivity
            SharedPreferences.Editor editor = sp.edit();
            if(MainActivity.DEBUG) {
                Log.i(MainActivity.DEBUGTAG, "passing deposit of: " + tmpDepositInt + " to end");
            }
            editor.putInt("deposit", tmpDepositInt);
            editor.commit();

            Intent i = new Intent(WalletMainActivity.this, EncourageActivity.class);
            startActivity(i);
            return;
        } else if(v == btnDraw) {
            if(MainActivity.DEBUG) {
                Log.i(MainActivity.DEBUGTAG, "draw button was clicked");
            }

            if(etDraw.getText() == null || etDraw.getText().toString().equalsIgnoreCase("")) {
                Toast.makeText(WalletMainActivity.this, "לא הוכנס ערך", Toast.LENGTH_SHORT).show();
                clearEdittexts();
                return;
            }
            String tmpDrawStr = etDraw.getText().toString();
            int tmpDrawInt = -1;
            try {
                tmpDrawInt = Integer.parseInt(tmpDrawStr);
            } catch (Exception e) {
                Log.e(MainActivity.DEBUGTAG, "Illegal value in draw");
                Toast.makeText(WalletMainActivity.this, "הערך אינו חוקי", Toast.LENGTH_SHORT).show();
                clearEdittexts();
                return;
            }
            if(tmpDrawInt== -1) {
                Log.e(MainActivity.DEBUGTAG, "value in draw is still -1");
                Toast.makeText(WalletMainActivity.this, "הערך אינו חוקי", Toast.LENGTH_SHORT).show();
                clearEdittexts();
                return;
            }

            wdbh.open();

            if(tmpDrawInt > wdbh.currentMoneyAmount()) {
                Log.e(MainActivity.DEBUGTAG, "not enough money to draw");
                Toast.makeText(WalletMainActivity.this, "יתרה לא מספקת למשיכה זו", Toast.LENGTH_SHORT).show();
                clearEdittexts();
                wdbh.close();
                return;
            }
            wdbh.insertDraw(tmpDrawStr);
            tvMoney.setText(String.valueOf(wdbh.currentMoneyAmount()));
            tvInfo.setText(wdbh.lastActivityDate());
            clearEdittexts();
            wdbh.close();

        } else if(v == btnResetDb) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton("כן", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(MainActivity.DEBUG) {
                        Log.i(MainActivity.DEBUGTAG, "reset button was clicked");
                    }
                    Toast.makeText(WalletMainActivity.this, "הרשומות אופסו", Toast.LENGTH_SHORT).show();
                    wdbh.open();
                    wdbh.resetTableToScratch();
                    wdbh.close();
                    tvInfo.setText("אין פעילות רשומה");
                    tvMoney.setText("0");

                }
            });
            builder.setNegativeButton("לא", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setTitle("איפוס כל הרשומות");
            builder.setMessage("האם לאפס את הרשומות?");
            AlertDialog dlg = builder.create();
            dlg.show();
        }
    }

    private void clearEdittexts() {
        etDeposit.setText("");
        etDraw.setText("");
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
    public boolean onLongClick(View v) {
        if(v == ivWalletDisplay) {
            Intent i = new Intent(this, LogActivity.class);
            startActivity(i);
        }
        return true;
    }
}
