package com.romanbrunner.apps.budgetrecorder;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
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

	private static final int NAME_LABEL_WIDTH = 100;
	private static final int DATA_FIELD_WIDTH = 200;
	private static final int DATA_ROW_HEIGHT = 40;
	private static final int ADD_BUTTON_HEIGHT = 40;
	private static final String ADD_BUTTON_TEXT = "Add entry";
	private static final String ADD_BUTTON_TOOLTIP = "Adds the values defined above to the data base";


	// --------------------
	// Functional code
	// --------------------

	private JComponent dataFields[] = new JComponent[DataEntry.DATA_ROW_COUNT];

	private String getDataFieldValueAsString(int index)
	{
		var dataField = dataFields[index];
		if (dataField instanceof JFormattedTextField)
		{
			return ((JFormattedTextField)dataField).getText().toString();
		}
		else if (dataField instanceof JComboBox)
		{
			return ((JComboBox)dataField).getSelectedItem().toString();
		}
		else if (dataField instanceof JSpinner)
		{
			return DateFormat.getDateInstance(DateFormat.SHORT).format((Date)((JSpinner)dataField).getModel().getValue());
		}
		else if (dataField instanceof JCheckBox)
		{
			var cb = (JCheckBox)dataField;
			var returnString = cb.getText();
			if (cb.isSelected() == false)
			{
				returnString = "Not " + returnString;
			}
			return returnString;
		}
		else if (dataField instanceof JTextField)
		{
			return ((JTextField)dataField).getText();
		}
		return "ERROR: Unspecified data field type";
	}

	private class AddDataEntryAL implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Extract current data row values:
				var valueStrings = new String[DataEntry.DATA_ROW_COUNT];
				for (int i = 0; i < DataEntry.DATA_ROW_COUNT; i++)
				{
					valueStrings[i] = getDataFieldValueAsString(i);
				}
				// Create data entry of the extracted values and add it to the database:
				MainFrame.addDataEntry(valueStrings);
				// Write database to json:
				MainFrame.writeDatabaseToJson();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
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
		for (var r : DataEntry.DataRow.values())
		{
			// Create name label:
			c.gridx = 0;
			var name = r.toString();
			var label = new JLabel(name + ":", JLabel.CENTER);
			label.setToolTipText("Define the value for " + name + " in the field to the right.");
			label.setPreferredSize(new Dimension(NAME_LABEL_WIDTH, DATA_ROW_HEIGHT));
			add(label, c);
			// Create data field:
			c.gridx = 1;
			JComponent dataField;
			switch (r)
			{
				case MONEY:
					var displayFormat = NumberFormat.getCurrencyInstance();
					displayFormat.setMinimumFractionDigits(2);
					var displayFormatter = new NumberFormatter(displayFormat);
					var formatedTextField = new JFormattedTextField(new DefaultFormatterFactory(displayFormatter, displayFormatter, new NumberFormatter(NumberFormat.getNumberInstance())));
					formatedTextField.setValue(0);
					dataField = formatedTextField;
					break;
				case TYPE:
					dataField = new JComboBox<>(DataEntry.TYPE_NAMES);
					break;
				case DATE:
					var calendar = Calendar.getInstance();
					var initDate = calendar.getTime();
					calendar.add(Calendar.YEAR, -100);
					var earliestDate = calendar.getTime();
					calendar.add(Calendar.YEAR, 1100);
					var latestDate = calendar.getTime();
					var spinner = new JSpinner(new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR));
					spinner.setEditor(new JSpinner.DateEditor(spinner, "dd.MM.yyyy"));
					dataField = spinner;
					break;
				case REPEAT:
					dataField = new JCheckBox("Monthly");
					break;
				default:
					dataField = new JTextField();
					break;
			}
			dataField.setToolTipText("Define the value for " + name + " here.");
			dataField.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, DATA_ROW_HEIGHT));
			add(dataField, c);
			dataFields[c.gridy] = dataField;

			c.gridy++;
		}
		// Create add button:
		var button = new JButton(ADD_BUTTON_TEXT);
		button.setToolTipText(ADD_BUTTON_TOOLTIP);
		button.setPreferredSize(new Dimension(NAME_LABEL_WIDTH + DATA_FIELD_WIDTH, ADD_BUTTON_HEIGHT));
		button.addActionListener(new AddDataEntryAL());
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = DataEntry.DATA_ROW_COUNT;
		add(button, c);
	}
}
