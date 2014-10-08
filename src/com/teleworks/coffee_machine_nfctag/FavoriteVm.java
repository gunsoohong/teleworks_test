package com.teleworks.coffee_machine_nfctag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FavoriteVm extends Activity {

	private ArrayAdapter<String> mFavoriteVmArrayAdapter;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.favorite_vm_list);
		setResult(Activity.RESULT_CANCELED);
		ListView newDevicesListView = (ListView) findViewById(R.id.list_favorite_vm);
		mFavoriteVmArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		newDevicesListView.setAdapter(mFavoriteVmArrayAdapter);
		newDevicesListView.setOnItemClickListener(mFavoriteVmClickListener);
		Thread thread_Add_Machine_List = new Thread(Add_Machine_List);
		thread_Add_Machine_List.start();

		Button button_cancel = (Button) findViewById(R.id.btn_cancel);
		button_cancel.setOnClickListener(new OnClickListener() {
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

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mFavoriteVmClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			String info = ((TextView) v).getText().toString();
			String DevName = info
					.substring(0, CoffeeMain.VM_BT_NAME_1.length());
			Intent intent = new Intent();
			intent.putExtra("Dev", DevName);
			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	Runnable Add_Machine_List = new Runnable() {
		String favorite_vm_list_Array = "";
		String delimiter = ",";
		String[] favorite_vm_list;
		int favorite_vm_list_lenth = 0;

		public void run() {
			favorite_vm_list_Array = getIntent().getStringExtra("favorite_vm");
			favorite_vm_list = favorite_vm_list_Array.split(delimiter);
			favorite_vm_list_lenth = favorite_vm_list.length;
			Log.i(CoffeeMain.TAG, "favorite_vm_list_lenth: "
					+ favorite_vm_list_lenth);
			for (int i = 0; i < favorite_vm_list_lenth; i++) {
				if (CoffeeMain.VM_BT_NAME_1.length() == favorite_vm_list[i]
						.length()) {
					mFavoriteVmArrayAdapter.add(favorite_vm_list[i]
							+ " / 코엑스 전시장    [연결하기]");
				}
			}
		}
	};
}
