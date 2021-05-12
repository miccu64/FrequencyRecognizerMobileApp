package com.example.playfrequency;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    private TextView frequencyView;
    private TextView magnitudeView;
    private CaptureAudioObservable audio;
    private WebSocketFactory factory;
    private WebSocket socket;
    private TextView isConnected;

    private void updateElements(float freq, int magn) {
        //needed to update elements on UI from other thread
        runOnUiThread(() -> {
            frequencyView.setText("" + freq);
            magnitudeView.setText("" + magn);
        });
    }

    private void showToast(String text) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();

    }

    public void connect(View view) {
        EditText serverIP = findViewById(R.id.editIP);
        String serverName = serverIP.getText().toString();
        factory.setServerName(serverName);
        try {
            socket = factory.createSocket(serverName);
            // Register a listener to receive WebSocket events.
            socket.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) {
                    showToast(message);
                }

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    super.onConnected(websocket, headers);
                    isConnected.setText("Status: połączony");
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
                    isConnected.setText("Status: rozłączony");
                }
            });
        } catch (Exception e) {
            showToast("Nie można połączyć.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        factory = new WebSocketFactory();

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