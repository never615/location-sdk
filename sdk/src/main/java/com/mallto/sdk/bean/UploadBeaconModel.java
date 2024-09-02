package com.mallto.sdk.bean;

import java.io.Serializable;
import java.util.List;

public class UploadBeaconModel implements Serializable {
    public String user_uuid;
    public String mac;
    public List<MalltoBeacon> beacons;

}
