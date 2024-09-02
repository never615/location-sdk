package com.mallto.sdk;

import java.util.Random;

public class AoaUtils {
	public static byte[] hexStringToBytes(String hex) {
		int len = hex.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
				+ Character.digit(hex.charAt(i + 1), 16));
		}
		return data;
	}


	public static String getAoaData() {
//		String id1 = hexRandom(2);
//		String id2 = hexRandom(2);
//		String id3 = hexRandom(2);
		String uuid = "0001";
		String deviceIdFromServer = Global.slug; //16进制,服务器下发的设备唯一标识
		String checkSum = checksumFunc("1B", "03", "00", "01", deviceIdFromServer);
		String userData = deviceIdFromServer + checkSum;
		String aoaData = "03" + uuid + userData + "50bd" + "84b1" + "329f" + "149d" + "dd6f" + "d310" + "0f38" + "722d" + "a85e" + "c258";
//		String[] serviceUuids = new String[]{
//			uuid, userData1, userData2, "bd50", "b184", "9f32", "9d14", "6fdd", "10d3", "380f", "2d72", "5ea8", "58c2"
//		};

		return aoaData;
	}

	public static String hexRandom(int length) {
		String hexValues = "0123456789abcdef";
		StringBuilder result = new StringBuilder();
		Random random = new Random();

		for (int i = 0; i < length; i++) {
			int index = random.nextInt(hexValues.length());
			result.append(hexValues.charAt(index));
		}

		return result.toString();
	}

	/**
	 * // 示例调用
	 * String result = checksumFunc("1A", "2B", "3C", "4D", "5E", "6F", "7A");
	 * System.out.println(result); // 输出结果
	 *
	 * @param hex1
	 * @param hex2
	 * @param hex3
	 * @param hex4
	 * @param deviceIdFromServer
	 * @return
	 */
	public static String checksumFunc(String hex1, String hex2, String hex3, String hex4, String deviceIdFromServer) {
		// 将十六进制字符串转换为十进制数字
		int dec1 = Integer.parseInt(hex1, 16);
		int dec2 = Integer.parseInt(hex2, 16);
		int dec3 = Integer.parseInt(hex3, 16);
		int dec4 = Integer.parseInt(hex4, 16);
		int dec5 = Integer.parseInt(deviceIdFromServer.substring(0, 2), 16);
		int dec6 = Integer.parseInt(deviceIdFromServer.substring(2, 4), 16);
		int dec7 = Integer.parseInt(deviceIdFromServer.substring(4, 6), 16);

		// 进行加法运算
		int result = dec1 + dec2 + dec3 + dec4 + dec5 + dec6 + dec7;

		// 将结果转换回十六进制字符串
		String hexResult = Integer.toHexString(result).toUpperCase();

		// 返回结果的最后两位
		return hexResult.length() > 2 ? hexResult.substring(hexResult.length() - 2) : hexResult;
	}

}
