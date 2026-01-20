package com.ufony.trackersdk;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rey.material.widget.RadioButton;
public class FontUtils {

	public static String MYRIADPRO_REGULAR = "fonts/MyriadPro-Regular.otf";
	public static String MYRIADPRO_SEMIBOLD = "fonts/MyriadPro-Semibold.otf";

	public static String GIBSON_SEMIBOLD = "fonts/Gibson-SemiBold.otf";
	
	public static String ANDROID_EMOJI = "fonts/AndroidEmoji.ttf";

	public static float FONT_SIZE_SMALL = 12;
	public static float FONT_SIZE = 14;
	public static float FONT_SIZE_LARGE = 16;
	public static float FONT_SIZE_EXTRA_LARGE = 18;
	public static float FONT_SIZE_X_EXTRA_LARGE = 28;

	/**
	 * 
	 * @param context
	 * @param textView
	 * @param name
	 * @param size
	 */
	public static void setFont(Context context, TextView textView, String name,  float size) {
		Typeface tf = Typeface.createFromAsset(((ContextWrapper) context).getBaseContext().getAssets(), name);

		if (size != 0)
			textView.setTextSize(size);

		textView.setTypeface(tf);
	}
	
	/**
	 * 
	 * @param context
	 * @param button
	 * @param name
	 * @param size
	 */
	public static void setFont(Context context, Button button, String name,
			float size) {
		Typeface tf = Typeface.createFromAsset(((ContextWrapper) context)
				.getBaseContext().getAssets(), name);

		if (size != 0)
			button.setTextSize(size);

		button.setTransformationMethod(null);
		button.setTypeface(tf);
	}

	public static void setFont(Context context, RadioButton button, String name,
							   float size) {
		Typeface tf = Typeface.createFromAsset(((ContextWrapper) context)
				.getBaseContext().getAssets(), name);

		if (size != 0)
			button.setTextSize(size);

		button.setTransformationMethod(null);
		button.setTypeface(tf);
	}
	
	public static void setFont(Context context, EditText editText, String name,
			float size) {
		Typeface tf = Typeface.createFromAsset(((ContextWrapper) context)
				.getBaseContext().getAssets(), name);

		if (size != 0)
			editText.setTextSize(size);

		editText.setTypeface(tf);
	}
	
}