package com.example.handwriting;

import java.io.File;

import com.example.exec.*;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.R.string;
import android.widget.Toast;


public class MovementClassfied {

	private ArrayList<float[]> floatcollectArray = new ArrayList<float[]>();
	private translatedata translatedatapre;
	private int window_length;
	private int window_shift;
	private String path;
	public float num;
	
	public MovementClassfied(String path, int window_length, int window_shift)
			throws IOException {
		translatedatapre = new translatedata(path);
		translatedatapre.file_array();
		floatcollectArray = translatedatapre.floatcollectArray;
		num=floatcollectArray.get(0)[0];
		floatcollectArray.remove(0);
		this.window_length = window_length;
		this.window_shift = window_shift;
		this.path=path;
		Data_predict( path);
	}


	private void Data_predict(String path) throws IOException {

		String pathString =path.replace("sensortestacc.txtThree", "startpoint.txt"); ;
		int length;
		if (floatcollectArray.size() > window_length) {
			length = (floatcollectArray.size() - window_length) / window_shift + 2;
		} else {
			length = 1;
		}
		for (int i = 0; i < length; i++) {
			int end = window_length + i * window_shift;
			if (i == (length - 1)) {
				end = floatcollectArray.size();
			}
			List<float[]> floatinoutlist = floatcollectArray.subList(i
					* window_shift, end);
			ArrayList<float[]> floatinout = new ArrayList<float[]>();
			int listlength = floatinoutlist.size();
			for (int list = 0; list < listlength; list++) {
				floatinout.add(floatinoutlist.get(list));
			}
			//起点判断的特征
			float[] accxmeanshift_extract = translatedata.accxmeanshift_extract(floatinout, false);
			float[] grymean_extract = translatedata.grymean_extract(floatinout,false);
			try {
				FileOutputStream train_model = new FileOutputStream(pathString,true);
				float[] conn = new float[accxmeanshift_extract.length+ grymean_extract.length];
				System.arraycopy(accxmeanshift_extract, 0, conn, 0,accxmeanshift_extract.length);
				System.arraycopy(grymean_extract, 0, conn,accxmeanshift_extract.length, grymean_extract.length);
				StringBuilder featurebuilder = new StringBuilder();
				int connlen = conn.length;
				featurebuilder.append(String.valueOf(1));
				featurebuilder.append(" ");
				for (int k = 0; k < connlen; k++) {
					featurebuilder.append(String.valueOf(k + 1));
					featurebuilder.append(":");
					featurebuilder.append(String.valueOf(conn[k]));
					featurebuilder.append(" ");
				}
				featurebuilder.append("\n");
				String feature = featurebuilder.toString();
				byte[] buffer = new byte[feature.length() * 2];
				buffer = feature.getBytes();
				train_model.write(buffer);
				train_model.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String modelPath=path.replace("sensortestacc.txtThree", "train_out.txt");
		if (!new File(modelPath).exists()) {
			return;
		}
		String[] arg = {pathString, modelPath,
				pathString.replace(".txt", "_pd.txt")};

		try {
			svm_predict.main(arg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 训练割出一个段
	public ArrayList<float[]> Data_splited_return() throws IOException {
		ArrayList<float[]> floatArrayListreturn = new ArrayList<float[]>();
		int movementflag = 0;
		boolean sublistflag = false;
		translatedata translatepredict = new translatedata(
				path.replace("sensortestacc.txtThree", "startpoint_pd.txt"));
		translatepredict.file_array();
		ArrayList<float[]> floatcollectArraypre = translatepredict.floatcollectArray;
		int j = 0;
		for (int i = 0; i < floatcollectArraypre.size(); i++) {
			if (floatcollectArraypre.get(i)[0] == 0) {
				if (j % 2 == 0) {
					movementflag = i;
					j++;
				}
				// 第二次进入这个循环就用movementflag和i来切原始数据
				sublistflag = true;
				continue;
			}
			if (sublistflag) {
				sublistflag = false;
				j++;
				// 在这里切割
				ArrayList<float[]> floatinoutpreArrayList = new ArrayList<float[]>();
				List<float[]> floatcollectArrayreal = floatcollectArray
						.subList(movementflag * window_shift, window_length
								+ (i - 1) * window_shift);
				int listlen = floatcollectArrayreal.size();
				for (int list = 0; list < listlen; list++) {
					floatinoutpreArrayList.add(floatcollectArrayreal.get(list));
				}
				floatArrayListreturn = floatinoutpreArrayList;
			}
		}
		// 如果j是奇数的话，说明最后一段都是运动且还没有被采集过来
		if (j % 2 == 1) {
			// 在这里切割
			ArrayList<float[]> floatinoutpreArrayList = new ArrayList<float[]>();
			List<float[]> floatcollectArrayreal = floatcollectArray.subList(
					movementflag * window_shift, floatcollectArray.size());
			int listlen = floatcollectArrayreal.size();
			for (int list = 0; list < listlen; list++) {
				floatinoutpreArrayList.add(floatcollectArrayreal.get(list));
			}
			floatArrayListreturn = floatinoutpreArrayList;
		}
		new File(path.replace("sensortestacc.txtThree", "startpoint_pd.txt")).delete();
		new File(path.replace("sensortestacc.txtThree", "startpoint.txt")).delete();
		new File(path).delete();
		return floatArrayListreturn;

	}



}
