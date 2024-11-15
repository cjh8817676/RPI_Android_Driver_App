package com.example.jni_spi;

import android.util.Log;

public class SPIManager {
    static {
        System.loadLibrary("spi-native");
    }

    private static volatile SPIManager instance;
    private boolean isRunning = false;
    private Thread readThread;
    private SpeedUpdateListener listener;
    private boolean isStartCommand = true;  // 追蹤是否要發送START命令

    private SPIManager() {
        // Initialize SPI device
        nativeInit();
    }

    public static SPIManager getInstance() {
        if (instance == null) {
            synchronized (SPIManager.class) {
                if (instance == null) {
                    instance = new SPIManager();
                }
            }
        }
        return instance;
    }

    public void setSpeedUpdateListener(SpeedUpdateListener listener) {
        this.listener = listener;
    }


    public void startReading() {
        if (!isRunning) {
            isRunning = true;
            isStartCommand = true;  // 第一次要發送START命令
            readThread = new Thread(() -> {
                while (isRunning) {
                    byte[] data = nativeRead(isStartCommand);  // 傳遞命令標誌
                    if (data != null && data.length > 0 && listener != null) {
                        int speed = data[0] & 0xFF;
                        Log.d("Raw byte:", String.valueOf(data[0]));
                        Log.d("Converted speed: ", String.valueOf(speed));

                        listener.onSpeedUpdate(speed);
                    }
                    isStartCommand = false;  // 後續的讀取不發送START命令
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            readThread.start();
        }
    }

    public void stopReading() {
        if (isRunning) {
            isRunning = false;
            if (readThread != null) {
                readThread.interrupt();
                readThread = null;
            }
            // 發送停止命令
            nativeRead(false);  // 發送非START命令
        }
    }

    public interface SpeedUpdateListener {
        void onSpeedUpdate(int speed);
    }
    // 修改native方法聲明
    private native void nativeInit();
    private native byte[] nativeRead(boolean isStartCommand);
    private native void nativeClose();
}