package com.mallto.sdk;

import com.mallto.sdk.bean.MalltoBeacon;

import java.util.List;

public class BeaconSDK {


    public static void init(BeaconConfig config) {
        Internal.stop();
        Global.debug = config.isDebug();
        Global.domain = config.getDomain();
        Global.projectUUID = config.getProjectUUID();
        if (config.getDeviceUUIDList() != null && !config.getDeviceUUIDList().isEmpty()) {
            Global.setSupportedUUIDList(config.getDeviceUUIDList());
        }
        Global.scanInterval = config.getScanInterval();
        if (config.getNotification() != null) {
            Global.notification = config.getNotification();
        }
    }

    public static void start(String userId, Callback callback) {
        Internal.start(userId, callback);
    }

    public static void updateBLEInfo(String userId){Internal.updateUserId(userId);}
    public static void stop() {
        Internal.stop();
    }

    public static boolean isRunning() {
        return Internal.isRunning();
    }

    public static String getUserSlug(){
        return "";
    }

    public interface Callback {
        void onRangingBeacons(List<MalltoBeacon> beacons);

        void onAdvertising();
    }

}
