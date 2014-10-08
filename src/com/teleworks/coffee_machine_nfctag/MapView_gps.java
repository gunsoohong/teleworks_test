package com.teleworks.coffee_machine_nfctag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapView_gps extends FragmentActivity implements LocationListener {

	TextView text;

	// String address1 = "00:18:9A:23:41:CB";

	private GoogleMap mmap;
	private Button mMyPoint;
	private LocationManager locationManager;
	private String provider;

	// LatLng loc1 = new LatLng(37.5130055555556, 127.059066666667); //�ڿ���
	LatLng loc1 = new LatLng(36.38004615067235, 127.36790278672784); // ��Ʈ��
	// LatLng loc2 = new LatLng(37.5128944444444, 127.058680555556); // �ڿ���
	LatLng loc2 = new LatLng(37.48069667124464, 126.8824363182814); // ��������д���

	SharedPreferences pref;
	SharedPreferences.Editor editPref;
	String Dev_name;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.mapview);
		setResult(Activity.RESULT_CANCELED);

		GooglePlayServicesUtil.isGooglePlayServicesAvailable(MapView_gps.this);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, true);

		if (provider == null) { // ��ġ���� ������ �ȵǾ� ������ �����ϴ� ��Ƽ��Ƽ�� �̵��մϴ�
			new AlertDialog.Builder(MapView_gps.this)

					.setIcon(R.drawable.marker_me)
					.setTitle("Location Service")
					.setNeutralButton("Move",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startActivityForResult(
											new Intent(
													android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
											0);
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									finish();
								}
							}).show();
		} else { // ��ġ ���� ������ �Ǿ� ������ ������ġ�� �޾ƿɴϴ�
			locationManager.requestLocationUpdates(provider, 1, 1,
					MapView_gps.this);
			setUpMapIfNeeded();
		}

		Dev_name = getIntent().getStringExtra("Dev");
		text = (TextView) findViewById(R.id.machine_name);
		text.setText(" ID : " + Dev_name);

		if (0 == CoffeeMain.VM_BT_NAME_1.compareTo(Dev_name)) {
			text = (TextView) findViewById(R.id.machine_info);
			// text.setText(" ��ġ : ����Ư���� �Ｚ1�� 172-20");
			// text.setText(" ��ġ : ���������� ������ �ѱ�������� ������");
			text.setText(" Location : ETRI, Yuseong-gu, Daejeon");
			text = (TextView) findViewById(R.id.machine_clean);
			text.setText(" Cleaning State: Clean");
		} else if (0 == CoffeeMain.VM_BT_NAME_2.compareTo(Dev_name)) {
			text = (TextView) findViewById(R.id.machine_info);
			text.setText(" ��ġ : �ѱ�������� ������");
			text = (TextView) findViewById(R.id.machine_clean);
			text.setText(" û����� : ������");
		}

		pref = getSharedPreferences("cafe_saveData", 0); // ���о��
		editPref = pref.edit(); // ������

		Button button_favorite = (Button) findViewById(R.id.button_favorite);
		button_favorite.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (false == CoffeeMain.FAVORITE_VM_LIST.matches(".*"
						+ Dev_name + ".*")) {
					CoffeeMain.FAVORITE_VM_LIST = String.format("%s,%s",
							CoffeeMain.FAVORITE_VM_LIST, Dev_name);
					editPref.putString("favorite_vm",
							CoffeeMain.FAVORITE_VM_LIST);
					editPref.commit();
					CoffeeMain.FAVORITE_VM_LIST = pref.getString("favorite_vm",
							"");
					Toast.makeText(MapView_gps.this, "Add to favorites.",
							Toast.LENGTH_LONG).show();

				} else {
					Toast.makeText(MapView_gps.this, "Already be favorites.",
							Toast.LENGTH_LONG).show();
				}
			}
		});
		Button button_connect = (Button) findViewById(R.id.button_connect);
		button_connect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("Dev", Dev_name);
				// Set result and finish this Activity
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
		mMyPoint = (Button) findViewById(R.id.location);
		mMyPoint.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// markerPoint_me();
				if (mmap != null) {
					mmap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc1, 15));
				}
			}
		});
		// markerPoint_me();
	}

	private void markerPoint_me() {
		Drawable drawable = MapView_gps.this.getResources().getDrawable(
				R.drawable.marker_me);
		if (CoffeeMain.versionName == CoffeeMain.VERSION_ETRI)
			mmap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc1, 17));
		else
			mmap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc2, 17));

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 0:
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			provider = locationManager.getBestProvider(criteria, true);
			if (provider == null) {// ����ڰ� ��ġ�������� �������� ����
				finish();
			} else {// ����ڰ� ��ġ���� ���� ������
				locationManager.requestLocationUpdates(provider, 1L, 2F,
						MapView_gps.this);
				setUpMapIfNeeded();
			}
			break;
		}
	}

	@Override
	public void onBackPressed() {
		this.finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();

	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	private void setUpMapIfNeeded() {
		if (mmap == null) {
			mmap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			mmap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc1, 15));
			mmap.addMarker(new MarkerOptions()
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.marker_cup))
					.position(loc1).title("Detail Location")
					.snippet("ETRI, Yuseong-gu, Daejeon").visible(true));
			mmap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

				@Override
				public void onInfoWindowClick(Marker arg0) {
					AlertDialog alert2 = new AlertDialog.Builder(
							MapView_gps.this)
							.setIcon(R.drawable.map_click_ex)
							.setTitle(" ")
							.setMessage(" ")
							.setPositiveButton("Close",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									}).show();
				}
			});
		}
		if (mmap != null) {
			setUpMap();
		}
	}

	private void setUpMap() {
		mmap.setMyLocationEnabled(true);
		mmap.getMyLocation();
	}

	boolean locationTag = true;

	@Override
	public void onLocationChanged(Location location) {
		if (locationTag) {// �ѹ��� ��ġ�� �������� ���ؼ� tag�� �־����ϴ�
			Log.d("myLog", "onLocationChanged: !!" + "onLocationChanged!!");
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			locationTag = false;
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
