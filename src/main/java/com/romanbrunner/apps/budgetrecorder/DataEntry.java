package com.romanbrunner.apps.budgetrecorder;

import java.lang.Exception;


class DataEntry
{
	// --------------------
	// Data code
	// --------------------
		
	public static final String[] typeNames = { "Food/Grooming", "Clothing", "Electronics", "Media", "Housing", "Amusement", "Vacation", "Locomotion", "Education" };
	public static final int dataRowCount = DataRow.values().length;

	public enum DataRow
	{
		MONEY("Money"), NAME("Name"), LOCATION("Location"), TYPE("Type"), DATE("Date"), REPEAT("Repeat");

		private final String val;

		private DataRow(String val) 
		{
			this.val = val;
		}

		@Override
		public String toString() 
		{
			return val;
		}
	}


	// --------------------
	// Functional code
	// --------------------

	private String[] dataRows = new String[dataRowCount];

	public DataEntry(String... dataRows) throws Exception
	{
		if (dataRows.length != dataRowCount)
		{
			throw new Exception("Invalid number of data rows (" + dataRows.length + " instead of " + dataRowCount + ")");
		}

		int i = 0;
		for (var data : dataRows) 
		{
			this.dataRows[i++] = data;
		}
	}
}