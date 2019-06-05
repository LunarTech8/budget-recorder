package com.romanbrunner.apps.budgetrecorder;

// import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.JsonNode;

import com.romanbrunner.apps.budgetrecorder.InputPanel;
import com.romanbrunner.apps.budgetrecorder.DataEntry.DataRowSorting;
import com.romanbrunner.apps.budgetrecorder.DataEntry.DataRowType;
import com.romanbrunner.apps.budgetrecorder.DataEntry;


@SuppressWarnings("serial")
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
	private static final String LOGO_FILE_PATH = "/images/Logo.jpg";
	private static final String CONFIG_PATH = "/config.properties";
	private static final int VERSION_MAJOR = 1;
	private static final int VERSION_MINOR = 1;
	private static final int VERSION_PATCH = 0;


	// --------------------
	// Functional code
	// --------------------

	private static MainFrame instance = null;
	private static List<DataEntry> dataEntries = new LinkedList<DataEntry>();
	private static DataPanel dataPanel;
	private static JFrame inputFrame;
	private static JFrame dataFrame;
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

		public Serializer()
		{
			this(null);
		}
		public Serializer(Class<MainFrame> t)
		{
			super(t);
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

		public Deserializer()
		{
			this(null);
		}
		public Deserializer(Class<?> vc)
		{
			super(vc);
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

	private static void createInputFrame() throws Exception
	{
		// Create the frame:
		inputFrame = new JFrame(INPUT_FRAME_NAME);
		inputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set the frame appearance:
		var imgURL = MainFrame.class.getResource(LOGO_FILE_PATH);
		if (imgURL != null)
		{
			inputFrame.setIconImage(new ImageIcon(imgURL).getImage());
		}
		else
		{
			throw new Exception("ERROR: Logo file not found");
		}
		inputFrame.setContentPane(new InputPanel());

		// Set the frame size and position:
		inputFrame.pack();
		inputFrame.setLocationRelativeTo(null);  // Set frame position to center of screen

		// Make frame escapable:
		var cancelAction = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		};
		inputFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
		inputFrame.getRootPane().getActionMap().put("Cancel", cancelAction);

		// Display the frame:
		inputFrame.setVisible(true);
	}

	private static void createDataFrame() throws Exception
	{
		// Create the frame:
		dataFrame = new JFrame(DATA_FRAME_NAME);
		dataFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set the frame appearance:
		var imgURL = MainFrame.class.getResource(LOGO_FILE_PATH);
		if (imgURL != null)
		{
			dataFrame.setIconImage(new ImageIcon(imgURL).getImage());
		}
		else
		{
			throw new Exception("ERROR: Logo file not found");
		}
		dataPanel = new DataPanel();
		dataFrame.setContentPane(dataPanel);

		// Set the frame size and position:
		dataFrame.pack();
		dataFrame.setLocationRelativeTo(null);  // Set frame position to center of screen

		// Make frame escapable:
		var cancelAction = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		};
		dataFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
		dataFrame.getRootPane().getActionMap().put("Cancel", cancelAction);

		// Display the frame:
		dataFrame.setVisible(true);
	}

	private static void readConfigFile() throws Exception
	{
		var inputStream = MainFrame.class.getResourceAsStream(CONFIG_PATH);
		if (inputStream != null)
		{
			var prop = new Properties();
			prop.load(inputStream);
			databaseName = prop.getProperty("databaseName");
			testModeActive = prop.getProperty("testModeActive").equals("true");
			if (testModeActive)
			{
				databasePath = prop.getProperty("testDatabasePath");
				backupPath = prop.getProperty("testBackupPath");
			}
			else
			{
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

	public static List<DataEntry> getDataEntries(DataRowSorting sorting) throws Exception
	{
		var sortedList = new LinkedList<>(dataEntries);
        Collections.sort(sortedList, new DataEntry.DataComparator(sorting));
		return sortedList;
	}

	public static ArrayList<String> getDataRowValuesAsStrings(DataRowType dataRowType) throws Exception
	{
		var keywords = new LinkedList<String>();
		for (var dataEntry : dataEntries)
		{
			var newKeyword = dataEntry.getDataRowValueAsString(dataRowType);
			if (keywords.contains(newKeyword) == false)
			{
				keywords.add(newKeyword);
			}
		}
		return new ArrayList<>(keywords);
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
		dataPanel.refresh();
	}

}