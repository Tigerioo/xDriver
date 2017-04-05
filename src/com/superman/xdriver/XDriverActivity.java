package com.superman.xdriver;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.superman.xdriver.SQLite.Mile;
import com.superman.xdriver.SQLite.Oil;
import com.superman.xdriver.backup.Backup;
import com.superman.xdriver.backup.Restore;
import com.superman.xdriver.manager.CalendarManager;
import com.superman.xdriver.manager.CommonManager;
import com.superman.xdriver.model.MileModel;
import com.superman.xdriver.model.OilModel;
import com.superman.xdriver.util.ConstantUtil;

@SuppressLint({ "NewApi", "ValidFragment" })
public class XDriverActivity extends ActionBarActivity {
	
	private SQLiteDatabase db;
	private ViewPager viewPager;
	private CarFragmentPagerAdapter mCarFragmentPagerAdapter;
	private ActionBar actionBar;
	private MyHandler myHandler = new MyHandler();
	private AlertDialog backup_dialog;
	private double latitude;//add since v0.04 纬度
	private double longitude;//add since v0.04 经度
	private String address;//add since v0.04 街道名
	private int speed;//add since v0.04 当前速度
	public LocationClient mLocationClient = null;//add since v0.04 地图位置
	public BDLocationListener myListener = new MyLocationListener();//add since v0.04 位置监听
	private TextView locationInfo;//add since v0.04 当前位置信息
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xdriver);
		db = CommonManager.getInstance(XDriverActivity.this);//获取数据库实例
		
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		locationInfo = (TextView)findViewById(R.id.locationInfo);
		
		//地图位置处理
		mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(1000);// 设置发起定位请求的间隔时间为1000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
		mLocationClient.registerLocationListener(myListener); // 注册监听函数
		if(!mLocationClient.isStarted()) mLocationClient.start();
		
		viewPager = (ViewPager)findViewById(R.id.pager);
		mCarFragmentPagerAdapter = new CarFragmentPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(mCarFragmentPagerAdapter);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		ActionBar.Tab mileTab = actionBar.newTab().setText("里程记录");
		mileTab.setTabListener(new MyListener());
		
		ActionBar.Tab oilTab = actionBar.newTab().setText("加油记录");
		oilTab.setTabListener(new MyListener());
		
		actionBar.addTab(mileTab);   
		actionBar.addTab(oilTab);
		
		actionBar.setSelectedNavigationItem(getIntent().getIntExtra("tab_id", 0));
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.xdriver, menu);
		
		MenuItem saveItem = menu.findItem(R.id.action_save);
		saveItem.setShowAsAction(MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		MenuItem settingItem = menu.findItem(R.id.action_settings);
		settingItem.setShowAsAction(MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int tab_id = getSupportActionBar().getSelectedTab().getPosition();
		
		switch (item.getItemId()) {
		case R.id.action_save:
			if(tab_id == 0){
				mile_dialog();
			}else {
				oil_dialog();
			}
			break;
		case R.id.action_settings:
			AlertDialog.Builder backup_builder = new AlertDialog.Builder(XDriverActivity.this);
			backup_builder.setTitle(getString(R.string.setting_backup_restore));
			backup_builder.setPositiveButton(R.string.backup_tip, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
					Backup backup = new Backup(XDriverActivity.this, db, getFilesDir().getAbsolutePath(), myHandler);
					backup.backup();
				}
			});
			backup_builder.setNegativeButton(R.string.restore_tip, new DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
					Restore restore = new Restore(XDriverActivity.this, db, myHandler);
					restore.restore();
				}
			});
			backup_dialog = backup_builder.create();
			backup_dialog.show();
			break;
		default:
			break;
		}
		return true;
	}

	private void mile_dialog(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(XDriverActivity.this);
		final View dialogView = getLayoutInflater().inflate(R.layout.activity_xdriver_mile_add_dialog, null);
		final EditText editText = (EditText)dialogView.findViewById(R.id.xdriver_mile_add_dialog_edit);
		editText.setText(String.valueOf(getCurrentMile()));
		
		dialog.setTitle("开始本次行程");
		dialog.setView(dialogView).setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				ContentValues values = new ContentValues();
				values.put(Mile.START_MILE, editText.getText().toString());
				values.put(Mile.START_TIME, System.currentTimeMillis());
				values.put(Mile.CREATE_TIME, System.currentTimeMillis());
				//add since v0.0.4
				values.put(Mile.BEGIN_LATITUDE, latitude);
				values.put(Mile.BEGIN_LONGITUDE, longitude);
				values.put(Mile.BEGIN_ADDRESS, address);
				db.insert(Mile.TABLE_NAME, null, values);
				Toast.makeText(XDriverActivity.this, "开始本次行程……", Toast.LENGTH_SHORT).show();
				arg0.dismiss();
				
				Intent i = new Intent();
				i.putExtra("tab_id", 0);
				i.setClass(XDriverActivity.this, XDriverActivity.class);
				startActivity(i);
				finish();
			}
		});
		dialog.show();
	}
	
	private void oil_dialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(XDriverActivity.this);
		builder.setTitle("新增加油记录");
		final View dialogView = getLayoutInflater().inflate(R.layout.activity_xdriver_oil_add_dialog, null);
		final EditText currentMileEdit = (EditText)dialogView.findViewById(R.id.oil_add_dialog_currentMile);
		final EditText leftMileEdit = (EditText)dialogView.findViewById(R.id.oil_add_dialog_leftMile);
		final EditText priceEdit = (EditText)dialogView.findViewById(R.id.oil_add_dialog_price);
		
		builder.setView(dialogView);
		builder.setPositiveButton("确认", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface diag, int arg1) {
				String currentMile = currentMileEdit.getText().toString();
				String leftMile = leftMileEdit.getText().toString();
				String priceStr = priceEdit.getText().toString();
				
				if(currentMile.length() == 0) {
					Toast.makeText(XDriverActivity.this, "当前里程不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(leftMile.length() == 0) {
					Toast.makeText(XDriverActivity.this, "剩余里程不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(priceStr.length() == 0) {
					Toast.makeText(XDriverActivity.this, "金额不能为空", Toast.LENGTH_SHORT).show();
					return;
				}

				//获取当前最新的加油记录
				OilModel model = getCurrentModel();
				if(model != null){
					//本次的公里数 - 上次的公里数 - 上次剩余里程  + 当前剩余公里数
					int mile = Integer.parseInt(currentMile) - model.getCurrent_mile() - model.getLeft_mile() + Integer.parseInt(leftMile);
					
					//更新上次的公里数
					ContentValues updateValues = new ContentValues();
					updateValues.put(Oil.MILE, mile);
					db.update(Oil.TABLE_NAME, updateValues, Oil.ID + "=?", new String[]{String.valueOf(model.getId())});
				}
				
				//插入本次数据
				ContentValues values = new ContentValues();
				values.put(Oil.CURRENT_MILE, Integer.parseInt(currentMile));
				values.put(Oil.LEFT_MILE, Integer.parseInt(leftMile));
				values.put(Oil.PRICE, Integer.parseInt(priceStr));
				values.put(Oil.CREATE_TIME, System.currentTimeMillis());
				values.put(Oil.MILE, 0);
				db.insert(Oil.TABLE_NAME, null, values);
				
				
				Intent i = new Intent();
				i.putExtra("tab_id", 1);
				i.setClass(XDriverActivity.this, XDriverActivity.class);
				startActivity(i);
				finish();
			}
		});
		builder.show();
	}
	
	private int getCurrentMile(){
		Cursor c = null;
		try {
			c = db.query(Mile.TABLE_NAME, new String[]{Mile.END_MILE}, null, null, null, null, Mile.END_MILE + " desc");
			if(c.moveToFirst()){
				return c.getInt(c.getColumnIndex(Mile.END_MILE));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(c != null) c.close();
		}
		return 0;
	}
	
	/**
	 * 获取当前最新一次的记录数据
	 * @return
	 */
	private OilModel getCurrentModel(){
		Cursor c = null;
		try {
			c = db.query(Oil.TABLE_NAME, new String[]{Oil.ID, Oil.CURRENT_MILE, Oil.LEFT_MILE}, null, null, null, null, Oil.CREATE_TIME + " desc");
			if(c.moveToFirst()){
				OilModel model = new OilModel();
				model.setId(c.getLong(c.getColumnIndex(Oil.ID)));
				model.setCurrent_mile(c.getInt(c.getColumnIndex(Oil.CURRENT_MILE)));
				model.setLeft_mile(c.getInt(c.getColumnIndex(Oil.LEFT_MILE)));
				return model;
			}
		} catch (Exception e) {
			Log.e("com.superman.xdriver.XDriverActivity.getCurrentModel()", e.toString());
		} finally {
			if(c != null) c.close();
		}
		return null;
	}
	
	public class MyListener implements ActionBar.TabListener{

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction arg1) {
			viewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction arg1) {
			viewPager.setCurrentItem(tab.getPosition());
			
		}

		@Override
		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * fragments data adapter
	 * <p>Title: com.superman.xdriver.XDriverActivity.java</p>
	 *
	 * <p>Description: </p>
	 *
	 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
	 *
	 * <p>Company: Newland SoftWare Company</p>
	 *
	 * @author Lewis.Lynn
	 *
	 * @version 1.0 CreateTime：2014-8-8 上午11:10:46
	 */
	class CarFragmentPagerAdapter extends FragmentPagerAdapter{

		public CarFragmentPagerAdapter(FragmentManager fm){
			super(fm);
		}
		
		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				MileFragment mileFragment = new MileFragment(getMenuInflater());
				mileFragment.update();
				return mileFragment;
			case 1:
				OilFragment oilFragment = new OilFragment(getMenuInflater());
				oilFragment.update();
				return oilFragment;
			default:
				break;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			
			switch (position) {
			case 0:
				MileFragment mileFragment = (MileFragment)super.instantiateItem(container, position);
				mileFragment.update();
				return mileFragment;
			case 1:
				OilFragment oilFragment = (OilFragment)super.instantiateItem(container, position);
				oilFragment.update();
				return oilFragment;
			default:
				break;
			}
			return null;
		}
		
		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}
	
	public class MyHandler extends Handler{
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Bundle bundle = msg.getData();
			boolean is_backup_finish = bundle.getBoolean(ConstantUtil.IS_BACKUP_FINISH);
			boolean is_restore_finish = bundle.getBoolean(ConstantUtil.IS_RESTORE_FINISH);
			long mile_count = bundle.getLong(ConstantUtil.MILE_COUNT, 0);
			long oil_count = bundle.getLong(ConstantUtil.OIL_COUNT, 0);
			
			long total = mile_count + oil_count;
			
			if(is_backup_finish){
				if(total == 0){
					Toast.makeText(XDriverActivity.this, "暂无数据，不进行备份", Toast.LENGTH_SHORT).show();
				}else {
					showBuckupSuccessDialog(mile_count, oil_count, "备份");
				}
			}
			if(is_restore_finish){
				if(total == 0){
					Toast.makeText(XDriverActivity.this, "请先备份数据", Toast.LENGTH_SHORT).show();
				}else {
					showBuckupSuccessDialog(mile_count, oil_count, "恢复");
				}
			}
		}
		
		private void showBuckupSuccessDialog(long plan_count, long planTemplate_count, String tip){
			AlertDialog.Builder builder = new AlertDialog.Builder(XDriverActivity.this);
			builder.setTitle("提示");
			TextView tv = new TextView(XDriverActivity.this);
			String content = tip + "Mile【"+plan_count+"】条 \nOil【" + planTemplate_count + "】条";
			tv.setText(content);
			builder.setView(tv);
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
					XDriverActivity.this.finish();
					startActivity(getIntent());
				}
			});
			builder.create().show();
		}
	}
	
	public class MyLocationListener implements BDLocationListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.baidu.location.BDLocationListener#onReceiveLocation(com.baidu
		 * .location.BDLocation)
		 */
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			
			if(location.getStreet() != null){//只有当不等于null的时候才进行更新
				address = location.getStreet();
			}
			
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				speed = (int)location.getSpeed();//只保留整数部分
			}
			
			locationInfo.setText("当前位置：" + address + "			当前速度:" + speed);
		}
	}

	/**
	 * <p>Title: com.superman.xdriver.fragment.MileFragment.java</p>
	 *
	 * <p>Description: </p>
	 *
	 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
	 *
	 * <p>Company: Newland SoftWare Company</p>
	 *
	 * @author Lewis.Lynn
	 *
	 * @version 1.0 CreateTime：2014-8-11 下午5:36:18
	 */

	@SuppressLint("ValidFragment")
	public class MileFragment extends Fragment{

		private ListView lv ;
		private List<MileModel> mData;
		private MileAdapter adapter;
//		private Context context;
		private SQLiteDatabase db; 
		private MenuInflater menuInflater;
		
		public MileFragment(){
			
		}
		
		public MileFragment(MenuInflater menuInflater){
			this.menuInflater = menuInflater;
		}
		
		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_mile, container, false);
			db = CommonManager.getInstance(getActivity());
			//initialize data
			mData = getData();
			
			lv = (ListView)view.findViewById(R.id.fragment_mile_listview);// listview 
			adapter = new MileAdapter();
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> l, View v, final int position, long id) {
					if(mData.get(position).getEND_MILE() > 0 ) return ;
					AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
					View dialogView = inflater.inflate(R.layout.activity_xdriver_mile_add_dialog, null);
					final EditText editText = (EditText)dialogView.findViewById(R.id.xdriver_mile_add_dialog_edit);
					editText.setText(String.valueOf(getCurrentMile()));
					
					dialog.setTitle("结束本次行程");
					dialog.setView(dialogView).setPositiveButton("确认", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							ContentValues values = new ContentValues();
							values.put(Mile.END_MILE, editText.getText().toString());
							values.put(Mile.END_TIME, System.currentTimeMillis());
							values.put(Mile.END_ADDRESS, address);
							values.put(Mile.END_LATITUDE, latitude);
							values.put(Mile.END_LONGITUDE, longitude);
							db.update(Mile.TABLE_NAME, values, Mile.ID + "=?", new String[]{String.valueOf(mData.get(position).getID())});
							Toast.makeText(getActivity(), "结束本次行程……", Toast.LENGTH_SHORT).show();
							arg0.dismiss();
							update();
						}
					});
					dialog.show();
				}
				
			});

			registerForContextMenu(lv);
//			lv.setOnCreateContextMenuListener(new MileContextMenuListener());
			
			return view;
		}
		
		private int getCurrentMile(){
			Cursor c = null;
			try {
				c = db.query(Mile.TABLE_NAME, new String[]{Mile.END_MILE}, null, null, null, null, Mile.END_MILE + " desc");
				if(c.moveToFirst()){
					return c.getInt(c.getColumnIndex(Mile.END_MILE));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(c != null) c.close();
			}
			return 0;
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
//			menu.add(0, 1, Menu.NONE, "删除");	
			
			menuInflater.inflate(R.menu.fragment_mile_list_menu, menu);
			menu.setHeaderTitle("Choose an Option");
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			
			if(!getUserVisibleHint()) return false;
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			
			switch (item.getItemId()) {
			case R.id.menu_mile_list_delete:
				db.delete(Mile.TABLE_NAME, Mile.ID + "=?", new String[]{((TextView)info.targetView.findViewById(R.id.fragment_mile_detail_id)).getText().toString()});
				update();
				break;
			default:
				break;
			}
			
			return true;
		}
		
	/*	class MileContextMenuListener implements OnCreateContextMenuListener{

			@Override
			public void onCreateContextMenu(ContextMenu menu, View arg1,
					ContextMenuInfo arg2) {
				menu.add(0, 1, 0, "删除");
			}
			
		}
	*/	
		public List<MileModel> getData(){
			List<MileModel> list = new ArrayList<MileModel>();
			Cursor c = null;
			try {
				c = db.query(Mile.TABLE_NAME, new String[]{Mile.ID, Mile.START_MILE, Mile.END_MILE,
						 Mile.START_TIME, Mile.END_TIME, Mile.CREATE_TIME,
						 Mile.BEGIN_LATITUDE, Mile.BEGIN_LONGITUDE,//add since v0.0.4
						 Mile.END_LATITUDE, Mile.END_LONGITUDE,//add since v0.0.4
						 Mile.BEGIN_ADDRESS, Mile.END_ADDRESS}, null, null, null, null, Mile.START_TIME + " desc");
				
				while(c.moveToNext()){
					MileModel model = new MileModel();
					model.setID(c.getLong(c.getColumnIndex(Mile.ID)));
					model.setSTART_MILE(c.getInt(c.getColumnIndex(Mile.START_MILE)));
					model.setEND_MILE(c.getInt(c.getColumnIndex(Mile.END_MILE)));
					model.setSTART_TIME(c.getLong(c.getColumnIndex(Mile.START_TIME)));
					model.setEND_TIME(c.getLong(c.getColumnIndex(Mile.END_TIME)));
					model.setCREATE_TIME(c.getLong(c.getColumnIndex(Mile.CREATE_TIME)));
					
					model.setBegin_latitude(c.getDouble(c.getColumnIndex(Mile.BEGIN_LATITUDE)));//add since v0.0.4
					model.setBegin_longitude(c.getDouble(c.getColumnIndex(Mile.BEGIN_LONGITUDE)));//add since v0.0.4
					model.setEnd_latitude(c.getDouble(c.getColumnIndex(Mile.END_LATITUDE)));//add since v0.0.4
					model.setEnd_longitude(c.getDouble(c.getColumnIndex(Mile.END_LONGITUDE)));//add since v0.0.4
					
					String begin_address = c.getString(c.getColumnIndex(Mile.BEGIN_ADDRESS));
					String end_address = c.getString(c.getColumnIndex(Mile.END_ADDRESS));
					model.setBegin_address(begin_address == null ? "" : begin_address);//add since v0.0.4
					model.setEnd_address(end_address == null ? "" : end_address);//add since v0.0.4
					list.add(model);
				}
			} catch (Exception e) {
				Log.e("XDriverActivity", e.toString());
			} finally {
				if(c != null) c.close();
			}
			return list;
		}
		
		public void update(){
			if(mData != null && adapter != null){
				mData.clear();
				mData.addAll(getData());
				adapter.notifyDataSetChanged();
				lv.invalidateViews();
				lv.refreshDrawableState();
			}
		}
		
		public final class ViewHolder{
			public TextView id;
			public TextView start_mile;
			public TextView end_mile;
			public TextView mile;
			public TextView start_time;
			public TextView end_time;
		}
		
		public class MileAdapter extends BaseAdapter{

			private LayoutInflater inflater;
			
			/**
			 * 
			 */
			public MileAdapter() {
				inflater = LayoutInflater.from(getActivity());
			}
			
			@Override
			public int getCount() {
				return mData.size();
			}

			@Override
			public Object getItem(int arg0) {
				return null;
			}

			@Override
			public long getItemId(int arg0) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder = null;
				if(convertView == null){
					holder = new ViewHolder();
					convertView = inflater.inflate(R.layout.fragment_mile_detail, null);
					holder.id = (TextView)convertView.findViewById(R.id.fragment_mile_detail_id);
					holder.start_mile = (TextView)convertView.findViewById(R.id.fragment_mile_detail_start_mile);
					holder.end_mile = (TextView)convertView.findViewById(R.id.fragment_mile_detail_end_mile);
					holder.start_time = (TextView)convertView.findViewById(R.id.fragment_mile_detail_start_time);
					holder.end_time = (TextView)convertView.findViewById(R.id.fragment_mile_detail_end_time);
					holder.mile = (TextView)convertView.findViewById(R.id.fragment_mile_detail_mile);
					
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder)convertView.getTag();
				}
				
				MileModel model = mData.get(position);
				holder.id.setText(String.valueOf(model.getID()));
				
				String beginAddressInfo = model.getBegin_address().length() > 0 ? "("+model.getBegin_address()+")" : "";
				String endAddressInfo = model.getEnd_address().length() > 0 ? "("+model.getEnd_address()+")" : "";
				
				holder.start_mile.setText(String.valueOf(model.getSTART_MILE()) + "公里" + beginAddressInfo );
				holder.end_mile.setText(model.getEND_MILE() == 0 ? "" : String.valueOf(model.getEND_MILE()) + "公里" + endAddressInfo);
				holder.start_time.setText(model.getSTART_TIME() == 0 ? "" : CalendarManager.truncDateStringByMillis(model.getSTART_TIME(), "MM月dd日 HH:mm"));
				holder.end_time.setText(model.getEND_TIME() == 0 ? "" : CalendarManager.truncDateStringByMillis(model.getEND_TIME(), "MM月dd日 HH:mm"));
				holder.mile.setText(model.getEND_MILE() == 0 ? "点击结束行程" : String.valueOf(model.getEND_MILE() - model.getSTART_MILE()) + "公里");
				
				return convertView;
			}
			
		}

	}
	
	@SuppressLint("ValidFragment")
	public class OilFragment extends Fragment{

		
		private List<OilModel> mData;
		private ListView lv;
		private OilAdapter adapter;
//		private Context context;
		private SQLiteDatabase db;
		private MenuInflater menuInflater;
		
		public OilFragment(){
			
		}
		
		public OilFragment(MenuInflater menuInflater){
			this.menuInflater = menuInflater;
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_oil, container, false);
			db = CommonManager.getInstance(getActivity());
			mData = getData();
			lv = (ListView)view.findViewById(R.id.fragment_oil_listview);
			adapter = new OilAdapter();
			lv.setAdapter(adapter);
//			lv.setOnCreateContextMenuListener(new OilContextMenuListener());
			registerForContextMenu(lv);
			return view;
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
//			menu.add(0,1,0, "删除");
			menuInflater.inflate(R.menu.fragment_oil_list_menu, menu);
			menu.setHeaderTitle("Choose an Option");
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			
			if(!getUserVisibleHint()) return false;
			
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			switch (item.getItemId()) {
			case R.id.menu_oil_list_delete:
				db.delete(Oil.TABLE_NAME, Oil.ID + "=?", new String[]{((TextView)info.targetView.findViewById(R.id.fragment_oil_detail_id)).getText().toString()});
				update();
				break;
			default:
				break;
			}
			return true;
		}
		
		public List<OilModel> getData(){
			List<OilModel> list = new ArrayList<OilModel>();
			Cursor c = null;
			try {
				c = db.query(Oil.TABLE_NAME, new String[]{Oil.ID, Oil.CURRENT_MILE, Oil.LEFT_MILE, Oil.PRICE, Oil.CREATE_TIME, Oil.MILE}, 
							null, null, null, null, Oil.CREATE_TIME + " desc");
				while(c.moveToNext()){
					OilModel model = new OilModel();
					model.setId(c.getLong(c.getColumnIndex(Oil.ID)));
					model.setCurrent_mile(c.getInt(c.getColumnIndex(Oil.CURRENT_MILE)));
					model.setLeft_mile(c.getInt(c.getColumnIndex(Oil.LEFT_MILE)));
					model.setPrice(c.getDouble(c.getColumnIndex(Oil.PRICE)));
					model.setMile(c.getInt(c.getColumnIndex(Oil.MILE)));
					model.setCreate_time(c.getLong(c.getColumnIndex(Oil.CREATE_TIME)));
					list.add(model);
				}
			} catch (Exception e) {
				Log.e("com.superman.xdriver.XDriverActivity.OilFragment.getData()", e.toString());
			} finally {
				if(c != null) c.close();
			}
			return list;
		}
		
		public void update(){
			if(mData != null && adapter != null){
				mData.clear();
				mData.addAll(getData());
				adapter.notifyDataSetChanged();
				lv.invalidateViews();
				lv.refreshDrawableState();
			}
		}
		
		public final class ViewHolder {
			public TextView id;
			public TextView current_mile;
			public TextView left_mile;
			public TextView price;
			public TextView create_time;
			public TextView last_day;//距离上次加油时间, 单位：天
			public TextView mile;//本次加油所跑的里程
		}
		
		class OilAdapter extends BaseAdapter{

			private LayoutInflater inflater;
			
			public OilAdapter(){
				inflater = LayoutInflater.from(getActivity());
			}
			
			@Override
			public int getCount() {
				return mData.size();
			}

			@Override
			public Object getItem(int arg0) {
				return null;
			}

			@Override
			public long getItemId(int arg0) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder = null;
				if(convertView == null){
					holder = new ViewHolder();
					convertView = inflater.inflate(R.layout.fragment_oil_detail, null, false);
					holder.id = (TextView)convertView.findViewById(R.id.fragment_oil_detail_id);
					holder.current_mile = (TextView)convertView.findViewById(R.id.fragment_oil_detail_current_mile);
					holder.left_mile = (TextView)convertView.findViewById(R.id.fragment_oil_detail_left_mile);
					holder.price = (TextView)convertView.findViewById(R.id.fragment_oil_detail_price);
					holder.create_time = (TextView)convertView.findViewById(R.id.fragment_oil_detail_create_time);
					holder.last_day = (TextView)convertView.findViewById(R.id.fragment_oil_detail_last_day);
					holder.mile = (TextView)convertView.findViewById(R.id.fragment_oil_detail_mile);
					
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder)convertView.getTag();
				}
				
				OilModel model = mData.get(position);
				holder.id.setText(String.valueOf(model.getId()));
				holder.current_mile.setText("当前里程：" + model.getCurrent_mile() + "公里");
				holder.left_mile.setText("剩余里程：" + model.getLeft_mile() + "公里");
				holder.price.setText(model.getPrice() + "元");
				holder.create_time.setText("加油时间：" + CalendarManager.truncDateStringByMillis(model.getCreate_time(), "yyyy年MM月dd日 HH:mm"));
				int last_day = 0;
				if(mData.size() > (position+1)) {
					long last_millis = mData.get(position+1).getCreate_time();
					last_day = (int)(model.getCreate_time() - last_millis) / (24 * 60 * 60 * 1000);
				}
				holder.last_day.setText("距上次加油：" + last_day + "天");
				holder.mile.setText(model.getMile() == 0 ? "" : model.getMile() + "公里");
				
				return convertView;
			}
			
		}

	}
	
}
