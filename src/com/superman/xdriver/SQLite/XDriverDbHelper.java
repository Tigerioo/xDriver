/**
 * 
 */
package com.superman.xdriver.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <p>Title: com.superman.xdriver.SQLite.XDriverDbHelper.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-8-7 下午11:18:31
 */

public class XDriverDbHelper extends SQLiteOpenHelper{

	private static final String DB_NAME = "xDriver.db";
	private static final int DB_VERSION = 7;
	
	/**
	 * 
	 */
	public XDriverDbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Mile.SQL_CREATE_TABLE);
		db.execSQL(Oil.SQL_CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(Mile.SQL_DROP_TABLE);
		db.execSQL(Oil.SQL_DROP_TABLE);
		db.execSQL(Mile.SQL_CREATE_TABLE);
		db.execSQL(Oil.SQL_CREATE_TABLE);
	}
	
	
	
}
