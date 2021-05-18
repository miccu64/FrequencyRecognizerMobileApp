package com.example.playfrequency;

import android.util.Pair;

import org.jtransforms.fft.FloatFFT_1D;

import static java.lang.Math.log;

public class ProcessSound {
    private final FloatFFT_1D fft;
    private final int len;
    //buf containing also older values
    private final float[] copiedBufFloat;
    private final int sampleRate;
    private final int sampleSizeInBytes;
    private float frequency;
    private int maxMagnitude;

    public ProcessSound(int _sampleRate, int _sampleSizeInBytes) {
        sampleRate = _sampleRate;
        sampleSizeInBytes = _sampleSizeInBytes;
        //init FFT with number of FFTsamples, which we want to get
        //that's why we need to derive by frame size
        //frameSize can take 1 or 2 places in buffer
        len = sampleRate / sampleSizeInBytes / 2;// /2 to get shorter buffer and faster changes
        fft = new FloatFFT_1D(len);
        copiedBufFloat = new float[len];
    }

    public float getFrequency() {
        return frequency;
    }

    public int getMaxMagnitude() {
        return maxMagnitude;
    }

    public void doProcessing(byte[] bufByte) {
        //convert to floats and resample it
        float[] bufFloat = bytesToFloat(bufByte);
        //shift data in buffer and place some new data
        int end = copiedBufFloat.length - bufFloat.length;
        System.arraycopy(copiedBufFloat, bufFloat.length, copiedBufFloat, 0, end);
        System.arraycopy(bufFloat, 0, copiedBufFloat, end - 1, bufFloat.length);

        //buffer for FFT
        float[] currentBuf = new float[copiedBufFloat.length];
        System.arraycopy(copiedBufFloat, 0, currentBuf, 0, copiedBufFloat.length);

        //save FFT result to currentBuf
        //realForward, bcs we have got real data
        fft.realForward(currentBuf);

        Pair<float[], float[]> fftRealImag = getRealAndImag(currentBuf);
        double[] magnitudes = getMagnitudes(fftRealImag);

        //find max magnitude
        double max = 0;
        int index = 0;
        //from 1, bcs 0Hz is not useful in DSP
        //bin 0 is the 0 Hz term and is equivalent to the average of all the samples in the window
        //MY MIC FREQ SPAN - 100 Hz–10 kHz
        for (int i = 1; i < magnitudes.length; i++) {
            if (magnitudes[i] > max) {
                max = magnitudes[i];
                index = i;
            }
        }

        //f = i * Fs / N
        //Fs = sample rate (Hz)
        //i = bin index
        //N = FFT size
        //return index * format.getSampleRate() / len;

        //Gaussian interpolation for getting in-between frequency
        double inter_bin = index + log(magnitudes[index + 1] / magnitudes[index - 1]) * 0.5 / log(magnitudes[index] * magnitudes[index] / (magnitudes[index + 1] * magnitudes[index - 1]));
        double res = sampleRate * inter_bin / currentBuf.length;
        frequency = (float) Math.round(res * 10) / 10;
        maxMagnitude = (int) max;
    }

    private Pair<float[], float[]> getRealAndImag(float[] bufFloat) {
        //derive it to real and complex result
        float[] real = new float[len / 2];
        float[] imag = new float[len / 2];

        //pattern:
        //Re[k] = a[2*k], 0<=k<n/2
        //Im[k] = a[2*k+1], 0<k<n/2
        //Re[n/2] = a[1]
        for (int pos = 0; pos < len / 2; pos++) {
            real[pos] = bufFloat[2 * pos];
            imag[pos] = bufFloat[2 * pos + 1];
        }
        real[len / 2 - 1] = bufFloat[1];

        return new Pair<>(real, imag);
    }

    private double[] getMagnitudes(Pair<float[], float[]> realImag) {
        float[] real = realImag.first;
        float[] imag = realImag.second;
        double[] magn = new double[real.length];
        for (int i = 0; i < magn.length - 1; i++) {
            magn[i] = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }
        return magn;
    }


    private float[] bytesToFloat(byte[] bytes) {
        int frameSize = sampleSizeInBytes;
        float[] res = new float[bytes.length / frameSize];
        for (int pos = 0; pos < bytes.length / frameSize; pos++) {
            //convert bytes to float (little endian)
            float sample = 0;
            for (int part = 0; part < frameSize; part++) {
                int help = bytes[pos * frameSize + part] & 0xff;
                sample += help << (8 * part);
            }
            //derived by maxValue + 1.0f to get normalization (binarization)
            float maxValue = 0;
            for (int a = 0; a < frameSize; a++)
                maxValue += 0xff << (8 * a);
            res[pos] = sample / maxValue;
        }
        return res;
    }
}

