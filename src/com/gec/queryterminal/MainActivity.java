package com.gec.queryterminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.gec.queryterminal.library.BuzzerDev;
import com.gec.queryterminal.library.LedDev;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author GEC-LAB
 * @version 1.0
 */
public class MainActivity extends Activity {

	// 调试用
	private static final String TAG = "MainActivity";
	private static final boolean D = false;
	// 端口
	private static final int PORT = 8081;
	// 成功指令
	private static final int CMD_SUCCEED = 0;
	// 失败指令
	private static final int CMD_FAIL = 1;
	// 异常指令
	private static final int CMD_EXCEPTION = 2;
	// 服务器IP输入框
	private EditText mServerIp = null;
	// 订单号输入框
	private EditText mOrderNum = null;
	// 等待对话框
	private Dialog mWaitDialog = null;
	// 客户端连接服务器端子线程
	private ClientRunnable mClientRunnable = null;
	// 上下文
	private Context mContext = null;
	// 处理者
	private Handler mHandler = null;
	// LED设备
	private LedDev mLedDev = null;
	// 蜂鸣器设备
	private BuzzerDev mBuzzerDev = null;
	// 延时子线程
	private DelayRunnable mDelayRunnable = null;
	// LED位置
	private int ledNo = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initAnimation();
		init();
		initButton();
	}

	@Override
	protected void onDestroy() {
		if (mLedDev != null)
			mLedDev.closeLedDev();
		if (mBuzzerDev != null) {
			mBuzzerDev.opBuzzerDev(0, 100);
			mBuzzerDev.closeBuzzerDev();
		}
		super.onDestroy();
	}

	/**
	 * 初始化动画
	 */
	private void initAnimation() {
		AlphaAnimation aa = new AlphaAnimation(0, 1.0f);
		aa.setDuration(2000);
		this.findViewById(R.id.tv_title).setAnimation(aa);

		TranslateAnimation ta = new TranslateAnimation(-300.0f, 0, 0, 0);
		ta.setDuration(1500);
		this.findViewById(R.id.ll_server_ip).setAnimation(ta);

		ta = new TranslateAnimation(300.0f, 0, 0, 0);
		ta.setDuration(1500);
		this.findViewById(R.id.ll_order_number).setAnimation(ta);

		ta = new TranslateAnimation(-300.0f, 0, 300.0f, 0);
		ta.setDuration(2000);
		this.findViewById(R.id.ll_clear).setAnimation(ta);

		ta = new TranslateAnimation(300.0f, 0, 300.0f, 0);
		ta.setDuration(2000);
		this.findViewById(R.id.ll_confirm).setAnimation(ta);
	}

	private void init() {
		mServerIp = (EditText) this.findViewById(R.id.et_server_ip);
		mOrderNum = (EditText) this.findViewById(R.id.et_order_number);
		mClientRunnable = new ClientRunnable();
		mContext = this;
		mHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case CMD_SUCCEED:
					succeedDialog();
					break;
				case CMD_FAIL:
					failDialog();
					break;
				case CMD_EXCEPTION:
					exceptionDialog();
					break;
				default:
					break;
				}
				return false;
			}
		});
		// 判断网络是否可用
		if (!isNetworkConnected()) {
			Toast.makeText(getApplicationContext(), R.string.toast_network_connected, Toast.LENGTH_SHORT).show();
		}
		openDev();
	}

	/**
	 * 初始化按钮
	 */
	private void initButton() {
		this.findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(mOrderNum.getText().toString()))
					return;
				mOrderNum.setText("");
			}
		});

		this.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(mServerIp.getText().toString().trim())) {
					Toast.makeText(getApplicationContext(), R.string.toast_ip_null, Toast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(mOrderNum.getText().toString().trim())) {
					Toast.makeText(getApplicationContext(), R.string.toast_order_number_null, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (!isNetworkConnected()) {
					Toast.makeText(getApplicationContext(), R.string.toast_network_connected, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				waitDialog();
			}
		});
	}

	/**
	 * 客户端连接服务器端子线程
	 */
	private class ClientRunnable implements Runnable {

		@Override
		public void run() {
			Socket client = null;
			try {
				client = new Socket(mServerIp.getText().toString(), PORT);
				if (D)
					Log.d(TAG, "客户端IP：" + client.getLocalAddress() + "\t端口：" + client.getPort());

				OutputStream os = client.getOutputStream();
				InputStream is = client.getInputStream();

				byte[] bt = new byte[1024];
				os.write(mOrderNum.getText().toString().getBytes());
				os.flush();
				int len = is.read(bt);
				String response = new String(bt, 0, len);
				if (D)
					Log.d(TAG, "服务器端IP：" + client.getInetAddress().getHostAddress() + "\t端口：" + client.getPort() + ">>"
							+ response);

				if ("succeed".equals(response)) {
					mHandler.sendEmptyMessage(CMD_SUCCEED);
				} else if ("fail".equals(response)) {
					mHandler.sendEmptyMessage(CMD_FAIL);
				}
			} catch (IOException e) {
				if (client != null)
					try {
						client.close();
					} catch (IOException e1) {
						mHandler.sendEmptyMessage(CMD_EXCEPTION);
					}
				mHandler.sendEmptyMessage(CMD_EXCEPTION);
			}
		}
	}

	/**
	 * 打开led和蜂鸣器设备
	 */
	private void openDev() {
		mLedDev = LedDev.getInstance();
		mLedDev.openLedDev();

		mBuzzerDev = BuzzerDev.getInstance();
		mBuzzerDev.openBuzzerDev();

		mDelayRunnable = new DelayRunnable();
	}

	private void waitDialog() {
		mWaitDialog = new AlertDialog.Builder(mContext).setTitle(R.string.prompt).setMessage(R.string.message_wait)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null) {
							dialog.dismiss();
						}
					}
				}).setCancelable(false).create();
		mWaitDialog.show();
		new Thread(mClientRunnable).start();
	}

	/**
	 * 返回成功指令显示成功对话框
	 */
	private void succeedDialog() {
		if (mWaitDialog != null)
			mWaitDialog.dismiss();

		mHandler.postDelayed(mDelayRunnable, 500);

		mBuzzerDev.opBuzzerDev(1, 100);

		new AlertDialog.Builder(mContext).setTitle(R.string.prompt).setMessage(R.string.message_succeed)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null)
							dialog.dismiss();
						for (int i = 1; i < 5; i++)
							mLedDev.opLedDev(i, "off");
						if (mDelayRunnable != null) {
							mHandler.removeCallbacks(mDelayRunnable);
						}
						mBuzzerDev.opBuzzerDev(0, 100);
					}
				}).setCancelable(false).create().show();
	}

	/**
	 * 返回失败指令显示失败对话框
	 */
	private void failDialog() {
		if (mWaitDialog != null)
			mWaitDialog.dismiss();

		new AlertDialog.Builder(mContext).setTitle(R.string.prompt).setMessage(R.string.message_fail)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null)
							dialog.dismiss();
					}
				}).setCancelable(false).create().show();
	}

	/**
	 * 返回异常指令显示异常对话框
	 */
	private void exceptionDialog() {
		if (mWaitDialog != null)
			mWaitDialog.dismiss();

		new AlertDialog.Builder(mContext).setTitle(R.string.prompt).setMessage(R.string.message_exception)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null)
							dialog.dismiss();
					}
				}).setCancelable(false).create().show();
	}

	/**
	 * 
	 * LED延时子线程
	 */
	private class DelayRunnable implements Runnable {

		@Override
		public void run() {
			if (ledNo == 5) {
				for (int i = 1; i < 5; i++)
					mLedDev.opLedDev(i, "off");
				ledNo = 0;
			}
			mLedDev.opLedDev(ledNo++, "on");
			mHandler.postDelayed(mDelayRunnable, 500);
		}
	}

	/**
	 * 检测网络是否可用
	 */
	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}
}
