package com.mallto.sdk;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;

import androidx.annotation.RequiresPermission;

import org.altbeacon.beacon.BeaconManager;

import java.nio.ByteBuffer;

public class BluetoothAOAAdvertiser {

    private static final String BLUETOOTH_AOA_SERVICE_UUID = "0000110a-0000-1000-8000-00805f9b34fb";

    private final BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseCallback mAdvertiseCallback;

    public static BluetoothAOAAdvertiser INSTANCE = new BluetoothAOAAdvertiser();

    public BluetoothAOAAdvertiser() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    public void startAdvertising() {
        AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(false)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();

        byte[] bytes = getManufacturerDataBytes();

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
//                .addServiceUuid(new ParcelUuid(ParcelUuid.fromString(BLUETOOTH_AOA_SERVICE_UUID).getUuid()))
                .addManufacturerData(76, bytes) // 厂家id？
                .build();

        mAdvertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                // 广播启动成功
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                // 广播启动失败
            }
        };

        mBluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, mAdvertiseCallback);
    }

    /**
     * 拼接
     * length 1 byte
     * type 1 byte
     * device info 7bytes
     * crc16 2byte
     * cte info 20bytes
     */
//            return new byte[]{
////                (byte) 0x42,
////                (byte) 0x25,
////                (byte) 0xAA,(byte) 0xBB,(byte) 0xCC,(byte) 0xDD,(byte) 0xEE,(byte) 0xFF,
// (byte) 0x1E,
////                (byte) 0xFF,
////                (byte) 0x01,(byte) 0x00,(byte) 0x01,(byte) 0x00, (byte) 0x00,(byte) 0x00,(byte) 0x00,
////               (byte) 0x00, (byte) 0xFD, // crc16
////        };
    private byte[] getManufacturerDataBytes() {

        ByteBuffer advA_deviceInfo = ByteBuffer.allocate(15);
        advA_deviceInfo.put(new byte[]{ // advA 应该是固定的吧
                (byte) 0xAA,
                (byte) 0xBB,
                (byte) 0xCC,
                (byte) 0xDD,
                (byte) 0xEE,
                (byte) 0xFF,
        });
        ByteBuffer manufacturerData = ByteBuffer.allocate(31);
        // 状态、配置信息分配表，从图里拿到7bytes的信息
        // update By filling in cmd=0xBB, all other fields can be filled with 0x00, 先用0xBB填充
        byte[] deviceInfo = new byte[]{
                (byte) 0xBB, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };
        manufacturerData.put(0, (byte) 0x1E); // 长度30
        manufacturerData.put((byte) 0xFF); // type
        manufacturerData.put(deviceInfo); // device info 7 bytes

        advA_deviceInfo.put(manufacturerData.array());

        int crc16 = CRC16.crc16(advA_deviceInfo.array(), 15, 0XFFFF);

// 将 int 类型的 CRC 值转换为 byte 数组
        byte[] crcBytes = new byte[2];
        crcBytes[0] = (byte) (crc16 >> 8);
        crcBytes[1] = (byte) crc16;

        manufacturerData.put(crcBytes); // crc16

        byte[] cteInfo = new byte[]{
                (byte) 0x50, (byte) 0xbd, (byte) 0x84, (byte) 0xb1, // CTE Info
                (byte) 0x32, (byte) 0x9F, (byte) 0x14, (byte) 0x9d,
                (byte) 0xdd, (byte) 0x6f, (byte) 0xd3, (byte) 0x10,
                (byte) 0x0f, (byte) 0x38, (byte) 0x72, (byte) 0x2d,
                (byte) 0xa8, (byte) 0x5e, (byte) 0xc2, (byte) 0x58
        };
        manufacturerData.put(cteInfo); // cte 固定
        return manufacturerData.array();
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    public void stopAdvertising() {
        if (mBluetoothLeAdvertiser != null && mAdvertiseCallback != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        }
    }

}