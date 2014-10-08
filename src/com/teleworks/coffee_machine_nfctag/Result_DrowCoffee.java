package com.teleworks.coffee_machine_nfctag;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Result_DrowCoffee extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_drow_coffee);

		Button beam_exit = (Button) findViewById(R.id.btn_exit);
		beam_exit.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
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
