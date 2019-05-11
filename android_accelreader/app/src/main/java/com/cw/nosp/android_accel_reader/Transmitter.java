package com.cw.nosp.android_accel_reader;

/**
 * interface for interaction with transmitter
 */
public interface Transmitter {
    void transmit(String line);
    void onChangeIp(String newValidIp);
}
