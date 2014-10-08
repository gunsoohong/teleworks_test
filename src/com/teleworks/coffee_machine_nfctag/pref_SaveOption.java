package com.teleworks.coffee_machine_nfctag;

import android.app.Activity;
import android.content.SharedPreferences;

public class pref_SaveOption extends Activity {
	SharedPreferences pref;
	SharedPreferences.Editor editPref;

	void pref_SaveOption() {
		pref = getSharedPreferences("cafe_login", 0); // 값읽어옴
		editPref = pref.edit(); // 값저장
	}

	void load_PrefData() {

		// strID = pref.getString("id", "");
		// strPW = pref.getString("pw", "");
	}
}
