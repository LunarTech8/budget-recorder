package com.romanbrunner.apps.budgetrecorder;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Properties;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
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
import com.romanbrunner.apps.budgetrecorder.DataEntry;


// @SpringBootApplication
@JsonSerialize(using = MainFrame.Serializer.class)
@JsonDeserialize(using = MainFrame.Deserializer.class)
public class MainFrame  // Singleton class
{
	// --------------------
	// Data code
	// --------------------

	private static final String FRAME_NAME = "Budget Recorder";
	private static final String LOGO_FILE_PATH = "images/Logo.jpg";
	private static final String CONFIG_PATH = "src/main/resources";
	private static final int VERSION_MAJOR = 1;
	private static final int VERSION_MINOR = 0;
	private static final int VERSION_PATCH = 0;


	// --------------------
	// Functional code
	// --------------------

	private static MainFrame instance = null;
	private static List<DataEntry> dataEntries = new LinkedList<DataEntry>();
	private static String databaseName;
	private static String databasePath;
	private static String backupPath;

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
				if (jsonType.compareTo("config") == 0)
				{
					databaseName = node.get("databaseName").textValue();
					databasePath = node.get("databasePath").textValue();
					backupPath = node.get("backupPath").textValue();
				}
				else if (jsonType.compareTo("database") == 0)
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

	private static void createInputFrame()
	{
		// Create the frame:
		JFrame frame = new JFrame(FRAME_NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set the frame appearance:
		var imgURL = MainFrame.class.getResource(LOGO_FILE_PATH);
		if (imgURL != null)
		{
			frame.setIconImage(new ImageIcon(imgURL).getImage());
		}
		// frame.add(new InputPanel());
		frame.setContentPane(new InputPanel());

		// Set the frame size and position:
		frame.pack();
		frame.setLocationRelativeTo(null);  // Set frame position to center of screen

		// Display the frame:
		frame.setVisible(true);
	}

	private static void readConfigFromJson() throws Exception
	{
		final var configFile = new File(CONFIG_PATH + "/config.json");

		// DEBUG
		// getResourceAsStream/getResource only finds files in folder of MainFrame.java
		var inputStream = MainFrame.class.getResourceAsStream("config.properties");
		if (inputStream != null)
		{
			var prop = new Properties();
			prop.load(inputStream);
			System.out.println(prop.getProperty("jsonType"));
		}
		else
		{
			System.out.println("DEBUG: File not found");
		}

		// Load settings from json config file:
		var mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		mapper.readValue(configFile, MainFrame.class);
	}

	private static void readDatabaseFromJson() throws Exception
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
			// Load settings from stored config json:
			readConfigFromJson();
			// Load database from stored json:
			readDatabaseFromJson();
			// Schedule a job for the event dispatch thread:
			SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						createInputFrame();
					}
				}
			);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public static void addDataEntry(String[] dataRows) throws Exception
	{
		dataEntries.add(new DataEntry(dataRows));
	}

	public static void writeDatabaseToJson() throws Exception
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

}