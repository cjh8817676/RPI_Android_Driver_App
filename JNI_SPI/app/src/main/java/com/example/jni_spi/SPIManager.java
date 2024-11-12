package com.example.jni_spi;

public class SPIManager {
    static {
        System.loadLibrary("spi-native");
    }

    private static volatile SPIManager instance;
    private boolean isRunning = false;
    private Thread readThread;
    private SpeedUpdateListener listener;

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
            readThread = new Thread(() -> {
                while (isRunning) {
                    byte[] data = nativeRead();
                    if (data != null && data.length > 0 && listener != null) {
                        // Assuming the speed value is in the first byte
                        int speed = data[0] & 0xFF;
                        listener.onSpeedUpdate(speed);
                    }
                    try {
                        Thread.sleep(100); // Read every 100ms
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            readThread.start();
        }
    }

    public void stopReading() {
        isRunning = false;
        if (readThread != null) {
            readThread.interrupt();
            readThread = null;
        }
    }

    public interface SpeedUpdateListener {
        void onSpeedUpdate(int speed);
    }

    // Native methods
    private native void nativeInit();
    private native byte[] nativeRead();
    private native void nativeClose();
}