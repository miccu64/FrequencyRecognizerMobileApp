package com.example.playfrequency;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.playfrequency.models.FreqMagnModel;

import java.util.Observable;
import java.util.Observer;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;


public class MainActivity extends AppCompatActivity implements Observer {

    private TextView frequencyView;
    private TextView magnitudeView;
    private CaptureAudioObservable audio;
    private StompClient stompClient;
    private String serverName = "";

    private void updateElements(float freq, int magn) {
        //needed to update elements on UI from other thread
        runOnUiThread(() -> {
            frequencyView.setText("" + freq);
            magnitudeView.setText("" + magn);
        });
    }

    private void showToast(String text) {
        this.runOnUiThread(() -> Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show());
    }

    public void connect(View view) {
        EditText serverIP = findViewById(R.id.editIP);
        serverName = serverIP.getText().toString();

        try {
            stompClient = Stomp.over(Stomp.ConnectionProvider.JWS, "ws://" + serverName + ":8080/register/websocket");
            stompClient.connect();

            //wait for connection
            Thread t1 = new Thread();
            synchronized (t1) {
                t1.start();
                t1.wait(500);
            }

            if (stompClient.isConnected()) {
                showToast("Połączono z serwerem");
                stompClient.topic("/freqPlay/connected").subscribe(topicMessage -> {
                    int a = 0;
                    int b = 0;
                    Log.d("TAG", topicMessage.getPayload());
                });
            } else {
                showToast("Nie udało się połączyć");
            }
        } catch (Exception e) {

        }


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

        stompClient = Stomp.over(Stomp.ConnectionProvider.JWS, "ws://");
    }

    //update on frequency change
    @Override
    public void update(Observable o, Object arg) {
        //instance for audio capture
        CaptureAudioObservable audio = (CaptureAudioObservable) o;
        float frequency = audio.getFrequency();
        int magnitude = audio.getMagnitude();
        updateElements(frequency, magnitude);

        if (stompClient.isConnected()) {
            FreqMagnModel f = new FreqMagnModel(frequency, magnitude);
            stompClient.send("ws://" + serverName + "/connected/sendData", f.toString());
        }
    }
}