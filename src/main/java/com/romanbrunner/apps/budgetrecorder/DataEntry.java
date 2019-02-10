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

	public static final String[] TYPE_NAMES = { "Food/Grooming", "Clothing", "Electronics", "Media", "Housing", "Amusement", "Vacation", "Locomotion", "Education" };
	public static final int DATA_ROW_COUNT = DataRow.values().length;

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
				for (var dataRow : DataRow.values())
				{
					jsonGenerator.writeStringField(dataRow.toString(), obj.dataRows[i++]);
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
				String[] dataRows = new String[DATA_ROW_COUNT];
				int i = 0;
				for (var dataRow : DataRow.values())
				{
					dataRows[i++] = node.get(dataRow.toString()).textValue();
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
		if (dataRows.length != DATA_ROW_COUNT)
		{
			throw new Exception("Invalid number of data rows (" + dataRows.length + " instead of " + DATA_ROW_COUNT + ")");
		}
		this.dataRows = dataRows;
	}
}