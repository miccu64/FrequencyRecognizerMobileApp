package com.example.playfrequency.models;

public class ResultModel {
    private final float frequency;
    private final boolean strongEnough;

    public ResultModel(float freq, boolean strong) {
        frequency = freq;
        strongEnough = strong;
    }

    public float getFrequency() {
        return frequency;
    }

    public boolean getStrongEnough() {
        return strongEnough;
    }
}
