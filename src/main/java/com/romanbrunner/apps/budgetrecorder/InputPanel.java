package com.romanbrunner.apps.budgetrecorder;

import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.util.List;
import static java.util.Arrays.asList;


@SuppressWarnings("serial")
public class InputPanel extends JPanel
{
	final static int labelWidth = 100;
	final static int labelHeight = 40;
	final static int textFieldWidth = 200;
	final static int textFieldHeight = labelHeight;
	final static int buttonWidth = labelWidth + textFieldWidth;
	final static int buttonHeight = labelHeight;
	final static List<String> labelNames = asList("Money", "Name", "Location", "Type", "Date", "Repeat");

	public InputPanel()
	{
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// Define default constrains:
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 0.5;

		// Add labels:
		c.gridx = 0;
		c.gridy = 0;
		for (var name : labelNames)
		{
			var label = new JLabel(name + ":", JLabel.CENTER);
			label.setToolTipText("A label containing only text");
			label.setPreferredSize(new Dimension(labelWidth, labelHeight));
			add(label, c);
			c.gridy++;
		}

		// Add text fields:
		c.gridx = 1;
		c.gridy = 0;
		for (int i = 0; i < labelNames.size(); i++)
		{
			var textField = new JTextField();
			textField.setToolTipText("A text field containing only text");
			textField.setPreferredSize(new Dimension(textFieldWidth, textFieldHeight));
			add(textField, c);
			c.gridy++;
		}

		// Add button:
		var button = new JButton("Add entry");
		button.setToolTipText("A button containing only text");
		button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = labelNames.size();
		add(button, c);
	}
}