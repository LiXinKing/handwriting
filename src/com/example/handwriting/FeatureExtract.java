package com.example.handwriting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.R.string;
import android.util.Log;

public class FeatureExtract {
	private String handlefileString;
	private final static int accslt = 0;
	public   FeatureExtract(String inputString) {
		// TODO Auto-generated constructor stub
		handlefileString=inputString;
	}
		
	boolean ExtractDone() throws IOException{
		File inputFile=new File(handlefileString);
		String sclass=null;
		ArrayList<float[]> floatDataArrayList=new ArrayList<float[]>();
		if(!inputFile.exists())return false;
		BufferedReader intputStream = new BufferedReader(new FileReader(handlefileString));
		FileOutputStream outputStream = new FileOutputStream(handlefileString+"Feature", true); // 定义传感器数据的输出流
		String s = intputStream.readLine();// 清楚第一个标量号
		if(s==null||s.length()!=1)return false;
		sclass=s;
		s = s + "\n";
		byte[] buffer = new byte[s.length() * 2];
		buffer = s.getBytes();
		outputStream.write(buffer);
		// 读出后写回去
		String StringBuffer[]=null;
		float[] FloatBuffer=new float[8];
		while((s=intputStream.readLine())!=null){

		s =intputStream.readLine();
		try {
			StringBuffer= s.split(" ");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		int count=0;
		for(String buff:StringBuffer ){
			FloatBuffer[count++]=Float.parseFloat(buff);
		}
		floatDataArrayList.add(FloatBuffer);
		
		}
		
		float[] conn = new float[3 * 5];
		float[] energy = null;
		float[] IRQ = null;
		float[] corr = null;
		float[] rms = null;
		try {
			energy = energyExtract(floatDataArrayList);
			IRQ = IRQExtract(floatDataArrayList);
			corr = corrExtract(floatDataArrayList);
			rms = RMSExtract(floatDataArrayList);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		float[] MAD = accxmeanshiftExtract(floatDataArrayList);
		System.arraycopy(rms, 0, conn, 0, 3);
		System.arraycopy(energy, 0, conn, 3, 3);
		System.arraycopy(MAD, 0, conn, 6, 3);
		System.arraycopy(IRQ, 0, conn, 9, 3);
		System.arraycopy(corr, 0, conn, 12, 3);
		
		String[] RFtract = RFExtract(floatDataArrayList);
		try {
			StringBuilder featurebuilder = new StringBuilder();
			featurebuilder.append(sclass);
			featurebuilder.append(" ");
			int connlen = conn.length;
			int k=0;
			for (k = 0; k < connlen; k++) {
				featurebuilder.append(String.valueOf(k + 1));
				featurebuilder.append(":");
				featurebuilder.append(String.valueOf(conn[k]));
				featurebuilder.append(" ");
			}
			for (int i=0; i < 3; i++) {
				featurebuilder.append(String.valueOf(k + 1));
				featurebuilder.append(":");
				featurebuilder.append(RFtract[i]);
				featurebuilder.append(" ");
				k++;
			}
			featurebuilder.append("\n");
			String feature = featurebuilder.toString();
			byte[] bufferout = new byte[feature.length() * 2];
			bufferout = feature.getBytes();
			outputStream.write(bufferout);
			outputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	//加速度平均值
	public static float[] accxmeanExtract(
			ArrayList<float[]> floatcollectArray) {
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;
		long num = floatcollectArray.size();
		int iflag=0;

		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			sumx +=buffer[accslt] ;
			sumy += buffer[accslt + 1] ;
			sumz += buffer[accslt + 2] ;
		}
		float avrx = sumx / (num - iflag);
		float avry = sumy / (num - iflag);
		float avrz = sumz / (num - iflag);

		float acc[] = {avrx, avry, avrz};
		return acc;
	}
	// 标准差的提取函数
	private static float[] STDExtract(ArrayList<float[]> floatcollectArray) throws Exception {
		float[] mean = accxmeanExtract(floatcollectArray);
		float meanx = mean[0];
		float meany = mean[1];
		float meanz = mean[2];
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;
		int num = floatcollectArray.size();
		int iflag=0;

		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			sumx += (buffer[0] - meanx) * (buffer[0] - meanx);
			sumy += (buffer[1] - meany) * (buffer[1] - meany);
			sumz += (buffer[2] - meanz) * (buffer[2] - meanz);
		}
		float mtdx = (float) Math.sqrt(sumx / (num - 1));
		float mtdy = (float) Math.sqrt(sumy / (num - 1));
		float mtdz = (float) Math.sqrt(sumz / (num - 1));
		float[] mtd = {mtdx, mtdy, mtdz};
		return mtd;

	}
	// MAD的提取
		public static float[] accxmeanshiftExtract(
				ArrayList<float[]> floatcollectArray) {
			float sumx = 0;
			float sumy = 0;
			float sumz = 0;
			long num = floatcollectArray.size();
			int iflag=0;

			// i=0为标志位，下同
			float[] re = accxmeanExtract(floatcollectArray);
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
	// 超过10s的数据在这里会出现问题的，注意
	private static float[] energyExtract(ArrayList<float[]> floatcollectArray) throws Exception {

		float eneryx = 0;
		float eneryy = 0;
		float eneryz = 0;
		int num = floatcollectArray.size();
		int iflag=0;
		float[] x = new float[num - iflag];
		float[] y = new float[num - iflag];
		float[] z = new float[num - iflag];
		for (int i = iflag, k = 0; i < num; i++, k++) {
			x[k] = floatcollectArray.get(i)[0];
			y[k] = floatcollectArray.get(i)[1];
			z[k] = floatcollectArray.get(i)[2];
		}
		FFTCal fftx = new FFTCal(x);
		eneryx = fftx.energy_cal();

		FFTCal ffty = new FFTCal(y);
		eneryy = ffty.energy_cal();

		FFTCal fftz = new FFTCal(z);
		eneryz = fftz.energy_cal();

		float acc[] = {eneryx, eneryy, eneryz};
		return acc;

	}
	// IRQ四分位差的计算
	private static float[] IRQExtract(ArrayList<float[]> floatcollectArray) throws Exception {
		int num = floatcollectArray.size();

		int iflag=0;
		float[] x = new float[num - iflag];
		float[] y = new float[num - iflag];
		float[] z = new float[num - iflag];
		for (int i = iflag, k = 0; i < num; i++, k++) {
			x[k] = floatcollectArray.get(i)[0];
			y[k] = floatcollectArray.get(i)[1];
			z[k] = floatcollectArray.get(i)[2];
		}
		Arrays.sort(x);
		Arrays.sort(y);
		Arrays.sort(z);
		int numreal = num - iflag + 1;
		int q1, q3;// 位置
		float Q1x, Q3x;
		float Q1y, Q3y;
		float Q1z, Q3z;
		if (numreal % 4 == 0) {
			q1 = numreal / 4;
			q3 = 3 * q1;
			Q1x = x[q1 - 1];
			Q3x = x[q3 - 1];

			Q1y = y[q1 - 1];
			Q3y = y[q3 - 1];

			Q1z = z[q1 - 1];
			Q3z = z[q3 - 1];
		} else {
			float q1_f = ((float) numreal) / 4;
			float q3_f = 3 * q1_f;
			q1 = numreal / 4;
			q3 = 3 * q1;
			Q1x = (q1_f - q1) * x[q1 + 1] + (q1 + 1 - q1_f) * x[q1];
			Q3x = (q3_f - q3) * x[q3 + 1] + (q3 + 1 - q3_f) * x[q3];

			Q1y = (q1_f - q1) * y[q1 + 1] + (q1 + 1 - q1_f) * y[q1];
			Q3y = (q3_f - q3) * y[q3 + 1] + (q3 + 1 - q3_f) * y[q3];

			Q1z = (q1_f - q1) * z[q1 + 1] + (q1 + 1 - q1_f) * z[q1];
			Q3z = (q3_f - q3) * z[q3 + 1] + (q3 + 1 - q3_f) * z[q3];
		}
		float IRQx = Q3x - Q1x;
		float IRQy = Q3y - Q1y;
		float IRQz = Q3z - Q1z;

		float[] rms = {IRQx, IRQy, IRQz};
		return rms;

	}
	// rms提取
	private static float[] RMSExtract(ArrayList<float[]> floatcollectArray) throws Exception {
		int num = floatcollectArray.size();
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;

		float rmsx = 0;
		float rmsy = 0;
		float rmsz = 0;

		int iflag=0;

		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			sumx += (buffer[0]) * (buffer[0]);
			sumy += (buffer[1]) * (buffer[1]);
			sumz += (buffer[2]) * (buffer[2]);

		}
		rmsx = sumx / num;
		rmsy = sumy / num;
		rmsz = sumz / num;

		float[] rms = {rmsx, rmsy, rmsz};
		return rms;

	}
	private static float[] corrExtract(ArrayList<float[]> floatcollectArray) throws Exception {
		float[] std = STDExtract(floatcollectArray);
		float[] mean = accxmeanExtract(floatcollectArray);
		int num = floatcollectArray.size();

		float stdx = std[0];
		float stdy = std[1];
		float stdz = std[2];

		float meanx = mean[0];
		float meany = mean[1];
		float meanz = mean[2];

		float corrxy = 0;
		float corrxz = 0;
		float corryz = 0;

		float sumxy = 0;
		float sumxz = 0;
		float sumyz = 0;

		int iflag=0;

		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			sumxy += (buffer[0] - meanx) * (buffer[1] - meany);
			sumxz += (buffer[0] - meanx) * (buffer[2] - meanz);
			sumyz += (buffer[1] - meany) * (buffer[2] - meanz);
		}
		if(stdx==0||stdy==0||stdz==0){
			float[] corr = {corrxy, corrxz, corryz};
			return corr;
		}
		corrxy = sumxy / (num * stdx * stdy);
		corrxz = sumxz / (num * stdx * stdz);
		corryz = sumyz / (num * stdy * stdz);
		float[] corr = {corrxy, corrxz, corryz};
		return corr;

	}
	/*
	 * RF特征：该特征主要表达的是加速度旋转的方向。Cxy来表示三维加速度映射到Z轴的分量，x>0,y>0时以编码0表示；x>0,y<0时以编码1来表示；
	 * x<0,y>0时以编码2来表示；x<0,y<0时以编码3来表示；然后Cxz和Cyz类推。
	 */
	public String[] RFExtract(ArrayList<float[]> floatcollectArray) {
		StringBuilder bufferCxy = new StringBuilder();
		StringBuilder bufferCxz = new StringBuilder();
		StringBuilder bufferCyz = new StringBuilder();
		int num = floatcollectArray.size();
		int Cxy = 0, Cxyl = 0;
		int Cxz = 0, Cxzl = 0;
		int Cyz = 0, Cyzl = 0;
		int iflag=0;

		// 提取Cxy
		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			if (i == iflag + 1) {
				Cxyl = Cxy;
				Cxzl = Cxz;
				Cyzl = Cyz;
			}
			if ((buffer[accslt] > 0.5) && (buffer[accslt + 1] > 0.5))
				Cxy = 0;
			else if ((buffer[accslt] > 0.5) && (buffer[accslt + 1] < -0.5))
				Cxy = 1;
			else if ((buffer[accslt] < -0.5) && (buffer[accslt + 1] > 0.5))
				Cxy = 2;
			else if ((buffer[accslt] < -0.5) && (buffer[accslt + 1] < -0.5))
				Cxy = 3;

			if ((buffer[accslt] > 0.5) && (buffer[accslt + 2] > 0.5))
				Cxz = 0;
			else if ((buffer[accslt] > 0.5) && (buffer[accslt + 2] < -0.5))
				Cxz = 1;
			else if ((buffer[accslt] < -0.5) && (buffer[accslt + 2] > 0.5))
				Cxz = 2;
			else if ((buffer[accslt] < -0.5) && (buffer[accslt + 2] < -0.5))
				Cxz = 3;

			if ((buffer[accslt + 1] > 0.5) && (buffer[accslt + 2] > 0.5))
				Cyz = 0;
			else if ((buffer[accslt + 1] > 0.5) && (buffer[accslt + 2] < -0.5))
				Cyz = 1;
			else if ((buffer[accslt + 1] < -0.5) && (buffer[accslt + 2] > 0.5))
				Cyz = 2;
			else if ((buffer[accslt + 1] < -0.5) && (buffer[accslt + 2] < -0.5))
				Cyz = 3;
			if (i == iflag) {
				bufferCxy.append(String.valueOf(Cxy));
				bufferCxz.append(String.valueOf(Cxz));
				bufferCyz.append(String.valueOf(Cyz));
				continue;
			}

			if (!(Cxy == Cxyl)) {
				bufferCxy.append(String.valueOf(Cxy));
				Cxyl = Cxy;
			}
			if (!(Cxz == Cxzl)) {
				bufferCxz.append(String.valueOf(Cxz));
				Cxzl = Cxz;
			}
			if (!(Cyz == Cyzl)) {
				bufferCyz.append(String.valueOf(Cyz));
				Cyzl = Cyz;
			}

		}
		String[] bufferStrings = {bufferCxy.toString(), bufferCxz.toString(),
				bufferCyz.toString()};
		return bufferStrings;
	}
}
