package com.santander.efx.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class DateUtils {

	private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss:SSS";

	public Date formatDate(String strDate) {
		try {
			return new SimpleDateFormat(DATE_FORMAT).parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
