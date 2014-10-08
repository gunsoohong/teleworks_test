package com.teleworks.coffee_machine_nfctag;

import java.util.Arrays;
import java.util.Timer;
import org.apache.http.util.EncodingUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class CoffeeMain extends Activity {
	private static final boolean D = true;

	// 버젼
	public static final int VERSION_ETRI = 1;
	public static final int VERSION_TELEWORKS = 2;
	public static int versionName = VERSION_ETRI;
	// public static int versionName = VERSION_TELEWORKS;

	public static final String VM_BT_NAME_1 = "Comus A"; // 55
	public static final String VM_BT_NAME_2 = "Comus B"; // 56
	public static final String VM_BT_ADRESS_1 = "00:18:9A:04:36:9D"; // 55
	public static final String VM_BT_ADRESS_2 = "00:18:9A:04:36:95"; // 56
	private final String VM_BT_TagID_A = "606A8269725802E0"; // 55
	private final String VM_BT_TagID_B = "62318269725802E0"; // 56

	private static int BT_CONNECT_MODE = 0;
	private static final int BT_CONNECT_REQ_STATE = 1;
	private static final int BT_CONNECT_REQ_DROW_COFFEE = 2;

	private static final int NFC_TAG_SCAN = 1;
	private static final int NFC_BEAM = 2;
	private static int NFC_MODE = NFC_TAG_SCAN;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_CONNECT = 6;
	public static final int MESSAGE_CONNECT_FAIL = 7;
	public static final int MESSAGE_DISCONNECT = 8;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static String MACHINE_NAME = "";
	public static String DEVICE_ADD = "";
	public static String FAVORITE_VM_LIST = "";
	public static final String TOAST = "toast";
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_MACHINE_INFO = 3;
	private static final int REQUEST_NFC_TAG_READ = 4;
	private static final int REQUEST_RESULT_DROW_COFFEE = 5;
	private static final int REQUEST_CHARGE_COIN = 6;

	// main handler
	public static final int GOTO_INTRO = 0x00;
	public static final int GOTO_MAIN = 0x01;
	public static final int GOTO_LIST = 0x02;
	public static final int ADD_LIST = 0x03;
	public static final int END_OF_CMD = 0x04;
	public static final int CHANGE_SCAN_BTN_TEXT = 0x05;
	public static final int TASTE_COFFEE_LIST = 0x06;
	public static final int GOTO_DROW_COFFEE = 0x07;
	public static final int SET_STATUS = 0x08;
	public static final int PROGRESS_COFFEE_DRIP = 0x09;

	public static final int MENU_CAFU_COFFEE = 0x11;
	public static final int MENU_LATTE_COFFEE = 0x12;
	public static final int MENU_ESPRESSO_COFFEE = 0x13;
	public static final int MENU_AMERICANO_COFFEE = 0x14;

	public static boolean SEND_SUCCESS_FLAG = false;
	public static int WAIT_ACK = 0;
	public static final int RE_STATUS = 1;
	public static final int RE_DROP_TASTE_COFFEE = 2;
	public static final int RE_DROP_COFFEE = 3;
	public static final int RE_SET_CLEAN_TIME = 4;

	public static boolean waterFlag = false;
	public static boolean cupFlag = false;
	public static int dustLevel = 0;
	public static int cleanTime = 0;
	public static int cleanDay = 0;
	public static byte taste_coffee = 3;
	public static byte taste_cream = 3;
	public static byte taste_sugar = 3;
	public static boolean taste_select = false;
	public static byte choose_coffee = 0;
	public static int HAVE_MONEY = 0;
	public static int coffee_cost = 0;
	private static byte[] send_coffee_data_Array = { (byte) 0xBB, 0x01, 0x11,
			0x03, 0x00, 0x00, 0x00, 0x7E };
	// Name of the connected device
	// private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	// private ListView mConversationView;
	public static byte bufferMSG[] = new byte[256];
	public static boolean mBtConnecFlag = false;
	// private String BluetoothDevMacAdd = null;
	// private Timer mTimer_1 = null;
	// private boolean Timer1flag = false;
	private ProgressDialog loagindDialog; // Loading Dialog

	boolean ProgressTimeout = false;
	boolean BtPairDialogDialog = false;
	// boolean fProgressDialog = true;

	public static final String TAG = "COFFEE_MACHINE"; // LOGCAT 로그 구분 TAG
	ImageView iv;
	boolean bDeviceSetting = false;
	private static SoundPool soundPool;
	private static int sound;
	LinearLayout layer_menu;
	Button btn;
	Drawable draw;
	TextView text;
	LinearLayout layout;

	// 개인정보관련 - 칼로리, 당분 계산
	public static int user_height = 0;// 신장
	public static int user_moving_level = 0;// 활동량
	public static int user_caffeine = 0;// 카페인
	public static int user_calorie = 0;// 칼로리
	public static int user_sugar = 0;// 당분
	public static int count_latte = 0;// 카페라떼 뽑은횟수
	public static int count_cafu = 0;// 카푸치노 뽑은횟수
	public static int count_espresso = 0;// 에스프레소 뽑은횟수
	public static int count_americano = 0;// 블랙커피 뽑은횟수
	public static int count_add_coffee = 0;// 커피양 추가한 횟수
	public static int count_add_cream = 0;// 크림양 추가한 횟수
	public static int count_add_sugar = 0;// 설탕양 추가한 횟수

	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	SharedPreferences pref;
	SharedPreferences.Editor editPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// -------------------------------------[gunsoo]
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, getString(R.string.no_device),
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		if (!mBluetoothAdapter.isEnabled()) {
			createThreadBtOnDialog();
		} else if (mChatService == null) {
			setupChat();
			mdelay(200);
		}
		// -------------------------------------------------------[*]

		pref = getSharedPreferences("cafe_saveData", 0); // 값읽어옴
		editPref = pref.edit(); // 값저장
		FAVORITE_VM_LIST = pref.getString("favorite_vm", "");
		HAVE_MONEY = pref.getInt("have_money", 1000);
		MACHINE_NAME = pref.getString("witch_machine", "");
		choose_coffee = (byte) pref.getInt("witch_coffee", MENU_CAFU_COFFEE);
		taste_coffee = (byte) pref.getInt("lv_coffee", 3);
		taste_cream = (byte) pref.getInt("lv_cream", 3);
		taste_sugar = (byte) pref.getInt("lv_sugar", 3);
		count_latte = pref.getInt("count_latte", 0);// 카페라떼 뽑은횟수
		count_cafu = pref.getInt("count_cafu", 0);// 카푸치노 뽑은횟수
		count_espresso = pref.getInt("count_espresso", 0);// 에스프레소 뽑은횟수
		count_americano = pref.getInt("count_americano", 0);// 블랙커피 뽑은횟수
		count_add_coffee = pref.getInt("count_add_coffee", 0);// 커피양 추가한 횟수
		count_add_cream = pref.getInt("count_add_cream", 0);// 크림양 추가한 횟수
		count_add_sugar = pref.getInt("count_add_sugar", 0);// 설탕양 추가한 횟수
		// user_height = pref.getInt("user_height", 170);
		// user_moving_level = pref.getInt("user_moving_level", 1);
		// EditIntent.putExtra("user_moving_level",
		mMainHandler.obtainMessage(GOTO_INTRO, -1, -1, null).sendToTarget();

	}

	public static void playSound() {
		soundPool.play(sound, 1, 1, 0, 0, 1);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		text = (TextView) findViewById(R.id.text_money);
		if (text != null)
			text.setText(getString(R.string.have_coin) + " " + HAVE_MONEY
					+ " won");

		// -------------------------------------[gunsoo]
		if (mChatService != null) {
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
		// --------------------------------------------------------[*]
	}

	// 초기 로딩화면 - 블루투스 안켜져있으면 켜지는 시간동안 ProgressDialog 표시
	void createThreadBtOnDialog() {
		/* ProgressDialog */
		loagindDialog = ProgressDialog.show(this, "Turning ON...",
				getString(R.string.turning_on), true, true);

		Thread thread = new Thread(new Runnable() {
			public void run() {
				// 시간걸리는 처리
				mBluetoothAdapter.enable();
				for (; !mBluetoothAdapter.isEnabled();) {
					mdelay(100);
				}
				mdelay(200);
				if (mChatService == null)
					setupChat();
				handlerDlog.sendEmptyMessage(0);
				mdelay(200);
			}
		});
		thread.start();
	}

	// ProgressDialog 표시
	void createThreadBtPairDialog() {
		/* ProgressDialog */
		loagindDialog = ProgressDialog.show(this,
				getString(R.string.title_connecting),
				getString(R.string.connecting_to), true, true);

		Thread ThreadBtPairDialog = new Thread(new Runnable() {
			public void run() {
				// 시간걸리는 처리
				for (; !BtPairDialogDialog;) {
				}
				handlerDlog.sendEmptyMessage(0);
			}
		});
		ThreadBtPairDialog.start();
	}

	// ProgressDialog 헨들러
	private Handler handlerDlog = new Handler() {
		public void handleMessage(Message msg) {
			loagindDialog.dismiss(); // 다이얼로그 삭제
			// View갱신
		}
	};

	// protected String sendArray;

	private void mdelay(long times) {
		try {
			Thread.sleep(times);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	// BT chat 서비스 시작
	private void setupChat() {
		if (D)
			Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	// BT로 메시지 보내는 함수
	private void m_sendMessage(byte[] send) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			return;
		}
		// Check that there's actually something to send
		if (send.length > 0) {
			mChatService.write(send);
			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			if (D)
				Log.i(TAG, "sendMessage == [ " + hexToString(send) + "]");
		}
	}

	private void BuffClean() {
		for (int i = 0; i < bufferMSG.length; i++) {
			bufferMSG[i] = 0;
		}
	}

	// Bt에서 들어오는 내용 처리
	private void rf_RSP_CMD(byte[] response) {
		byte[] response_HEAD = new byte[4];
		byte[] RSP_STAUS = { (byte) 0xBB, 0x02, 0x20, 0x05 };
		byte[] RSP_TASTE = { (byte) 0xBB, 0x02, 0x21, 0x00 };
		byte[] RSP_END_CMD = { (byte) 0xBB, 0x02, 0x21, 0x00 };

		// BT에서 들오온 데이터의 Header부분 4bit를 복사하여 비교.
		System.arraycopy(response, 0, response_HEAD, 0, 4);

		if (Arrays.equals(response_HEAD, RSP_STAUS)) {
			if (response[9] == (byte) 0x7E) {
				if (D)
					Log.e(TAG, "[RSP_STAUS]");
				if (WAIT_ACK == RE_STATUS)
					SEND_SUCCESS_FLAG = true;
				if (0x01 == response[4])
					cupFlag = true;
				else
					cupFlag = false;
				if (0x01 == response[5])
					waterFlag = true;
				else
					waterFlag = false;
				cleanTime = (int) response[7];
				cleanDay = cleanTime / 24;
				dustLevel = (int) response[8];
				mMainHandler.obtainMessage(SET_STATUS, -1, -1, null)
						.sendToTarget();
				mMainHandler.obtainMessage(END_OF_CMD, -1, -1, null)
						.sendToTarget();

			} else {
				BuffClean();
			}
		} else if (Arrays.equals(response_HEAD, RSP_TASTE)) {
			// 맛조절 커피 뽑기 성공 응답.
			if (response[4] == (byte) 0x7E) {
				if (D)
					Log.e(TAG, "[RSP_TASTE]");
				if (WAIT_ACK == RE_DROP_TASTE_COFFEE)
					SEND_SUCCESS_FLAG = true;
			} else {
				BuffClean();
			}
		} else if (Arrays.equals(response_HEAD, RSP_END_CMD)) {
			// 커피뽑기 종료
			if (response[4] == (byte) 0x7E) {
				if (D)
					Log.e(TAG, "[RSP_END_OF_CMD]");

			} else {
				BuffClean();
			}
		}
		BuffClean();
	}

	// main 헨들러
	private final Handler mMainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case GOTO_INTRO:
				setContentView(R.layout.intro);
				btn = (Button) findViewById(R.id.BtnBtconnect);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						mMainHandler.obtainMessage(GOTO_LIST, -1, -1, null)
								.sendToTarget();
					}

				});

				btn = (Button) findViewById(R.id.BtnBtsave);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (0 == pref.getString("witch_machine", "").compareTo(
								VM_BT_NAME_1)
								|| 0 == pref.getString("witch_machine", "")
										.compareTo(VM_BT_NAME_2)) {
							mMainHandler.obtainMessage(GOTO_DROW_COFFEE, -1,
									-1, null).sendToTarget();
						} else {
							Toast.makeText(getApplicationContext(),
									getString(R.string.no_booked_up_coffee),
									Toast.LENGTH_SHORT).show();
						}
					}
				});
				// 즐겨찾는 자판기
				btn = (Button) findViewById(R.id.btn_favorite_vm);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent Intent = new Intent(CoffeeMain.this,
								FavoriteVm.class);
						Intent.putExtra("favorite_vm", FAVORITE_VM_LIST);
						startActivityForResult(Intent, REQUEST_MACHINE_INFO);
					}
				});
				// 칼로리 보기 버튼
				btn = (Button) findViewById(R.id.btn_calorie);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent EditIntent = new Intent(CoffeeMain.this,
								CalorieDialog.class);
						startActivity(EditIntent);
					}
				});
				break;
			case GOTO_MAIN:
				setContentView(R.layout.coffee_main);
				// choose_coffee = MENU_LATTE_COFFEE;

				text = (TextView) findViewById(R.id.text_money);
				text.setText(getString(R.string.have_coin) + " " + HAVE_MONEY
						+ " won");

				btn = (Button) findViewById(R.id.Btn_coffee_milk);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						choose_coffee = MENU_LATTE_COFFEE;
						mMainHandler.obtainMessage(choose_coffee, -1, -1, null)
								.sendToTarget();
					}
				});

				btn = (Button) findViewById(R.id.Btn_coffee_cream);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						choose_coffee = MENU_CAFU_COFFEE;
						mMainHandler.obtainMessage(choose_coffee, -1, -1, null)
								.sendToTarget();
					}
				});

				btn = (Button) findViewById(R.id.Btn_coffee_sugar);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						choose_coffee = MENU_ESPRESSO_COFFEE;
						mMainHandler.obtainMessage(choose_coffee, -1, -1, null)
								.sendToTarget();
					}
				});

				btn = (Button) findViewById(R.id.Btn_coffee_black);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						choose_coffee = MENU_AMERICANO_COFFEE;
						mMainHandler.obtainMessage(choose_coffee, -1, -1, null)
								.sendToTarget();
					}
				});

				btn = (Button) findViewById(R.id.Btn_minus1);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (1 < taste_coffee)
							taste_coffee--;
						text = (TextView) findViewById(R.id.txt_taste_coffee);
						text.setText(String.format("%d", taste_coffee));
					}
				});
				draw = btn.getBackground();
				draw.setAlpha(255);

				btn = (Button) findViewById(R.id.Btn_plus1);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (5 > taste_coffee)
							taste_coffee++;
						text = (TextView) findViewById(R.id.txt_taste_coffee);
						text.setText(String.format("%d", taste_coffee));
					}
				});
				draw = btn.getBackground();
				draw.setAlpha(255);

				btn = (Button) findViewById(R.id.Btn_minus2);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (1 < taste_cream)
							taste_cream--;
						text = (TextView) findViewById(R.id.txt_taste_cream);
						text.setText(String.format("%d", taste_cream));
					}
				});
				draw = btn.getBackground();
				draw.setAlpha(255);

				btn = (Button) findViewById(R.id.Btn_plus2);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (5 > taste_cream)
							taste_cream++;
						text = (TextView) findViewById(R.id.txt_taste_cream);
						text.setText(String.format("%d", taste_cream));
					}
				});
				draw = btn.getBackground();
				draw.setAlpha(255);

				btn = (Button) findViewById(R.id.Btn_minus3);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (1 < taste_sugar)
							taste_sugar--;
						text = (TextView) findViewById(R.id.txt_taste_sugar);
						text.setText(String.format("%d", taste_sugar));
					}
				});
				draw = btn.getBackground();
				draw.setAlpha(255);

				btn = (Button) findViewById(R.id.Btn_plus3);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (5 > taste_sugar)
							taste_sugar++;
						text = (TextView) findViewById(R.id.txt_taste_sugar);
						text.setText(String.format("%d", taste_sugar));
					}
				});
				draw = btn.getBackground();
				draw.setAlpha(255);

				// booking 버튼
				btn = (Button) findViewById(R.id.Btn_taste_coffee);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						editPref.putString("witch_machine", VM_BT_NAME_1);
						editPref.putInt("witch_coffee", choose_coffee);
						editPref.putInt("lv_coffee", (int) taste_coffee);
						editPref.putInt("lv_cream", (int) taste_cream);
						editPref.putInt("lv_sugar", (int) taste_sugar);
						editPref.commit();
						Toast.makeText(getApplicationContext(),
								getString(R.string.book_up_coffee),
								Toast.LENGTH_SHORT).show();
					}
				});

				// 커피뽑기 버튼
				btn = (Button) findViewById(R.id.Btn_taste_cho);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {

						mMainHandler.obtainMessage(GOTO_DROW_COFFEE, -1, -1,
								null).sendToTarget();
					}
				});

				Thread thread_Req_Status = new Thread(SendMsg_Req_Status);
				thread_Req_Status.start();

				text = (TextView) findViewById(R.id.txt_taste_coffee);
				text.setText(String.format("%d", taste_coffee));
				text = (TextView) findViewById(R.id.txt_taste_cream);
				text.setText(String.format("%d", taste_cream));
				text = (TextView) findViewById(R.id.txt_taste_sugar);
				text.setText(String.format("%d", taste_sugar));
				mMainHandler.obtainMessage(choose_coffee, -1, -1, null)
						.sendToTarget();
				break;
			case GOTO_LIST:
				setContentView(R.layout.machine_list);
				mNewDevicesArrayAdapter = new ArrayAdapter<String>(
						CoffeeMain.this, R.layout.device_name_2);
				// Find and set up the ListView for newly discovered devices
				ListView newDevicesListView = (ListView) findViewById(R.id.list_new_devices);
				newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
				newDevicesListView.setOnItemClickListener(mDeviceClickListener);
				Thread thread_Add_Machine_List = new Thread(Add_Machine_List);
				thread_Add_Machine_List.start();
				break;

			case ADD_LIST:
				if (1 == msg.arg1) {
					mNewDevicesArrayAdapter.add(VM_BT_NAME_1 + "             "
							+ "Clean" + "                " + "possible  ");
				} else if (2 == msg.arg1) {
					mNewDevicesArrayAdapter.add(VM_BT_NAME_2 + "             "
							+ "Clean" + "                " + "Possible  ");
				}
				break;

			case CHANGE_SCAN_BTN_TEXT:
				if (null != findViewById(R.id.button_scan)) {
					btn = (Button) findViewById(R.id.button_scan);
					btn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							mMainHandler.obtainMessage(GOTO_LIST, -1, -1, null)
									.sendToTarget();
						}
					});

					btn.setText(getString(R.string.button_scan_again));
				}
			case TASTE_COFFEE_LIST:
				// if (null != findViewById(R.id.Btn_taste_coffee)) {
				// Intent TasteIntent = new Intent(CoffeeMain.this,
				// Taste.class);
				// startActivity(TasteIntent);
				// }
				break;
			case END_OF_CMD:
				if (null != findViewById(R.id.machine_cleanning)) {
					btn = (Button) findViewById(R.id.machine_cleanning);
					btn.setClickable(true);
					btn.setText("");
				}
				break;
			case MENU_LATTE_COFFEE:

				layout = (LinearLayout) findViewById(R.id.layer_coffee);
				layout.setAlpha((float) 1);
				layout = (LinearLayout) findViewById(R.id.layer_cream);
				layout.setAlpha((float) 1);
				layout = (LinearLayout) findViewById(R.id.layer_sugar);
				layout.setAlpha((float) 1);

				btn = (Button) findViewById(R.id.Btn_minus1);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_plus1);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_minus2);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_plus2);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_minus3);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_plus3);
				btn.setClickable(true);

				text = (TextView) findViewById(R.id.text_cost);
				text.setText("Price : 300 won");

				btn = (Button) findViewById(R.id.Btn_coffee_milk);
				btn.setBackgroundResource(R.drawable.coffee_milk_on);
				btn = (Button) findViewById(R.id.Btn_coffee_cream);
				btn.setBackgroundResource(R.drawable.coffee_cream_off);
				btn = (Button) findViewById(R.id.Btn_coffee_sugar);
				btn.setBackgroundResource(R.drawable.coffee_sugar_off);
				btn = (Button) findViewById(R.id.Btn_coffee_black);
				btn.setBackgroundResource(R.drawable.coffee_black_off);

				break;
			case MENU_CAFU_COFFEE:
				layout = (LinearLayout) findViewById(R.id.layer_coffee);
				layout.setAlpha((float) 1);
				layout = (LinearLayout) findViewById(R.id.layer_cream);
				layout.setAlpha((float) 1);
				layout = (LinearLayout) findViewById(R.id.layer_sugar);
				layout.setAlpha((float) 1);

				btn = (Button) findViewById(R.id.Btn_minus1);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_plus1);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_minus2);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_plus2);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_minus3);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_plus3);
				btn.setClickable(true);

				text = (TextView) findViewById(R.id.text_cost);
				text.setText("Price : 300 won");

				btn = (Button) findViewById(R.id.Btn_coffee_milk);
				btn.setBackgroundResource(R.drawable.coffee_milk_off);
				btn = (Button) findViewById(R.id.Btn_coffee_cream);
				btn.setBackgroundResource(R.drawable.coffee_cream_on);
				btn = (Button) findViewById(R.id.Btn_coffee_sugar);
				btn.setBackgroundResource(R.drawable.coffee_sugar_off);
				btn = (Button) findViewById(R.id.Btn_coffee_black);
				btn.setBackgroundResource(R.drawable.coffee_black_off);

				break;
			case MENU_ESPRESSO_COFFEE:

				layout = (LinearLayout) findViewById(R.id.layer_coffee);
				layout.setAlpha((float) 1);
				layout = (LinearLayout) findViewById(R.id.layer_cream);
				layout.setAlpha((float) 0.2);
				layout = (LinearLayout) findViewById(R.id.layer_sugar);
				layout.setAlpha((float) 0.2);

				btn = (Button) findViewById(R.id.Btn_minus1);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_plus1);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_minus2);
				btn.setClickable(false);
				btn = (Button) findViewById(R.id.Btn_plus2);
				btn.setClickable(false);
				btn = (Button) findViewById(R.id.Btn_minus3);
				btn.setClickable(false);
				btn = (Button) findViewById(R.id.Btn_plus3);
				btn.setClickable(false);

				text = (TextView) findViewById(R.id.text_cost);
				text.setText("Price : 200 won");

				btn = (Button) findViewById(R.id.Btn_coffee_milk);
				btn.setBackgroundResource(R.drawable.coffee_milk_off);
				btn = (Button) findViewById(R.id.Btn_coffee_cream);
				btn.setBackgroundResource(R.drawable.coffee_cream_off);
				btn = (Button) findViewById(R.id.Btn_coffee_sugar);
				btn.setBackgroundResource(R.drawable.coffee_sugar_on);
				btn = (Button) findViewById(R.id.Btn_coffee_black);
				btn.setBackgroundResource(R.drawable.coffee_black_off);

				break;
			case MENU_AMERICANO_COFFEE:

				layout = (LinearLayout) findViewById(R.id.layer_coffee);
				layout.setAlpha((float) 1);
				layout = (LinearLayout) findViewById(R.id.layer_cream);
				layout.setAlpha((float) 0.2);
				layout = (LinearLayout) findViewById(R.id.layer_sugar);
				layout.setAlpha((float) 1);

				btn = (Button) findViewById(R.id.Btn_minus1);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_plus1);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_minus2);
				btn.setClickable(false);
				btn = (Button) findViewById(R.id.Btn_plus2);
				btn.setClickable(false);
				btn = (Button) findViewById(R.id.Btn_minus3);
				btn.setClickable(true);
				btn = (Button) findViewById(R.id.Btn_plus3);
				btn.setClickable(true);

				text = (TextView) findViewById(R.id.text_cost);
				text.setText("Price : 200 won");

				btn = (Button) findViewById(R.id.Btn_coffee_milk);
				btn.setBackgroundResource(R.drawable.coffee_milk_off);
				btn = (Button) findViewById(R.id.Btn_coffee_cream);
				btn.setBackgroundResource(R.drawable.coffee_cream_off);
				btn = (Button) findViewById(R.id.Btn_coffee_sugar);
				btn.setBackgroundResource(R.drawable.coffee_sugar_off);
				btn = (Button) findViewById(R.id.Btn_coffee_black);
				btn.setBackgroundResource(R.drawable.coffee_black_on);

				break;

			case GOTO_DROW_COFFEE:

				set_coffee_data();

				if (coffee_cost > HAVE_MONEY) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.no_coin), Toast.LENGTH_SHORT)
							.show();
				} else {

					if (NFC_TAG_SCAN == NFC_MODE) {
						Intent TasteIntent = new Intent(CoffeeMain.this,
								NfcTagRead.class);
						startActivityForResult(TasteIntent,
								REQUEST_NFC_TAG_READ);
					} else if (NFC_BEAM == NFC_MODE) {
						// Beam
						Intent TasteIntent = new Intent(CoffeeMain.this,
								Beam.class);
						TasteIntent.putExtra("txt_taste_coffee",
								send_coffee_data_Array);
						startActivity(TasteIntent);
					}
				}
				break;

			case SET_STATUS:
				// 상태정보 확인 후 UI변경.
				text = (TextView) findViewById(R.id.text_displayResultw);
				text.setText(/*
							 * "ID : " + MACHINE_NAME +
							 */String
						.format("Cleanness   : clean\nLast Clean: %ddays %dhours ago\nAvg/Week: 5 times\nDust Level: %d (0~9)",
								cleanDay, (cleanTime % 24), dustLevel));
				text = (TextView) findViewById(R.id.txt_cup);
				iv = (ImageView) findViewById(R.id.icon_cup);
				if (true == cupFlag) {
					iv.setImageResource(R.drawable.cup_y);
					text.setText("Cup OK");
				} else {
					iv.setImageResource(R.drawable.cup_n);
					text.setText("no Cup");
				}
				text = (TextView) findViewById(R.id.txt_water);
				iv = (ImageView) findViewById(R.id.icon_water);
				if (true == waterFlag) {
					iv.setImageResource(R.drawable.water_y);
					text.setText("Water OK");
				} else {
					iv.setImageResource(R.drawable.water_n);
					text.setText("no Water");
				}
				break;

			case PROGRESS_COFFEE_DRIP:
				// loading_layout.setVisibility(View.VISIBLE);
				break;
			}
		}
	};
	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			String info = ((TextView) v).getText().toString();
			String DevName = info.substring(0, VM_BT_NAME_1.length());
			Intent DevInfoIntent = new Intent(CoffeeMain.this,
					MapView_gps.class);
			DevInfoIntent.putExtra("Dev", DevName);
			startActivityForResult(DevInfoIntent, REQUEST_MACHINE_INFO);
		}
	};
	// BT Thread 헨들러
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					break;
				case BluetoothChatService.STATE_LISTEN:
					break;
				case BluetoothChatService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				// BT 데이터리드 후 parse
				for (int i = 0; i < 10; i++) {
					if (bufferMSG[i] == (byte) 0x7E) {
						rf_RSP_CMD(bufferMSG);
						break;
					}
				}
				break;

			case MESSAGE_DEVICE_NAME:// connected
				BtPairDialogDialog = true;
				mBtConnecFlag = true;

				if (BT_CONNECT_REQ_STATE == BT_CONNECT_MODE) {
					mMainHandler.obtainMessage(GOTO_MAIN, -1, -1, null)
							.sendToTarget();
				} else if (BT_CONNECT_REQ_DROW_COFFEE == BT_CONNECT_MODE) {
					// for drow coffee - 블루투스로 메시지 전달
					count_coffee_material();

					Thread thread = new Thread(SendMsg_Drop_Taste_Coffee);
					thread.start();

					Intent Intent = new Intent(CoffeeMain.this,
							Result_DrowCoffee.class);
					startActivityForResult(Intent, REQUEST_RESULT_DROW_COFFEE);
				}
				break;

			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			case MESSAGE_CONNECT: // connecting
				BtPairDialogDialog = false;
				createThreadBtPairDialog();
				break;
			case MESSAGE_CONNECT_FAIL: // connect fail
				BtPairDialogDialog = true;
				handlerDlog.sendEmptyMessage(0);
				break;
			case MESSAGE_DISCONNECT:
				BtPairDialogDialog = false;
				mBtConnecFlag = false;
				if (mChatService != null) {
					mChatService.stop();
					mChatService = null;
				}
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				setupChat();
				break;
			}
		}
	};

	private void set_coffee_data() {
		if (choose_coffee == MENU_LATTE_COFFEE) {
			coffee_cost = 300;
			send_coffee_data_Array[2] = MENU_LATTE_COFFEE;
			send_coffee_data_Array[4] = taste_coffee;
			send_coffee_data_Array[5] = taste_cream;
			send_coffee_data_Array[6] = taste_sugar;
		} else if (choose_coffee == MENU_CAFU_COFFEE) {
			coffee_cost = 300;
			send_coffee_data_Array[2] = MENU_CAFU_COFFEE;
			send_coffee_data_Array[4] = taste_coffee;
			send_coffee_data_Array[5] = taste_cream;
			send_coffee_data_Array[6] = taste_sugar;
		} else if (choose_coffee == MENU_ESPRESSO_COFFEE) {
			coffee_cost = 200;
			send_coffee_data_Array[2] = MENU_ESPRESSO_COFFEE;
			send_coffee_data_Array[4] = taste_coffee;

		} else if (choose_coffee == MENU_AMERICANO_COFFEE) {
			coffee_cost = 200;
			send_coffee_data_Array[2] = MENU_AMERICANO_COFFEE;
			send_coffee_data_Array[4] = taste_coffee;
			send_coffee_data_Array[6] = taste_sugar;
		}
	}

	private void count_coffee_material() {
		HAVE_MONEY -= coffee_cost;
		editPref.putInt("have_money", HAVE_MONEY);
		editPref.putString("witch_machine", "");

		if (choose_coffee == MENU_LATTE_COFFEE) {
			editPref.putInt("count_latte", ++count_latte);// 카페라떼 뽑은횟수
		} else if (choose_coffee == MENU_CAFU_COFFEE) {
			editPref.putInt("count_cafu", ++count_cafu);// 카푸치노 뽑은횟수
		} else if (choose_coffee == MENU_ESPRESSO_COFFEE) {
			editPref.putInt("count_espresso", ++count_espresso);// 에스프레소 뽑은횟수

		} else if (choose_coffee == MENU_AMERICANO_COFFEE) {
			editPref.putInt("count_americano", ++count_americano);// 아메리카노 뽑은횟수
		}
		count_add_coffee += taste_coffee;
		count_add_cream += taste_cream;
		count_add_sugar += taste_sugar;
		editPref.putInt("count_add_coffee", count_add_coffee);// 커피양 추가한 횟수
		editPref.putInt("count_add_cream", count_add_cream);// 크림양 추가한 횟수
		editPref.putInt("count_add_sugar", count_add_sugar);// 설탕양 추가한 횟수

		editPref.commit();
	}

	Runnable SendMsg_Req_Status = new Runnable() {
		public void run() {
			int iii = 0;
			byte[] sendArray = { (byte) 0xBB, 0x01, 0x01, 0x00, 0x7E };

			mdelay(1000);
			for (iii = 0; iii < 3; iii++) {
				m_sendMessage(sendArray);
				WAIT_ACK = RE_STATUS;
				if (true == SEND_SUCCESS_FLAG)
					break;
				mdelay(500);
			}
			SEND_SUCCESS_FLAG = false;
			WAIT_ACK = 0;

			// if (mBluetoothAdapter != null) {
			// mBluetoothAdapter.disable();
			// mBluetoothAdapter = null;
			// }
			if (mChatService != null) {
				mChatService.stop();
				mChatService = null;
			}
		}
	};

	Runnable SendMsg_Drop_Taste_Coffee = new Runnable() {
		public void run() {
			int iii = 0;
			mdelay(1000);
			for (iii = 0; iii < 3; iii++) {
				m_sendMessage(send_coffee_data_Array);
				WAIT_ACK = RE_DROP_TASTE_COFFEE;
				if (true == SEND_SUCCESS_FLAG)
					break;
				mdelay(500);
			}
			SEND_SUCCESS_FLAG = false;
			WAIT_ACK = 0;
			// if (mBluetoothAdapter != null) {
			// mBluetoothAdapter.disable();
			// mBluetoothAdapter = null;
			// }
			if (mChatService != null) {
				mChatService.stop();
				mChatService = null;
			}
		}
	};

	Runnable Add_Machine_List = new Runnable() {
		public void run() {
			mdelay(500);
			if (versionName == VERSION_ETRI)
				mMainHandler.obtainMessage(ADD_LIST, 1, -1, null)
						.sendToTarget();
			else if (versionName == VERSION_TELEWORKS)
				mMainHandler.obtainMessage(ADD_LIST, 2, -1, null)
						.sendToTarget();
			// mdelay(500);
			// mMainHandler.obtainMessage(ADD_LIST, 2, -1, null).sendToTarget();
			// mdelay(500);
			mMainHandler.obtainMessage(CHANGE_SCAN_BTN_TEXT, 5, -1, null)
					.sendToTarget();
		}
	};

	// Intent 리턴 처리 함수.
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
				// BluetoothDevMacAdd = address;
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				if (D)
					Log.d(TAG, "BT not enabled");
				Toast.makeText(this,
						getString(R.string.bt_not_enabled_leaving),
						Toast.LENGTH_SHORT).show();
				finish();
			}
		case REQUEST_MACHINE_INFO:
			if (resultCode == Activity.RESULT_OK) {
				MACHINE_NAME = data.getExtras().getString("Dev");
				BT_CONNECT_MODE = BT_CONNECT_REQ_STATE;

				if (0 == MACHINE_NAME.compareTo(VM_BT_NAME_1)) {
					BluetoothDevice device = mBluetoothAdapter
							.getRemoteDevice(VM_BT_ADRESS_1);
					mChatService.connect(device);
				} else if (0 == MACHINE_NAME.compareTo(VM_BT_NAME_2)) {
					BluetoothDevice device = mBluetoothAdapter
							.getRemoteDevice(VM_BT_ADRESS_2);
					mChatService.connect(device);
				} else
					Toast.makeText(this, "not working.", Toast.LENGTH_SHORT)
							.show();
			}
			// else {
			// Toast.makeText(this, "자판기에 연결할 수 없습니다.", Toast.LENGTH_SHORT)
			// .show();
			// }

			break;

		case REQUEST_NFC_TAG_READ:
			String NFC_TAG_UID = null;

			// after Finished nfcread activity
			if (resultCode == Activity.RESULT_OK) {
				BT_CONNECT_MODE = BT_CONNECT_REQ_DROW_COFFEE;
				NFC_TAG_UID = data.getExtras().getString("TAG_UID");
				if (D)
					Log.d(TAG, "Return UID = " + NFC_TAG_UID);

				// after coffee page here
				//

				// 블루투스 접속 코드
				if (0 == VM_BT_TagID_A.compareTo(NFC_TAG_UID)) {
					// connect to vm_1
					BluetoothDevice device = mBluetoothAdapter
							.getRemoteDevice(VM_BT_ADRESS_1);
					// Attempt to connect to the device
					mChatService.connect(device);

				} else if (0 == VM_BT_TagID_B.compareTo(NFC_TAG_UID)) {
					// connect to vm_2
					BluetoothDevice device = mBluetoothAdapter
							.getRemoteDevice(VM_BT_ADRESS_2);
					// Attempt to connect to the device
					mChatService.connect(device);
				}
				// m_sendMessage(sendArray);
			} else {
				if (D)
					Log.d(TAG, "Nfc Read Error");
			}
			break;
		case REQUEST_RESULT_DROW_COFFEE:
			// MACHINE_NAME = pref.getString("witch_machine", "");
			break;
		case REQUEST_CHARGE_COIN:
			// String resultPassWord = data.getExtras().getString("PassWord");
			if (resultCode == Activity.RESULT_OK) {
				if (0 == data.getExtras().getString("PassWord")
						.compareTo("1212")) {
					HAVE_MONEY = 2000;
					editPref.putInt("have_money", HAVE_MONEY);
					editPref.commit();
					text = (TextView) findViewById(R.id.text_money);
					if (text != null)
						text.setText(getString(R.string.have_coin) + " "
								+ HAVE_MONEY + " won");
				}
			}
			break;
		}
	}

	// 메뉴키 눌렀을 때 Layout
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	// 메뉴에서 선택한 item 처리
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// case R.id.scan:
		// // Launch the DeviceListActivity to see devices and do scan
		// Intent serverIntent = new Intent(this, DeviceListActivity.class);
		// startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		// return true;
		case R.id.charge_coin:
			// charge coin
			Intent serverIntent = new Intent(this, ChargeCoin.class);
			startActivityForResult(serverIntent, REQUEST_CHARGE_COIN);
			return true;
		}

		return false;
	}

	// --------------------------------------------------------[*]

	@Override
	public void onDestroy() {
		super.onDestroy();
		// if (mBluetoothAdapter != null) {
		// mBluetoothAdapter.disable();
		// mBluetoothAdapter = null;
		// }
		if (mChatService != null) {
			mChatService.stop();
			mChatService = null;
		}
		BtPairDialogDialog = true;

		// 어플리케이션 완전히 종료
		moveTaskToBack(true);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public static String byteToString(byte[] bytebuf) {
		String result = EncodingUtils.getString(bytebuf, 0, bytebuf.length,
				"UTF-8");
		return result;
	}

	// protected void showPopup(String title, String message) {
	// new AlertDialog.Builder(this).setTitle(title).setMessage(message)
	// .setNeutralButton("확인", new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dlg, int sumthin) {
	// System.exit(0);
	// }
	// }).show();
	// }

	public static String hexToString(byte[] bytebuf) {
		String str = "";
		for (int i = 0; i < bytebuf.length; i++) {
			str += String.format("%02X", bytebuf[i]);
		}
		return str;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		switch (newConfig.orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			if (D)
				Log.i(TAG, "ORIENTATION_PORTRAIT");
			// Toast.makeText(this, "ORIENTATION_PORTRAIT", Toast.LENGTH_LONG)
			// .show();

			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			if (D)
				Log.i(TAG, "ORIENTATION_LANDSCAPE");
			// Toast.makeText(this, "ORIENTATION_LANDSCAPE", Toast.LENGTH_LONG)
			// .show();

			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			// Back 키를 눌렀을 때 현재화면이 메인화면이면 어플종료
			// 메인화면이 아니면 메인으로 이동

			if (null != findViewById(R.id.BtnBtconnect)) {
				// finish();
				// 어플리케이션 완전히 종료
				moveTaskToBack(true);
				android.os.Process.killProcess(android.os.Process.myPid());
			} else {
				mMainHandler.obtainMessage(GOTO_INTRO, -1, -1, null)
						.sendToTarget();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
