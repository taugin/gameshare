package com.chukong.sdkdemo.service;

interface TimeSyncControl {
    void sync();
    long getNetworkTime();
}