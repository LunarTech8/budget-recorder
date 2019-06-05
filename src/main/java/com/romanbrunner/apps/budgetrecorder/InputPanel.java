package com.romanbrunner.apps.budgetrecorder;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;
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
import javax.swing.KeyStroke;
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
	private static final int ADD_CONFIRMATION_TIME = 250;  // Miliseconds
	private static final String ADD_BUTTON_TEXT = "Add entry";
	private static final String ADD_BUTTON_TOOLTIP = "Adds the values defined above to the data base";
	private static final String DATA_ROW_TEXT = "<NAME>:";
	private static final String DATA_ROW_TOOLTIP = "Define the value for <NAME> in the field to the right.";
	private static final String DATA_FIELD_TOOLTIP = "Define the value for <NAME> here.";


	// --------------------
	// Functional code
	// --------------------

	private DataField dataFields[] = new DataField[DataEntry.DATA_ROW_TYPE_COUNT];

	private abstract class DataField
	{
		private JLabel label;

		public DataField(JLabel label)
		{
			this.label = label;
		}

		public JLabel getJLabel()
		{
			return label;
		}

		public void setVisible(boolean aFlag)
		{
			getJComponent().setVisible(aFlag);
			label.setVisible(aFlag);
		}

		public abstract JComponent getJComponent();
		public abstract Object getValue();
		public abstract String getValueAsText();
	}

	private class CurrencyDataField extends DataField
	{
		private JFormattedTextField dataField;

		public CurrencyDataField(JLabel label, float initValue, int fractionDigits)
		{
			super(label);
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

		public Object getValue()
		{
			Number number = (Number)dataField.getValue();
			return number.floatValue();
		}

		public String getValueAsText()
		{
			return dataField.getText().toString();
		}
	}

	private class ComboBoxDataField extends DataField
	{
		private JComboBox<String> dataField;

		public ComboBoxDataField(JLabel label, String[] items)
		{
			super(label);
			dataField = new JComboBox<String>(items);
		}
		public ComboBoxDataField(JLabel label, String[] items, ActionListener actionListener)
		{
			super(label);
			dataField = new JComboBox<String>(items);
			dataField.addActionListener(actionListener);
		}

		public JComponent getJComponent()
		{
			return dataField;
		}

		public Object getValue()
		{
			return dataField.getSelectedIndex();
		}

		public String getValueAsText()
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

	private class DateDataField extends DataField
	{
		private JSpinner dataField;

		public DateDataField(JLabel label, int maxBackYears, int maxUpYears)
		{
			super(label);
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

		public Object getValue()
		{
			return DateFormat.getDateInstance(DateFormat.MEDIUM).format((Date)dataField.getModel().getValue());
		}

		public String getValueAsText()
		{
			return DateFormat.getDateInstance(DateFormat.SHORT).format((Date)dataField.getModel().getValue());
		}
	}

	private class CheckBoxDataField extends DataField
	{
		private JCheckBox dataField;

		public CheckBoxDataField(JLabel label, String text, ActionListener actionListener)
		{
			super(label);
			dataField = new JCheckBox(text);
			dataField.addActionListener(actionListener);
		}

		public JComponent getJComponent()
		{
			return dataField;
		}

		public Object getValue()
		{
			return dataField.isSelected();
		}

		public String getValueAsText()
		{
			var returnString = dataField.getText();
			if (dataField.isSelected() == false)
			{
				returnString = "Not " + returnString;
			}
			return returnString;
		}
	}

	private class TextDataField extends DataField
	{
		private static final String COMMIT_ACTION = "commit";
		private static final String COMMIT_KEY = "ENTER";
		private JTextField dataField;
		private Autocomplete autoComplete;

		public TextDataField(JLabel label)
		{
			super(label);
			dataField = new JTextField();
		}
		public TextDataField(JLabel label, ArrayList<String> keywords)
		{
			super(label);
			dataField = new JTextField();
			// Add autocompletion:
			autoComplete = new Autocomplete(dataField, keywords);
			dataField.getDocument().addDocumentListener(autoComplete);
			// Maps the commit key to the commit action, which finishes the autocomplete when given a suggestion:
			dataField.getInputMap().put(KeyStroke.getKeyStroke(COMMIT_KEY), COMMIT_ACTION);
			dataField.getActionMap().put(COMMIT_ACTION, autoComplete.new CommitAction());
		}

		public JComponent getJComponent()
		{
			return dataField;
		}

		public Object getValue()
		{
			return dataField.getText();
		}

		public String getValueAsText()
		{
			return dataField.getText();
		}

		public void updateAutocomplete(ArrayList<String> keywords)
		{
			autoComplete.setKeywords(keywords);
		}
	}

	private class TypeDataFieldAL implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Change items in subtype combo-box with currently selected subset of names as options:
				var typeComboBox = (ComboBoxDataField)dataFields[DataEntry.DataRowType.TYPE.toInt()];
				var subtypeComboBox = (ComboBoxDataField)dataFields[DataEntry.DataRowType.SUBTYPE.toInt()];
				subtypeComboBox.changeItems(DataEntry.SUBTYPE_NAMES[typeComboBox.getSelectedIndex()]);
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private class RepeatDataFieldAL implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Evaluate repeat data field:
				var repeatDataField = dataFields[DataEntry.DataRowType.REPEAT.toInt()];
				boolean isVisible = ((int)repeatDataField.getValue() != 0);  // 0 = "Never"
				// Adjust visibility for duration data field:
				var durationDataField = dataFields[DataEntry.DataRowType.DURATION.toInt()];
				durationDataField.getJComponent().setVisible(isVisible);
				durationDataField.getJLabel().setVisible(isVisible);
				// Evaluate duration data field:
				isVisible = (isVisible && (boolean)durationDataField.getValue() != true);  // true = "Infinitely"
				// Adjust visibility for until data field:
				var untilDataField = dataFields[DataEntry.DataRowType.UNTIL.toInt()];
				untilDataField.getJComponent().setVisible(isVisible);
				untilDataField.getJLabel().setVisible(isVisible);
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private class DurationDataFieldAL implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Evaluate duration data field:
				var durationDataField = dataFields[DataEntry.DataRowType.DURATION.toInt()];
				boolean isVisible = ((boolean)durationDataField.getValue() != true);  // true = "Infinitely"
				// Adjust visibility for until data field:
				var untilDataField = dataFields[DataEntry.DataRowType.UNTIL.toInt()];
				untilDataField.getJComponent().setVisible(isVisible);
				untilDataField.getJLabel().setVisible(isVisible);
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
				// Extract entered values, create data entry and add it to the database:
				int i = 0;
				MainFrame.addDataEntry(new DataEntry(
					(float)dataFields[i++].getValue(),
					(String)dataFields[i++].getValue(),
					(String)dataFields[i++].getValue(),
					(int)dataFields[i++].getValue(),
					(int)dataFields[i++].getValue(),
					Stream.of(((String)dataFields[i++].getValue()).split("[.]")).mapToInt(Integer::parseInt).toArray(),
					(int)dataFields[i++].getValue(),
					(boolean)dataFields[i++].getValue(),
					Stream.of(((String)dataFields[i++].getValue()).split("[.]")).mapToInt(Integer::parseInt).toArray()
					));
				// Write database to json:
				MainFrame.writeDatabaseFile();
				// Refresh data panel:
				MainFrame.refreshDataPanel();
				// Give visual confimation:
				for (var component : getComponents())
				{
					if (component instanceof JLabel)
					{
						component.setForeground(Color.GREEN);
					}
				}
				var task = new java.util.TimerTask()
				{
					@Override
					public void run()
					{
						for (var component : getComponents())
						{
							if (component instanceof JLabel)
							{
								component.setForeground(Color.BLACK);
							}
						}
					}
				};
				new java.util.Timer().schedule(task, ADD_CONFIRMATION_TIME);
				// Update autocompletions:
				((TextDataField)dataFields[DataEntry.DataRowType.NAME.toInt()]).updateAutocomplete(MainFrame.getDataRowValuesAsStrings(DataEntry.DataRowType.NAME));
				((TextDataField)dataFields[DataEntry.DataRowType.LOCATION.toInt()]).updateAutocomplete(MainFrame.getDataRowValuesAsStrings(DataEntry.DataRowType.LOCATION));
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

		try
		{
			GridBagConstraints constraints = new GridBagConstraints();
			// Define default constraints:
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 0.5;
			constraints.weighty = 0.5;

			// Create data row components:
			constraints.gridy = 0;
			for (var dataRowType : DataEntry.DataRowType.values())
			{
				// Create name label:
				constraints.gridx = 0;
				var name = dataRowType.toString();
				var label = new JLabel(DATA_ROW_TEXT.replace("<NAME>", name), JLabel.CENTER);
				label.setToolTipText(DATA_ROW_TOOLTIP.replace("<NAME>", name));
				label.setPreferredSize(new Dimension(NAME_LABEL_WIDTH, DATA_ROW_HEIGHT));
				add(label, constraints);
				// Create data field component:
				constraints.gridx = 1;
				DataField dataField;
				switch (dataRowType)
				{
					case MONEY:
						dataField = new CurrencyDataField(label, 0, 2);
						break;
					case NAME:
					case LOCATION:
						dataField = new TextDataField(label, MainFrame.getDataRowValuesAsStrings(dataRowType));
						break;
					case TYPE:
						dataField = new ComboBoxDataField(label, DataEntry.TYPE_NAMES, new TypeDataFieldAL());
						break;
					case SUBTYPE:
						dataField = new ComboBoxDataField(label, DataEntry.SUBTYPE_NAMES[0]);
						break;
					case DATE:
						dataField = new DateDataField(label, 100, 1000);
						break;
					case REPEAT:
						dataField = new ComboBoxDataField(label, DataEntry.REPEAT_NAMES, new RepeatDataFieldAL());
						break;
					case DURATION:
						dataField = new CheckBoxDataField(label, "Infinitely", new DurationDataFieldAL());
						dataField.setVisible(false);
						break;
					case UNTIL:
						dataField = new DateDataField(label, 100, 1000);
						dataField.setVisible(false);
						break;
					default:
						dataField = new TextDataField(label);
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
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

}