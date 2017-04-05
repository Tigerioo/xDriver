package com.superman.xdriver.backup;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.superman.xdriver.manager.CommonManager;

/**
 * <p>Title: com.newland.test.WriteXml.java</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2001-2013 Newland SoftWare Company</p>
 *
 * <p>Company: Newland SoftWare Company</p>
 *
 * @author Lewis.Lynn
 *
 * @version 1.0 CreateTime 2014-03-31 09:55:32
 */

public class WriteXml {
	
	public void writeXml(String xml, String file_name, String currentPath){
		
		String savePath = CommonManager.findDefaultBackupPath();
		if(savePath == null){
			savePath = currentPath;
		}
		System.out.println("savePath ==== " + savePath);
		File savePathDir = new File(savePath);
		if(!savePathDir.exists()){
			savePathDir.mkdirs();
		}
		savePath = savePath + file_name;
		OutputStream os = null;
		BufferedOutputStream bos = null;
		try {
			File file = new File(savePath);
			os = new FileOutputStream(file);
			bos = new BufferedOutputStream(os);
			bos.write(xml.getBytes("UTF-8"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(bos != null){
					bos.close();
				}
				if(os != null){
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
