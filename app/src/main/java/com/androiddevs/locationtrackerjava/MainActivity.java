package com.androiddevs.locationtrackerjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 0;

    private boolean locationPermissionGranted = false;
    private String curUid = "n7SW3MmmKPC4L6Ho9HKo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION});

        WorkManager.getInstance(this).cancelAllWork();
        if (locationPermissionGranted) {
            startLocationWorker();
        }
    }

    private void startLocationWorker() {
        Data workerData = new Data.Builder().putString("uid", curUid).build();
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest
                .Builder(LocationWorker.class, 15, TimeUnit.MINUTES)
                .setInputData(workerData).build();
        WorkManager.getInstance(this).enqueue(workRequest);
    }

    private void requestPermissions(String[] permissions) {
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            allPermissionsGranted &= ContextCompat.checkSelfPermission(this,
                    permission)
                    == PackageManager.PERMISSION_GRANTED;
        }
        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            locationPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                startLocationWorker();
            }
        }
    }
}
