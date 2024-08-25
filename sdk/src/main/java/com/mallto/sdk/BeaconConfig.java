package com.mallto.sdk;

import android.app.Notification;

import java.util.List;

public class BeaconConfig {
    private long scanInterval;
    private long aoaInterval;
    private List<String> deviceUUIDList;
    private String userId;
    private boolean debug;

    private Notification notification;

    private BeaconConfig(Builder builder) {
        this.scanInterval = builder.scanInterval;
        this.aoaInterval = builder.aoaInterval;
        this.deviceUUIDList = builder.deviceUUIDList;
        this.userId = builder.userId;
        this.debug = builder.debug;
        this.notification = builder.notification;
    }

    public static class Builder {
        private long scanInterval;
        private long aoaInterval;
        private List<String> deviceUUIDList;
        private String userId;
        private boolean debug;
        private Notification notification;

        public Builder setScanInterval(long scanInterval) {
            this.scanInterval = scanInterval;
            return this;
        }

        public Builder setAoaInterval(long aoaInterval) {
            this.aoaInterval = aoaInterval;
            return this;
        }

        public Builder setDeviceUUIDList(List<String> deviceUUIDList) {
            this.deviceUUIDList = deviceUUIDList;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder setNotification(Notification notification) {
            this.notification = notification;
            return this;
        }

        public BeaconConfig build() {
            return new BeaconConfig(this);
        }
    }

    // Getters and Setters (if needed)

    public long getScanInterval() {
        return scanInterval;
    }

    public long getAoaInterval() {
        return aoaInterval;
    }

    public List<String> getDeviceUUIDList() {
        return deviceUUIDList;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isDebug() {
        return debug;
    }

    public Notification getNotification() {
        return notification;
    }
}