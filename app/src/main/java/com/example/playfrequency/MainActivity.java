package com.example.playfrequency;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    private TextView frequencyView;
    private TextView magnitudeView;
    private CaptureAudioObservable audio;

    private void updateElements(float freq, int magn) {
        //needed to update elements on UI from other thread
        runOnUiThread(() -> {
            frequencyView.setText("" + freq);
            magnitudeView.setText("" + magn);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audio = new CaptureAudioObservable();
        audio.addObserver(this);
        frequencyView = findViewById(R.id.frequency);
        magnitudeView = findViewById(R.id.magnitude);

        //thread to not block activity
        Thread audioThread = new Thread() {
            @Override
            public void run() {
                audio.captureAudio();
            }
        };
        audioThread.start();
    }

    //update on frequency change
    @Override
    public void update(Observable o, Object arg) {
        //instance for audio capture
        CaptureAudioObservable audio = (CaptureAudioObservable) o;
        float frequency = audio.getFrequency();
        int magnitude = audio.getMagnitude();
        updateElements(frequency, magnitude);
    }
}