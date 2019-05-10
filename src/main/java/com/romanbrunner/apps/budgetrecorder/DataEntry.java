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
		MONEY("Money", 0), NAME("Name", 1), LOCATION("Location", 2), TYPE("Type", 3), SUBTYPE("Subtype", 4), DATE("Date", 5), REPEAT("Repeat", 6), UNTIL("Until", 7), DURATION("Duration", 8);

		private final String name;
		private final int index;

		private DataRowType(String name, int index)
		{
			// TODO: check why this throws an error
			// dataRowTypeCount += 1;
			// if (index < 0 || index >= dataRowTypeCount)
			// {
			// 	System.out.println("ERROR: Invalid index for " + name + " (" + index + " has to be at least 0 and smaller than " + dataRowTypeCount + ")");
			// }

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
	private int[] until;
	private boolean duration;

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
				for (int j = 0; j < DATE_ARRAY_SIZE; j++)
				{
					jsonGenerator.writeNumber(obj.date[j]);
				}
				jsonGenerator.writeEndArray();
				jsonGenerator.writeBooleanField(dataRowType[i++].toString(), obj.repeat);
				jsonGenerator.writeArrayFieldStart(dataRowType[i++].toString());
				for (int j = 0; j < DATE_ARRAY_SIZE; j++)
				{
					jsonGenerator.writeNumber(obj.until[j]);
				}
				jsonGenerator.writeEndArray();
				jsonGenerator.writeBooleanField(dataRowType[i++].toString(), obj.duration);

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
				int[] until = arrayNodeToIntArray(node.get(dataRowType[i++].toString()), DATE_ARRAY_SIZE);
				boolean duration = node.get(dataRowType[i++].toString()).booleanValue();

				// Convert extracted values into new data entry:
				return new DataEntry(money, name, location, type, subtype, date, repeat, until, duration);
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

	public static class DataComparator implements Comparator<DataEntry>
	{
		private DataRowSorting sorting;

		public DataComparator(DataRowSorting sorting)
		{
			this.sorting = sorting;
		}

		public int compare(DataEntry entryA, DataEntry entryB)
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
					case NAME:
						return entryA.name.compareTo(entryB.name);
					case LOCATION:
						return entryA.location.compareTo(entryB.location);
					case TYPE:
						return entryA.type - entryB.type;
					case SUBTYPE:
						return entryA.subtype - entryB.subtype;
					case DATE:
						return (entryA.date[0] - entryB.date[0]) + (entryA.date[1] - entryB.date[1]) * 100 + (entryA.date[2] - entryB.date[2]) * 10000;
					case REPEAT:
						if (entryA.repeat == entryB.repeat)
						{
							return 0;
						}
						else if (entryA.repeat == true)
						{
							return 1;
						}
						else
						{
							return -1;
						}
					case UNTIL:
						return (entryA.until[0] - entryB.until[0]) + (entryA.until[1] - entryB.until[1]) * 100 + (entryA.until[2] - entryB.until[2]) * 10000;
					case DURATION:
						if (entryA.duration == entryB.duration)
						{
							return 0;
						}
						else if (entryA.duration == true)
						{
							return 1;
						}
						else
						{
							return -1;
						}
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

	public DataEntry(float money, String name, String location, int type, int subtype, int[] date, boolean repeat, int[] until, boolean duration) throws Exception
	{
		if (date.length != DATE_ARRAY_SIZE)
		{
			throw new Exception("ERROR: Invalid date format (" + date.length + " numbers instead of " + DATE_ARRAY_SIZE + ")");
		}
		else if (until.length != DATE_ARRAY_SIZE)
		{
			throw new Exception("ERROR: Invalid until format (" + until.length + " numbers instead of " + DATE_ARRAY_SIZE + ")");
		}

		this.money = money;
		this.name = name;
		this.location = location;
		this.type = type;
		this.subtype = subtype;
		this.date = date;
		this.repeat = repeat;
		this.until = until;
		this.duration = duration;
	}

	public String getDataRowValueAsString(DataRowType dataRowType) throws Exception
	{
		switch (dataRowType)
		{
			case MONEY:
				return Float.toString(money);
			case NAME:
				return name;
			case LOCATION:
				return location;
			case TYPE:
				return Integer.toString(type);
			case SUBTYPE:
				return Integer.toString(subtype);
			case DATE:
				return Arrays.stream(date).mapToObj(String::valueOf).collect(Collectors.joining("."));
			case REPEAT:
				return Boolean.toString(repeat);
			case UNTIL:
				return Arrays.stream(until).mapToObj(String::valueOf).collect(Collectors.joining("."));
			case DURATION:
				return Boolean.toString(duration);
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
			case NAME:
				return name;
			case LOCATION:
				return location;
			case TYPE:
				return TYPE_NAMES[type];
			case SUBTYPE:
				return SUBTYPE_NAMES[type][subtype];
			case DATE:
			{
				String dateText = "";
				int i = 0;
				if (date[i] <= 9)
				{
					dateText = dateText + "0";
				}
				dateText = dateText + Integer.toString(date[i]) + ".";
				i++;
				if (date[i] <= 9)
				{
					dateText = dateText + "0";
				}
				dateText = dateText + Integer.toString(date[i]) + ".";
				i++;
				return dateText + Integer.toString(date[i]);
			}
			case REPEAT:
			{
				if (repeat)
				{
					return "Monthly";
				}
				else
				{
					return "Once";
				}
			}
			case UNTIL:
			{
				String untilText = "";
				int i = 0;
				if (until[i] <= 9)
				{
					untilText = untilText + "0";
				}
				untilText = untilText + Integer.toString(until[i]) + ".";
				i++;
				if (until[i] <= 9)
				{
					untilText = untilText + "0";
				}
				untilText = untilText + Integer.toString(until[i]) + ".";
				i++;
				return untilText + Integer.toString(until[i]);
			}
			case DURATION:
			{
				if (duration)
				{
					return "Infinitely";
				}
				else
				{
					return "Limited";
				}
			}
			default:
				throw new Exception("ERROR: Invalid data row type (" + dataRowType.toString() + ")");
		}
	}

}