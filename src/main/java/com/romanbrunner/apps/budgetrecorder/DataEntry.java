package com.romanbrunner.apps.budgetrecorder;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
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
	public static final String[] REPEAT_NAMES = { "Never", "Daily", "Weekly", "Monthly", "Yearly" };
	public static final int DATA_ROW_TYPE_COUNT = DataRowType.values().length;

	public enum DataRowType
	{
		MONEY("Money", 0), NAME("Name", 1), LOCATION("Location", 2), TYPE("Type", 3), SUBTYPE("Subtype", 4), DATE("Date", 5), REPEAT("Repeat", 6), DURATION("Duration", 7), UNTIL("Until", 8);

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

	public static Calendar getCalendarStart(int[] date, int view) throws Exception
	{
		Calendar calendar = new GregorianCalendar(date[2], date[1], date[0]);
		switch (view)
		{
			case 1:  // Daily
				break;
			case 2:  // Weekly
				calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);  // TODO: test, doesn't seem to work as intended
				break;
			case 3:  // Monthly
				calendar.set(Calendar.DAY_OF_MONTH, 1);  // TODO: test
				break;
			case 4:  // Yearly
				calendar.set(Calendar.DAY_OF_YEAR, 1);  // TODO: test
				break;
			default:
				throw new Exception("ERROR: Invalid view selection");
		}
		return calendar;
	}

	public static int[] getEndDate(Calendar calendarStart, int view) throws Exception
	{
		switch (view)
		{
			case 1:  // Daily
				break;
			case 2:  // Weekly
				calendarStart.add(Calendar.WEEK_OF_MONTH, 1);
				break;
			case 3:  // Monthly
					calendarStart.add(Calendar.MONTH, 1);
				break;
			case 4:  // Yearly
				calendarStart.add(Calendar.YEAR, 1);
				break;
			default:
				throw new Exception("ERROR: Invalid view selection");
		}
		int[] end = {calendarStart.get(Calendar.DAY_OF_MONTH), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.YEAR)};
		return end;
	}

	// --------------------
	// Functional code
	// --------------------

	private static final int DATE_ARRAY_SIZE = 3;
	private float money;
	private String name;
	private String location;
	private int type;
	private int subtype;
	private int[] date;
	private int repeat;
	private boolean duration;
	private int[] until;

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
				jsonGenerator.writeNumberField(dataRowType[i++].toString(), obj.repeat);
				if (obj.repeat != 0)  // 0 = "Never"
				{
					jsonGenerator.writeBooleanField(dataRowType[i++].toString(), obj.duration);
					if (obj.duration != true)  // true = "Infinitely"
					{
						jsonGenerator.writeArrayFieldStart(dataRowType[i++].toString());
						for (int j = 0; j < DATE_ARRAY_SIZE; j++)
						{
							jsonGenerator.writeNumber(obj.until[j]);
						}
						jsonGenerator.writeEndArray();
					}
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
				JsonNode iNode;
				float money = node.get(dataRowType[i++].toString()).floatValue();
				String name = node.get(dataRowType[i++].toString()).textValue();
				String location = node.get(dataRowType[i++].toString()).textValue();
				int type = node.get(dataRowType[i++].toString()).intValue();
				int subtype = node.get(dataRowType[i++].toString()).intValue();
				int[] date = arrayNodeToIntArray(node.get(dataRowType[i++].toString()), DATE_ARRAY_SIZE);
				int repeat = node.get(dataRowType[i++].toString()).intValue();
				boolean duration = true;  // Default value
				iNode = node.get(dataRowType[i++].toString());
				if (iNode != null)
				{
					duration = iNode.booleanValue();
				}
				int[] until = new int[]{1, 1, 0};  // Default value
				iNode = node.get(dataRowType[i++].toString());
				if (iNode != null)
				{
					until = arrayNodeToIntArray(iNode, DATE_ARRAY_SIZE);
				}

				// Convert extracted values into new data entry:
				return new DataEntry(money, name, location, type, subtype, date, repeat, duration, until);
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
						return entryA.repeat - entryB.repeat;
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
					case UNTIL:
						return (entryA.until[0] - entryB.until[0]) + (entryA.until[1] - entryB.until[1]) * 100 + (entryA.until[2] - entryB.until[2]) * 10000;
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

	public DataEntry(float money, String name, String location, int type, int subtype, int[] date, int repeat, boolean duration, int[] until) throws Exception
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
		this.duration = duration;
		this.until = until;
	}

	public DataBundle newDataBundle(int view) throws Exception
	{
		return new DataBundle(money, 1, date, getEndDate(getCalendarStart(date, view), view));
	}

	public DataBundle addToDataBundle(DataBundle dataBundle, int view) throws Exception
	{
		if (dataBundle.isInTimeframe(date))
		{
			return dataBundle.addEntry(money);
		}
		else
		{
			return new DataBundle(money, 1, date, getEndDate(dataBundle.getNextCalendarStart(), view));
		}
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
				return Integer.toString(repeat);
			case DURATION:
				return Boolean.toString(duration);
			case UNTIL:
				return Arrays.stream(until).mapToObj(String::valueOf).collect(Collectors.joining("."));
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
				return REPEAT_NAMES[repeat];
			}
			case DURATION:
			{
				if (repeat == 0)  // 0 = "Never"
				{
					return "-";
				}
				else
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
			}
			case UNTIL:
			{
				if (repeat == 0 || duration)  // 0 = "Never" / true = "Infinitely"
				{
					return "-";
				}
				else
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
			}
			default:
				throw new Exception("ERROR: Invalid data row type (" + dataRowType.toString() + ")");
		}
	}

}