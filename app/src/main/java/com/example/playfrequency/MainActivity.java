package com.example.playfrequency;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Observable;
import java.util.Observer;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;


public class MainActivity extends AppCompatActivity implements Observer {
    private CaptureAudioObservable audio;
    private StompClient stompClient;

    private void updateElements(float freq, int magn) {
        //needed to update elements on UI from other thread
        runOnUiThread(() -> {
            TextView frequencyView = findViewById(R.id.frequency);
            TextView magnitudeView = findViewById(R.id.magnitude);
            frequencyView.setText("" + freq);
            magnitudeView.setText("" + magn);
        });
    }

    private void updateConnectionStatus(String text) {
        //needed to update elements on UI from other thread
        runOnUiThread(() -> {
            TextView textView = findViewById(R.id.connectStatus);
            textView.setText(text);
        });
    }

    private void showToast(String text) {
        this.runOnUiThread(() -> Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("CheckResult")
    public void connect(View view) {
        EditText serverIP = findViewById(R.id.editIP);
        String serverName = serverIP.getText().toString();

        //connect
        stompClient = Stomp.over(Stomp.ConnectionProvider.JWS, "ws://" + serverName + ":8080/register/websocket");

        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    //subscribe to get messages from server
                    stompClient.topic("/freqPlay/connected").subscribe(topicMessage -> {
                        //process JSON
                        String json = topicMessage.getPayload();
                        int fromWhere = json.indexOf("strongEnough");
                        String magnString = json.substring(fromWhere + 14, json.length()-1);
                        String freqString = json.substring(13,fromWhere-2);

                        //show on UI
                        runOnUiThread(() -> {
                            TextView textView = findViewById(R.id.recognizedFreq);
                            textView.setText(freqString);
                            textView = findViewById(R.id.strongEnough);
                            textView.setText(magnString);
                        });
                    });
                    showToast("Połączono z serwerem");
                    updateConnectionStatus("POłĄCZONO");
                    break;
                case ERROR:
                    showToast("Nie udało się połączyć");
                    updateConnectionStatus("Status: rozłączony");
                    break;
                case CLOSED:
                    showToast("Rozłączono");
                    updateConnectionStatus("Status: rozłączony");
                    break;
            }
        });
        stompClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        audio = new CaptureAudioObservable();
        audio.addObserver(this);

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
            //send as JSON string
            String jsonString = "{\"frequency\":" + frequency + ",\"magnitude\":" + magnitude + "}";
            stompClient.send("/connected/sendData", jsonString).subscribe();
        }
    }


}