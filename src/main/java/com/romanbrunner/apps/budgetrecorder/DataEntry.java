package com.romanbrunner.apps.budgetrecorder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.romanbrunner.apps.budgetrecorder.Date.Interval;


@JsonSerialize(using = DataEntry.Serializer.class)
@JsonDeserialize(using = DataEntry.Deserializer.class)
class DataEntry
{
	// --------------------
	// Data code
	// --------------------

	public static final float DEFAULT_VALUE_MONEY = 0.F;
	public static final int DEFAULT_VALUE_TYPE = 0;
	public static final int DEFAULT_VALUE_SUBTYPE = 0;
	public static final String DEFAULT_VALUE_NAME = "";
	public static final String DEFAULT_VALUE_LOCATION = "";
	public static final Date DEFAULT_VALUE_DATE = Date.CURRENT_DATE;
	public static final Interval DEFAULT_VALUE_REPEAT = Interval.NEVER;
	public static final boolean DEFAULT_VALUE_DURATION = true;
	public static final Date DEFAULT_VALUE_UNTIL = Date.CURRENT_DATE;

	public static String[] TYPE_NAMES;  // Extracted of database.json in MainFrame.readDatabaseFile
	public static String[][] SUBTYPE_NAMES;  // Extracted of database.json in MainFrame.readDatabaseFile
	public static Boolean[] IS_POSITIVE_BALANCE_TYPE;  // Extracted of database.json in MainFrame.readDatabaseFile
	public static final String DURATION_TEXT_ON = "Infinitely";
	public static final String DURATION_TEXT_OFF = "Limited";

	public enum DataRowType
	{
		MONEY("Money", 0), TYPE("Type", 1), SUBTYPE("Subtype", 2), NAME("Name", 3), LOCATION("Location", 4), DATE("Date", 5), REPEAT("Repeat", 6), DURATION("Duration", 7), UNTIL("Until", 8);

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
	private int type;
	private int subtype;
	private String name;
	private String location;
	private Date date;
	private Interval repeat;
	private boolean duration;
	private Date until;

	/**
	 * @return the money
	 */
	public Float getMoney()
    {
        return money;
	}

	/**
	 * @return the type
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @return the subtype
	 */
	public int getSubtype()
	{
		return subtype;
	}

	/**
	 * @return the name
	 */
	public String getName()
    {
        return name;
	}

	/**
	 * @return the location
	 */
	public String getLocation()
    {
        return location;
	}

	/**
	 * @return the date
	 */
	public Date getDate()
    {
        return date;
	}

	/**
	 * @return the repeat
	 */
	public Interval getRepeat()
    {
        return repeat;
	}

	/**
	 * @return the duration
	 */
	public boolean getDuration()
    {
        return duration;
	}

	/**
	 * @return the until
	 */
	public Date getUntil()
    {
        return until;
	}

	public static class Serializer extends StdSerializer<DataEntry>
	{
		private static final long serialVersionUID = 1L;

		public Serializer(Class<DataEntry> t)
		{
			super(t);
		}
		public Serializer()
		{
			this(null);
		}

		@Override
		public void serialize(DataEntry obj, JsonGenerator jsonGenerator, SerializerProvider serializer)
		{
			try
			{
				jsonGenerator.writeStartObject();

				// Store data rows:
				int i = 0;
				jsonGenerator.writeNumberField(DataRowType.byIndex(i++).toString(), obj.money);
				jsonGenerator.writeNumberField(DataRowType.byIndex(i++).toString(), obj.type);
				jsonGenerator.writeNumberField(DataRowType.byIndex(i++).toString(), obj.subtype);
				jsonGenerator.writeStringField(DataRowType.byIndex(i++).toString(), obj.name);
				jsonGenerator.writeStringField(DataRowType.byIndex(i++).toString(), obj.location);
				jsonGenerator.writeArrayFieldStart(DataRowType.byIndex(i++).toString());
				for (int j = 0; j < Date.ARRAY_SIZE; j++)
				{
					jsonGenerator.writeNumber(obj.date.getValue(j));
				}
				jsonGenerator.writeEndArray();
				jsonGenerator.writeNumberField(DataRowType.byIndex(i++).toString(), obj.repeat.toInt());
				if (obj.repeat != Interval.NEVER)
				{
					jsonGenerator.writeBooleanField(DataRowType.byIndex(i++).toString(), obj.duration);
					if (obj.duration != true)  // true = "Infinitely"
					{
						jsonGenerator.writeArrayFieldStart(DataRowType.byIndex(i++).toString());
						for (int j = 0; j < Date.ARRAY_SIZE; j++)
						{
							jsonGenerator.writeNumber(obj.until.getValue(j));
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

		public Deserializer(Class<?> vc)
		{
			super(vc);
		}
		public Deserializer()
		{
			this(null);
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
				int i = 0;
				JsonNode iNode;
				float money = node.get(DataRowType.byIndex(i++).toString()).floatValue();
				int type = node.get(DataRowType.byIndex(i++).toString()).intValue();
				int subtype = node.get(DataRowType.byIndex(i++).toString()).intValue();
				String name = node.get(DataRowType.byIndex(i++).toString()).textValue();
				String location = node.get(DataRowType.byIndex(i++).toString()).textValue();
				int[] date = arrayNodeToIntArray(node.get(DataRowType.byIndex(i++).toString()), Date.ARRAY_SIZE);
				int repeat = node.get(DataRowType.byIndex(i++).toString()).intValue();
				boolean duration = DEFAULT_VALUE_DURATION;
				iNode = node.get(DataRowType.byIndex(i++).toString());
				if (iNode != null)
				{
					duration = iNode.booleanValue();
				}
				int[] until = DEFAULT_VALUE_UNTIL.getValues();
				iNode = node.get(DataRowType.byIndex(i++).toString());
				if (iNode != null)
				{
					until = arrayNodeToIntArray(iNode, Date.ARRAY_SIZE);
				}

				// Convert extracted values into new data entry:
				return new DataEntry(money, type, subtype, name, location, new Date(date), Interval.byIndex(repeat), duration, new Date(until));
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
					case TYPE:
						return entryA.type - entryB.type;
					case SUBTYPE:
						return entryA.subtype - entryB.subtype;
					case NAME:
						return entryA.name.compareTo(entryB.name);
					case LOCATION:
						return entryA.location.compareTo(entryB.location);
					case DATE:
						return entryA.date.compareTo(entryB.date);
					case REPEAT:
						return entryA.repeat.compareTo(entryB.repeat);
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
						return entryA.until.compareTo(entryB.until);
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

	public DataEntry(float money, int type, int subtype, String name, String location, Date date, Interval repeat, boolean duration, Date until)
	{
		this.money = money;
		this.type = type;
		this.subtype = subtype;
		this.name = name;
		this.location = location;
		this.date = date;
		this.repeat = repeat;
		this.duration = duration;
		this.until = until;
	}
	public DataEntry(DataEntry origin, Date date, boolean noRepeat)
	{
		this(origin.money, origin.type, origin.subtype, origin.name, origin.location, date, origin.repeat, origin.duration, origin.until);
		if (noRepeat)
		{
			repeat = Interval.NEVER;
			duration = DEFAULT_VALUE_DURATION;
			until = DEFAULT_VALUE_UNTIL;
		}
	}

	public DataBundle createNewDataBundle(Interval interval) throws Exception
	{
		var start = Interval.getIntervalStart(date, interval);
		return new DataBundle(money, 1, start, Interval.getIntervalEnd(start, interval));
	}

	public DataBundle createNextDataBundle(DataBundle lastDataBundle, Interval interval) throws Exception
	{
		var start = lastDataBundle.getEnd().getNextDay();
		return new DataBundle(0f, 0, start, Interval.getIntervalEnd(start, interval));
	}

	public boolean tryAddToDataBundle(DataBundle dataBundle) throws Exception
	{
		if (date.isInTimeframe(dataBundle.getStart(), dataBundle.getEnd()))
		{
			dataBundle.addEntry(money);
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isRepeatedIntoTimeframe(Date start, Date end) throws Exception
	{
		if (repeat != Interval.NEVER)
		{
			Date currentDate = Interval.getIntervalNextDate(date, repeat);  // Skip entry itself
			// Regard duration:
			if (duration == false && until.compareTo(end) < 0)
			{
				end = until;
			}
			// Check for repeating entries in timeframe:
			while (currentDate.compareTo(end) <= 0)
			{
				if (currentDate.compareTo(start) >= 0)
				{
					return true;
				}
				currentDate = Interval.getIntervalNextDate(currentDate, repeat);
			}
		}
		return false;
	}

	public void unpackToList(LinkedList<DataEntry> list, Date end) throws Exception
	{
		if (repeat != Interval.NEVER)
		{
			Date currentDate = Interval.getIntervalNextDate(date, repeat);  // Don't add entry itself again
			// Regard duration:
			if (duration == false && until.compareTo(end) < 0)
			{
				end = until;
			}
			// Create and add repeating entries in timeframe:
			while (currentDate.compareTo(end) <= 0)
			{
				list.add(new DataEntry(this, currentDate, true));
				currentDate = Interval.getIntervalNextDate(currentDate, repeat);
			}
		}
	}

	public void setValue(DataRowType dataRowType, Object value) throws Exception
	{
		switch (dataRowType)
		{
			case MONEY:
				money = (float)value;
				break;
			case TYPE:
				type = (int)value;
				break;
			case SUBTYPE:
				subtype = (int)value;
				break;
			case NAME:
				name = (String)value;
				break;
			case LOCATION:
				location = (String)value;
				break;
			case DATE:
				date = new Date(Stream.of(((String)value).split("[.]")).mapToInt(Integer::parseInt).toArray());
				break;
			case REPEAT:
				repeat = Interval.byIndex((int)value);
				break;
			case DURATION:
				duration = (boolean)value;
				break;
			case UNTIL:
				until = new Date(Stream.of(((String)value).split("[.]")).mapToInt(Integer::parseInt).toArray());
				break;
			default:
				throw new Exception("ERROR: Invalid data row type (" + dataRowType.toString() + ")");
		}
	}

	public String getDataRowValueAsString(DataRowType dataRowType) throws Exception
	{
		switch (dataRowType)
		{
			case MONEY:
				return Float.toString(money);
			case TYPE:
				return Integer.toString(type);
			case SUBTYPE:
				return Integer.toString(subtype);
			case NAME:
				return name;
			case LOCATION:
				return location;
			case DATE:
				return date.toString();
			case REPEAT:
				return repeat.toString();
			case DURATION:
				return Boolean.toString(duration);
			case UNTIL:
				return until.toString();
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
			case TYPE:
				return TYPE_NAMES[type];
			case SUBTYPE:
				return SUBTYPE_NAMES[type][subtype];
			case NAME:
				return name;
			case LOCATION:
				return location;
			case DATE:
			{
				return date.getAsText();
			}
			case REPEAT:
			{
				return repeat.toString();
			}
			case DURATION:
			{
				if (repeat == Interval.NEVER)
				{
					return "-";
				}
				else
				{
					if (duration)
					{
						return DURATION_TEXT_ON;
					}
					else
					{
						return DURATION_TEXT_OFF;
					}
				}
			}
			case UNTIL:
			{
				if (repeat == Interval.NEVER || duration)  // true = "Infinitely"
				{
					return "-";
				}
				else
				{
					return until.getAsText();
				}
			}
			default:
				throw new Exception("ERROR: Invalid data row type (" + dataRowType.toString() + ")");
		}
	}

}