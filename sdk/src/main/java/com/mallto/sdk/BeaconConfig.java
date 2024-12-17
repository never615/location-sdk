package com.mallto.sdk;

import android.app.Notification;
import android.text.TextUtils;

import java.util.List;

public class BeaconConfig {
    private final String domain;
    private final String projectUUID;
    private final long scanInterval;
    private final List<String> deviceUUIDList;
    private final boolean debug;
    private final String userId;
    private final Notification notification;
    private final boolean ignoreCertification;

    private BeaconConfig(Builder builder) {
        this.domain = builder.domain;
        this.projectUUID = builder.projectUUID;
        this.scanInterval = builder.scanInterval;
        this.deviceUUIDList = builder.deviceUUIDList;
        this.debug = builder.debug;
        this.notification = builder.notification;
        this.userId = builder.userId;
        this.ignoreCertification = builder.ignoreCertification;
    }

    public static class Builder {
        private final String domain;
        private final String projectUUID;
        private long scanInterval;
        private List<String> deviceUUIDList;
        private boolean debug;
        private Notification notification;
        private String userId;
        private boolean ignoreCertification;

        public Builder(String domain, String projectUUID) {
            if (TextUtils.isEmpty(domain) || TextUtils.isEmpty(projectUUID)) {
                throw new RuntimeException("domain and projectUUID can not be empty");
            }
            this.domain = domain;
            this.projectUUID = projectUUID;
        }

        public Builder setScanInterval(long scanInterval) {
            this.scanInterval = scanInterval;
            return this;
        }

        public Builder setDeviceUUIDList(List<String> deviceUUIDList) {
            this.deviceUUIDList = deviceUUIDList;
            return this;
        }


        public Builder setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder setUserName(String username) {
            this.userId = username;
            return this;
        }

        public Builder setNotification(Notification notification) {
            this.notification = notification;
            return this;
        }

        public Builder setIgnoreCertification(boolean ignore) {
            this.ignoreCertification = ignore;
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

    public List<String> getDeviceUUIDList() {
        return deviceUUIDList;
    }

    public boolean isDebug() {
        return debug;
    }

    public Notification getNotification() {
        return notification;
    }

    public String getDomain() {
        return domain;
    }

    public String getProjectUUID() {
        return projectUUID;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isIgnoreCertification() {
        return ignoreCertification;
    }
}