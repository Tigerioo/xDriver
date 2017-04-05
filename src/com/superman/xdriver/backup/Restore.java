/**
 * 
 */
package com.superman.xdriver.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.superman.xdriver.R;
import com.superman.xdriver.SQLite.Mile;
import com.superman.xdriver.SQLite.Oil;
import com.superman.xdriver.manager.CommonManager;
import com.superman.xdriver.model.MileModel;
import com.superman.xdriver.model.OilModel;
import com.superman.xdriver.util.ConstantUtil;

/**
 * <p>
 * Title: com.superman.smsalarm.backup.Restore.java
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2001-2013 Newland SoftWare Company
 * </p>
 * 
 * <p>
 * Company: Newland SoftWare Company
 * </p>
 * 
 * @author Lewis.Lynn
 * 
 * @version 1.0 CreateTime：2015-04-15 14:59:27
 */

public class Restore {

	private Context context;
	private SQLiteDatabase db;

	private ProgressDialog progressBar;
	private int progressBarStatus = 0;
	private Handler progressBarHandle = new Handler();
	private long total_size;
	private long size;// backup size

	private long mile_count, oil_count;
	private ParseXmlService parse = new ParseXmlService();
	private Handler handler;

	/**
	 * 
	 */
	public Restore(Context context, SQLiteDatabase db, Handler handler) {
		this.context = context;
		this.db = db;
		this.handler = handler;
	}

	public void restore() {
		if (db == null) {
			db = CommonManager.getInstance(context);
		}
		progressBar = new ProgressDialog(context);
		progressBar.setCancelable(true);
		progressBar.setMessage(context.getString(R.string.setting_restore));
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setProgress(0);
		progressBar.setMax(100);
		progressBar.show();

		progressBarStatus = 0;

		final List<MileModel> mileList = queryMileData(ConstantUtil.BACKUP_MILE_FILE_NAME);
		final List<OilModel> oilList = queryOilData(ConstantUtil.BACKUP_OIL_FILE_NAME);

		mile_count = mileList == null ? 0 : mileList.size();
		oil_count = oilList == null ? 0 : oilList.size();
		
		total_size = size = mile_count + oil_count;
		
		if(total_size == 0){
			progressBar.dismiss();
			sendHandler();
			return ;
		}
		
		clearAllData();//clear data

		new Thread(new Runnable() {

			@Override
			public void run() {
				saveMileRecord(mileList);
				saveOilRecord(oilList);
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {

				while (progressBarStatus < 100) {

					// deal progress
					progressBarStatus = dealBackUp();

					// sleep 1 second
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// update status
					progressBarHandle.post(new Runnable() {

						@Override
						public void run() {
							progressBar.setProgress(progressBarStatus);
						}
					});

					if (progressBarStatus >= 100) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						sendHandler();
						progressBar.dismiss();
					}
				}
			}
		}).start();

	}
	
	private void sendHandler(){
		//deal handler
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putBoolean(ConstantUtil.IS_BACKUP_FINISH, false);
		bundle.putBoolean(ConstantUtil.IS_RESTORE_FINISH, true);
		bundle.putLong(ConstantUtil.MILE_COUNT, mile_count);
		bundle.putLong(ConstantUtil.OIL_COUNT, oil_count);
		message.setData(bundle);
		handler.sendMessage(message);
	}
	
	/**
	 * clear all data
	 * @param list
	 */
	private void clearAllData(){
		db.delete(Mile.TABLE_NAME, null, null);
		db.delete(Oil.TABLE_NAME, null, null);
	}
	
	private void saveMileRecord(List<MileModel> list){
		//list must not null or empty
		if(list == null || list.size() == 0){
			return ;
		}
		//loop list
		for (MileModel model : list) {
			ContentValues values = new ContentValues();
			values.put(Mile.ID, model.getID());
			values.put(Mile.START_TIME, model.getSTART_TIME());
			values.put(Mile.END_TIME, model.getEND_TIME());
			values.put(Mile.START_MILE, model.getSTART_MILE());
			values.put(Mile.END_MILE, model.getEND_MILE());
			values.put(Mile.CREATE_TIME, model.getCREATE_TIME());
			db.insert(Mile.TABLE_NAME, null, values);
			size-- ;//size reduce 1
		}
		
	}
	
	/**
	 * add since v0.0.4
	 * @param list
	 */
	private void saveOilRecord(List<OilModel> list){
		//list must not null or empty
		if(list == null || list.size() == 0){
			return ;
		}
		//loop list
		for (OilModel model : list) {
			ContentValues values = new ContentValues();
			values.put(Oil.ID, model.getId());
			values.put(Oil.CURRENT_MILE, model.getCurrent_mile());
			values.put(Oil.LEFT_MILE, model.getLeft_mile());
			values.put(Oil.PRICE, model.getPrice());
			values.put(Oil.MILE, model.getMile());
			values.put(Oil.CREATE_TIME, model.getCreate_time());
			db.insert(Oil.TABLE_NAME, null, values);
			size-- ;//size reduce 1
		}
		
	}
	
	
	/**
	 * add since v2.4.5.1 backup data
	 * 
	 * @return
	 */
	private int dealBackUp() {
		return Math.round(100 - (((float)size / (float)total_size) * 100));
	}

	/**
	 * @throws FileNotFoundException 
	 */
	private List<MileModel> queryMileData(String backup_name) {
		List<MileModel> reList = new ArrayList<MileModel>();
		String file_name = CommonManager.findDefaultBackupPath() + backup_name;
		File file = new File(file_name);
		if(!file.exists()){
			return null;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			List<Map<String, String>> list = parse.parseXml(is);
			for (Map<String, String> map : list) {
				MileModel model = new MileModel();
				Iterator iter =  map.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<?, ?> entry = (Map.Entry<?, ?>)iter.next();
					String key = (String)entry.getKey();
					String value = (String)entry.getValue();
					System.out.println(key + "==" + value);
					if("id".equals(key)){
						model.setID(Long.parseLong(value));
					}else if("start_time".equals(key)){
						model.setSTART_TIME(Long.parseLong(value));
					}else if("end_time".equals(key)){
						model.setEND_TIME(Long.parseLong(value));
					}else if("start_mile".equals(key)){
						model.setSTART_MILE(Integer.parseInt(value));
					}else if("end_mile".equals(key)){
						model.setEND_MILE(Integer.parseInt(value));
					}else if("create_time".equals(key)){
						model.setCREATE_TIME(Long.parseLong(value));
					}
				}
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(is != null){
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return reList;
	}

	/**
	 * add since v0.0.8
	 * query plan template data from xml
	 * @throws FileNotFoundException 
	 */
	private List<OilModel> queryOilData(String backup_name) {
		List<OilModel> reList = new ArrayList<OilModel>();
		String file_name = CommonManager.findDefaultBackupPath() + backup_name;
		File file = new File(file_name);
		if(!file.exists()){
			return null;
		}
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			List<Map<String, String>> list = parse.parseXml(is);
			for (Map<String, String> map : list) {
				OilModel model = new OilModel();
				Iterator iter =  map.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<?, ?> entry = (Map.Entry<?, ?>)iter.next();
					String key = (String)entry.getKey();
					String value = (String)entry.getValue();
					if("id".equals(key)){
						model.setId(Long.parseLong(value));
					}else if("current_mile".equals(key)){
						model.setCurrent_mile(Integer.parseInt(value));
					}else if("left_mile".equals(key)){
						model.setLeft_mile(Integer.parseInt(value));
					}else if("price".equals(key)){
						model.setPrice(Double.parseDouble(value));
					}else if("mile".equals(key)){
						model.setMile(Integer.parseInt(value));
					}else if("create_time".equals(key)){
						model.setCreate_time(Long.parseLong(value));
					}
				}
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(is != null){
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return reList;
	}
	
}
