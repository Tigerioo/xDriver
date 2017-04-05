/**
 * 
 */
package com.superman.xdriver.model;

/**
 * <p>Title: com.superman.xdriver.model.OilModel.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-8-11 下午2:12:37
 */

public class OilModel {
	
	private long id;
	private int current_mile;//当前里程数
	private int left_mile;//剩余里程数
	private double price;//加油价格
	private long create_time;//加油时间
	private int mile;//本次加油跑的里程数
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getCurrent_mile() {
		return current_mile;
	}
	public void setCurrent_mile(int current_mile) {
		this.current_mile = current_mile;
	}
	public int getLeft_mile() {
		return left_mile;
	}
	public void setLeft_mile(int left_mile) {
		this.left_mile = left_mile;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}
	public int getMile() {
		return mile;
	}
	public void setMile(int mile) {
		this.mile = mile;
	}
	
}
