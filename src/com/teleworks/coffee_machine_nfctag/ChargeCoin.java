package com.teleworks.coffee_machine_nfctag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ChargeCoin extends Activity {
	private EditText m_EditText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.charge_coin);
		setResult(Activity.RESULT_CANCELED);

		m_EditText = (EditText) findViewById(R.id.edit_password);
		Button button = (Button) findViewById(R.id.btn_ok);
		button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("PassWord", m_EditText.getText().toString());
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
