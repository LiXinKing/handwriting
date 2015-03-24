package com.example.handwriting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import android.R.integer;
import android.provider.SyncStateContract.Constants;
import android.util.Log;

public class translatedata {
	public ArrayList<float[]> floatcollectArray = new ArrayList<float[]>();
	private final static int accslt = 0;
	private final static int gryslt = 4;
	private final static int timepp = 3;
	// private final int time=10;
	private String path_origin;
	private int window_length = 120;
	private int window_shift = 20;
	public translatedata(String path_originpass) {

		path_origin = path_originpass;
		File dirFile = new File("//sdcard/train/feature/");
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

	}

	public void file_array() throws IOException {
		File path_file = new File(path_origin);
		if (!(path_file.exists())) {
			path_file.createNewFile();
		}
		FileInputStream fis;

		fis = new FileInputStream(path_origin);

		byte[] buff = new byte[24];
		int hasread = 0;
		StringBuilder sBuilder = new StringBuilder();
		String receiveString;
		while ((hasread = fis.read(buff)) > 0)// 读取文件中的数据，多次调用read函数光标始终在往下走
		{
			sBuilder.append(new String(buff, 0, hasread));

		}
		receiveString = sBuilder.toString();
		int measure = receiveString.length();
		StringBuilder bufferbuild = new StringBuilder();
		ArrayList<Float> floatbuffer = new ArrayList<Float>();
		
		for (int i = 0, k = 0; i < measure; i++) {
			char c = receiveString.charAt(i);
			if ((c == 32)) {// 32的时候是空格将记的数据存入数组中

				String collect = bufferbuild.toString();
				bufferbuild.delete(0, collect.length());
				Log.v("strtest", collect);

				try {
					float m = Float.parseFloat(collect);
					Log.v("numtest", String.valueOf(m));
					floatbuffer.add(m);
				} catch (Exception NumberFormatException) {

					Log.v("NumberFormatException", "OK");
				}
				// 这里try和catch必须要用，否则会出现错误，用了之后不用改也有效果
				// 可能是系统为了避免可能存在的错误
			} else if (c == 10) {// 10的时候是换行
				String collect = bufferbuild.toString();
				bufferbuild.delete(0, collect.length());
				try {
					float m = Float.parseFloat(collect);
					floatbuffer.add(m);
				} catch (Exception NumberFormatException) {

					Log.v("NumberFormatException", "OK");
				}
				Float[] arraybuffer = new Float[floatbuffer.size()];
				for (int j = 0; j < floatbuffer.size(); j++) {
					arraybuffer[j] = floatbuffer.get(j);
				}

				float[] buffer = new float[arraybuffer.length + 3];
				for (int j = 0; j < arraybuffer.length; j++) {
					buffer[j] = arraybuffer[j].floatValue();
				}

				floatcollectArray.add(buffer);

				floatbuffer.clear();
			} else if (((47 < c) && (c < 58)) || c == 46 || c == 45 || c == 69)
				bufferbuild.append(c);
		}
		if (floatcollectArray.size() > 2)// 为了splited_return而加的
			if (floatcollectArray.get(floatcollectArray.size() - 1).length != floatcollectArray
					.get(floatcollectArray.size() - 2).length)
				floatcollectArray.remove(floatcollectArray.size() - 1);
	}
	
	public static float[] accxmean_extract(
			ArrayList<float[]> floatcollectArray, Boolean flag) {
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;
		long num = floatcollectArray.size();
		int iflag;
		// i=0为标志位，下同
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		for (int i = iflag; i < num - 1; i++) {
			float[] buffer = floatcollectArray.get(i);
			float[] buffer1 = floatcollectArray.get(i + 1);
			sumx += (buffer[accslt] + buffer1[accslt])
					* (buffer1[timepp] - buffer[timepp]) / 2;
			sumy += (buffer[accslt + 1] + buffer1[accslt + 1])
					* (buffer1[timepp] - buffer[timepp]) / 2;
			sumz += (buffer[accslt + 2] + buffer1[accslt + 2])
					* (buffer1[timepp] - buffer[timepp]) / 2;
		}

		float avrx = sumx
				/ (floatcollectArray.get(iflag)[timepp] - floatcollectArray
						.get((int) (num - 1))[timepp]);
		float avry = sumy
				/ (floatcollectArray.get(iflag)[timepp] - floatcollectArray
						.get((int) (num - 1))[timepp]);
		float avrz = sumz
				/ (floatcollectArray.get(iflag)[timepp] - floatcollectArray
						.get((int) (num - 1))[timepp]);
		float acc[] = {avrx, avry, avrz};
		return acc;
	}
	public static float[] grymean_extract(ArrayList<float[]> floatcollectArray,
			Boolean flag) {
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;
		long num = floatcollectArray.size();
		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			sumx += Math.abs(buffer[gryslt]);
			sumy += Math.abs(buffer[gryslt + 1]);
			sumz += Math.abs(buffer[gryslt + 2]);
		}
		float avrx = sumx / (num - iflag);
		float avry = sumy / (num - iflag);
		float avrz = sumz / (num - iflag);
		float gry[] = {avrx, avry, avrz};
		return gry;

	}

	// MAD的提取
	public static float[] accxmeanshift_extract(
			ArrayList<float[]> floatcollectArray, Boolean flag) {
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;
		long num = floatcollectArray.size();
		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		// i=0为标志位，下同
		float[] re = accxmean_extract(floatcollectArray, flag);
		float avrx = re[0];
		float avry = re[1];
		float avrz = re[2];

		for (int i = iflag; i < floatcollectArray.size(); i++) {
			float[] buffer = floatcollectArray.get(i);
			sumx += Math.abs(buffer[accslt] - avrx);
			sumy += Math.abs(buffer[accslt + 1] - avry);
			sumz += Math.abs(buffer[accslt + 2] - avrz);
		}
		avrx = sumx / (num - iflag);
		avry = sumy / (num - iflag);
		avrz = sumz / (num - iflag);
		float acc[] = {avrx, avry, avrz};
		return acc;

	}
	 static void printFloatArrarylist(ArrayList<float[]> floatinout, String path,float num)
			throws IOException {
		 //添加数字
		 {
		 	StringBuilder featurebuilder = new StringBuilder();
		 	featurebuilder.append(String.valueOf(num));
			featurebuilder.append("\n");
			String feature = featurebuilder.toString();
			byte[] bufferbyte = new byte[feature.length() * 2];
			bufferbyte = feature.getBytes();
			FileOutputStream Outstream = new FileOutputStream(path, true);
			Outstream.write(bufferbyte);
			Outstream.close();
		 }
		for (float[] buffer : floatinout) {
			StringBuilder featurebuilder = new StringBuilder();
			int connlen = buffer.length;
			for (int k = 0; k < connlen; k++) {
				featurebuilder.append(String.valueOf(buffer[k]));
				featurebuilder.append(" ");
			}
			featurebuilder.append("\n");
			String feature = featurebuilder.toString();
			byte[] bufferbyte = new byte[feature.length() * 2];
			bufferbyte = feature.getBytes();
			FileOutputStream Outstream = new FileOutputStream(path, true);
			Outstream.write(bufferbyte);
			Outstream.close();
		}

	}
	public static boolean isint(float test) {
		Float aaFloat = new Float(test);
		int i = aaFloat.intValue();
		float t = aaFloat.floatValue();
		if ((t - i) == 0)
			return true;
		else
			return false;

	}


}
