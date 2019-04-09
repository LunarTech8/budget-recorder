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
	private static final String DATA_ROW_TEXT = "<NAME>:";
	private static final String DATA_ROW_TOOLTIP = "Define the value for <NAME> in the field to the right.";
	private static final String DATA_FIELD_TOOLTIP = "Define the value for <NAME> here.";


	// --------------------
	// Functional code
	// --------------------

	private DataField dataFields[] = new DataField[DataEntry.DATA_ROW_TYPE_COUNT];

	private interface DataField
	{
		JComponent getJComponent();
		String getValueAsString();
	}

	private class CurrencyDataField implements DataField
	{
		private JFormattedTextField dataField;

		public CurrencyDataField(float initValue, int fractionDigits)
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

	private class ComboBoxDataField implements DataField
	{
		private JComboBox<String> dataField;

		public ComboBoxDataField(String[] items)
		{
			dataField = new JComboBox<String>(items);
		}
		public ComboBoxDataField(String[] items, ActionListener actionListener)
		{
			dataField = new JComboBox<String>(items);
			dataField.addActionListener(actionListener);
		}

		public JComponent getJComponent()
		{
			return dataField;
		}

		public String getValueAsString()
		{
			return dataField.getSelectedItem().toString();
		}

		public int getSelectedIndex()
		{
			return dataField.getSelectedIndex();
		}

		public void changeItems(String[] newItems)
		{
			dataField.removeAllItems();
			for (var item : newItems)
			{
				dataField.addItem(item);
			}
		}
	}

	private class DateDataField implements DataField
	{
		private JSpinner dataField;

		public DateDataField(int maxBackYears, int maxUpYears)
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

	private class CheckBoxDataField implements DataField
	{
		private JCheckBox dataField;

		public CheckBoxDataField(String text)
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

	private class TextDataField implements DataField
	{
		private JTextField dataField;

		public TextDataField()
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

	private class TypeDataFieldAL implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Get combo-boxes:
				ComboBoxDataField typeComboBox = (ComboBoxDataField)dataFields[DataEntry.DataRowType.TYPE.toInt()];
				ComboBoxDataField subtypeComboBox = (ComboBoxDataField)dataFields[DataEntry.DataRowType.SUBTYPE.toInt()];
				// Change items in subtype combo-box with currently selected subset of names as options:
				subtypeComboBox.changeItems(DataEntry.SUBTYPE_NAMES[typeComboBox.getSelectedIndex()]);
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private class AddButtonAL implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Extract current data row values:
				var valueStrings = new String[dataFields.length];
				for (int i = 0; i < dataFields.length; i++)
				{
					valueStrings[i] = dataFields[i].getValueAsString();
				}
				// Create data entry of the extracted values and add it to the database:
				MainFrame.addDataEntry(valueStrings);
				// Write database to json:
				MainFrame.writeDatabaseFile();
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
		GridBagConstraints constraints = new GridBagConstraints();
		// Define default constraints:
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;

		// Create data row components:
		constraints.gridy = 0;
		for (var r : DataEntry.DataRowType.values())
		{
			// Create name label:
			constraints.gridx = 0;
			var name = r.toString();
			var label = new JLabel(DATA_ROW_TEXT.replace("<NAME>", name), JLabel.CENTER);
			label.setToolTipText(DATA_ROW_TOOLTIP.replace("<NAME>", name));
			label.setPreferredSize(new Dimension(NAME_LABEL_WIDTH, DATA_ROW_HEIGHT));
			add(label, constraints);
			// Create data field component:
			constraints.gridx = 1;
			DataField dataField;
			switch (r)
			{
				case MONEY:
					dataField = new CurrencyDataField(0, 2);
					break;
				case TYPE:
					dataField = new ComboBoxDataField(DataEntry.TYPE_NAMES, new TypeDataFieldAL());
					break;
				case SUBTYPE:
					dataField = new ComboBoxDataField(DataEntry.SUBTYPE_NAMES[0]);
					break;
				case DATE:
					dataField = new DateDataField(100, 1000);
					break;
				case REPEAT:
					dataField = new CheckBoxDataField("Monthly");
					break;
				default:
					dataField = new TextDataField();
					break;
			}
			var component = dataField.getJComponent();
			component.setToolTipText(DATA_FIELD_TOOLTIP.replace("<NAME>", name));
			component.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, DATA_ROW_HEIGHT));
			add(component, constraints);
			dataFields[constraints.gridy] = dataField;

			constraints.gridy++;
		}
		// Create add button:
		var button = new JButton(ADD_BUTTON_TEXT);
		button.setToolTipText(ADD_BUTTON_TOOLTIP);
		button.setPreferredSize(new Dimension(NAME_LABEL_WIDTH + DATA_FIELD_WIDTH, ADD_BUTTON_HEIGHT));
		button.addActionListener(new AddButtonAL());
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = DataEntry.DATA_ROW_TYPE_COUNT;
		add(button, constraints);
	}
}
