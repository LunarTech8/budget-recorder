// package com.romanbrunner.apps.budgetrecorder;

// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.util.Properties;


// class ResourceImportTest
// {
// 	private static String resource = "/config.properties";

// 	public static void test1()
// 	{
// 		try
// 		{
// 			var url = MainFrame.class.getResource(resource);
// 			System.out.println("-> Attempting input resource: " + resource);
// 			if (url != null)
// 			{
// 				String path = url.getPath();
// 				path = path.replaceFirst("^/(.:/)", "$1");
// 				System.out.println("    Absolute resource path found :\n    " + path);
// 				String s = new String(Files.readAllBytes(Paths.get(path)));
// 				System.out.println("    File content: \n" + s);
// 			}
// 			else
// 			{
// 				System.out.println("    ERROR: No resource found: " + resource);
// 			}
// 		}
// 		catch (Exception exception)
// 		{
// 			exception.printStackTrace();
// 		}
// 	}

// 	public static void test2()
// 	{
// 		try
// 		{
// 			var inputStream = MainFrame.class.getResourceAsStream(resource);
// 			if (inputStream != null)
// 			{
// 				var prop = new Properties();
// 				prop.load(inputStream);
// 				System.out.println(prop.getProperty("databasePath"));
// 			}
// 			else
// 			{
// 				System.out.println("ERROR: File not found");
// 			}
// 		}
// 		catch (Exception exception)
// 		{
// 			exception.printStackTrace();
// 		}
// 	}
// }