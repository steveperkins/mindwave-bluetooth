package com.sperkins.mindwave;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	public static String currentDateAsString() {
		return dateFormat.format(new Date());
	}
}
