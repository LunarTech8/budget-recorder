package com.romanbrunner.apps.budgetrecorder;

// import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import com.romanbrunner.apps.budgetrecorder.InputPanel;


@SpringBootApplication
public class MainFrame 
{
	final static String frameName = "Budget Recorder";

	private static void createInputFrame()
	{

		// Create the frame:
		JFrame frame = new JFrame(frameName);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set the frame appearance:
		var imgURL = MainFrame.class.getResource("images/Logo.jpg");
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

}