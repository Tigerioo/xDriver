/**
 * 
 */
package com.superman.xdriver.manager;

import android.annotation.SuppressLint;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <p>Title: com.superman.plancalendar.manager.CalendarManager.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-5-9 上午9:56:56
 */

@SuppressLint("SimpleDateFormat")
public class CalendarManager {
	
	public static int getCurrentWeekly(){
		Calendar cal = Calendar.getInstance();
		int weekly = cal.get(Calendar.DAY_OF_WEEK);
		return weekly == 1 ? 7 : weekly-1;
	}
	
	public static int getWeekly(Calendar cal){
		int weekly = cal.get(Calendar.DAY_OF_WEEK);
		return weekly == 1 ? 7 : weekly-1;
	}
	
	public static String truncDateStringByMillis(long millis, String formatString){
		DateFormat format = new SimpleDateFormat(formatString);
		Date date = new Date(millis);
		return format.format(date);
	}
	
	
	/**
	 * 从字符时间转成毫秒数
	 * @return
	 */
	public static long truncStringDateToMillis(String dateTime, String dateFormat){
		DateFormat format = new SimpleDateFormat(dateFormat);
		try {
			Date date = format.parse(dateTime);
			return date.getTime();
		} catch (ParseException e) {
			return System.currentTimeMillis();
		}
	}
	
	/**
	 * 当前小时的起始时间，如现在15:28 ,则返回15:00
	 * @return
	 */
	public static String getCurrentHourBeginTime(){
		DateFormat format = new SimpleDateFormat("HH:00");
		return format.format(new Date());
	}
	
	/**
	 * 下个小时的起始时间，如现在15:28 ,则返回16:00
	 * @return
	 */
	public static String getNextHourBeginTime(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		DateFormat format = new SimpleDateFormat("HH:00");
		return format.format(cal.getTime());
	}
	
	/**
	 * 当前小时数，如现在15:28 ,则返回15
	 * @return
	 */
	public static int getCurrentHour(){
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.HOUR_OF_DAY);
	}
	
	public static int getCurrentMin(){
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.MINUTE);
	}
	
	/**
	 * 下一个小时数，如现在15:28 ,则返回16
	 * @return
	 */
	public static int getNextHour(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		return cal.get(Calendar.HOUR_OF_DAY);
	}
	
	public static String truncSingleTime(int hour, int min) {
		String hour_str = String.valueOf(hour);
		if (hour >= 0 && hour < 10 && String.valueOf(hour).length() == 1) {
			hour_str = "0" + hour;
		}
		String min_str = String.valueOf(min);
		if (min >= 0 && min < 10 && String.valueOf(min).length() == 1) {
			min_str = "0" + min;
		}

		return hour_str + ":" + min_str;
	}
	
	public static String truncSingleDate(int year, int monthOfYear, int dayOfMonth){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date d = null;
		try {
			d = format.parse(year + "-" + monthOfYear + "-" + dayOfMonth);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return format.format(d);
	}
	
	/**
	 * 获取当前日期，如：2014-05-08
	 * @return
	 */
	public static String getCurrentDate(){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(new Date());
	}
	
	public static int getCurrentYear(){
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.YEAR);
	}
	
	public static int getCurrentMonthOfYear(){
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.MONTH);
	}
	
	public static int getCurrentDayOfMonth(){
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * 把数字的星期转成中文
	 * @param week
	 * @return
	 */
	public static String getWeekNameByNumber(int week){
		switch (week) {
		case 1:
			return "一";
		case 2:
			return "二";
		case 3:
			return "三";
		case 4:
			return "四";
		case 5:
			return "五";
		case 6:
			return "六";
		case 7:
			return "日";
		default:
			return "";
		}
	}
	
	/**
	 * add since v0.0.9.2
	 * @return
	 */
	public static List<String> getHourList(){
		List<String> reList = new ArrayList<String>();
		for (int i = 0; i <= 24; i++) {
			reList.add(i < 10 ? "0" + i : "" + i);
		}
		return reList;
	}
	
	/**
	 * add since v0.0.9.2
	 * @return
	 */
	public static List<String> getMinList(){
		List<String> reList = new ArrayList<String>();
		for (int i = 0; i <= 60; i+=5) {
			reList.add(i < 10 ? "0" + i : "" + i );
		}
		return reList;
	}
	
	/**
	 * add since v0.0.9.2
	 * 当前分钟数位于总分钟数的哪一个位置， 给spinner定位初始数据使用
	 * @param minutes
	 * @param current_min
	 * @return
	 */
	public static int getPositionByMin(List<String> minutes, int current_min){
		for (int i = 0; i < minutes.size(); i++) {
			if(current_min == Integer.parseInt(minutes.get(i))) return i;
		}
		return 1;
	}
	
	/**
	 * 根据提供的毫秒数， 转成 具体的时长，如： 5天10小时10分钟； 10小时10分钟； 10分钟
	 * @param millis
	 * @return
	 */
	public static String truncTimeByMillis(long millis){
		if(millis == 0) return "0分钟";
		long minutes = millis / (60 * 1000);
		if(minutes < 60) return minutes + "分钟";//xx分钟
		if(minutes >= 60 && minutes < (24 * 60)) { //xx小时xx分钟
			int hour = (int)minutes / 60;//小时
			int _minutes = (int)(minutes - (hour * 60));//剩余分钟数
			if(_minutes == 0){
				return hour + "小时";
			}
			return hour + "小时 " + _minutes + "分钟";
		}
		
		if(minutes >= (24 * 60)){//xx天xx小时xx分钟
			int day = (int)minutes / (24 * 60);//天
			int hourMin = (int)(minutes - day * (24 * 60));//剩余的小时分钟数
			int hour = hourMin / 60;//小时
			int _minutes = hourMin - (hour * 60);//剩余分钟数
			
			if(_minutes == 0 && hour == 0) return day + "天";
			if(_minutes == 0) return day + "天 " + hour + "小时";
			return day + "天 " + hour + "小时 " + _minutes + "分钟";
		}
		
		return "";
	}
	
}
