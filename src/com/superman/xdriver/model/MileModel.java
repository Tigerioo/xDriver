/**
 * 
 */
package com.superman.xdriver.model;

/**
 * <p>Title: com.superman.xdriver.model.MileModel.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime：2014-8-8 上午11:41:51
 */

public class MileModel {
	
	private long ID;
	private long START_TIME;
	private long END_TIME;
	private int START_MILE;
	private int END_MILE;
	private long CREATE_TIME;
	private double begin_latitude;
	private double begin_longitude;
	private double end_latitude;
	private double end_longitude;
	private String begin_address;
	private String end_address;
	
	public long getID() {
		return ID;
	}
	public void setID(long iD) {
		ID = iD;
	}
	public long getSTART_TIME() {
		return START_TIME;
	}
	public void setSTART_TIME(long sTART_TIME) {
		START_TIME = sTART_TIME;
	}
	public long getEND_TIME() {
		return END_TIME;
	}
	public void setEND_TIME(long eND_TIME) {
		END_TIME = eND_TIME;
	}
	public long getSTART_MILE() {
		return START_MILE;
	}
	public void setSTART_MILE(int sTART_MILE) {
		START_MILE = sTART_MILE;
	}
	public long getEND_MILE() {
		return END_MILE;
	}
	public void setEND_MILE(int eND_MILE) {
		END_MILE = eND_MILE;
	}
	public long getCREATE_TIME() {
		return CREATE_TIME;
	}
	public void setCREATE_TIME(long cREATE_TIME) {
		CREATE_TIME = cREATE_TIME;
	}
	public double getBegin_latitude() {
		return begin_latitude;
	}
	public void setBegin_latitude(double begin_latitude) {
		this.begin_latitude = begin_latitude;
	}
	public double getBegin_longitude() {
		return begin_longitude;
	}
	public void setBegin_longitude(double begin_longitude) {
		this.begin_longitude = begin_longitude;
	}
	public double getEnd_latitude() {
		return end_latitude;
	}
	public void setEnd_latitude(double end_latitude) {
		this.end_latitude = end_latitude;
	}
	public double getEnd_longitude() {
		return end_longitude;
	}
	public void setEnd_longitude(double end_longitude) {
		this.end_longitude = end_longitude;
	}
	public String getBegin_address() {
		return begin_address;
	}
	public void setBegin_address(String begin_address) {
		this.begin_address = begin_address;
	}
	public String getEnd_address() {
		return end_address;
	}
	public void setEnd_address(String end_address) {
		this.end_address = end_address;
	}
}
