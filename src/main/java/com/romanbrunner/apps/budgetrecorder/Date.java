package com.romanbrunner.apps.budgetrecorder;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.stream.Collectors;


public class Date
{
	// --------------------
	// Data code
	// --------------------

	public static final int ARRAY_SIZE = 3;
	public static final Date CURRENT_DATE;
	static
	{
		try
		{
			CURRENT_DATE = calendarToDate(Calendar.getInstance());
		}
		catch (final Exception exception)
		{
			throw new Error(exception);
		}
	}

	public enum Interval
	{
		NEVER("Never", 0), DAILY("Daily", 1), WEEKLY("Weekly", 2), MONTHLY("Monthly", 3), YEARLY("Yearly", 4);

		private final String name;
		private final int index;

		public static class Data
		{
			public static int length = 0;
			public static Interval[] values = {};
		}

		private Interval(String name, int index)
		{
			Data.length += 1;
			if (index < 0 || index >= Data.length)
			{
				System.out.println("ERROR: Invalid index for " + name + " (" + index + " has to be at least 0 and smaller than " + Data.length + ")");
			}
			Data.values = Arrays.copyOf(Data.values, Data.length);
			Data.values[Data.length - 1] = this;

			this.name = name;
			this.index = index;
		}

		public static Interval byIndex(int index)
		{
			if (index < 0 || index >= Data.length)
			{
				System.out.println("ERROR: Invalid index (" + index + " has to be at least 0 and smaller than " + Data.length + ")");
			}

			return Data.values[index];
		}

		public static String[] getNames()
		{
			var names = new String[Data.length];
			for (int i = 0; i < Data.length; i++)
			{
				names[i] = Data.values[i].toString();
			}
			return names;
		}

		private static Calendar getIntervalNextDateCalendar(Calendar calendar, Interval interval)
		{
			switch (interval)
			{
				case NEVER:
				case DAILY:
					calendar.add(Calendar.DAY_OF_YEAR, 1);
					break;
				case WEEKLY:
					calendar.add(Calendar.WEEK_OF_MONTH, 1);
					break;
				case MONTHLY:
					calendar.add(Calendar.MONTH, 1);
					break;
				case YEARLY:
					calendar.add(Calendar.YEAR, 1);
					break;
			}
			return calendar;
		}

		// Returns the start date from the timeframe of given interval and date:
		public static Date getIntervalStart(Date date, Interval interval) throws Exception
		{
			Calendar calendar = Date.dateToCalendar(date);
			switch (interval)
			{
				case WEEKLY:
					calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					break;
				case MONTHLY:
					calendar.set(Calendar.DAY_OF_MONTH, 1);
					break;
				case YEARLY:
					calendar.set(Calendar.DAY_OF_YEAR, 1);
					break;
				default:
					break;
			}
			return Date.calendarToDate(calendar);
		}

		// Returns the end date from the timeframe of given interval and date:
		public static Date getIntervalEnd(Date date, Interval interval) throws Exception
		{
			Calendar calendar = getIntervalNextDateCalendar(Date.dateToCalendar(getIntervalStart(date, interval)), interval);
			calendar.add(Calendar.DAY_OF_YEAR, -1);
			return Date.calendarToDate(calendar);
		}

		// Returns the next date with given interval after given date:
		public static Date getIntervalNextDate(Date date, Interval interval) throws Exception
		{
			return Date.calendarToDate(getIntervalNextDateCalendar(Date.dateToCalendar(date), interval));
		}

		@Override
		public String toString()
		{
			return name;
		}

		public int toInt()
		{
			return index;
		}
	}

	// --------------------
	// Functional code
	// --------------------

	private final int[] values;  // Values should stay fixed after construction. Use new objects if other values are required.

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
	public Date(int day, int month, int year) throws Exception
	{
		this(new int[] { day, month, year });
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

	public Date getNextDay() throws Exception
	{
		Calendar calendar = Date.dateToCalendar(this);
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		return Date.calendarToDate(calendar);
	}

	public int getValue(int index) throws Exception
	{
		if (index < 0 || index >= ARRAY_SIZE)
		{
			throw new Exception("ERROR: Invalid index (" + index + " has to be at least 0 and smaller than " + ARRAY_SIZE + ")");
		}

		return values[index];
	}

	public int[] getValues()
	{
		return values;
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
			throw new Exception("ERROR: clone() shouldn't be required because date values are final. Thus you can savely use the same object multiple times.");
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