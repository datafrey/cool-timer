package com.datafrey.cooltimer.main;

import android.app.Application;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.datafrey.cooltimer.R;

public class MainActivityViewModel extends AndroidViewModel
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private boolean timerIsOn = false;
    private CountDownTimer timer;
    private final SharedPreferences sharedPreferences;

    private final MutableLiveData<Integer> startStopButtonText = new MutableLiveData(R.string.start_stop_button_start_text);
    public LiveData<String> getStartStopButtonText() {
        return Transformations.map(startStopButtonText, text -> getApplication().getString(text));
    }

    private final MutableLiveData<Boolean> seekBarEnabled = new MutableLiveData(true);
    public LiveData<Boolean> getSeekBarEnabled() {
        return seekBarEnabled;
    }

    private final MutableLiveData<Integer> timeLeftSeconds = new MutableLiveData();
    public LiveData<Integer> getTimeLeftSeconds() {
        return timeLeftSeconds;
    }
    public LiveData<String> getTimeLeftString() {
        return Transformations.map(timeLeftSeconds, this::getTimeLeftString);
    }
    public void setTimeLeftSeconds(int timeLeft) {
        timeLeftSeconds.setValue(timeLeft);
    }

    private void setDefaultValues() {
        setIntervalFromSharedPreferences();
        startStopButtonText.setValue(R.string.start_stop_button_start_text);
        seekBarEnabled.setValue(true);
        timerIsOn = false;
    }

    public MainActivityViewModel(@NonNull Application application) {
        super(application);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        setIntervalFromSharedPreferences();
    }

    public void startStopTimer() {
        if (!timerIsOn) {
            startStopButtonText.setValue(R.string.start_stop_button_stop_text);
            seekBarEnabled.setValue(false);

            setupTimer();
            timer.start();
            timerIsOn = true;
        } else {
            timer.cancel();
            setDefaultValues();
        }
    }

    private void setIntervalFromSharedPreferences() {
        timeLeftSeconds.setValue(
                Integer.valueOf(sharedPreferences.getString("default_interval", "30"))
        );
    }

    private void setupTimer() {
        timer = new CountDownTimer(timeLeftSeconds.getValue() * 1000, 1000) {
            @Override
            public void onTick(long millisecondsLeft) {
                timeLeftSeconds.setValue((int) millisecondsLeft / 1000);
            }

            @Override
            public void onFinish() {
                if (sharedPreferences.getBoolean("enable_sound", true)) {
                    String melodyName = sharedPreferences.getString("timer_melody", "bell");

                    MediaPlayer mediaPlayer;
                    switch (melodyName) {
                        case "alarm_siren":
                            mediaPlayer = MediaPlayer.create(getApplication(), R.raw.alarm_siren_sound);
                            break;
                        case "bip":
                            mediaPlayer = MediaPlayer.create(getApplication(), R.raw.bip_sound);
                            break;
                        default:
                            mediaPlayer = MediaPlayer.create(getApplication(), R.raw.bell_sound);
                    }

                    mediaPlayer.start();
                }

                setDefaultValues();
            }
        };
    }

    private String getTimeLeftString(int secondsCount) {
        int minutesLeft = secondsCount / 60;
        int secondsLeft = secondsCount % 60;

        return (minutesLeft < 10 ? "0" : "") + minutesLeft + ":" +
                (secondsLeft < 10 ? "0" : "") + secondsLeft;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("default_interval")) {
            setIntervalFromSharedPreferences();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

}
