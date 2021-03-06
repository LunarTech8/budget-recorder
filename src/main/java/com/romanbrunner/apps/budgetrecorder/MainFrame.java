package com.romanbrunner.apps.budgetrecorder;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

// import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// @SuppressWarnings("serial")
@SpringBootApplication
@JsonSerialize(using = MainFrame.Serializer.class)
@JsonDeserialize(using = MainFrame.Deserializer.class)
public class MainFrame  // Singleton class
{
	// --------------------
	// Data code
	// --------------------

	private static final String INPUT_FRAME_NAME = "Budget Recorder (Input)";
	private static final String DATA_FRAME_NAME = "Budget Recorder (Data)";
	private static final String EXCERPT_DATA_FRAME_NAME = "Budget Recorder (Excerpt Data)";
	private static final String LOGO_FILE_PATH = "/images/Logo.jpg";
	private static final String CONFIG_PATH = "/config.properties";

	public static final int VERSION_MAJOR = 2;
	public static final int VERSION_MINOR = 1;
	public static final int VERSION_PATCH = 0;


	// --------------------
	// Functional code
	// --------------------

	private static MainFrame instance = null;
	private static List<DataEntry> dataEntries = new LinkedList<DataEntry>();
	private static InputPanel inputPanel;
	private static DataPanel dataPanel;
	private static DataPanel excerptDataPanel;
	private static JFrame inputFrame;
	private static JFrame dataFrame;
	private static JFrame excerptDataFrame;
	private static String databaseName;
	private static String databasePath;
	private static String backupPath;
	private static boolean testModeActive;

	/**
	 * @return the instance
	 */
	public static MainFrame getInstance()
    {
		if (instance == null)
		{
			instance = new MainFrame();
		}
        return instance;
    }

	public static class Serializer extends StdSerializer<MainFrame>
	{
		private static final long serialVersionUID = 1L;

		public Serializer(Class<MainFrame> t)
		{
			super(t);
		}
		public Serializer()
		{
			this(null);
		}

		@Override
		public void serialize(MainFrame obj, JsonGenerator jsonGenerator, SerializerProvider serializer)
		{
			try
			{
				jsonGenerator.writeStartObject();
				jsonGenerator.writeStringField("jsonType", "database");
				// Store current version:
				jsonGenerator.writeNumberField("versionMajor", VERSION_MAJOR);
				jsonGenerator.writeNumberField("versionMinor", VERSION_MINOR);
				jsonGenerator.writeNumberField("versionPatch", VERSION_PATCH);
				// Store type and subtype names:
				jsonGenerator.writeArrayFieldStart("types");
				for (int i = 0; i < DataEntry.TYPE_NAMES.length; i++)
				{
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringField("typeName", DataEntry.TYPE_NAMES[i]);
					jsonGenerator.writeArrayFieldStart("subtypeNames");
					for (int j = 0; j < DataEntry.SUBTYPE_NAMES[i].length; j++)
					{
						jsonGenerator.writeString(DataEntry.SUBTYPE_NAMES[i][j]);
					}
					jsonGenerator.writeEndArray();
					jsonGenerator.writeBooleanField("isPositiveBalanceType", DataEntry.IS_POSITIVE_BALANCE_TYPE[i]);
					jsonGenerator.writeEndObject();
				}
				jsonGenerator.writeEndArray();
				// Store data entries:
				jsonGenerator.writeArrayFieldStart("dataEntries");
				for (var dataEntry : dataEntries)
				{
					jsonGenerator.writeObject(dataEntry);
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

	public static class Deserializer extends StdDeserializer<MainFrame>
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

		@Override
		public MainFrame deserialize(JsonParser parser, DeserializationContext deserializer)
		{
			try
			{
				final ObjectMapper mapper = new ObjectMapper();
				final var codec = parser.getCodec();
				final JsonNode node = codec.readTree(parser);

				// Deserialize depending on the json-type:
				var jsonType = node.get("jsonType").textValue();
				if (jsonType.compareTo("database") == 0)
				{
					// Check json compatibility:
					var versionMajor = node.get("versionMajor").intValue();
					if (versionMajor != VERSION_MAJOR)
					{
						throw new Exception("ERROR: Target database json does not have matching major version (" + versionMajor + " instead of " + VERSION_MAJOR + ")");
					}
					var versionMinor = node.get("versionMinor").intValue();
					if (versionMinor != VERSION_MINOR)
					{
						System.out.println("WARNING: Target database json does not have matching minor version (" + versionMinor + " instead of " + VERSION_MINOR + ")");
					}
					var versionPatch = node.get("versionPatch").intValue();
					if (versionPatch != VERSION_PATCH)
					{
						System.out.println("WARNING: Target database json does not have matching patch version (" + versionPatch + " instead of " + VERSION_PATCH + ")");
					}
					// Extract type and subtype names:
					var typeNames = new LinkedList<String>();
					var subtypeNames = new LinkedList<List<String>>();
					var isPositiveBalanceTypes = new LinkedList<Boolean>();
					var typesNode = node.get("types");
					for (var typeNode : typesNode)
					{
						typeNames.add(typeNode.get("typeName").asText());
						var list = new LinkedList<String>();
						var iterator = typeNode.get("subtypeNames").elements();
						while (iterator.hasNext())
						{
							list.add(iterator.next().asText());
						}
						subtypeNames.add(list);
						isPositiveBalanceTypes.add(typeNode.get("isPositiveBalanceType").booleanValue());
					}
					// Convert name lists to arrays:
					DataEntry.TYPE_NAMES = typeNames.toArray(new String[0]);
					DataEntry.SUBTYPE_NAMES = new String[subtypeNames.size()][];
					for (int j = 0; j < subtypeNames.size(); j++)
					{
						DataEntry.SUBTYPE_NAMES[j] = subtypeNames.get(j).toArray(new String[0]);
					}
					DataEntry.IS_POSITIVE_BALANCE_TYPE = isPositiveBalanceTypes.toArray(new Boolean[0]);
					// Extract and create data entries:
					var dataEntriesNode = node.get("dataEntries");
					for (var dataEntryNode : dataEntriesNode)
					{
						dataEntries.add(mapper.treeToValue(dataEntryNode, DataEntry.class));
					}
				}
				else
				{
					throw new Exception("ERROR: Invalid json type (" + jsonType + ")");
				}
			}
			catch (Exception exception)
			{
				if (exception instanceof NullPointerException)
				{
					exception.initCause(new Exception("ERROR: Couldn't find a required json node"));
				}
				exception.printStackTrace();
				System.exit(1);  // Terminate program to avoid data corruption
			}
			return getInstance();
		}
	}

	private static JRootPane getRootPane(Component component)
	{
		if (component == null)
		{
			return null;
		}
		else if (component instanceof JRootPane)
		{
			return (JRootPane)component;
		}
		else if (component.getParent() != null)
		{
			return getRootPane(component.getParent());
		}
		else
		{
			var window = SwingUtilities.windowForComponent(component);
			if (window == null)
			{
				return null;  // Can't find parent of type JRootPane or a window for given component
			}
			return getRootPane(window);
		}
	}

	private static void setLogoForFrame(JFrame frame) throws Exception
	{
		var imgURL = MainFrame.class.getResource(LOGO_FILE_PATH);
		if (imgURL != null)
		{
			frame.setIconImage(new ImageIcon(imgURL).getImage());
		}
		else
		{
			throw new Exception("ERROR: Logo file not found");
		}
	}

	private static void readConfigFile() throws Exception
	{
		var inputStream = MainFrame.class.getResourceAsStream(CONFIG_PATH);
		if (inputStream != null)
		{
			var prop = new Properties();
			prop.load(inputStream);
			// Extract database and backup paths:
			testModeActive = prop.getProperty("testModeActive").equals("true");
			if (testModeActive)
			{
				databaseName = prop.getProperty("testDatabaseName");
				databasePath = prop.getProperty("testDatabasePath");
				backupPath = prop.getProperty("testBackupPath");
			}
			else
			{
				databaseName = prop.getProperty("databaseName");
				databasePath = prop.getProperty("databasePath");
				backupPath = prop.getProperty("backupPath");
			}
		}
		else
		{
			throw new Exception("ERROR: Config not found");
		}
	}

	private static void readDatabaseFile() throws Exception
	{
		final var databaseFile = new File(databasePath + "/" + databaseName + ".json");

		// Load database from json database file:
		var mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		mapper.readValue(databaseFile, MainFrame.class);
	}

	private static void createInputFrame() throws Exception
	{
		// Create the frame:
		inputFrame = new JFrame(INPUT_FRAME_NAME);
		inputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set the frame appearance:
		setLogoForFrame(inputFrame);
		inputPanel = new InputPanel();
		inputFrame.setContentPane(inputPanel);

		// Set the frame size and position:
		inputFrame.pack();
		inputFrame.setLocationRelativeTo(null);  // Set frame position to center of screen

		// Get panel into focus:
		inputFrame.getRootPane().requestFocusInWindow();

		// Display the frame:
		inputFrame.setVisible(true);
	}

	private static void createDataFrame() throws Exception
	{
		// Create the frame:
		dataFrame = new JFrame(DATA_FRAME_NAME);
		dataFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set the frame appearance:
		setLogoForFrame(dataFrame);
		dataPanel = new DataPanel();
		dataFrame.setJMenuBar(dataPanel.createMenuBar());
		dataFrame.setContentPane(dataPanel);

		// Set the frame size and position:
		dataFrame.pack();
		dataFrame.setLocationRelativeTo(null);  // Set frame position to center of screen

		// Display the frame:
		dataFrame.setVisible(true);
	}

	public static void createExcerptDataFrame(DataPanel dataPanel) throws Exception
	{
		// Close old frame if existent:
		if (excerptDataFrame != null)
		{
			excerptDataFrame.dispose();
		}

		// Create the frame:
		excerptDataFrame = new JFrame(EXCERPT_DATA_FRAME_NAME);
		excerptDataFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Set the frame appearance:
		setLogoForFrame(excerptDataFrame);
		excerptDataPanel = dataPanel;
		excerptDataFrame.setContentPane(excerptDataPanel);

		// Set the frame size and position:
		excerptDataFrame.pack();
		excerptDataFrame.setLocationRelativeTo(null);  // Set frame position to center of screen

		// Display the frame:
		excerptDataFrame.setVisible(true);
	}

	public static void main(String[] args)
	{
		try
		{
			// Load settings from config file:
			readConfigFile();
			// Load database from json file:
			readDatabaseFile();
			// Schedule a job for the event dispatch thread:
			var runnable = new Runnable()
			{
				public void run()
				{
					try
					{
						// Create frames:
						createDataFrame();
						createInputFrame();
						excerptDataFrame = null;

						// Key manangement:
						KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
						kfm.addKeyEventDispatcher(
							new KeyEventDispatcher()
							{
								public boolean dispatchKeyEvent(KeyEvent event)
								{
									try
									{
										var focusedRootPane = getRootPane(kfm.getFocusOwner());
										if (focusedRootPane == dataFrame.getRootPane())
										{
											return dataPanel.reactOnKeyStroke(kfm, event);
										}
										else if (focusedRootPane == inputFrame.getRootPane())
										{
											return inputPanel.reactOnKeyStroke(kfm, event);
										}
										else if (excerptDataFrame != null && focusedRootPane == excerptDataFrame.getRootPane())
										{
											return excerptDataPanel.reactOnKeyStroke(kfm, event);
										}
									}
									catch (Exception exception)
									{
										exception.printStackTrace();
									}
									return false;
								}
							}
						);

						// Bring input frame to the front:
						inputFrame.toFront();
						inputFrame.repaint();
					}
					catch (Exception exception)
					{
						exception.printStackTrace();
					}
				}
			};
			SwingUtilities.invokeLater(runnable);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public static void addDataEntry(DataEntry e) throws Exception
	{
		dataEntries.add(e);
	}

	public static void removeDataEntry(DataEntry e) throws Exception
	{
		dataEntries.remove(e);
	}

	public static LinkedList<DataEntry> getDataEntries() throws Exception
	{
		return new LinkedList<>(dataEntries);
	}
	public static LinkedList<DataEntry> getDataEntries(com.romanbrunner.apps.budgetrecorder.Date start, com.romanbrunner.apps.budgetrecorder.Date end) throws Exception
	{
		var filteredDataEntries = new LinkedList<DataEntry>();
		for (var dataEntry : dataEntries)
		{
			if (dataEntry.getDate().isInTimeframe(start, end) || dataEntry.isRepeatedIntoTimeframe(start, end))
			{
				filteredDataEntries.add(dataEntry);
			}
		}
		return filteredDataEntries;
	}

	@SuppressWarnings("unchecked")
	public static LinkedList<String>[][] getDataRowValuesAsStrings(DataEntry.DataRowType dataRowType) throws Exception
	{
		// Create and initialise keywords lists:
		LinkedList<String>[][] keywords = new LinkedList[DataEntry.TYPE_NAMES.length][];
		for (int i = 0; i < DataEntry.TYPE_NAMES.length; i++)
		{
			var subtypeLength = DataEntry.SUBTYPE_NAMES[i].length;
			keywords[i] = new LinkedList[subtypeLength];
			for (int j = 0; j < subtypeLength; j++)
			{
				keywords[i][j] = new LinkedList<String>();
			}
		}
		// Fill lists with data entries:
		for (var dataEntry : dataEntries)
		{
			var type = dataEntry.getType();
			var subtype = dataEntry.getSubtype();
			var newKeyword = dataEntry.getDataRowValueAsString(dataRowType);
			if (keywords[type][subtype].contains(newKeyword) == false)
			{
				keywords[type][subtype].add(newKeyword);
			}
		}
		return keywords;
	}

	public static void writeDatabaseFile() throws Exception
	{
		final var databaseFile = new File(databasePath + "/" + databaseName + ".json");
		final var backupFile = new File(backupPath + "/" + databaseName + "_" + DateFormat.getDateInstance(DateFormat.SHORT).format(new Date()) + ".json");

		// Create a backup of the json database file:
		Files.copy(databaseFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		// Write database to json database file:
		var mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.writeValue(databaseFile, getInstance());
	}

	public static void refreshDataPanel()
	{
		dataPanel.refreshPanel();
	}

	public static void disposeExcerptDataFrame()
	{
		if (excerptDataFrame != null)
		{
			excerptDataFrame.dispose();
		}
	}

}