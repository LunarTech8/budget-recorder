package com.romanbrunner.apps.budgetrecorder;

// import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.List;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import com.romanbrunner.apps.budgetrecorder.InputPanel;


@SpringBootApplication
public class MainFrame
{
	// --------------------
	// Data code
	// --------------------

	private final static String frameName = "Budget Recorder";
	private final static String logoPath = "images/Logo.jpg";


	// --------------------
	// Functional code
	// --------------------

	private static List<DataEntry> dataEntries = new LinkedList<DataEntry>();

	private static void createInputFrame()
	{
		// Create the frame:
		JFrame frame = new JFrame(frameName);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set the frame appearance:
		var imgURL = MainFrame.class.getResource(logoPath);
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

	public static void main(String[] args)
	{
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

	public static void addDataEntry(String[] dataRows)
	{
		try
		{
			dataEntries.add(new DataEntry(dataRows));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}