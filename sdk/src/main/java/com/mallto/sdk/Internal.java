package com.mallto.sdk;

import android.annotation.SuppressLint;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.mallto.sdk.bean.MalltoBeacon;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Internal {

    @SuppressLint("StaticFieldLeak")
    private static BeaconManager sBeaconManager;
    private static long lastReportTs = 0L;
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static boolean isRunning() {
        return sBeaconManager != null && !sBeaconManager.getRangingNotifiers().isEmpty();
    }

    static class Instance {
        public static Region region = new Region("all-beacons-region", null, null, null);
    }

    static void start(BeaconSDK.Callback callback) {
        BeaconParser parser = new BeaconParser().setBeaconLayout(Global.BEACON_LAYOUT);
        sBeaconManager = BeaconManager.getInstanceForApplication(Global.application);
        sBeaconManager.getBeaconParsers().add(parser);
        long interval = Math.max(Global.scanInterval, 1100L);
        sBeaconManager.setForegroundScanPeriod(interval);
        sBeaconManager.setBackgroundBetweenScanPeriod(0L);
        sBeaconManager.setBackgroundScanPeriod(interval);
        sBeaconManager.enableForegroundServiceScanning(Global.notification, 456);
        sBeaconManager.removeAllRangeNotifiers();
        sBeaconManager.addRangeNotifier((beacons, region) -> {

            List<Beacon> supportedBeacons = getSupportedBeacons(beacons);
            if (!supportedBeacons.isEmpty()) {
                // stopAdvertising() AOA
                lastReportTs = SystemClock.elapsedRealtime();
                List<MalltoBeacon> malltoBeacons = convertToMallToBeacons(supportedBeacons);
                HttpUtil.upload(malltoBeacons);
                handler.post(() -> callback.onRangingBeacons(malltoBeacons));
                MtLog.i("upload... size=" + supportedBeacons.size());
            } else {
                if (SystemClock.elapsedRealtime() - lastReportTs > Global.regionTimeout) {
                    // advertising AOA
                    MtLog.d("AOA...");
                    advertising();
                    handler.post(() -> callback.onAdvertising());
                }
            }
        });
        // 因为要过滤多个uuid，所以不按region过滤
        Region region = Instance.region;
        lastReportTs = SystemClock.elapsedRealtime();
        sBeaconManager.startRangingBeacons(region);
    }

    /**
     * 按传入的uuid过滤beacon设备
     *
     * @param beacons 检测到的全部设备
     * @return 支持的设备
     */
    private static List<Beacon> getSupportedBeacons(Collection<Beacon> beacons) {
        List<String> supportedUUIDList = Global.getSupportedUuidList();
        List<Beacon> result = new ArrayList<>();
        for (Beacon beacon : beacons) {
            if (supportedUUIDList.contains(beacon.getId1().toString().toUpperCase())) {
                result.add(beacon);
            }
        }
        return result;
    }

    public static void stop() {
        if (sBeaconManager != null) {
            sBeaconManager.stopRangingBeacons(Instance.region);
            sBeaconManager.removeAllRangeNotifiers();
            sBeaconManager = null;
        }
    }


    private static void advertising() {
        Beacon beacon = new Beacon.Builder().setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
                .setId2("1")
                .setId3("2")
                .setBluetoothName("beacon")
                .setBluetoothAddress("A4:07:B6:D9:B0:4C")
                .setManufacturer(0x0118) // Radius Networks.  Change this for other beacon layouts
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();
        BeaconParser parser = new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        int result = BeaconTransmitter.checkTransmissionSupported(Global.application);
        Log.d("beacon", "support advertising: " + result);
        BeaconTransmitter transmitter = new BeaconTransmitter(Global.application, parser);
        transmitter.startAdvertising(beacon, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Log.e("beacon", "advertising failed " + errorCode);
            }
        });
    }

    private static List<MalltoBeacon> convertToMallToBeacons(List<Beacon> beaconList) {
        List<MalltoBeacon> result = new ArrayList<>();
        for (Beacon beacon : beaconList) {
            MalltoBeacon malltoBeacon = new MalltoBeacon();
            malltoBeacon.setUuid(beacon.getId1().toString());
            malltoBeacon.setMajor(beacon.getId2().toString());
            malltoBeacon.setMinor(beacon.getId3().toString());
            malltoBeacon.setRssi(beacon.getRssi());
            result.add(malltoBeacon);
        }
        return result;
    }
}
