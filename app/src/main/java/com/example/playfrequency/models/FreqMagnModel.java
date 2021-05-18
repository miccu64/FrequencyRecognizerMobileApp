package com.example.playfrequency.models;

public class FreqMagnModel {
    private final float frequency;
    private final int magnitude;

    public FreqMagnModel(float freq, int magn) {
        frequency = freq;
        magnitude = magn;
    }

    public float getFrequency() {
        return frequency;
    }

    public int getMagnitude() {
        return magnitude;
    }
}
