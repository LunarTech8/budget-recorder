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

	private DataRow dataRows[] = new DataRow[DataEntry.DATA_ROW_COUNT];

	private interface DataRow
	{
		JComponent getJComponent();
		String getValueAsString();
	}

	private class CurrencyDataRow implements DataRow
	{
		private JFormattedTextField dataField;

		public CurrencyDataRow(float initValue, int fractionDigits)
		{
			var displayFormat = NumberFormat.getCurrencyInstance();
			displayFormat.setMinimumFractionDigits(fractionDigits);
			var displayFormatter = new NumberFormatter(displayFormat);
			dataField = new JFormattedTextField(new DefaultFormatterFactory(displayFormatter, displayFormatter, new NumberFormatter(NumberFormat.getNumberInstance())));
			dataField.setValue(initValue);
		}

		public JComponent getJComponent()
		{
			return dataField;
		}

		public String getValueAsString()
		{
			return dataField.getText().toString();
		}
	}

	private class ComboBoxDataRow implements DataRow
	{
		private JComboBox<String> dataField;

		public ComboBoxDataRow(String[] items)
		{
			dataField = new JComboBox<String>(items);
		}

		public JComponent getJComponent()
		{
			return dataField;
		}

		public String getValueAsString()
		{
			return dataField.getSelectedItem().toString();
		}
	}

	private class DateDataRow implements DataRow
	{
		private JSpinner dataField;

		public DateDataRow(int maxBackYears, int maxUpYears)
		{
			var calendar = Calendar.getInstance();
			var initDate = calendar.getTime();
			calendar.add(Calendar.YEAR, -maxBackYears);
			var earliestDate = calendar.getTime();
			calendar.add(Calendar.YEAR, maxBackYears + maxUpYears);
			var latestDate = calendar.getTime();
			dataField = new JSpinner(new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR));
			dataField.setEditor(new JSpinner.DateEditor(dataField, "dd.MM.yyyy"));
		}

		public JComponent getJComponent()
		{
			return dataField;
		}

		public String getValueAsString()
		{
			return DateFormat.getDateInstance(DateFormat.SHORT).format((Date)dataField.getModel().getValue());
		}
	}

	private class CheckBoxDataRow implements DataRow
	{
		private JCheckBox dataField;

		public CheckBoxDataRow(String text)
		{
			dataField = new JCheckBox(text);
		}

		public JComponent getJComponent()
		{
			return dataField;
		}

		public String getValueAsString()
		{
			var returnString = dataField.getText();
			if (dataField.isSelected() == false)
			{
				returnString = "Not " + returnString;
			}
			return returnString;
		}
	}

	private class TextDataRow implements DataRow
	{
		private JTextField dataField;

		public TextDataRow()
		{
			dataField = new JTextField();
		}

		public JComponent getJComponent()
		{
			return dataField;
		}

		public String getValueAsString()
		{
			return dataField.getText();
		}
	}

	private class AddDataEntryAL implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Extract current data row values:
				var valueStrings = new String[dataRows.length];
				for (int i = 0; i < dataRows.length; i++)
				{
					valueStrings[i] = dataRows[i].getValueAsString();
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
			DataRow dataRow;
			switch (r)
			{
				case MONEY:
					dataRow = new CurrencyDataRow(0, 2);
					break;
				case TYPE:
					dataRow = new ComboBoxDataRow(DataEntry.TYPE_NAMES);
					break;
				case DATE:
					dataRow = new DateDataRow(100, 1000);
					break;
				case REPEAT:
					dataRow = new CheckBoxDataRow("Monthly");
					break;
				default:
					dataRow = new TextDataRow();
					break;
			}
			var component = dataRow.getJComponent();
			component.setToolTipText("Define the value for " + name + " here.");
			component.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, DATA_ROW_HEIGHT));
			add(component, c);
			dataRows[c.gridy] = dataRow;

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
