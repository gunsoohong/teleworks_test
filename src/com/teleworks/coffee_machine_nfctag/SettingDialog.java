package com.teleworks.coffee_machine_nfctag;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * This Activity appears as a dialog. It lists any paired devices and devices
 * detected in the area after discovery. When a device is chosen by the user,
 * the MAC address of the device is sent back to the parent Activity in the
 * result Intent.
 */
public class SettingDialog extends Activity implements
		RadioGroup.OnCheckedChangeListener {
	// Return Intent extra
	RadioGroup radioLevel;
	RadioButton mRadioButton;
	Button btnOk, btnCancle;
	EditText edit_height;
	int tempHeight = 0;
	int tempMovinglevel = 0;

	SharedPreferences pref;
	SharedPreferences.Editor editPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.setting_dialog);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);

		pref = getSharedPreferences("cafe_saveData", 0); // 값읽어옴
		editPref = pref.edit(); // 값저장

		tempHeight = pref.getInt("user_height", 170);
		tempMovinglevel = pref.getInt("user_moving_level", 1);

		edit_height = (EditText) findViewById(R.id.edit_height);
		edit_height.setText("" + tempHeight);

		radioLevel = (RadioGroup) findViewById(R.id.radioLevel);
		radioLevel.setOnCheckedChangeListener(this);
		switch (tempMovinglevel) {
		case 1:
			mRadioButton = (RadioButton) findViewById(R.id.radio_1);
			mRadioButton.setChecked(true);
			break;
		case 2:
			mRadioButton = (RadioButton) findViewById(R.id.radio_2);
			mRadioButton.setChecked(true);
			break;
		case 3:
			mRadioButton = (RadioButton) findViewById(R.id.radio_3);
			mRadioButton.setChecked(true);
			break;
		case 4:
			mRadioButton = (RadioButton) findViewById(R.id.radio_4);
			mRadioButton.setChecked(true);
			break;
		}

		btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				tempHeight = atoi(edit_height.getText().toString());
				// CoffeeMain.user_moving_level = tempMovinglevel;

				editPref.putInt("user_height", tempHeight);
				editPref.putInt("user_moving_level", tempMovinglevel);
				editPref.commit();

				Toast.makeText(SettingDialog.this, "저장되었습니다.",
						Toast.LENGTH_SHORT).show();
				finish();
			}
		});
		btnCancle = (Button) findViewById(R.id.btnCancle);
		btnCancle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub

		switch (checkedId) {
		case R.id.radio_1:
			tempMovinglevel = 1;
			break;

		case R.id.radio_2:
			tempMovinglevel = 2;
			break;

		case R.id.radio_3:
			tempMovinglevel = 3;
			break;

		case R.id.radio_4:
			tempMovinglevel = 4;
			break;

		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public static int atoi(String number) {
		if (number == null || number.trim().length() == 0) {
			throw new IllegalArgumentException("Number cannot be null/empty.");
		}
		int result = 0;
		number = number.trim();
		boolean negate = false;
		char sign = number.charAt(0);
		if (sign == '+' || sign == '-') {
			if (sign == '-') {
				negate = true;
			}
			number = number.substring(1);
		}
		int length = number.length();
		for (int index = 0; index < length; index++) {
			char digit = number.charAt(index);
			if (!(digit >= '0' && digit <= '9')) {
				throw new IllegalArgumentException(
						"Number contains characters other than digits at index "
								+ index);
			}
			digit = (char) (digit - '0');
			result += (digit * Math.pow(10, (length - index - 1)));
		}
		if (negate) {
			result = 0 - result;
		}
		return result;
	}
}
