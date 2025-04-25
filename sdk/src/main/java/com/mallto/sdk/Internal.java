package com.mallto.sdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import com.mallto.sdk.bean.MalltoBeacon;
import com.mallto.sdk.callback.FetchSlugCallback;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressLint("MissingPermission")
public class Internal {

    private static final boolean BEACON = false;

    private static BeaconSDK.Callback callback;
    @SuppressLint("StaticFieldLeak")
    private static BeaconManager sBeaconManager;
    private static long lastReportTs = 0L;
    private static BeaconTransmitter transmitter;

    private static final BoundedQueue<Boolean> qualityQueue = new BoundedQueue<>(10);

    public static boolean isRunning() {
        return sBeaconManager != null && !sBeaconManager.getRangingNotifiers().isEmpty();
    }

    public static void updateUserId(String userId) {
        if (userId == null) {
            return;
        }
        if ("unknown".equals(userId) || TextUtils.isEmpty(userId)) {
            // 解绑username 调用接口解除userid 和设备的绑定
            Global.userId = "unknown";
            Global.slug = null;
            bindUserId("");
        } else {
            Global.userId = userId;
            bindUserId(userId);
        }
    }

    static class Instance {
        public static Region region = new Region("all-beacons-region", null, null, null);
        private static final Handler handler = new Handler(Looper.getMainLooper());
    }


    static void start(BeaconSDK.Callback callback) {
//        Internal.callback = callback;
        doAfterFetchSlug(new OnFetchSlugCallback() {
            @Override
            public void onSuccess() {
                startRangingOrAoa(callback);
            }

            @Override
            public void onFail(String error) {
                Instance.handler.post(() -> {
                    if (callback != null) {
                        callback.onError(error);
                    }
                });
            }
        });
    }

    private static void startRangingOrAoa(BeaconSDK.Callback callback) {
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
            // 是否存在信号强度>-90的beacon
            // 队列保存每次扫描的质量，true的个数/qualityQueue.size
            qualityQueue.add(checkQuality(supportedBeacons));
            if (!supportedBeacons.isEmpty()) {
                // stopAdvertising() AOA
                lastReportTs = SystemClock.elapsedRealtime();
                if (shouldStopAdvertising()) {
                    stopAdvertising();
                }
                List<MalltoBeacon> malltoBeacons = convertToMallToBeacons(supportedBeacons);
                HttpUtil.upload(Global.slug, malltoBeacons);
                Instance.handler.post(() -> {
                    if (callback != null) {
                        callback.onRangingBeacons(malltoBeacons);
                    }
                });
                MtLog.i("upload... size=" + supportedBeacons.size());
            } else {
                if (SystemClock.elapsedRealtime() - lastReportTs > Global.regionTimeout) {
                    // advertising AOA
                    if (isAdvertising) {
                        return;
                    }
                    MtLog.d("AOA...");
                    Instance.handler.post(() -> {
                        if (callback != null) {
                            callback.onAdvertising();
                        }
                    });
                    advertising();
                }
            }
        });
        // 因为要过滤多个uuid，所以不按region过滤
        Region region = Instance.region;
        lastReportTs = SystemClock.elapsedRealtime();
        sBeaconManager.startRangingBeacons(region);
    }

    private static Boolean checkQuality(List<Beacon> supportedBeacons) {
        for (Beacon beacon : supportedBeacons) {
            if (beacon.getRssi() >= -90) {
                return true;
            }
        }
        return false;
    }

    /**
     * 切换到扫描这块的逻辑后面应该要优化一下，因为实际中即便距离信标很远偶尔一阵妖风吹过就扫到了。。。比如距离50m。。。
     *
     * 所以要优化成10s内扫到过三次。  并且rssi大于-90算作一次有效扫描
     *
     * 目前实现的不是10s，是10次扫描
     */
    private static boolean shouldStopAdvertising() {
        int qualifiedCount = 0;
        for (Boolean b : qualityQueue.getDeque()) {
            if (b) {
                qualifiedCount += 1;
            }
        }
        return qualifiedCount >= 3;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    private static void stopAdvertising() {
        isAdvertising = false;
        if (BEACON) {
            if (transmitter != null) {
                transmitter.stopAdvertising();
            }
        } else {
            BluetoothAOAAdvertiser.INSTANCE.stopAdvertising();
        }

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
        if (supportedUUIDList.isEmpty()) {
            // 未设置支持的设备uuidList
            result.addAll(getTopDeviceList(new ArrayList<>(beacons), 200));
        } else {
            for (Beacon beacon : beacons) {
                if (supportedUUIDList.contains(beacon.getId1().toString().toUpperCase())) {
                    result.add(beacon);
                }
            }
        }

        return result;
    }

    private static Collection<? extends Beacon> getTopDeviceList(List<Beacon> beacons, int size) {
        Collections.sort(beacons, (o1, o2) -> o2.getRssi() - o1.getRssi());
        if (beacons.size() > size) {
            return beacons.subList(0, size);
        } else {
            return beacons;
        }


    }

    public static void stop() {
        if (sBeaconManager != null) {
            sBeaconManager.stopRangingBeacons(Instance.region);
            sBeaconManager.removeAllRangeNotifiers();
            sBeaconManager = null;
        }
        callback = null;
        qualityQueue.clear();
        stopAdvertising();
    }

    private static boolean isAdvertising = false;

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    @SuppressLint("MissingPermission")
    private static void advertising() {
        isAdvertising = true;
        if (BEACON) {
            advertisingBeacon();
        } else {
            BluetoothAOAAdvertiser.INSTANCE.startAdvertising();
        }
    }

    private static void advertisingBeacon() {
        Beacon beacon = new Beacon.Builder()
                .setId1("000050bd-84b1-329f-149d-dd6fd3100f38")
                .setId2("29229")
                .setId3("43102")
                .setManufacturer(0x004c)
                .setBluetoothName("mall-1")
//                .setManufacturer(0x0118) // Radius Networks.  Change this for other beacon layouts
//                .setTxPower(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
//                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();
        BeaconParser parser = new BeaconParser().setBeaconLayout(Global.BEACON_LAYOUT);
        int result = BeaconTransmitter.checkTransmissionSupported(Global.application);
        Log.d("beacon", "support advertising: " + result);
        transmitter = new BeaconTransmitter(Global.application, parser);
        transmitter.setAdvertiseTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        transmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
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

    public static void doAfterFetchSlug(@Nullable OnFetchSlugCallback callback) {
        String userId = Global.userId;
        String slug = Global.slug;
        if (TextUtils.isEmpty(slug)) {
            HttpUtil.fetchUserSlug(userId, new FetchSlugCallback() {
                @Override
                public void onSuccess(String slug) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void onFail(String reason) {
                    if (callback != null) {
                        callback.onFail(reason);
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onSuccess();
            }
        }
    }

    public interface OnFetchSlugCallback {
        void onSuccess();
        void onFail(String error);
    }

    public static void bindUserId(String userId) {
        HttpUtil.fetchUserSlug(userId, new FetchSlugCallback() {
            @Override
            public void onSuccess(String slug) {

            }

            @Override
            public void onFail(String reason) {

            }
        });
    }
}
