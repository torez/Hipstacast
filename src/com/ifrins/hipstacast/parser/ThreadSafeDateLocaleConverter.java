package com.ifrins.hipstacast.parser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.sjl.dsl4xml.XmlReadingException;
import com.sjl.dsl4xml.support.Converter;

public class ThreadSafeDateLocaleConverter implements Converter<Date> {
	private ThreadLocal<DateFormat> dateFormat;
	
	public ThreadSafeDateLocaleConverter(final String aDateFormatPattern) {
		dateFormat = new ThreadLocal<DateFormat>() {
			protected DateFormat initialValue() {
				return new SimpleDateFormat(aDateFormatPattern, Locale.ENGLISH);
			}
		};
	}

	public Date convert(String aValue) {
		try {
			return ((aValue == null) || ("".equals(aValue))) ? null : dateFormat.get().parse(aValue);
		} catch (ParseException anExc) {
			throw new XmlReadingException(anExc);
		}
	}

	public boolean canConvertTo(Class<?> aClass) {
		return aClass.isAssignableFrom(Date.class);
	}

}
