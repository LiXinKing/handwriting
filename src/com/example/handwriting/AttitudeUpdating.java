package com.example.handwriting;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.R.string;
import android.hardware.SensorManager;

public class AttitudeUpdating{
	private String tmpString ;
	private String realString ;
	public AttitudeUpdating(String tmp,String real){
		tmpString=tmp;
		realString=real;
	}
	public void ThreeUpdating() throws IOException {
		float DT = 1.0f / 1000.0f;
		float[] mRotationMatrix = new float[9];
		BufferedReader sb = new BufferedReader(new FileReader(tmpString));
		FileOutputStream foStream = new FileOutputStream(realString+"Three", true); // 定义传感器数据的输出流
		String s = sb.readLine();// 清楚第一个标量号
		s = s + "\n";
		byte[] buffer = new byte[s.length() * 2];
		buffer = s.getBytes();
		foStream.write(buffer);
		// 读出后写回去
		s = sb.readLine();
		String stringArray[] = s.split(" ");
		float preaccx = Float.parseFloat(stringArray[0]);
		float preaccy = Float.parseFloat(stringArray[1]);
		float preaccz = Float.parseFloat(stringArray[2]);

		long pretimeacc = Long.parseLong(stringArray[3]);

		float pregryx = Float.parseFloat(stringArray[8]);
		float pregryy = Float.parseFloat(stringArray[9]);
		float pregryz = Float.parseFloat(stringArray[10]);

		long pretimegry = Long.parseLong(stringArray[11]);

		float preRotationx = Float.parseFloat(stringArray[4]);
		float preRotationy = Float.parseFloat(stringArray[5]);
		float preRotationz = Float.parseFloat(stringArray[6]);
		while ((s = sb.readLine()) != null) {
			stringArray = s.split(" ");
			long accTime = Integer.parseInt(stringArray[3]);
			long grytime = Integer.parseInt(stringArray[11]);
			long rotationTime = Integer.parseInt(stringArray[7]);

			float tmpaccx = Float.parseFloat(stringArray[0]);
			float tmpaccy = Float.parseFloat(stringArray[1]);
			float tmpaccz = Float.parseFloat(stringArray[2]);

			float tmpgryx = Float.parseFloat(stringArray[8]);
			float tmpgryy = Float.parseFloat(stringArray[9]);
			float tmpgryz = Float.parseFloat(stringArray[10]);

			float tmpRotationx = Float.parseFloat(stringArray[4]);
			float tmpRotationy = Float.parseFloat(stringArray[5]);
			float tmpRotationz = Float.parseFloat(stringArray[6]);

			if (accTime != pretimeacc) {
				tmpaccx = (tmpaccx - preaccx) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccx * accTime - tmpaccx * pretimeacc)
						/ (accTime - pretimeacc);
				tmpaccy = (tmpaccy - preaccy) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccy * accTime - tmpaccy * pretimeacc)
						/ (accTime - pretimeacc);
				tmpaccz = (tmpaccz - preaccz) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccz * accTime - tmpaccz * pretimeacc)
						/ (accTime - pretimeacc);
			} else {
				tmpaccx = preaccx;
				tmpaccy = preaccy;
				tmpaccz = preaccz;
			}
			if (grytime != pretimegry) {
				tmpgryx = (tmpgryx - pregryx) / (grytime - pretimegry)
						* rotationTime
						+ (pregryx * grytime - tmpgryx * pretimegry)
						/ (grytime - pretimegry);
				tmpgryy = (tmpgryy - pregryy) / (grytime - pretimegry)
						* rotationTime
						+ (pregryy * grytime - tmpgryy * pretimegry)
						/ (grytime - pretimegry);
				tmpgryz = (tmpgryz - pregryz) / (grytime - pretimegry)
						* rotationTime
						+ (pregryz * grytime - tmpgryz * pretimegry)
						/ (grytime - pretimegry);

			} else {
				tmpgryx = pregryx;
				tmpgryy = pregryy;
				tmpgryz = pregryz;
			} //

			long h1 = pretimegry + (rotationTime - pretimegry) / 3;
			long h2 = pretimegry + (rotationTime - pretimegry) * 2 / 3;

			float wxgain1 = (h1 - pretimegry)
					* (pregryx + (2 * pregryx + tmpgryx) / 3) / 2;
			float wygain1 = (h1 - pretimegry)
					* (pregryy + (2 * pregryy + tmpgryy) / 3) / 2;
			float wzgain1 = (h1 - pretimegry)
					* (pregryz + (2 * pregryz + tmpgryz) / 3) / 2;

			float wxgain2 = (h1 - pretimegry)
					* ((pregryx + 2 * tmpgryx) / 3 + (2 * pregryx + tmpgryx) / 3)
					/ 2;
			float wygain2 = (h1 - pretimegry)
					* ((pregryy + 2 * tmpgryy) / 3 + (2 * pregryy + tmpgryy) / 3)
					/ 2;
			float wzgain2 = (h1 - pretimegry)
					* ((pregryz + 2 * tmpgryz) / 3 + (2 * pregryz + tmpgryz) / 3)
					/ 2;

			float wxgain3 = (h1 - pretimegry)
					* ((pregryx + 2 * tmpgryx) / 3 + tmpgryx) / 2;
			float wygain3 = (h1 - pretimegry)
					* ((pregryy + 2 * tmpgryy) / 3 + tmpgryy) / 2;
			float wzgain3 = (h1 - pretimegry)
					* ((pregryz + 2 * tmpgryz) / 3 + tmpgryz) / 2;

			float mx = 33
					/ 80
					* (wygain1 * wzgain3 - wzgain1 * wygain3)
					+ 57
					/ 80
					* (wygain2 * (wzgain3 - wzgain1) - wzgain2
							* (wygain3 - wygain1)) + wxgain1 + wxgain2
					+ wxgain3;
			float my = 33
					/ 80
					* (wzgain1 * wxgain3 - wxgain1 * wzgain3)
					+ 57
					/ 80
					* (wzgain2 * (wxgain3 - wxgain1) - wxgain2
							* (wzgain3 - wzgain1)) + wygain1 + wygain2
					+ wygain3;
			float mz = 33
					/ 80
					* (wxgain1 * wygain3 - wygain1 * wxgain3)
					+ 57
					/ 80
					* (wxgain2 * (wygain3 - wygain1) - wygain2
							* (wxgain3 - wxgain1)) + wzgain1 + wzgain2
					+ wzgain3;

			float m = (float) Math.sqrt(mx * mx + my * my + mz * mz);
			float q1, q2, q3, q4;
			if (m != 0) {
				q1 = (float) Math.cos(m / 2 * DT);
				q2 = (float) (mx / m * Math.sin(m / 2 * DT));
				q3 = (float) (my / m * Math.sin(m / 2 * DT));
				q4 = (float) (mz / m * Math.sin(m / 2 * DT));
			} else {
				q1 = (float) Math.cos(m / 2);
				q2 = 0;
				q3 = 0;
				q4 = 0;
			}

			float preRotation = 1 - preRotationx * preRotationx - preRotationy
					* preRotationy - preRotationz * preRotationz;

			float calRotation = q1 * preRotation - q2 * preRotationx - q3
					* preRotationy - q4 * preRotationz;
			float calRotationx = q2 * preRotation + q1 * preRotationx + q4
					* preRotationy - q3 * preRotationz;
			float calRotationy = q3 * preRotation - q4 * preRotationx + q1
					* preRotationy + q2 * preRotationz;
			float calRotationz = q4 * preRotation + q3 * preRotationx - q2
					* preRotationy + q1 * preRotationz;

			preaccx = Float.parseFloat(stringArray[0]);
			preaccy = Float.parseFloat(stringArray[1]);
			preaccz = Float.parseFloat(stringArray[2]);
			pretimeacc = accTime;

			pregryx = tmpgryx;
			pregryy = tmpgryy;
			pregryz = tmpgryz;
			pretimegry = rotationTime;

			preRotationx = (calRotationx + tmpRotationx) / 2;
			preRotationy = (calRotationy + tmpRotationy) / 2;
			preRotationz = (calRotationz + tmpRotationz) / 2;

			float[][] bufferacc = {{tmpaccx, 0, 0}, {tmpaccy, 0, 0},
					{tmpaccz, 0, 0}};// 前面三个是加速度
			float[] rotationVect = {(calRotationx + tmpRotationx) / 2,
					(calRotationy + tmpRotationy) / 2,
					(calRotationz + tmpRotationz) / 2};
			SensorManager.getRotationMatrixFromVector(mRotationMatrix,
					rotationVect);
			float[][] rotationversion = new float[3][];
			float[][] mk = {
					{mRotationMatrix[0], mRotationMatrix[1], mRotationMatrix[2]},
					{mRotationMatrix[3], mRotationMatrix[4], mRotationMatrix[5]},
					{mRotationMatrix[6], mRotationMatrix[7], mRotationMatrix[8]}};
			rotationversion = maxtrixmutiply(mk, bufferacc);
			tmpaccx = rotationversion[0][0];
			tmpaccy = rotationversion[1][0];
			tmpaccz = rotationversion[2][0];

			String sensorstr = tmpaccx + " " + tmpaccy + " " + tmpaccz + " "
					+ rotationTime + " "+0 + " " + 0 + " " + 0 + " "
							+ rotationTime +"\n";
			byte[] buffer11 = new byte[sensorstr.length() * 2];
			buffer11 = sensorstr.getBytes();
			foStream.write(buffer11);

		}
		sb.close();
		foStream.close();
	}

	public void TwoUpdating() throws IOException {
		float DT = 1.0f / 1000.0f;
		float[] mRotationMatrix = new float[9];
		BufferedReader sb = new BufferedReader(new FileReader(tmpString));
		FileOutputStream foStream = new FileOutputStream(realString+"Two", true); // 定义传感器数据的输出流
		String s = sb.readLine();// 清楚第一个标量号
		s = s + "\n";
		byte[] buffer = new byte[s.length() * 2];
		buffer = s.getBytes();
		foStream.write(buffer);
		// 读出后写回去
		s = sb.readLine();
		String stringArray[] = s.split(" ");
		float preaccx = Float.parseFloat(stringArray[0]);
		float preaccy = Float.parseFloat(stringArray[1]);
		float preaccz = Float.parseFloat(stringArray[2]);

		long pretimeacc = Long.parseLong(stringArray[3]);

		float pregryx = Float.parseFloat(stringArray[8]);
		float pregryy = Float.parseFloat(stringArray[9]);
		float pregryz = Float.parseFloat(stringArray[10]);

		long pretimegry = Long.parseLong(stringArray[11]);

		float preRotationx = Float.parseFloat(stringArray[4]);
		float preRotationy = Float.parseFloat(stringArray[5]);
		float preRotationz = Float.parseFloat(stringArray[6]);
		while ((s = sb.readLine()) != null) {
			stringArray = s.split(" ");
			long accTime = Integer.parseInt(stringArray[3]);
			long grytime = Integer.parseInt(stringArray[11]);
			long rotationTime = Integer.parseInt(stringArray[7]);

			float tmpaccx = Float.parseFloat(stringArray[0]);
			float tmpaccy = Float.parseFloat(stringArray[1]);
			float tmpaccz = Float.parseFloat(stringArray[2]);

			float tmpgryx = Float.parseFloat(stringArray[8]);
			float tmpgryy = Float.parseFloat(stringArray[9]);
			float tmpgryz = Float.parseFloat(stringArray[10]);

			float tmpRotationx = Float.parseFloat(stringArray[4]);
			float tmpRotationy = Float.parseFloat(stringArray[5]);
			float tmpRotationz = Float.parseFloat(stringArray[6]);

			if (accTime != pretimeacc) {
				tmpaccx = (tmpaccx - preaccx) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccx * accTime - tmpaccx * pretimeacc)
						/ (accTime - pretimeacc);
				tmpaccy = (tmpaccy - preaccy) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccy * accTime - tmpaccy * pretimeacc)
						/ (accTime - pretimeacc);
				tmpaccz = (tmpaccz - preaccz) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccz * accTime - tmpaccz * pretimeacc)
						/ (accTime - pretimeacc);
			} else {
				tmpaccx = preaccx;
				tmpaccy = preaccy;
				tmpaccz = preaccz;
			}
			if (grytime != pretimegry) {
				tmpgryx = (tmpgryx - pregryx) / (grytime - pretimegry)
						* rotationTime
						+ (pregryx * grytime - tmpgryx * pretimegry)
						/ (grytime - pretimegry);
				tmpgryy = (tmpgryy - pregryy) / (grytime - pretimegry)
						* rotationTime
						+ (pregryy * grytime - tmpgryy * pretimegry)
						/ (grytime - pretimegry);
				tmpgryz = (tmpgryz - pregryz) / (grytime - pretimegry)
						* rotationTime
						+ (pregryz * grytime - tmpgryz * pretimegry)
						/ (grytime - pretimegry);

			} else {
				tmpgryx = pregryx;
				tmpgryy = pregryy;
				tmpgryz = pregryz;
			} //
			// 用二字样拟合w=a+2bx

			long h = (pretimegry + rotationTime) / 2;

			float wxgain1 = (h - pretimegry)
					* (pregryx + (pregryx + tmpgryx) / 2) / 2;
			float wygain1 = (h - pretimegry)
					* (pregryy + (pregryy + tmpgryy) / 2) / 2;
			float wzgain1 = (h - pretimegry)
					* (pregryz + (pregryz + tmpgryz) / 2) / 2;

			float wxgain2 = (h - pretimegry)
					* (tmpgryx + (pregryx + tmpgryx) / 2) / 2;
			float wygain2 = (h - pretimegry)
					* (tmpgryy + (pregryy + tmpgryy) / 2) / 2;
			float wzgain2 = (h - pretimegry)
					* (tmpgryz + (pregryz + tmpgryz) / 2) / 2;

			float mx = 2 / 3 * (wygain1 * wzgain2 - wzgain1 * wygain2)
					+ wxgain1 + wxgain2;
			float my = 2 / 3 * (wzgain1 * wxgain2 - wxgain1 * wzgain2)
					+ wygain1 + wygain2;
			float mz = 2 / 3 * (wxgain1 * wygain2 - wygain1 * wxgain2)
					+ wzgain1 + wzgain2;

			float m = (float) Math.sqrt(mx * mx + my * my + mz * mz);
			float q1, q2, q3, q4;
			if (m != 0) {
				q1 = (float) Math.cos(m / 2 * DT);
				q2 = (float) (mx / m * Math.sin(m / 2 * DT));
				q3 = (float) (my / m * Math.sin(m / 2 * DT));
				q4 = (float) (mz / m * Math.sin(m / 2 * DT));
			} else {
				q1 = (float) Math.cos(m / 2);
				q2 = 0;
				q3 = 0;
				q4 = 0;
			}

			float preRotation = 1 - preRotationx * preRotationx - preRotationy
					* preRotationy - preRotationz * preRotationz;

			float calRotation = q1 * preRotation - q2 * preRotationx - q3
					* preRotationy - q4 * preRotationz;
			float calRotationx = q2 * preRotation + q1 * preRotationx + q4
					* preRotationy - q3 * preRotationz;
			float calRotationy = q3 * preRotation - q4 * preRotationx + q1
					* preRotationy + q2 * preRotationz;
			float calRotationz = q4 * preRotation + q3 * preRotationx - q2
					* preRotationy + q1 * preRotationz;

			preaccx = Float.parseFloat(stringArray[0]);
			preaccy = Float.parseFloat(stringArray[1]);
			preaccz = Float.parseFloat(stringArray[2]);
			pretimeacc = accTime;

			pregryx = tmpgryx;
			pregryy = tmpgryy;
			pregryz = tmpgryz;
			pretimegry = rotationTime;

			preRotationx = (calRotationx + tmpRotationx) / 2;
			preRotationy = (calRotationy + tmpRotationy) / 2;
			preRotationz = (calRotationz + tmpRotationz) / 2;

			float[][] bufferacc = {{tmpaccx, 0, 0}, {tmpaccy, 0, 0},
					{tmpaccz, 0, 0}};// 前面三个是加速度
			float[] rotationVect = {calRotationx, calRotationy, calRotationz};
			SensorManager.getRotationMatrixFromVector(mRotationMatrix,
					rotationVect);
			float[][] rotationversion = new float[3][];
			float[][] mk = {
					{mRotationMatrix[0], mRotationMatrix[1], mRotationMatrix[2]},
					{mRotationMatrix[3], mRotationMatrix[4], mRotationMatrix[5]},
					{mRotationMatrix[6], mRotationMatrix[7], mRotationMatrix[8]}};
			rotationversion = maxtrixmutiply(mk, bufferacc);
			tmpaccx = rotationversion[0][0];
			tmpaccy = rotationversion[1][0];
			tmpaccz = rotationversion[2][0];

			float[] rotationVect1 = {tmpRotationx, tmpRotationy, tmpRotationz};
			SensorManager.getRotationMatrixFromVector(mRotationMatrix,
					rotationVect1);
			float[][] mk1 = {
					{mRotationMatrix[0], mRotationMatrix[1], mRotationMatrix[2]},
					{mRotationMatrix[3], mRotationMatrix[4], mRotationMatrix[5]},
					{mRotationMatrix[6], mRotationMatrix[7], mRotationMatrix[8]}};
			rotationversion = maxtrixmutiply(mk1, bufferacc);
			float tmpaccx1 = rotationversion[0][0];
			float tmpaccy1 = rotationversion[1][0];
			float tmpaccz1 = rotationversion[2][0];

			String sensorstr = tmpaccx + " " + tmpaccy + " " + tmpaccz + " "
					+ rotationTime + " "+0 + " " + 0 + " " + 0 + " "
							+ rotationTime +"\n";
			byte[] buffer11 = new byte[sensorstr.length() * 2];
			buffer11 = sensorstr.getBytes();
			foStream.write(buffer11);

		}
		sb.close();
		foStream.close();
	}
	public void OneUpdating() throws IOException {
		float DT = 1.0f / 1000.0f;
		float[] mRotationMatrix = new float[9];
		BufferedReader sb = new BufferedReader(new FileReader(tmpString));
		FileOutputStream foStream = new FileOutputStream(realString+"One", true); // 定义传感器数据的输出流
		String s = sb.readLine();// 清楚第一个标量号
		s = s + "\n";
		byte[] buffer = new byte[s.length() * 2];
		buffer = s.getBytes();
		foStream.write(buffer);
		// 读出后写回去
		s = sb.readLine();
		String stringArray[] = s.split(" ");
		float preaccx = Float.parseFloat(stringArray[0]);
		float preaccy = Float.parseFloat(stringArray[1]);
		float preaccz = Float.parseFloat(stringArray[2]);

		long pretimeacc = Long.parseLong(stringArray[3]);

		float pregryx = Float.parseFloat(stringArray[8]);
		float pregryy = Float.parseFloat(stringArray[9]);
		float pregryz = Float.parseFloat(stringArray[10]);

		long pretimegry = Long.parseLong(stringArray[11]);

		float preRotationx = Float.parseFloat(stringArray[4]);
		float preRotationy = Float.parseFloat(stringArray[5]);
		float preRotationz = Float.parseFloat(stringArray[6]);
		while ((s = sb.readLine()) != null) {
			stringArray = s.split(" ");
			long accTime = Integer.parseInt(stringArray[3]);
			long grytime = Integer.parseInt(stringArray[11]);
			long rotationTime = Integer.parseInt(stringArray[7]);

			float tmpaccx = Float.parseFloat(stringArray[0]);
			float tmpaccy = Float.parseFloat(stringArray[1]);
			float tmpaccz = Float.parseFloat(stringArray[2]);

			float tmpgryx = Float.parseFloat(stringArray[8]);
			float tmpgryy = Float.parseFloat(stringArray[9]);
			float tmpgryz = Float.parseFloat(stringArray[10]);

			float tmpRotationx = Float.parseFloat(stringArray[4]);
			float tmpRotationy = Float.parseFloat(stringArray[5]);
			float tmpRotationz = Float.parseFloat(stringArray[6]);

			if (accTime != pretimeacc) {
				tmpaccx = (tmpaccx - preaccx) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccx * accTime - tmpaccx * pretimeacc)
						/ (accTime - pretimeacc);
				tmpaccy = (tmpaccy - preaccy) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccy * accTime - tmpaccy * pretimeacc)
						/ (accTime - pretimeacc);
				tmpaccz = (tmpaccz - preaccz) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccz * accTime - tmpaccz * pretimeacc)
						/ (accTime - pretimeacc);
			} else {
				tmpaccx = preaccx;
				tmpaccy = preaccy;
				tmpaccz = preaccz;
			}
			if (grytime != pretimegry) {
				tmpgryx = (tmpgryx - pregryx) / (grytime - pretimegry)
						* rotationTime
						+ (pregryx * grytime - tmpgryx * pretimegry)
						/ (grytime - pretimegry);
				tmpgryy = (tmpgryy - pregryy) / (grytime - pretimegry)
						* rotationTime
						+ (pregryy * grytime - tmpgryy * pretimegry)
						/ (grytime - pretimegry);
				tmpgryz = (tmpgryz - pregryz) / (grytime - pretimegry)
						* rotationTime
						+ (pregryz * grytime - tmpgryz * pretimegry)
						/ (grytime - pretimegry);

			} else {
				tmpgryx = pregryx;
				tmpgryy = pregryy;
				tmpgryz = pregryz;
			} //
			// 用二字样拟合w=a+2bx

			long h = rotationTime-pretimegry ;

			float mx =h*(pregryx + tmpgryx) / 2;
			float my =h*(pregryy + tmpgryy) / 2;
			float mz = h* (pregryz + tmpgryz) / 2;

			float m = (float) Math.sqrt(mx * mx + my * my + mz * mz);
			float q1, q2, q3, q4;
			if (m != 0) {
				q1 = (float) Math.cos(m / 2 * DT);
				q2 = (float) (mx / m * Math.sin(m / 2 * DT));
				q3 = (float) (my / m * Math.sin(m / 2 * DT));
				q4 = (float) (mz / m * Math.sin(m / 2 * DT));
			} else {
				q1 = (float) Math.cos(m / 2);
				q2 = 0;
				q3 = 0;
				q4 = 0;
			}

			float preRotation = 1 - preRotationx * preRotationx - preRotationy
					* preRotationy - preRotationz * preRotationz;

			float calRotation = q1 * preRotation - q2 * preRotationx - q3
					* preRotationy - q4 * preRotationz;
			float calRotationx = q2 * preRotation + q1 * preRotationx + q4
					* preRotationy - q3 * preRotationz;
			float calRotationy = q3 * preRotation - q4 * preRotationx + q1
					* preRotationy + q2 * preRotationz;
			float calRotationz = q4 * preRotation + q3 * preRotationx - q2
					* preRotationy + q1 * preRotationz;

			preaccx = Float.parseFloat(stringArray[0]);
			preaccy = Float.parseFloat(stringArray[1]);
			preaccz = Float.parseFloat(stringArray[2]);
			pretimeacc = accTime;

			pregryx = tmpgryx;
			pregryy = tmpgryy;
			pregryz = tmpgryz;
			pretimegry = rotationTime;

//			preRotationx = (calRotationx + tmpRotationx) / 2;
//			preRotationy = (calRotationy + tmpRotationy) / 2;
//			preRotationz = (calRotationz + tmpRotationz) / 2;
			preRotationx = calRotationx ;
			preRotationy = calRotationy ;
			preRotationz = calRotationz ;

			float[][] bufferacc = {{tmpaccx, 0, 0}, {tmpaccy, 0, 0},
					{tmpaccz, 0, 0}};// 前面三个是加速度
			float[] rotationVect = {calRotationx, calRotationy, calRotationz};
			SensorManager.getRotationMatrixFromVector(mRotationMatrix,
					rotationVect);
			float[][] rotationversion = new float[3][];
			float[][] mk = {
					{mRotationMatrix[0], mRotationMatrix[1], mRotationMatrix[2]},
					{mRotationMatrix[3], mRotationMatrix[4], mRotationMatrix[5]},
					{mRotationMatrix[6], mRotationMatrix[7], mRotationMatrix[8]}};
			rotationversion = maxtrixmutiply(mk, bufferacc);
			tmpaccx = rotationversion[0][0];
			tmpaccy = rotationversion[1][0];
			tmpaccz = rotationversion[2][0];

			float[] rotationVect1 = {tmpRotationx, tmpRotationy, tmpRotationz};
			SensorManager.getRotationMatrixFromVector(mRotationMatrix,
					rotationVect1);
			float[][] mk1 = {
					{mRotationMatrix[0], mRotationMatrix[1], mRotationMatrix[2]},
					{mRotationMatrix[3], mRotationMatrix[4], mRotationMatrix[5]},
					{mRotationMatrix[6], mRotationMatrix[7], mRotationMatrix[8]}};
			rotationversion = maxtrixmutiply(mk1, bufferacc);
			float tmpaccx1 = rotationversion[0][0];
			float tmpaccy1 = rotationversion[1][0];
			float tmpaccz1 = rotationversion[2][0];

			String sensorstr = tmpaccx + " " + tmpaccy + " " + tmpaccz + " "
					+ rotationTime + " "+0 + " " + 0 + " " + 0 + " "
							+ rotationTime +"\n";
			byte[] buffer11 = new byte[sensorstr.length() * 2];
			buffer11 = sensorstr.getBytes();
			foStream.write(buffer11);

		}
		sb.close();
		foStream.close();
		
		
	}
	private static float[][] maxtrixmutiply(float[][] maxtrileft,
			float[][] maxtriright) {
		float[][] result = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
		// TODO Auto-generated method stub

		for (int i = 0; i < maxtrileft.length; i++)
			for (int j = 0; j < maxtrileft[0].length; j++) {
				result[i][j] = maxtrileft[i][0] * maxtriright[0][j]
						+ maxtrileft[i][1] * maxtriright[1][j]
						+ maxtrileft[i][2] * maxtriright[2][j];
			}
		return result;
	}// 一个矩阵乘法
	
	
}