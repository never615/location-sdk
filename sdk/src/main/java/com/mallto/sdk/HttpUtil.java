package com.mallto.sdk;

import android.annotation.SuppressLint;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.mallto.sdk.bean.MalltoBeacon;
import com.mallto.sdk.bean.UploadBeaconModel;
import com.mallto.sdk.bean.UserSlugResp;
import com.mallto.sdk.callback.FetchSlugCallback;

import java.io.IOException;
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

    public static void upload(String slug, List<MalltoBeacon> beaconList) {
        UploadBeaconModel uploadBeaconModel = new UploadBeaconModel();
        uploadBeaconModel.beacons = beaconList;
        uploadBeaconModel.mac = slug;

        MtLog.d("mac:" + slug + ",projectUUID=" + Global.projectUUID + ",domain=" + Global.domain);

        String requestData = new Gson().toJson(uploadBeaconModel);

        MediaType mediaType = MediaType.parse("application/json");

        RequestBody body =
                RequestBody.create(mediaType, requestData);
        Request request = new Request.Builder()
                .url(Global.domain + "/api/lbs/location_data")
                .post(body)
//			.addHeader("cache-control", "no-cache")
                .addHeader("UUID", Global.projectUUID)
                .addHeader("App-Id", "999")
                .addHeader("Sign-Version", "999")
                .build();

        Call call = Inner.CLIENT.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                MtLog.d("onFailure:" + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                MtLog.d("onResponse");
                MtLog.d("http code:" + response.code());
            }
        });

    }

    public static void fetchUserSlug(String userId, FetchSlugCallback callback) {
        @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(Global.application.getContentResolver(), Settings.Secure.ANDROID_ID);
        MtLog.d("user_uuid:" + Global.userId + ",projectUUID=" + Global.projectUUID + ",domain=" + Global.domain);
        Request request = new Request.Builder()
                .url(Global.domain + "/api/tp/locator_slug?android_device_id=" + android_id + "&third_slug=" + userId)
                .get()
//			.addHeader("cache-control", "no-cache")
                .addHeader("UUID", Global.projectUUID)
                .addHeader("App-Id", "999")
                .addHeader("Sign-Version", "999")
                .build();

        Call call = Inner.CLIENT.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MtLog.d("onFailure:" + e.toString());
                callback.onFail();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                MtLog.d("onResponse");
                String string = null;
                if (response.body() != null) {
                    string = response.body().string();
                }
                MtLog.d("http code:" + response.code() + " " + string);
                UserSlugResp slugResp = new Gson().fromJson(string, UserSlugResp.class);
                Global.slug = slugResp.slug;
                callback.onSuccess(slugResp.slug);
            }
        });
    }


}
