package com.example.handwriting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InlinedApi")
public class MainActivity extends Activity implements OnTouchListener{
	private SensorManager mySensorManager; // SensorManager对象引用
	private Sensor myaccelerometer; // 加速度传感器（包括重力）
	private Sensor myrotationSensor;
	private Sensor mygyrSensor;
	private float gyrd[]=new float[3]; // 用于存放最新的陀螺仪数据
	private float accd[]=new float[3]; // 用于存放最新的加速度传感器数据
	private float rotation[]=new float[3];
	private long timeacc;
	private long timerotation;
	private long timegyr;
	private Vibrator vibrator; // 震动
	private Button writebu;
	
	private String path;
	private int wc;
	private String tmpString ;
	private String realString ;
	AttitudeUpdating Aup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		writebu = (Button) findViewById(R.id.writecon);// 用于控制数据记录启动/暂停按钮的显示
		writebu.setOnTouchListener(this);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		// 获得SensorManager对象
		mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// 获取缺省的线性加速度传感器
		myaccelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		myrotationSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		mygyrSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		path=this.getExternalFilesDir(null).toString();
		Log.v("getExternalFilesDir", path);
		tmpString = path+"/sensortestacc.tmp";
		realString = path+"/sensortestacc.txt";
		Aup=new AttitudeUpdating(tmpString,realString);
	}
	
	@Override
	protected void onResume() { // 重写onResume方法
		super.onResume();
		// 监听陀螺仪传感器
		mySensorManager.registerListener(mySensorListener, // 添加监听
				myaccelerometer, // 传感器类型
				SensorManager.SENSOR_DELAY_FASTEST // 传感器事件传递的频度
				);

		mySensorManager.registerListener(mySensorListener, // 添加监听
				myrotationSensor, // 传感器类型
				SensorManager.SENSOR_DELAY_FASTEST // 传感器事件传递的频度
				);

		mySensorManager.registerListener(mySensorListener, // 添加监听
				mygyrSensor, // 传感器类型
				SensorManager.SENSOR_DELAY_FASTEST // 传感器事件传递的频度
				);
	}
	@Override
	protected void onPause() {// 重写onPause方法
		super.onPause();
		mySensorManager.unregisterListener(mySensorListener);// 取消注册监听器
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	
	private SensorEventListener mySensorListener = new SensorEventListener() {// 开发实现了SensorEventListener接口的传感器监听器
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {

			long time = System.currentTimeMillis();
			time = time - time / 10000000 * 10000000;
			float[] values = event.values;// 获取传感器的三个数据
			// 陀螺仪传感器变化
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

				accd[0] = Math.abs(values[0]) > 0.2 ? values[0] : 0;
				accd[1] = Math.abs(values[1]) > 0.2 ? values[1] : 0;
				accd[2] = Math.abs(values[2]) > 0.2 ? values[2] : 0;
				timeacc = time;
			}

			else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

				rotation[0] = (float) values[0];
				rotation[1] = (float) values[1];
				rotation[2] = (float) values[2]; // 将最新的加速度传感器数据存在加速度传感器数组中
				timerotation = time;
				if (wc == 1) {
					try {

						FileOutputStream foStream = new FileOutputStream(tmpString, true); // 定义传感器数据的输出流
						String sensorstr = accd[0] + " " + accd[1] + " "
								+ accd[2] + " " + timeacc + " " + rotation[0]
								+ " " + rotation[1] + " " + rotation[2] + " "
								+ timerotation + " " + gyrd[0] + " " + gyrd[1]
								+ " " + gyrd[2] + " " + timegyr + "\n";
						byte[] buffer = new byte[sensorstr.length() * 2];
						buffer = sensorstr.getBytes();
						foStream.write(buffer);
						foStream.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						Log.v("FileNotFoundException", "OK");
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			} else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				gyrd[0] = Math.abs(values[0]) > 0.02 ? values[0] : 0;
				gyrd[1] = Math.abs(values[1]) > 0.02 ? values[1] : 0;
				gyrd[2] = Math.abs(values[2]) > 0.02 ? values[2] : 0;
				timegyr = time;
			}

		}

	};
	public void onClick_delete(View view) throws IOException // 按下清除后的动作
	{
		final EditText numEditText = (EditText) findViewById(R.id.num);
		final Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("是否存入数据");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try{
					new FeatureExtract(realString+"Three").ExtractDone();
					new FeatureExtract(realString+"One").ExtractDone();
//					new FeatureExtract(realString).ExtractDone();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally {
					 new File(realString).delete(); // 获取文件对象
					new File(tmpString).delete();
				}

			}

		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new File(realString).delete(); // 获取文件对象
				new File(tmpString).delete();
			}
		});
		builder.create().show();

		vibrator.vibrate(200);
	}
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (new File(realString).exists()) {
			Toast.makeText(this, "请先将已经生成的数据加入", Toast.LENGTH_SHORT);
			return false;
		}
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_UP) {
			wc = 0;
			vibrator.vibrate(200);
			view.setBackgroundResource(R.drawable.button1);
 
			final Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("确认输入？");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							try {
								Aup.ThreeUpdating();
								Aup.OneUpdating();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
			builder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new File(tmpString).delete(); // 获取文件对象
						}
					});
			builder.create().show();

		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			
			EditText numEditText = (EditText) findViewById(R.id.num);
			if (numEditText.getEditableText().toString().equals("")) {
				Toast.makeText(this, "label wrong!", Toast.LENGTH_LONG).show();
				return false;
			}
			String numString = numEditText.getEditableText().toString();
			view.setBackgroundResource(R.drawable.button3);
			String sensorstr = numString + "\n";
			byte[] buffer = new byte[sensorstr.length() * 2];
			FileOutputStream foStream;
			try {
				foStream = new FileOutputStream(tmpString, true);
				buffer = sensorstr.getBytes();
				foStream.write(buffer);
				foStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 定义传感器数据的输出流
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			wc = 1;
		}
		return false;
	}
	
	
	
	
	
}
