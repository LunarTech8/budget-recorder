package com.romanbrunner.apps.budgetrecorder;

import java.lang.Exception;

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

	public static final String[] TYPE_NAMES = { "Food/Grooming", "Media", "Electronics", "Clothing", "Housing", "Amusement", "Locomotion", "Income" };
	public static final String[][] SUBTYPE_NAMES =
	{
		// Food/Grooming:
		{ "Supermarket", "Bakery", "Restaurant", "Bar", "Snack stand", "Barber", "Medical", "Fitness" },
		// Media:
		{ "Book", "Movie", "Video game", "Board game", "Education" },
		// Electronics:
		{ "Desktop", "Mobile", "Entertainment", "Cleaning", "Generic" },
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

	private static int dataRowTypeCount = 0;  // This is only needed/used for the DataRowType construction check
	private String[] dataRows;

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
				int i = 0;
				for (var dataRowType : DataRowType.values())
				{
					jsonGenerator.writeStringField(dataRowType.toString(), obj.dataRows[i++]);
				}
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

		@Override
		public DataEntry deserialize(JsonParser parser, DeserializationContext deserializer)
		{
			try
			{
				final var codec = parser.getCodec();
				final JsonNode node = codec.readTree(parser);

				// Extract data row values:
				String[] dataRows = new String[DATA_ROW_TYPE_COUNT];
				int i = 0;
				for (var dataRowType : DataRowType.values())
				{
					dataRows[i++] = node.get(dataRowType.toString()).textValue();
				}
				// Convert extracted values into new data entry:
				return new DataEntry(dataRows);
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
				return null;
			}
		}
	}

	public DataEntry(String[] dataRows) throws Exception
	{
		if (dataRows.length != DATA_ROW_TYPE_COUNT)
		{
			throw new Exception("ERROR: Invalid number of data rows (" + dataRows.length + " instead of " + DATA_ROW_TYPE_COUNT + ")");
		}
		this.dataRows = dataRows;
	}

	public String getDataRow(DataRowType dataRowType)
	{
		return dataRows[dataRowType.toInt()];
	}
}