/**
 * 
 */
package com.superman.xdriver.backup;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
 * Title: com.superman.util.Backup.java
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
 * @version 1.0 CreateTime：2014-3-30 下午7:18:15
 */

public class Backup {

	private Context context;
	private SQLiteDatabase db;
	private String currentDir;

	private ProgressDialog progressBar;
	private int progressBarStatus = 0;
	private Handler progressBarHandle = new Handler();
	private long total_size;
	private long size;// backup size

	private long mile_count, oil_count;

	private Handler handler;

	public Backup(Context context, SQLiteDatabase db, String currentDir,
			Handler handler) {
		this.context = context;
		this.db = db;
		this.currentDir = currentDir;
		this.handler = handler;
	}

	public Backup(Context context, SQLiteDatabase db, String currentDir) {
		this.context = context;
		this.db = db;
		this.currentDir = currentDir;
	}

	/**
	 * 升级的时候备份使用，无进度条
	 */
	public void upgradeBackup() {
		if(db == null){
			db = CommonManager.getInstance(context);
		}
		
		final List<MileModel> mileList = queryMileData();
		final List<OilModel> oilList = queryOilData();
		
		WriteXml write = new WriteXml();
		String mileXml = createMileXml(mileList);
		write.writeXml(mileXml, ConstantUtil.BACKUP_MILE_FILE_NAME, currentDir);
		
		String oilXml = createOilXml(oilList);
		write.writeXml(oilXml, ConstantUtil.BACKUP_OIL_FILE_NAME, currentDir);
		
	}
	
	/**
	 * add since v2.4.5.1 backup all data
	 */
	public void backup() {
		if (db == null) {
			db = CommonManager.getInstance(context);
		}

		progressBar = new ProgressDialog(context);
		progressBar.setCancelable(true);
		progressBar.setMessage(context.getString(R.string.setting_backup));
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setProgress(0);
		progressBar.setMax(100);
		progressBar.show();

		progressBarStatus = 0;

		final List<MileModel> mileList = queryMileData();
		final List<OilModel> oilList = queryOilData();

		mile_count = mileList.size();
		oil_count = oilList.size();
		total_size = size = mile_count + oil_count;

		if (total_size == 0) {
			progressBar.dismiss();
			sendHandler();
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				WriteXml write = new WriteXml();
				String mileXml = createMileXml(mileList);
				write.writeXml(mileXml, ConstantUtil.BACKUP_MILE_FILE_NAME, currentDir);
				
				String oilXml = createOilXml(oilList);
				write.writeXml(oilXml, ConstantUtil.BACKUP_OIL_FILE_NAME, currentDir);
				
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
						Thread.sleep(500);
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

	private void sendHandler() {
		// deal handler
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putBoolean(ConstantUtil.IS_BACKUP_FINISH, true);
		bundle.putBoolean(ConstantUtil.IS_RESTORE_FINISH, false);
		bundle.putLong(ConstantUtil.MILE_COUNT, mile_count);
		bundle.putLong(ConstantUtil.OIL_COUNT, oil_count);
		message.setData(bundle);
		handler.sendMessage(message);
		
	}

	private String createMileXml(List<MileModel> list) {
		StringBuilder buff = new StringBuilder();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buff.append("<xDriver_mile>");
		for (MileModel model : list) {
			buff.append("<rcd>");
			buff.append("<id>" + model.getID() + "</id>");
			buff.append("<start_time>" + model.getSTART_TIME() + "</start_time>");
			buff.append("<end_time>" + model.getEND_TIME() + "</end_time>");
			buff.append("<start_mile>" + model.getSTART_MILE() + "</start_mile>");
			buff.append("<end_mile>" + model.getEND_MILE() + "</end_mile>");
			buff.append("<create_time>" + model.getCREATE_TIME() + "</create_time>");
			buff.append("</rcd>");

			size--;
		}
		buff.append("</xDriver_mile>");
		return buff.toString();
	}

	/**
	 * add since v0.0.8
	 * write xml file using PlanTemplate list data
	 * @param list
	 * @return
	 */
	private String createOilXml(List<OilModel> list) {
		StringBuilder buff = new StringBuilder();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buff.append("<xDriver_oil>");
		for (OilModel model : list) {
			buff.append("<rcd>");
			buff.append("<id>" + model.getId() + "</id>");
			buff.append("<current_mile>" + model.getCurrent_mile() + "</current_mile>");
			buff.append("<left_mile>" + model.getLeft_mile() + "</left_mile>");
			buff.append("<price>" + model.getPrice() + "</price>");
			buff.append("<mile>" + model.getMile() + "</mile>");
			buff.append("<create_time>" + model.getCreate_time() + "</create_time>");
			buff.append("</rcd>");

			size--;
		}
		buff.append("</xDriver_oil>");
		return buff.toString();
	}
	
	
	/**
	 * add since v2.4.5.1 backup data
	 * 
	 * @return
	 */
	private int dealBackUp() {
		return Math.round(100 - (((float) size / (float) total_size) * 100));
	}

	private List<MileModel> queryMileData() {
		List<MileModel> reList = new ArrayList<MileModel>();
		Cursor c = null;
		try {
			c = db.query(Mile.TABLE_NAME, new String[]{Mile.ID, Mile.START_MILE, Mile.END_MILE,
					 Mile.START_TIME, Mile.END_TIME, Mile.CREATE_TIME}, null, null, null, null, Mile.START_TIME + " desc");
			while (c.moveToNext()) {
				MileModel model = new MileModel();
				try {
					model.setID(c.getLong(c.getColumnIndex(Mile.ID)));
					model.setSTART_MILE(c.getInt(c.getColumnIndex(Mile.START_MILE)));
					model.setEND_MILE(c.getInt(c.getColumnIndex(Mile.END_MILE)));
					model.setSTART_TIME(c.getLong(c.getColumnIndex(Mile.START_TIME)));
					model.setEND_TIME(c.getLong(c.getColumnIndex(Mile.END_TIME)));
					model.setCREATE_TIME(c.getLong(c.getColumnIndex(Mile.CREATE_TIME)));
				} catch (Exception e) {
					continue;
				}
				
				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return reList;
	}

	/**
	 * add since v0.0.8
	 * query plantemplate data
	 * @return
	 */
	private List<OilModel> queryOilData() {
		List<OilModel> reList = new ArrayList<OilModel>();
		Cursor c = null;
		try {
			c = db.query(Oil.TABLE_NAME, new String[] { Oil.ID, Oil.CURRENT_MILE, Oil.LEFT_MILE,
					Oil.PRICE, Oil.MILE, Oil.CREATE_TIME}, null,
					null, null, null, Oil.ID + " asc");
			while (c.moveToNext()) {
				OilModel model = new OilModel();
				model.setId(c.getLong(c.getColumnIndex(Oil.ID)));
				model.setCurrent_mile(c.getInt(c.getColumnIndex(Oil.CURRENT_MILE)));
				model.setLeft_mile(c.getInt(c.getColumnIndex(Oil.LEFT_MILE)));
				model.setPrice(c.getDouble(c.getColumnIndex(Oil.PRICE)));
				model.setMile(c.getInt(c.getColumnIndex(Oil.MILE)));
				model.setCreate_time(c.getLong(c.getColumnIndex(Oil.CREATE_TIME)));

				reList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return reList;
	}
	
}
