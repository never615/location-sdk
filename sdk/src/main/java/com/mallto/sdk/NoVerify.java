package com.mallto.sdk;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class NoVerify {
    @SuppressLint("CustomX509TrustManager")
    public static TrustManager[] getX509TrustManager() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
    }

    public static SSLSocketFactory getSocketFactory(TrustManager[] x509) {
        try {

            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, x509, new SecureRandom());
            return context.getSocketFactory();
        } catch (Exception e) {
            return null;
        }
    }

    public static HostnameVerifier getHostNameVerifier() {
        return new HostnameVerifier() {
            //这里存放不需要忽略SSL证书的域名，为空即忽略所有证书
            String[] ssls = {};

            @Override
            public boolean verify(String hostname, SSLSession session) {
                MtLog.d("ignore verify:" + hostname);
                if (TextUtils.isEmpty(hostname)) {
                    return false;
                }
                return !Arrays.asList(ssls).contains(hostname);
            }
        };
    }

    public static OkHttpClient getUnSafeOkhttpClient() {
        final TrustManager[] trustAllCerts = getX509TrustManager();
        SSLSocketFactory socketFactory = getSocketFactory(trustAllCerts);
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        if (socketFactory != null) {
            builder.sslSocketFactory(socketFactory, (X509TrustManager)(trustAllCerts[0]));
        }
        builder.hostnameVerifier(getHostNameVerifier());
        return builder.build();
    }
}
