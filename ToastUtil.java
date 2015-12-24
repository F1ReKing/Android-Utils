package com.yuexunit.fingerfinance.util;

import android.view.Gravity;
import android.widget.Toast;

import com.yuexunit.fingerfinance.app.AppApplication;

public class ToastUtil {
	private static Toast toast = null;
	
	/**
	 * 连续toast
	 * 
	 * @param msg
	 * @param duration
	 *            时长，0为SHORT,1为LONG
	 */
	public static void showToast(String msg, int duration) {
		if (toast == null) {
			toast = Toast.makeText(AppApplication.context, msg, duration);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}

	public static void showToastAtCenter(String msg){
		if (toast == null) {
			toast = Toast.makeText(AppApplication.context, msg, Toast.LENGTH_LONG);
		} else {
			toast.setText(msg);
		}
		toast.setGravity(Gravity.CENTER,0,0);
		toast.show();
	}

	/**
	 * 关闭toast
	 */
	public static void cancelToast() {
		if (toast != null) {
			toast.cancel();
		}
	}
}
