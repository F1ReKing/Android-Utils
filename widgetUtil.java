package com.yuexunit.fingerfinance.util;

import android.text.Selection;
import android.text.Spannable;
import android.widget.EditText;

/**
 * Created by HuangYH on 2015/8/31.
 */
public class widgetUtil {

	public static void setEditTextCursorLocation(EditText editText) {
		CharSequence text = editText.getText();
		if (text instanceof Spannable) {
			Spannable spanText = (Spannable) text;
			Selection.setSelection(spanText, text.length());
		}
	}
}
