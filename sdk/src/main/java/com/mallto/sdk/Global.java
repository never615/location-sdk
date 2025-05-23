package com.mallto.sdk;

import android.app.Notification;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Global {
    public static boolean debug = false;
    public volatile static Context application;
    // 检测设备超时时间
    public static long regionTimeout = 10_000L;
    public static final String BEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    // 外部传入/imei, 用于上传
    public static String userId = "unknown";
    public static String slug;
    // 支持的设备的uuid，按uuid过滤后，上传服务器，支持外部传入
    private static final List<String> mallToUuids = new ArrayList<>();

    public static String domain;
    public static String projectUUID = "";
    public static long scanInterval = 1100L;

    public static Notification notification;

    public static boolean ignoreCertification = false;

    static void setSupportedUUIDList(@NonNull List<String> uuids) {
        mallToUuids.clear();
        for (String uuid : uuids) {
            mallToUuids.add(uuid.toUpperCase());
        }
    }

    static List<String> getSupportedUuidList() {
        return mallToUuids;
    }
}
