/**
 * 
 */
package com.superman.xdriver.SQLite;

/**
 * <p>Title: com.superman.xdriver.SQLite.CarOil.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-8-7 下午11:17:49
 */

public class Oil {
	
	public static final String TABLE_NAME = "oil";
	public static final String ID = "id";
	public static final String CURRENT_MILE = "current_mile";
	public static final String LEFT_MILE = "left_mile";
	public static final String PRICE = "price";
	public static final String MILE = "mile";//本次加油所跑的里程
	public static final String CREATE_TIME = "create_time";
	
	public static final String SQL_CREATE_TABLE = 
			"create table " + TABLE_NAME + "(" + 
			ID + " integer primary key, " + 
			CURRENT_MILE + " integer, " +
			LEFT_MILE + " integer, " + 
			PRICE + " integer, " + 
			MILE + " integer, " + 
			CREATE_TIME + " integer) ";
	
	public static final String SQL_DROP_TABLE = "drop table if exists " + TABLE_NAME ;
	
}
