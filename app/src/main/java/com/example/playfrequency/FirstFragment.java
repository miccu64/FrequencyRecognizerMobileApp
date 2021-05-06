package com.example.playfrequency;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Observable;
import java.util.Observer;

public class FirstFragment extends Fragment implements Observer {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public FirstFragment() {
        //make observer for notifying when frequency is changed
        CaptureAudioObservable audio = new CaptureAudioObservable();
        audio.addObserver(this);
        audio.captureAudio();
    }

    //update on frequency change
    @Override
    public void update(Observable o, Object arg) {
        //instance for audio capture
        CaptureAudioObservable audio = (CaptureAudioObservable) o;
        float frequency = audio.getFrequency();
        int magnitude = audio.getMagnitude();
        TextView textView = (TextView) getView().findViewById(R.id.frequency);
        textView.setText("" + frequency);
    }
}