package com.example.playfrequency;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.util.Observable;


public class CaptureAudioObservable extends Observable {
    private final int len;
    private final int sampleRate;
    private final int sampleSizeInBytes;
    private final AudioRecord audioRecord;
    private float frequency;
    private int magnitude;

    public CaptureAudioObservable() {
        //better to set smaller sampleRate for compatibility and less computations
        sampleRate = 8000;//8000,16000,22050,44100 - only even numbers
        sampleSizeInBytes = 1;//1,2
        int channels = 1;//only 1 will work
        //buffer for sound samples - DERIVED BY 5 to get more frequent changes
        len = sampleRate * sampleSizeInBytes / 5;

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, channels, AudioFormat.ENCODING_PCM_8BIT, len);
    }

    public float getFrequency() {
        return frequency;
    }

    public int getMagnitude() {
        return magnitude;
    }

    private void setFrequencyAndMagnitude(float _freq, int _magn) {
        //notify observers about change in frequency
        frequency = _freq;
        magnitude = _magn;
        setChanged();
        notifyObservers();
    }

    public void captureAudio() {
        audioRecord.startRecording();

        byte[] bufByte = new byte[len];
        ProcessSound process = new ProcessSound(sampleRate, sampleSizeInBytes, len);

        while (true) {
            //read data from input
            int length = audioRecord.read(bufByte, 0, len);
            if (length > 0) {
                process.doProcessing(bufByte);
                float newFreq = process.getFrequency();
                int newMagn = process.getMaxMagnitude();
                //notify observers if found new frequency with big magnitude
                setFrequencyAndMagnitude(newFreq, newMagn);
            }
        }
    }
}

