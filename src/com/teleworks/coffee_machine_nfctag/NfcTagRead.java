package com.teleworks.coffee_machine_nfctag;

import com.teleworks.nfc.NFCTrans;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NfcTagRead extends Activity {
	String Tag_name = CoffeeMain.TAG;

	byte lResult;
	final byte RET_SUCCESS = 0;
	final byte RET_OTHER_CARD = 1;
	final byte RET_ERR_CONNECT = 2;
	final byte RET_ERR_TRANS = 3;
	final byte RET_ERR_PARAM = 4;
	final byte RET_ERR_LOADKEY = 5;
	final byte RET_ERR_MASTERKEY = 6;
	public static int nTime = 5000;

	public static final int BUTTON_WITE_DRAW_HUM = 1;
	public static final int SET_MAX = 4;
	public static final int IMAGE_SUCCESS_PROGRESS = 9;
	public static final int FINISH_SUCCESS_PROGRESS = 5;
	public static final int FINISH_ERROR_PROGRESS = 6;
	public static final int ABLE_PROGRESS = 7;
	public static final int DISABLE_PROGRESS = 8;
	public static final int TEST_CODE = 10;
	public static final int DETECTED_TAG = 11;
	public static final int LOADING_PRAGRESS = 12;
	public static final int UPDATE_SAVE_DATA = 13;

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private Tag mTag;
	LinearLayout hygro_layout, loading_layout;

	final byte ENABLE = 0;
	final byte DISABLE = 1;
	final byte LOG_DISPLAY = DISABLE;
	int bThread = 0;

	MyProcessHandler MainHandler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_read);
		setResult(Activity.RESULT_CANCELED);

		Log.e(Tag_name, "onCreate");

		// NFC Setting.
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		MainHandler = new MyProcessHandler();

		Intent intent = getIntent();
		mTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		MainHandler.sendMessage(MainHandler.obtainMessage(UPDATE_SAVE_DATA));

		Button beam_exit = (Button) findViewById(R.id.btn_exit);
		beam_exit.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		TextView m_text = (TextView) findViewById(R.id.beam_text_vm_name);
		m_text.setText(String.format(getString(R.string.vm_name) + " %s",
				CoffeeMain.MACHINE_NAME));
		String coffee_name;
		switch (CoffeeMain.choose_coffee) {
		case CoffeeMain.MENU_LATTE_COFFEE:
			coffee_name = "caffe latte";
			break;
		case CoffeeMain.MENU_CAFU_COFFEE:
			coffee_name = "cappuccino";
			break;
		case CoffeeMain.MENU_ESPRESSO_COFFEE:
			coffee_name = "caffe espresso";
			break;
		case CoffeeMain.MENU_AMERICANO_COFFEE:
			coffee_name = "caffe Americano";
			break;
		default:
			coffee_name = "unknown type";
			break;
		}
		m_text = (TextView) findViewById(R.id.beam_text_vm_which_coffee);
		m_text.setText(String.format("Choose Coffee : %s", coffee_name));

		m_text = (TextView) findViewById(R.id.beam_text_vm_taste);
		m_text.setText(String.format(
				"Taste : Coffee [%d], Cream [%d], Sugar [%d]",
				CoffeeMain.taste_coffee, CoffeeMain.taste_cream,
				CoffeeMain.taste_sugar));

		m_text = (TextView) findViewById(R.id.beam_text_vm_price_coffee);
		m_text.setText(String.format("Price : %d won / After Pay : %d won",
				CoffeeMain.coffee_cost, CoffeeMain.HAVE_MONEY
						- CoffeeMain.coffee_cost));
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);

	}

	@Override
	public void onNewIntent(Intent intent) {
		if (bThread == 0) {
			mTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			new DoRfProcessingJob().execute();
		}
	}

	// Hander
	@SuppressLint("HandlerLeak")
	class MyProcessHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ABLE_PROGRESS:
				break;

			case DISABLE_PROGRESS:
				// Toast.makeText(getBaseContext(), "Unfound Sensor Tag",
				// Toast.LENGTH_SHORT).show();
				break;

			case FINISH_SUCCESS_PROGRESS:
				break;
			case FINISH_ERROR_PROGRESS:
				// Toast.makeText(getBaseContext(), "Process Stop",
				// Toast.LENGTH_SHORT).show();
				break;
			case TEST_CODE:
				break;
			case DETECTED_TAG:
				// ImageCondition.setText("Detected Sensor Tag");
				// Toast.makeText(getBaseContext(),
				// "Detected.. Please wait, about 3 sec..",
				// Toast.LENGTH_SHORT).show();
				break;
			case LOADING_PRAGRESS:
				break;
			case UPDATE_SAVE_DATA:
				break;
			}

		}
	}

	// Sub Process
	private class DoRfProcessingJob extends AsyncTask<String, Integer, Long> {

		@Override
		protected void onPreExecute() {
			bThread = 1;
			super.onPreExecute();
		}

		@Override
		protected Long doInBackground(String... strData) {

			byte TagInfo = NFCTrans.TagInfo(mTag);
			if (strData.length != 1) {
				lResult = RET_ERR_PARAM;
			}

			Log.e(Tag_name, "doInBackground");
			if ((byte) ((byte) 0x08 & (byte) TagInfo) == (byte) 0x08) {
				Log.e(Tag_name, "ISO15693_Process before");
				MainHandler
						.sendMessage(MainHandler.obtainMessage(DETECTED_TAG));
				lResult = NFCTrans.ISO15693_Process(mTag, null);
				if (lResult == RET_SUCCESS)
					lResult = SL13_Process();

			} else {
				lResult = RET_OTHER_CARD;
			}
			if (lResult == RET_SUCCESS)
				MainHandler.sendMessage(MainHandler
						.obtainMessage(FINISH_SUCCESS_PROGRESS));
			else
				MainHandler.sendMessage(MainHandler
						.obtainMessage(FINISH_ERROR_PROGRESS));
			bThread = 0;
			return (long) lResult;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPostExecute(Long result) {

		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		byte SL13_Process() {
			byte[] GET_SYSTEMINFO = { 0x02, 0x2B };
			byte[] UID = new byte[8];
			byte[] info = new byte[100];
			Log.v(Tag_name, "Process in");
			try {
				if (0 == NFCTrans.ISO15693_TransData(LOG_DISPLAY,
						GET_SYSTEMINFO, info))
					return RET_OTHER_CARD;
				System.arraycopy(info, 2, UID, 0, 8);
				Log.v(Tag_name, "UID =[ " + UtilHex.hexToString(UID) + " ]");
				Intent intent = new Intent();
				intent.putExtra("TAG_UID", UtilHex.hexToString(UID));
				setResult(Activity.RESULT_OK, intent);
				finish();
			} catch (Exception e) {
				// DisplayLog("Connect Exception",0);
				e.printStackTrace();
			} finally {
				NFCTrans.ISO15693_DisConnect();
			}
			return RET_SUCCESS;
		}
	}

	/*
	 * android beam on/off ¼³Á¤Ã¢
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// If NFC is not available, we won't be needing this menu
		if (mAdapter == null) {
			return super.onCreateOptionsMenu(menu);
		}
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent intent = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}
}