package com.romanbrunner.apps.budgetrecorder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


@JsonSerialize(using = DataEntry.Serializer.class)
@JsonDeserialize(using = DataEntry.Deserializer.class)
class DataEntry
{
	// --------------------
	// Data code
	// --------------------

	public static final String[] TYPE_NAMES = { "Food/Grooming", "Media", "Hardeware", "Clothing", "Housing", "Amusement", "Locomotion", "Income" };
	public static final String[][] SUBTYPE_NAMES =
	{
		// Food/Grooming:
		{ "Supermarket", "Bakery", "Restaurant", "Bar", "Snack stand", "Barber", "Medical", "Fitness" },
		// Media:
		{ "Book", "Movie", "Video game", "Board game", "Education" },
		// Hardware:
		{ "Electronics", "Cleaning", "Tool", "Gardening", "Vehicle", "Generic" },
		// Clothing:
		{ "Work", "Sport", "Leisure", "Generic" },
		// Housing:
		{ "Rent", "Incidental costs", "Electricity", "Insurance", "Internet" },
		// Amusement:
		{ "Event", "Vacation" },
		// Locomotion:
		{ "Commute", "Train", "Bus", "Plane", "Car" },
		// Income:
		{ "Profession", "Job", "Gift", "Sale", "Generic" },
	};
	public static final int DATA_ROW_TYPE_COUNT = DataRowType.values().length;

	public enum DataRowType
	{
		MONEY("Money", 0), NAME("Name", 1), LOCATION("Location", 2), TYPE("Type", 3), SUBTYPE("Subtype", 4), DATE("Date", 5), REPEAT("Repeat", 6);

		private final String name;
		private final int index;


		private DataRowType(String name, int index)
		{
			dataRowTypeCount += 1;
			if (index < 0 || index >= dataRowTypeCount)
			{
				System.out.println("ERROR: Invalid index for " + name + " (" + index + " has to be at least 0 and smaller than " + dataRowTypeCount + ")");
			}

			this.name = name;
			this.index = index;
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

	private static final int DATE_ARRAY_SIZE = 3;
	private static int dataRowTypeCount = 0;  // This is only needed/used for the DataRowType construction check
	private float money;
	private String name;
	private String location;
	private int type;
	private int subtype;
	private int[] date;
	private boolean repeat;

	public static class Serializer extends StdSerializer<DataEntry>
	{
		private static final long serialVersionUID = 1L;

		public Serializer()
		{
			this(null);
		}
		public Serializer(Class<DataEntry> t)
		{
			super(t);
		}

		@Override
		public void serialize(DataEntry obj, JsonGenerator jsonGenerator, SerializerProvider serializer)
		{
			try
			{
				jsonGenerator.writeStartObject();

				// Store data rows:
				var dataRowType = DataRowType.values();
				int i = 0;
				jsonGenerator.writeNumberField(dataRowType[i++].toString(), obj.money);
				jsonGenerator.writeStringField(dataRowType[i++].toString(), obj.name);
				jsonGenerator.writeStringField(dataRowType[i++].toString(), obj.location);
				jsonGenerator.writeNumberField(dataRowType[i++].toString(), obj.type);
				jsonGenerator.writeNumberField(dataRowType[i++].toString(), obj.subtype);
				jsonGenerator.writeArrayFieldStart(dataRowType[i++].toString());
				jsonGenerator.writeArray(obj.date, 0, DATE_ARRAY_SIZE);
				jsonGenerator.writeEndArray();
				jsonGenerator.writeBooleanField(dataRowType[i++].toString(), obj.repeat);

				jsonGenerator.writeEndObject();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	public static class Deserializer extends StdDeserializer<DataEntry>
	{
		private static final long serialVersionUID = 1L;

		public Deserializer()
		{
			this(null);
		}
		public Deserializer(Class<?> vc)
		{
			super(vc);
		}

		private int[] arrayNodeToIntArray(JsonNode node, int arraySize) throws Exception
		{
			var iterator = node.elements();
			var intArray = new int[arraySize];
			int i = 0;
			while (iterator.hasNext())
			{
				intArray[i++] = iterator.next().intValue();
			}

			if (i != arraySize)
			{
				throw new Exception("ERROR: Given array size (" + arraySize + ") does not fit to given node (" + i + ")");
			}

			return intArray;
		}

		@Override
		public DataEntry deserialize(JsonParser parser, DeserializationContext deserializer)
		{
			try
			{
				final var codec = parser.getCodec();
				final JsonNode node = codec.readTree(parser);

				// Extract data row values:
				var dataRowType = DataRowType.values();
				int i = 0;
				float money = node.get(dataRowType[i++].toString()).floatValue();
				String name = node.get(dataRowType[i++].toString()).textValue();
				String location = node.get(dataRowType[i++].toString()).textValue();
				int type = node.get(dataRowType[i++].toString()).intValue();
				int subtype = node.get(dataRowType[i++].toString()).intValue();
				int[] date = arrayNodeToIntArray(node.get(dataRowType[i++].toString()), DATE_ARRAY_SIZE);
				boolean repeat = node.get(dataRowType[i++].toString()).booleanValue();

				// Convert extracted values into new data entry:
				return new DataEntry(money, name, location, type, subtype, date, repeat);
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
				return null;
			}
		}
	}

	public static class DataRowSorting
	{
		public DataRowType row;
		public Mode mode;

		public enum Mode
		{
			UPWARD, DOWNWARD
		}

		public DataRowSorting(DataRowType row, Mode mode)
		{
			this.row = row;
			this.mode = mode;
		}
	}

	public static class DataComparator implements Comparator<DataEntry>
	{
		private DataRowSorting sorting;

		public DataComparator(DataRowSorting sorting)
		{
			this.sorting = sorting;
		}

		public int compare(DataEntry entryA, DataEntry entryB)
		{
			switch (sorting.row)
			{
				case MONEY:
					var stringA = entryA.getDataRow(sorting.row);
					var stringB = entryB.getDataRow(sorting.row);
					return Math.round(Float.parseFloat(stringA.substring(0, stringA.length() - 2)) - Float.parseFloat(stringB.substring(0, stringB.length() - 2)));
					// DEBUG: doesn't work yet because numbers use comma instead of point
					// DEBUG: will become obsolet/fixed with different types
				// case TYPE:
				// 	return Character.getNumericValue(entryA.getDataRow(filterRow).charAt(0)) - Character.getNumericValue(entryB.getDataRow(filterRow).charAt(0));
				// case SUBTYPE:
				// 	return Character.getNumericValue(entryA.getDataRow(filterRow).charAt(0)) - Character.getNumericValue(entryB.getDataRow(filterRow).charAt(0));
				// case DATE:
				// 	return Character.getNumericValue(entryA.getDataRow(filterRow).charAt(0)) - Character.getNumericValue(entryB.getDataRow(filterRow).charAt(0));
				// case REPEAT:
				// 	return Character.getNumericValue(entryA.getDataRow(filterRow).charAt(0)) - Character.getNumericValue(entryB.getDataRow(filterRow).charAt(0));
				default:
					switch (sorting.mode)
					{
						case UPWARD:
							return entryA.getDataRow(sorting.row).compareTo(entryB.getDataRow(sorting.row));
						case DOWNWARD:
							return entryB.getDataRow(sorting.row).compareTo(entryA.getDataRow(sorting.row));
					}
			}
			return 0;
		}
	}

	public DataEntry(float money, String name, String location, int type, int subtype, int[] date, boolean repeat) throws Exception
	{
		if (date.length != DATE_ARRAY_SIZE)
		{
			throw new Exception("ERROR: Invalid date format (" + date.length + " numbers instead of " + DATE_ARRAY_SIZE + ")");
		}

		this.money = money;
		this.name = name;
		this.location = location;
		this.type = type;
		this.subtype = subtype;
		this.date = date;
		this.repeat = repeat;
	}

	public String getDataRow(DataRowType dataRowType)
	{
		return dataRows[dataRowType.toInt()];
	}
}