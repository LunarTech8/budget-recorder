package com.romanbrunner.apps.budgetrecorder;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.stream.Collectors;


public class Date
{
	public static final int ARRAY_SIZE = 3;
	public static final int[] DEFAULT_DATE_VALUES = new int[]{ 1, 1, 1900 };
	public static final Date DEFAULT_DATE;
	static
	{
		try
		{
			DEFAULT_DATE = new Date(DEFAULT_DATE_VALUES);
		}
		catch (final Exception exception)
		{
			throw new Error(exception);
		}
	}

	private int[] values;

	public Date(int day, int month, int year) throws Exception
	{
		values = new int[ARRAY_SIZE];
		values[0] = day;
		values[1] = month;
		values[2] = year;

		var calendar = dateToCalendar(this);
		if (calendar.get(Calendar.DAY_OF_MONTH) != values[0] || calendar.get(Calendar.MONTH) + 1 != values[1] || calendar.get(Calendar.YEAR) != values[2])
		{
			throw new Exception("ERROR: Invalid date (" + this.toString() + " is not a valid date)");
		}
	}
	public Date(int[] values) throws Exception
	{
		if (values.length != ARRAY_SIZE)
		{
			throw new Exception("ERROR: Invalid date format (" + values.length + " numbers instead of " + ARRAY_SIZE + ")");
		}

		this.values = values;

		var calendar = dateToCalendar(this);
		if (calendar.get(Calendar.DAY_OF_MONTH) != values[0] || calendar.get(Calendar.MONTH) + 1 != values[1] || calendar.get(Calendar.YEAR) != values[2])
		{
			throw new Exception("ERROR: Invalid date (" + this.toString() + " is not a valid date)");
		}
	}

	public static Date calendarToDate(Calendar calendar) throws Exception
	{
		return new Date(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
	}

	public static Calendar dateToCalendar(Date date)
	{
		return new GregorianCalendar(date.values[2], date.values[1] - 1, date.values[0]);
	}

	public boolean isInTimeframe(Date start, Date end)
	{
		Calendar calendar = dateToCalendar(this);
		return (calendar.compareTo(dateToCalendar(start)) >= 0 && calendar.compareTo(dateToCalendar(end)) <= 0);
	}

	public int getValue(int index) throws Exception
	{
		if (index < 0 || index >= ARRAY_SIZE)
		{
			throw new Exception("ERROR: Invalid index (" + index + " has to be at least 0 and smaller than " + ARRAY_SIZE + ")");
		}

		return values[index];
	}

	public String getAsText()
	{
		String text = "";
		int i = 0;
		if (values[i] <= 9)
		{
			text = text + "0";
		}
		text = text + Integer.toString(values[i]) + ".";
		i++;
		if (values[i] <= 9)
		{
			text = text + "0";
		}
		text = text + Integer.toString(values[i]) + ".";
		i++;
		return text + Integer.toString(values[i]);
	}

	@Override
	public String toString()
	{
		return Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining("."));
	}

	@Override
	public Date clone()
	{
		try
		{
			return new Date(values);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			return null;
		}
	}

	public int compareTo(Date date) throws Exception
	{
		return (values[0] - date.values[0]) + (values[1] - date.values[1]) * 100 + (values[2] - date.values[2]) * 10000;
	}

}