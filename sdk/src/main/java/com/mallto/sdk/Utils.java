package com.mallto.sdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

public class Utils {

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    public static String getMacAddress() {
        BluetoothManager bm = (BluetoothManager) Global.application.getSystemService(Context.BLUETOOTH_SERVICE);
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                .setTimeout(0)
                .setConnectable(false).build();
        AdvertiseData data = new AdvertiseData.Builder()
                .build();
        AdvertiseCallback callback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
            }
        };
        bm.getAdapter().getBluetoothLeAdvertiser().startAdvertising(settings, data, callback);
        return "";
    }
}
