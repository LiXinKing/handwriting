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
	private SensorManager mySensorManager; // SensorManager��������
	private Sensor myaccelerometer; // ���ٶȴ�����������������
	private Sensor myrotationSensor;
	private Sensor mygyrSensor;
	private float gyrd[]=new float[3]; // ���ڴ�����µ�����������
	private float accd[]=new float[3]; // ���ڴ�����µļ��ٶȴ���������
	private float rotation[]=new float[3];
	private long timeacc;
	private long timerotation;
	private long timegyr;
	private Vibrator vibrator; // ��
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
		writebu = (Button) findViewById(R.id.writecon);// ���ڿ������ݼ�¼����/��ͣ��ť����ʾ
		writebu.setOnTouchListener(this);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		// ���SensorManager����
		mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// ��ȡȱʡ�����Լ��ٶȴ�����
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
	protected void onResume() { // ��дonResume����
		super.onResume();
		// ���������Ǵ�����
		mySensorManager.registerListener(mySensorListener, // ��Ӽ���
				myaccelerometer, // ����������
				SensorManager.SENSOR_DELAY_FASTEST // �������¼����ݵ�Ƶ��
				);

		mySensorManager.registerListener(mySensorListener, // ��Ӽ���
				myrotationSensor, // ����������
				SensorManager.SENSOR_DELAY_FASTEST // �������¼����ݵ�Ƶ��
				);

		mySensorManager.registerListener(mySensorListener, // ��Ӽ���
				mygyrSensor, // ����������
				SensorManager.SENSOR_DELAY_FASTEST // �������¼����ݵ�Ƶ��
				);
	}
	@Override
	protected void onPause() {// ��дonPause����
		super.onPause();
		mySensorManager.unregisterListener(mySensorListener);// ȡ��ע�������
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	
	private SensorEventListener mySensorListener = new SensorEventListener() {// ����ʵ����SensorEventListener�ӿڵĴ�����������
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {

			long time = System.currentTimeMillis();
			time = time - time / 10000000 * 10000000;
			float[] values = event.values;// ��ȡ����������������
			// �����Ǵ������仯
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

				accd[0] = Math.abs(values[0]) > 0.2 ? values[0] : 0;
				accd[1] = Math.abs(values[1]) > 0.2 ? values[1] : 0;
				accd[2] = Math.abs(values[2]) > 0.2 ? values[2] : 0;
				timeacc = time;
			}

			else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

				rotation[0] = (float) values[0];
				rotation[1] = (float) values[1];
				rotation[2] = (float) values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				timerotation = time;
				if (wc == 1) {
					try {

						FileOutputStream foStream = new FileOutputStream(tmpString, true); // ���崫�������ݵ������
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
	public void onClick_delete(View view) throws IOException // ���������Ķ���
	{
		final EditText numEditText = (EditText) findViewById(R.id.num);
		final Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("�Ƿ��������");
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

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
					 new File(realString).delete(); // ��ȡ�ļ�����
					new File(tmpString).delete();
				}

			}

		});
		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new File(realString).delete(); // ��ȡ�ļ�����
				new File(tmpString).delete();
			}
		});
		builder.create().show();

		vibrator.vibrate(200);
	}
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (new File(realString).exists()) {
			Toast.makeText(this, "���Ƚ��Ѿ����ɵ����ݼ���", Toast.LENGTH_SHORT);
			return false;
		}
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_UP) {
			wc = 0;
			vibrator.vibrate(200);
			view.setBackgroundResource(R.drawable.button1);
 
			final Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("ȷ�����룿");
			builder.setPositiveButton("ȷ��",
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
			builder.setNegativeButton("ȡ��",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new File(tmpString).delete(); // ��ȡ�ļ�����
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
			} // ���崫�������ݵ������
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			wc = 1;
		}
		return false;
	}
	
	
	
	
	
}
