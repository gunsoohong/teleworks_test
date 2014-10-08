package com.teleworks.coffee_machine_nfctag;

import java.io.IOException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class Beam extends Activity {

	private boolean mWriteMode = false;
	NfcAdapter mNfcAdapter;
	EditText mNote;

	PendingIntent mNfcPendingIntent;
	IntentFilter[] mWriteTagFilters;
	IntentFilter[] mNdefExchangeFilters;

	TextView t_tagid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_read);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		// Handle all of our received NFC intents in this activity.
		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Intent filters for reading a note from a tag or exchanging over p2p.
		IntentFilter ndefDetected = new IntentFilter(
				NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefDetected.addDataType("text/plain");
		} catch (MalformedMimeTypeException e) {
		}
		mNdefExchangeFilters = new IntentFilter[] { ndefDetected };

		// Intent filters for writing to a tag
		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		mWriteTagFilters = new IntentFilter[] { tagDetected };

		Button beam_exit = (Button) findViewById(R.id.btn_exit);
		beam_exit.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		TextView m_text = (TextView) findViewById(R.id.beam_text_vm_name);
		m_text.setText(String.format(getString(R.string.vm_name) + " : %s",
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
	protected void onResume() {
		super.onResume();
		// mResumed = true;
		// Sticky notes received from Android
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			NdefMessage[] messages = getNdefMessages(getIntent());
			byte[] payload = messages[0].getRecords()[0].getPayload();
			setNoteBody(new String(payload));
			setIntent(new Intent()); // Consume this intent.
		}
		enableNdefExchangeMode();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mNfcAdapter.disableForegroundNdefPush(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Tag writing mode
		if (mWriteMode
				&& NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			writeTag(getNoteAsNdef(), detectedTag);
		}
	}

	private void setNoteBody(String body) {
		Editable text = mNote.getText();
		text.clear();
		text.append(body);
	}

	private NdefMessage getNoteAsNdef() {

		Intent tasteIntent = getIntent();
		byte[] coffee_taste = tasteIntent.getExtras().getByteArray(
				"txt_taste_coffee");

		NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				"".getBytes(), new byte[] {}, coffee_taste);
		return new NdefMessage(new NdefRecord[] { textRecord });
	}

	NdefMessage[] getNdefMessages(Intent intent) {
		// Parse the intent
		NdefMessage[] msgs = null;
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			} else {
				// Unknown tag type
				byte[] empty = new byte[] {};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
						empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}
		} else {
			Log.d(CoffeeMain.TAG, "Unknown intent.");
			finish();
		}
		return msgs;
	}

	private void enableNdefExchangeMode() {
		mNfcAdapter.enableForegroundNdefPush(Beam.this, getNoteAsNdef());
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
				mNdefExchangeFilters, null);
	}

	boolean writeTag(NdefMessage message, Tag tag) {
		int size = message.toByteArray().length;

		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();

				if (!ndef.isWritable()) {
					toast("Tag is read-only.");
					return false;
				}
				if (ndef.getMaxSize() < size) {
					toast("Tag capacity is " + ndef.getMaxSize()
							+ " bytes, message is " + size + " bytes.");
					return false;
				}

				ndef.writeNdefMessage(message);
				toast("Wrote message to pre-formatted tag.");
				return true;
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						toast("Formatted tag and wrote message");
						return true;
					} catch (IOException e) {
						toast("Failed to format tag.");
						return false;
					}
				} else {
					toast("Tag doesn't support NDEF.");
					return false;
				}
			}
		} catch (Exception e) {
			toast("Failed to write tag");
		}

		return false;
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	/*
	 * android beam on/off ¼³Á¤Ã¢
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// If NFC is not available, we won't be needing this menu
		if (mNfcAdapter == null) {
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
