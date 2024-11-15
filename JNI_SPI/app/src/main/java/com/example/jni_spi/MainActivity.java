package com.example.jni_spi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.jni_spi.databinding.ActivityMainBinding;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private SpeedometerView speedometer;
    private Button startButton;
    private Button stopButton;
    private SPIManager spiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedometer = findViewById(R.id.speedometer);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        spiManager = SPIManager.getInstance();
        spiManager.setSpeedUpdateListener(speed -> {
            runOnUiThread(() -> speedometer.setSpeed(speed));
            }
        );
        startButton.setOnClickListener(v -> {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            spiManager.startReading();
        });

        stopButton.setOnClickListener(v -> {
            stopButton.setEnabled(false);
            startButton.setEnabled(true);
            spiManager.stopReading();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        spiManager.stopReading();
    }
}