package com.teleworks.coffee_machine_nfctag;

import android.app.Activity;
import android.content.SharedPreferences;

public class pref_SaveOption extends Activity {
	SharedPreferences pref;
	SharedPreferences.Editor editPref;

	void pref_SaveOption() {
		pref = getSharedPreferences("cafe_login", 0); // ���о��
		editPref = pref.edit(); // ������
	}

	void load_PrefData() {

		// strID = pref.getString("id", "");
		// strPW = pref.getString("pw", "");
	}
}
