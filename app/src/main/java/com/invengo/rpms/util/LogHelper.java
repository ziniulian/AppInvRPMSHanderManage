package com.invengo.rpms.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.os.StatFs;

public class LogHelper {

	// 记录日志
	public static Boolean WriteLog(String mess) {
		try {

			if (UtilityHelper.checkSDCard() != 0)
				return false;

			String filePath = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/TempTag/log/";
			File fPath = new File(filePath);
			if (!fPath.exists()) {
				fPath.mkdirs();
			}

			SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
			Date d = new Date();
			String fileName = f.format(d) + "log.txt";
			File file = new File(filePath + fileName);
			if (!file.exists()) {
				file.createNewFile();
			}

			// true是说是否以append方式添加内容
			OutputStreamWriter stream = new OutputStreamWriter(
					new FileOutputStream(file, true), Charset.forName("gbk"));
			BufferedWriter bw = new BufferedWriter(stream);

			SimpleDateFormat fTime = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			String info = f.format(d) + "--" + mess;
			bw.write(info);
			bw.newLine();
			bw.flush();
			stream.close();
			bw.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}

