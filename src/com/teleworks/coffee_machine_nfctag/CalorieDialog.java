package com.teleworks.coffee_machine_nfctag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * This Activity appears as a dialog. It lists any paired devices and devices
 * detected in the area after discovery. When a device is chosen by the user,
 * the MAC address of the device is sent back to the parent Activity in the
 * result Intent.
 */
public class CalorieDialog extends Activity {
	// Return Intent extra
	RadioGroup radioLevel;
	RadioButton mRadioButton;
	Button btn;
	EditText edit_height;
	TextView text;
	int user_height = 170;// ����
	int user_moving_level = 1;// Ȱ����
	int user_moving = 1;// Ȱ����
	int user_caffeine = 0;// ī����
	int user_calorie = 0;// Į�θ�
	int user_sugar = 0;// ���
	private int count_latte = 0;// ī��� ����Ƚ��
	private int count_cafu = 0;// īǪġ�� ����Ƚ��
	private int count_espresso = 0;// ���������� ����Ƚ��
	private int count_americano = 0;// ��Ŀ�� ����Ƚ��
	int count_add_coffee = 0;// Ŀ�Ǿ� �߰��� Ƚ��
	int count_add_cream = 0;// ũ���� �߰��� Ƚ��
	int count_add_sugar = 0;// ������ �߰��� Ƚ��

	int calorie_aday = 0; // �����Ϸ缷�뷮 - Į�θ� (�������̿�)
	int caffeine_aday = 400; // �����Ϸ缷�뷮- ī���� 400mg
	int sugar_aday = 50; // �����Ϸ缷�뷮- ��� 50g

	SharedPreferences pref;
	SharedPreferences.Editor editPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.calorie_dialog);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);

		pref = getSharedPreferences("cafe_saveData", 0); // ���о��
		editPref = pref.edit(); // ������
		user_height = pref.getInt("user_height", 170);
		user_moving_level = pref.getInt("user_moving_level", 1);

		switch (user_moving_level) {
		case 1:
			user_moving = 25;
			break;
		case 2:
			user_moving = 30;
			break;
		case 3:
			user_moving = 35;
			break;
		case 4:
			user_moving = 40;
			break;
		}
		user_caffeine = CoffeeMain.user_caffeine;// ī����
		user_calorie = CoffeeMain.user_calorie;// Į�θ�
		user_sugar = CoffeeMain.user_sugar;// ���
		count_latte = CoffeeMain.count_latte;// ��ũĿ�� ����Ƚ��
		count_cafu = CoffeeMain.count_cafu;// ũ��Ŀ�� ����Ƚ��
		count_espresso = CoffeeMain.count_espresso;// ����Ŀ�� ����Ƚ��
		count_americano = CoffeeMain.count_americano;// ��Ŀ�� ����Ƚ��
		count_add_coffee = CoffeeMain.count_add_coffee;// Ŀ�Ǿ� �߰��� Ƚ��
		count_add_cream = CoffeeMain.count_add_cream;// ũ���� �߰��� Ƚ��
		count_add_sugar = CoffeeMain.count_add_sugar;// ������ �߰��� Ƚ��

		// ���� 1�� Į�θ� ���
		calorie_aday = (int) ((user_height - 100) * 0.9) * user_moving;

		/** ���� ���뷮 ��� */
		// ī���� mg
		user_caffeine = ((count_latte + count_cafu + count_espresso + count_americano) * 40)
				+ (count_add_coffee * 20);
		// Į�θ� Kcal
		user_calorie = ((count_latte + count_cafu) * 55)
				+ ((count_espresso + count_americano) * 15)
				+ ((count_add_coffee + count_add_cream + count_add_sugar) * 4);
		// ��� g
		user_sugar = ((count_latte + count_espresso) * 6)
				+ (count_add_sugar * 2);

		text = (TextView) findViewById(R.id.text_calorie_aday);
		text.setText("Recommended Intake : " + calorie_aday + " Kcal");
		text = (TextView) findViewById(R.id.text_calorie);
		text.setText("Today Intake : " + user_calorie + " Kcal");

		text = (TextView) findViewById(R.id.text_sugar);
		text.setText("Today Intake : " + user_sugar + " g");

		text = (TextView) findViewById(R.id.text_caffeine);
		text.setText("Today Intake : " + user_caffeine + " mg");

		// ����� ����
		btn = (Button) findViewById(R.id.btn_user_setting);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(CalorieDialog.this,
						SettingDialog.class);
				startActivity(intent);
			}
		});
		btn = (Button) findViewById(R.id.btnOk);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

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
