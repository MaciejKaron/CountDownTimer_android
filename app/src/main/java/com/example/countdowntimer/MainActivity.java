package com.example.countdowntimer;

import static com.example.countdowntimer.Notifications.CHANNEL_1_ID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText mEditTextHours;
    private EditText mEditTextSeconds;
    private EditText mEditTextInput;
    private TextView mTextViewCountDown;
    private Button mButtonStartPause;
    private Button mButtonReset;
    private Button mButtonSet;
    private Button mButtonSeconds;
    private Button mButtonHours;

    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;

    private ProgressBar mProgressBar;
    private int mMyProgress = 0;


    //powiadomienia
    private NotificationManagerCompat notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextHours = findViewById(R.id.edit_text_hours);
        mEditTextSeconds = findViewById(R.id.edit_text_seconds);
        mEditTextInput = findViewById(R.id.edit_text_input);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_reset);
        mButtonSet = findViewById(R.id.button_set);
        mButtonSeconds = findViewById(R.id.button_set_seconds);
        mButtonHours = findViewById(R.id.button_set_hours);
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setProgress(mMyProgress);



        //powiadomienia
        notificationManager = NotificationManagerCompat.from(this);

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTimerRunning){
                    pauseTimer();
                } else{
                    startTimer();
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mEditTextInput.getText().toString();
                if (input.length() == 0){
                    Toast.makeText(MainActivity.this, R.string.field_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                long millisInput = Long.parseLong(input) * 60000;
                if (millisInput == 0) {
                    Toast.makeText(MainActivity.this, R.string.positive_number, Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                mEditTextInput.setText("");
            }
        });


        mButtonSeconds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mEditTextSeconds.getText().toString();
                if (input.length() == 0){
                    Toast.makeText(MainActivity.this, R.string.field_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                long millisInput = Long.parseLong(input) * 1000;
                if (millisInput == 0) {
                    Toast.makeText(MainActivity.this, R.string.positive_number, Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                mEditTextSeconds.setText("");
            }
        });

        mButtonHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mEditTextHours.getText().toString();
                if (input.length() == 0){
                    Toast.makeText(MainActivity.this, R.string.field_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                long millisInput = Long.parseLong(input) * 3600000;
                if (millisInput == 0) {
                    Toast.makeText(MainActivity.this, R.string.positive_number, Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                mEditTextHours.setText("");
            }
        });




    }

    private void setTime(long milliseconds){
        mStartTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();
    }

    private void startTimer(){
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                updateProgress();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                updateButtons();
                sendOnChannel1();
                notificationVibration();
                mProgressBar.setProgress(100);
            }
        }.start();

        mTimerRunning = true;
        updateButtons();

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && mTimerRunning == true){
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 100 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long[] pattern = {0, 100, 1000, 200, 2000, 300, 3000};
                v.vibrate(VibrationEffect.createWaveform(pattern, -1));
                Toast.makeText(MainActivity.this,R.string.rotate_phone, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void pauseTimer(){
        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateButtons();
    }

    private void resetTimer(){
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateButtons();
        mMyProgress =0;
        mProgressBar.setProgress(0);
    }

    private void updateCountDownText(){
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) %3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0){
            timeLeftFormatted = String.format(Locale.getDefault(),"%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
        }

        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void updateButtons(){
        if (mTimerRunning){
            //WYSUNIECIE DODATKOWYCH FUNKCJI
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mEditTextHours.setVisibility(View.INVISIBLE);
                mButtonHours.setVisibility(View.INVISIBLE);
                mEditTextSeconds.setVisibility(View.INVISIBLE);
                mButtonSeconds.setVisibility(View.INVISIBLE);

            } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mEditTextHours.setVisibility(View.INVISIBLE);
                mButtonHours.setVisibility(View.INVISIBLE);
                mEditTextSeconds.setVisibility(View.INVISIBLE);
                mButtonSeconds.setVisibility(View.INVISIBLE);
            }
            mEditTextInput.setVisibility(View.INVISIBLE);
            mButtonSet.setVisibility(View.INVISIBLE);
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText(R.string.button_pause);
        } else {
            //WYSUNIECIE DODATKOWYCH FUNKCJI
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mEditTextHours.setVisibility(View.INVISIBLE);
                mButtonHours.setVisibility(View.INVISIBLE);
                mEditTextSeconds.setVisibility(View.INVISIBLE);
                mButtonSeconds.setVisibility(View.INVISIBLE);

            } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mEditTextHours.setVisibility(View.VISIBLE);
                mButtonHours.setVisibility(View.VISIBLE);
                mEditTextSeconds.setVisibility(View.VISIBLE);
                mButtonSeconds.setVisibility(View.VISIBLE);
            }
            mEditTextInput.setVisibility(View.VISIBLE);
            mButtonSet.setVisibility(View.VISIBLE);
            mButtonStartPause.setText(R.string.button_start);

            if (mTimeLeftInMillis < 1000) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if (mTimeLeftInMillis < mStartTimeInMillis) {
                mButtonReset.setVisibility(View.VISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    //nie resetuj gdy odwracasz ekran
    //nie resetuj gdy wylaczasz apke
    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);
        editor.putLong("startTimeInMillis", mStartTimeInMillis);
        editor.putInt("myProgress", mMyProgress);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        mTimeLeftInMillis = prefs.getLong("millisLeft" , mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning", false);
        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        mMyProgress = prefs.getInt("myProgress", mMyProgress);

        updateCountDownText();
        updateButtons();

        if (mTimerRunning){
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
            int timeLeft = ((int)mTimeLeftInMillis / 1000);
            int startTime = ((int)mStartTimeInMillis / 1000);
            mMyProgress = startTime - timeLeft -1;

            if (mTimeLeftInMillis < 0){
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
                updateButtons();
                mProgressBar.setProgress(100);
            } else {
                startTimer();
            }
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //POWIADOMIENIA
    public void sendOnChannel1(){
        String title = "Time is up!";
        String message = "Click to return to the application.";
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setSound(alarmSound)
                .setColor(Color.rgb(255, 153, 204))
                .build();

        notificationManager.notify(1, notification);

    }

    //Wibacje przy powiadomieniu
    public void notificationVibration(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 1000 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }
    //Wibracje przy resetowaniu
    public void resetVibration(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 100 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    //Progress bar
    public void updateProgress(){

        int progresMax = ((int)mStartTimeInMillis / 1000);

        mProgressBar.setMax(progresMax);

        if (mTimerRunning) {
            mMyProgress++;
            mProgressBar.setProgress((int) mMyProgress * 1);
        }
    }




}