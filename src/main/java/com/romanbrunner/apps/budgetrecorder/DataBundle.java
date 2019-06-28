package com.romanbrunner.apps.budgetrecorder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

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

	public static final int DATA_ROW_TYPE_COUNT = DataRowType.values().length;

	public enum DataRowType
	{
		MONEY("Money", 0), ENTRIES("Entries", 1), START("Start", 2), END("End", 3);

		private final String name;
		private final int index;

		private static class InitChecker
		{
			private static int counter = 0;
		}

		private DataRowType(String name, int index)
		{
			InitChecker.counter += 1;
			if (index < 0 || index >= InitChecker.counter)
			{
				System.out.println("ERROR: Invalid index for " + name + " (" + index + " has to be at least 0 and smaller than " + InitChecker.counter + ")");
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
	private float money;
	private int entries;
	private int[] start;
	private int[] end;

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
				var dataRowType = DataRowType.values();
				int i = 0;
				jsonGenerator.writeNumberField(dataRowType[i++].toString(), obj.money);
				jsonGenerator.writeNumberField(dataRowType[i++].toString(), obj.entries);
				jsonGenerator.writeArrayFieldStart(dataRowType[i++].toString());
				for (int j = 0; j < DATE_ARRAY_SIZE; j++)
				{
					jsonGenerator.writeNumber(obj.start[j]);
				}
				jsonGenerator.writeArrayFieldStart(dataRowType[i++].toString());
				for (int j = 0; j < DATE_ARRAY_SIZE; j++)
				{
					jsonGenerator.writeNumber(obj.end[j]);
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
				var dataRowType = DataRowType.values();
				int i = 0;
				float money = node.get(dataRowType[i++].toString()).floatValue();
				int entries = node.get(dataRowType[i++].toString()).intValue();
				int[] start = arrayNodeToIntArray(node.get(dataRowType[i++].toString()), DATE_ARRAY_SIZE);
				int[] end = arrayNodeToIntArray(node.get(dataRowType[i++].toString()), DATE_ARRAY_SIZE);

				// Convert extracted values into new data entry:
				return new DataBundle(money, entries, start, end);
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
						return (entryA.start[0] - entryB.start[0]) + (entryA.start[1] - entryB.start[1]) * 100 + (entryA.start[2] - entryB.start[2]) * 10000;
					case END:
						return (entryA.end[0] - entryB.end[0]) + (entryA.end[1] - entryB.end[1]) * 100 + (entryA.end[2] - entryB.end[2]) * 10000;
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

	public DataBundle(float money, int entries, int[] start, int[] end) throws Exception
	{
		if (start.length != DATE_ARRAY_SIZE)
		{
			throw new Exception("ERROR: Invalid start format (" + start.length + " numbers instead of " + DATE_ARRAY_SIZE + ")");
		}
		else if (end.length != DATE_ARRAY_SIZE)
		{
			throw new Exception("ERROR: Invalid end format (" + end.length + " numbers instead of " + DATE_ARRAY_SIZE + ")");
		}

		this.money = money;
		this.entries = entries;
		this.start = start;
		this.end = end;
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
				return Arrays.stream(start).mapToObj(String::valueOf).collect(Collectors.joining("."));
			case END:
				return Arrays.stream(end).mapToObj(String::valueOf).collect(Collectors.joining("."));
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
				String dateText = "";
				int i = 0;
				if (start[i] <= 9)
				{
					dateText = dateText + "0";
				}
				dateText = dateText + Integer.toString(start[i]) + ".";
				i++;
				if (start[i] <= 9)
				{
					dateText = dateText + "0";
				}
				dateText = dateText + Integer.toString(start[i]) + ".";
				i++;
				return dateText + Integer.toString(start[i]);
			}
			case END:
			{
				String dateText = "";
				int i = 0;
				if (end[i] <= 9)
				{
					dateText = dateText + "0";
				}
				dateText = dateText + Integer.toString(end[i]) + ".";
				i++;
				if (end[i] <= 9)
				{
					dateText = dateText + "0";
				}
				dateText = dateText + Integer.toString(end[i]) + ".";
				i++;
				return dateText + Integer.toString(end[i]);
			}
			default:
				throw new Exception("ERROR: Invalid data row type (" + dataRowType.toString() + ")");
		}
	}

}