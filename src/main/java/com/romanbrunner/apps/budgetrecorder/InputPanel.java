package com.romanbrunner.apps.budgetrecorder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.util.Calendar;
import java.text.NumberFormat;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;


@SuppressWarnings("serial")
public class InputPanel extends JPanel
{
	static final int nameLabelWidth = 100;
	static final int dataFieldWidth = 200;
	static final int dataRowHeight = 40;
	static final int addButtonHeight = 40;
	static final String[] typeNames = { "Food/Grooming", "Clothing", "Electronics", "Media", "Housing", "Amusement", "Vacation", "Locomotion", "Education" };

	private enum dataRow
	{
		MONEY, NAME, LOCATION, TYPE, DATE, REPEAT;
	}

	private String getDataRowName(dataRow r)
	{
		switch (r) 
		{
			case MONEY:
				return "Money";
			case NAME:
				return "Name";
			case LOCATION:
				return "Location";
			case TYPE:
				return "Type";
			case DATE:
				return "Date";
			case REPEAT:
				return "Repeat";
			default:
				return null;
		}
	}

	public InputPanel()
	{
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		// Define default constrains:
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 0.5;

		// Create data rows:
		c.gridy = 0;
		for (var r : dataRow.values())
		{
			// Create name label:
			c.gridx = 0;
			var name = getDataRowName(r);
			var label = new JLabel(name + ":", JLabel.CENTER);
			label.setToolTipText("Define the value for " + name + " in the field to the right.");
			label.setPreferredSize(new Dimension(nameLabelWidth, dataRowHeight));
			add(label, c);
			// Create data field:
			c.gridx = 1;
			JComponent dataField;
			if (r == dataRow.MONEY)
			{
				var displayFormat = NumberFormat.getCurrencyInstance();
				displayFormat.setMinimumFractionDigits(2);
				var displayFormatter = new NumberFormatter(displayFormat);
				var formatedTextField = new JFormattedTextField(new DefaultFormatterFactory(displayFormatter, displayFormatter, new NumberFormatter(NumberFormat.getNumberInstance())));
				formatedTextField.setValue(0);
				dataField = formatedTextField;
			}
			else if (r == dataRow.TYPE)
			{
				dataField = new JComboBox<>(typeNames);
			}
			else if (r == dataRow.DATE)
			{
				var calendar = Calendar.getInstance();
				var initDate = calendar.getTime();
				calendar.add(Calendar.YEAR, -100);
				var earliestDate = calendar.getTime();
				calendar.add(Calendar.YEAR, 1100);
				var latestDate = calendar.getTime();
				var spinner = new JSpinner(new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR));
				spinner.setEditor(new JSpinner.DateEditor(spinner, "dd.MM.yyyy"));
				dataField = spinner;
			}
			else if (r == dataRow.REPEAT)
			{
				dataField = new JCheckBox("Monthly");
			}
			else
			{
				dataField = new JTextField();
			}
			dataField.setToolTipText("Define the value for " + name + " here.");
			dataField.setPreferredSize(new Dimension(dataFieldWidth, dataRowHeight));
			add(dataField, c);

			c.gridy++;
		}
		// Create add button:
		var button = new JButton("Add entry");
		button.setToolTipText("Adds the values defined above to the data base");
		button.setPreferredSize(new Dimension(nameLabelWidth + dataFieldWidth, addButtonHeight));
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = dataRow.values().length;
		add(button, c);
	}
}