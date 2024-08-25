package com.mallto.sdk;

import com.google.gson.Gson;
import com.mallto.sdk.bean.MalltoBeacon;
import com.mallto.sdk.bean.UploadBeaconModel;

import org.altbeacon.beacon.Beacon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {

    public static class Inner {
        public static final OkHttpClient CLIENT = new OkHttpClient().newBuilder().build();
    }

    public static void upload(List<MalltoBeacon> beaconList) {
        UploadBeaconModel uploadBeaconModel = new UploadBeaconModel();
        uploadBeaconModel.beacons = beaconList;
        uploadBeaconModel.user_uuid = Global.userId;

        MtLog.d("user_uuid:" + Global.userId);

        String requestData = new Gson().toJson(uploadBeaconModel);

        MediaType mediaType = MediaType.parse("application/json");

        RequestBody body =
                RequestBody.create(mediaType, requestData);
        Request request = new Request.Builder()
                .url(Global.getServerDomain() + "/api/lbs/location_data")
                .post(body)
//			.addHeader("cache-control", "no-cache")
                .addHeader("UUID", Global.PROJECT_UUID)
                .addHeader("App-Id", "999")
                .addHeader("Sign-Version", "999")
                .build();

        Call call = Inner.CLIENT.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MtLog.d("onFailure:" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) {
                MtLog.d("onResponse");
                MtLog.d("http code:" + response.code());
            }
        });

    }


}