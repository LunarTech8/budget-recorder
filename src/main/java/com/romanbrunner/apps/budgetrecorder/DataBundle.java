package com.romanbrunner.apps.budgetrecorder;

import java.util.Arrays;
import java.util.Comparator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


@JsonSerialize(using = DataBundle.Serializer.class)
@JsonDeserialize(using = DataBundle.Deserializer.class)
class DataBundle
{
	// --------------------
	// Data code
	// --------------------

	public enum DataRowType
	{
		MONEY("Money", 0), ENTRIES("Entries", 1), START("Start", 2), END("End", 3);

		private final String name;
		private final int index;

		public static class Data
		{
			public static int length = 0;
			public static DataRowType[] values = {};
		}

		private DataRowType(String name, int index)
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

		public static DataRowType byIndex(int index)
		{
			if (index < 0 || index >= Data.length)
			{
				System.out.println("ERROR: Invalid index (" + index + " has to be at least 0 and smaller than " + Data.length + ")");
			}

			return Data.values[index];
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

	private float money;
	private int entries;
	private Date start;
	private Date end;

	/**
	 * @return the start
	 */
	public Date getStart()
    {
		return start;
	}

	/**
	 * @return the end
	 */
	public Date getEnd()
    {
		return end;
	}

	public static class Serializer extends StdSerializer<DataBundle>
	{
		private static final long serialVersionUID = 1L;

		public Serializer()
		{
			this(null);
		}
		public Serializer(Class<DataBundle> t)
		{
			super(t);
		}

		@Override
		public void serialize(DataBundle obj, JsonGenerator jsonGenerator, SerializerProvider serializer)
		{
			try
			{
				jsonGenerator.writeStartObject();

				// Store data rows:
				int i = 0;
				jsonGenerator.writeNumberField(DataRowType.byIndex(i++).toString(), obj.money);
				jsonGenerator.writeNumberField(DataRowType.byIndex(i++).toString(), obj.entries);
				jsonGenerator.writeArrayFieldStart(DataRowType.byIndex(i++).toString());
				for (int j = 0; j < Date.ARRAY_SIZE; j++)
				{
					jsonGenerator.writeNumber(obj.start.getValue(j));
				}
				jsonGenerator.writeArrayFieldStart(DataRowType.byIndex(i++).toString());
				for (int j = 0; j < Date.ARRAY_SIZE; j++)
				{
					jsonGenerator.writeNumber(obj.end.getValue(j));
				}
				jsonGenerator.writeEndArray();
				jsonGenerator.writeEndObject();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	public static class Deserializer extends StdDeserializer<DataBundle>
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
		public DataBundle deserialize(JsonParser parser, DeserializationContext deserializer)
		{
			try
			{
				final var codec = parser.getCodec();
				final JsonNode node = codec.readTree(parser);

				// Extract data row values:
				int i = 0;
				float money = node.get(DataRowType.byIndex(i++).toString()).floatValue();
				int entries = node.get(DataRowType.byIndex(i++).toString()).intValue();
				int[] start = arrayNodeToIntArray(node.get(DataRowType.byIndex(i++).toString()), Date.ARRAY_SIZE);
				int[] end = arrayNodeToIntArray(node.get(DataRowType.byIndex(i++).toString()), Date.ARRAY_SIZE);

				// Convert extracted values into new data entry:
				return new DataBundle(money, entries, new Date(start),new Date(end));
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
				return null;
			}
		}
	}

	public static class DataRowSorting  // Struct type
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

	public static class DataComparator implements Comparator<DataBundle>
	{
		private DataRowSorting sorting;

		public DataComparator(DataRowSorting sorting)
		{
			this.sorting = sorting;
		}

		public int compare(DataBundle entryA, DataBundle entryB)
		{
			try
			{
				switch (sorting.mode)
				{
					case UPWARD:
						break;
					case DOWNWARD:
						var entryTemp = entryA;
						entryA = entryB;
						entryB = entryTemp;
						break;
				}
				switch (sorting.row)
				{
					case MONEY:
						return Math.round((entryA.money - entryB.money) * 100F);
					case ENTRIES:
						return entryA.entries - entryB.entries;
					case START:
						return entryA.start.compareTo(entryB.start);
					case END:
						return entryA.end.compareTo(entryB.end);
					default:
						return entryA.getDataRowValueAsString(sorting.row).compareTo(entryB.getDataRowValueAsString(sorting.row));
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
			return 0;
		}
	}

	public DataBundle(float money, int entries, Date start, Date end)
	{
		this.money = money;
		this.entries = entries;
		this.start = start;
		this.end = end;
	}

	public void addEntry(float money)
	{
		this.money += money;
		entries += 1;
	}

	public boolean hasEntries()
	{
		return (entries > 0);
	}

	public String getDataRowValueAsString(DataRowType dataRowType) throws Exception
	{
		switch (dataRowType)
		{
			case MONEY:
				return Float.toString(money);
			case ENTRIES:
				return Integer.toString(entries);
			case START:
				return start.toString();
			case END:
				return end.toString();
			default:
				throw new Exception("ERROR: Invalid data row type (" + dataRowType.toString() + ")");
		}
	}

	public String getDataRowValueAsText(DataRowType dataRowType) throws Exception
	{
		switch (dataRowType)
		{
			case MONEY:
				return String.format("%.2f", money) + " €";
			case ENTRIES:
				return Integer.toString(entries);
			case START:
			{
				return start.getAsText();
			}
			case END:
			{
				return end.getAsText();
			}
			default:
				throw new Exception("ERROR: Invalid data row type (" + dataRowType.toString() + ")");
		}
	}

}