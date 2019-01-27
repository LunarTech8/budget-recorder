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
class InputPanel extends JPanel
{
	// --------------------
	// Data code
	// --------------------
		
	private static final int nameLabelWidth = 100;
	private static final int dataFieldWidth = 200;
	private static final int dataRowHeight = 40;
	private static final int addButtonHeight = 40;
	private static final String addButtonText = "Add entry";
	private static final String addButtonTooltip = "Adds the values defined above to the data base";


	// --------------------
	// Functional code
	// --------------------

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
		for (var r : DataEntry.DataRow.values())
		{
			// Create name label:
			c.gridx = 0;
			var name = r.toString();
			var label = new JLabel(name + ":", JLabel.CENTER);
			label.setToolTipText("Define the value for " + name + " in the field to the right.");
			label.setPreferredSize(new Dimension(nameLabelWidth, dataRowHeight));
			add(label, c);
			// Create data field:
			c.gridx = 1;
			JComponent dataField;
			if (r == DataEntry.DataRow.MONEY)
			{
				var displayFormat = NumberFormat.getCurrencyInstance();
				displayFormat.setMinimumFractionDigits(2);
				var displayFormatter = new NumberFormatter(displayFormat);
				var formatedTextField = new JFormattedTextField(new DefaultFormatterFactory(displayFormatter, displayFormatter, new NumberFormatter(NumberFormat.getNumberInstance())));
				formatedTextField.setValue(0);
				dataField = formatedTextField;
			}
			else if (r == DataEntry.DataRow.TYPE)
			{
				dataField = new JComboBox<>(DataEntry.typeNames);
			}
			else if (r == DataEntry.DataRow.DATE)
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
			else if (r == DataEntry.DataRow.REPEAT)
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
		var button = new JButton(addButtonText);
		button.setToolTipText(addButtonTooltip);
		button.setPreferredSize(new Dimension(nameLabelWidth + dataFieldWidth, addButtonHeight));
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = DataEntry.dataRowCount;
		add(button, c);
	}
}