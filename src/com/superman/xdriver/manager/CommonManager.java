/**
 * 
 */
package com.superman.xdriver.manager;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.superman.xdriver.SQLite.XDriverDbHelper;
import com.superman.xdriver.util.ConstantUtil;

/**
 * <p>Title: com.superman.xdriver.manager.CommonManager.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-8-8 上午10:53:46
 */

public class CommonManager {
	
	private static XDriverDbHelper dbHelper;
	
	public static SQLiteDatabase getInstance(Context context){
		if(dbHelper == null)
			dbHelper = new XDriverDbHelper(context);
		return dbHelper.getReadableDatabase();
	}
	
	public static String findDefaultBackupPath(){
		// 判断SD卡是否存在，并且是否具有读写权限
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// 获得存储卡的路径
			String sdpath = Environment.getExternalStorageDirectory()
					+ "/";
			return sdpath + ConstantUtil.BACKUP_PATH;
		}
		return null;
	}
}
