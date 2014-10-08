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

	// LatLng loc1 = new LatLng(37.5130055555556, 127.059066666667); //코엑스
	LatLng loc1 = new LatLng(36.38004615067235, 127.36790278672784); // 에트리
	// LatLng loc2 = new LatLng(37.5128944444444, 127.058680555556); // 코엑스
	LatLng loc2 = new LatLng(37.48069667124464, 126.8824363182814); // 가산디지털단지

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

		if (provider == null) { // 위치정보 설정이 안되어 있으면 설정하는 엑티비티로 이동합니다
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
		} else { // 위치 정보 설정이 되어 있으면 현재위치를 받아옵니다
			locationManager.requestLocationUpdates(provider, 1, 1,
					MapView_gps.this);
			setUpMapIfNeeded();
		}

		Dev_name = getIntent().getStringExtra("Dev");
		text = (TextView) findViewById(R.id.machine_name);
		text.setText(" ID : " + Dev_name);

		if (0 == CoffeeMain.VM_BT_NAME_1.compareTo(Dev_name)) {
			text = (TextView) findViewById(R.id.machine_info);
			// text.setText(" 위치 : 서울특별시 삼성1동 172-20");
			// text.setText(" 위치 : 대전광역시 유성구 한국전자통신 연구원");
			text.setText(" Location : ETRI, Yuseong-gu, Daejeon");
			text = (TextView) findViewById(R.id.machine_clean);
			text.setText(" Cleaning State: Clean");
		} else if (0 == CoffeeMain.VM_BT_NAME_2.compareTo(Dev_name)) {
			text = (TextView) findViewById(R.id.machine_info);
			text.setText(" 위치 : 한국전자통신 연구원");
			text = (TextView) findViewById(R.id.machine_clean);
			text.setText(" 청결상태 : 깨끗함");
		}

		pref = getSharedPreferences("cafe_saveData", 0); // 값읽어옴
		editPref = pref.edit(); // 값저장

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
			if (provider == null) {// 사용자가 위치설정동의 안했을때 종료
				finish();
			} else {// 사용자가 위치설정 동의 했을때
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
		if (locationTag) {// 한번만 위치를 가져오기 위해서 tag를 주었습니다
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
