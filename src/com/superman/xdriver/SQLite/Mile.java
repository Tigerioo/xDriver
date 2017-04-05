/**
 * 
 */
package com.superman.xdriver.SQLite;

import android.provider.BaseColumns;

/**
 * <p>Title: com.superman.xdriver.SQLite.CarMile.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-8-7 下午11:17:40
 */

public class Mile implements BaseColumns{
	
	public static final String TABLE_NAME = "mile";
	public static final String ID = "id";
	public static final String START_TIME = "start_time";
	public static final String END_TIME = "end_time";
	public static final String START_MILE = "start_mile";
	public static final String END_MILE = "end_mile";
	public static final String CREATE_TIME = "create_time";
	public static final String BEGIN_ADDRESS = "begin_address";//add since v0.0.4
	public static final String END_ADDRESS = "end_address";//add since v0.0.4
	public static final String BEGIN_LATITUDE = "begin_latitude";//add since v0.0.4
	public static final String BEGIN_LONGITUDE = "begin_longitude";//add since v0.0.4
	public static final String END_LATITUDE = "end_latitude";//add since v0.0.4
	public static final String END_LONGITUDE = "end_longitude";//add since v0.0.4
	
	public static final String SQL_CREATE_TABLE = 
			"create table " + TABLE_NAME + "(" + 
			ID + " integer primary key," + 
			START_TIME + " integer," + 
			END_TIME + " integer," +
			START_MILE + " integer," + 
			END_MILE + " integer," + 
			CREATE_TIME + " integer, " +
			BEGIN_ADDRESS + " text, " + 
			END_ADDRESS + " text, " + 
			BEGIN_LATITUDE + " integer," +
			BEGIN_LONGITUDE + " integer," +
			END_LATITUDE + " integer," +
			END_LONGITUDE + " integer)";
	
	public static String SQL_DROP_TABLE = "drop table if exists " + TABLE_NAME ;
}
