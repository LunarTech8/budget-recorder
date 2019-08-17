package com.romanbrunner.apps.budgetrecorder;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Calendar;
import java.util.LinkedList;
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

import com.romanbrunner.apps.budgetrecorder.Date.Interval;


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
	private static final float INIT_VALUE_MONEY = 0.F;
	private static final int INIT_INDEX_TYPE = 0;
	private static final int INIT_INDEX_SUBTYPE = 0;
	private static final int INIT_INDEX_REPEAT = 0;


	// --------------------
	// Functional code
	// --------------------

	private DataField dataFields[] = new DataField[DataEntry.DataRowType.Data.length];

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

		public ComboBoxDataField(JLabel label, String[] items, int initSelectedIndex)
		{
			super(label);
			dataField = new JComboBox<String>(items);
			dataField.setSelectedIndex(initSelectedIndex);
		}
		public ComboBoxDataField(JLabel label, String[] items, int initSelectedIndex, ActionListener actionListener)
		{
			super(label);
			dataField = new JComboBox<String>(items);
			dataField.setSelectedIndex(initSelectedIndex);
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
			java.util.Date initDate = calendar.getTime();
			calendar.add(Calendar.YEAR, -maxBackYears);
			java.util.Date earliestDate = calendar.getTime();
			calendar.add(Calendar.YEAR, maxBackYears + maxUpYears);
			java.util.Date latestDate = calendar.getTime();
			dataField = new JSpinner(new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR));
			dataField.setEditor(new JSpinner.DateEditor(dataField, "dd.MM.yyyy"));
		}

		public JComponent getJComponent()
		{
			return dataField;
		}

		public Object getValue()
		{
			return DateFormat.getDateInstance(DateFormat.MEDIUM).format((java.util.Date)dataField.getModel().getValue());
		}

		public String getValueAsText()
		{
			return DateFormat.getDateInstance(DateFormat.SHORT).format((java.util.Date)dataField.getModel().getValue());
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
		private Autocomplete autocompletes[][];
		private Autocomplete activatedAutocomplete;

		public TextDataField(JLabel label)
		{
			super(label);
			dataField = new JTextField();
		}
		public TextDataField(JLabel label, LinkedList<String>[][] keywords, int initTypeIndex, int initSubtypeIndex)
		{
			super(label);
			dataField = new JTextField();
			// Add autocompletions:
			autocompletes = new Autocomplete[keywords.length][];
			for (int i = 0; i < keywords.length; i++)
			{
				autocompletes[i] = new Autocomplete[keywords[i].length];
				for (int j = 0; j < keywords[i].length; j++)
				{
					// Create autocomplete and document listener:
					autocompletes[i][j] = new Autocomplete(dataField, keywords[i][j], false);
					dataField.getDocument().addDocumentListener(autocompletes[i][j]);
					// Maps the commit key to the commit action, which finishes the autocomplete when given a suggestion:
					dataField.getInputMap().put(KeyStroke.getKeyStroke(COMMIT_KEY), COMMIT_ACTION);
					dataField.getActionMap().put(COMMIT_ACTION, autocompletes[i][j].new CommitAction());
				}
			}
			activatedAutocomplete = autocompletes[initTypeIndex][initSubtypeIndex];
			activatedAutocomplete.changeActivation(true);
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

		public void updateAutocomplete(int typeIndex, int subtypeIndex, String keyword)
		{
			autocompletes[typeIndex][subtypeIndex].addKeyword(keyword);
		}

		public void activateAutocomplete(int typeIndex, int subtypeIndex)
		{
			activatedAutocomplete.changeActivation(false);
			activatedAutocomplete = autocompletes[typeIndex][subtypeIndex];
			activatedAutocomplete.changeActivation(true);
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

	private class SubtypeDataFieldAL implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Adjust autocompletion activations:
				var subtypeSelectedIndex = ((ComboBoxDataField)dataFields[DataEntry.DataRowType.SUBTYPE.toInt()]).getSelectedIndex();
				if (subtypeSelectedIndex < 0)
				{
					return;  // Ignore events with unselected subtype
				}
				var typeSelectedIndex = ((ComboBoxDataField)dataFields[DataEntry.DataRowType.TYPE.toInt()]).getSelectedIndex();
				((TextDataField)dataFields[DataEntry.DataRowType.NAME.toInt()]).activateAutocomplete(typeSelectedIndex, subtypeSelectedIndex);
				((TextDataField)dataFields[DataEntry.DataRowType.LOCATION.toInt()]).activateAutocomplete(typeSelectedIndex, subtypeSelectedIndex);
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
				boolean isVisible = (Interval.byIndex((int)repeatDataField.getValue()) != Interval.NEVER);
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
				var money = (float)dataFields[i++].getValue();
				var name = (String)dataFields[i++].getValue();
				var location = (String)dataFields[i++].getValue();
				var type = (int)dataFields[i++].getValue();
				var subtype = (int)dataFields[i++].getValue();
				MainFrame.addDataEntry(new DataEntry(
					money,
					name,
					location,
					type,
					subtype,
					new Date(Stream.of(((String)dataFields[i++].getValue()).split("[.]")).mapToInt(Integer::parseInt).toArray()),
					Interval.byIndex((int)dataFields[i++].getValue()),
					(boolean)dataFields[i++].getValue(),
					new Date(Stream.of(((String)dataFields[i++].getValue()).split("[.]")).mapToInt(Integer::parseInt).toArray())
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
				((TextDataField)dataFields[DataEntry.DataRowType.NAME.toInt()]).updateAutocomplete(type, subtype, name);
				((TextDataField)dataFields[DataEntry.DataRowType.LOCATION.toInt()]).updateAutocomplete(type, subtype, location);
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
			for (var dataRowType : DataEntry.DataRowType.Data.values)
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
						dataField = new CurrencyDataField(label, INIT_VALUE_MONEY, 2);
						break;
					case NAME:
					case LOCATION:
						dataField = new TextDataField(label, MainFrame.getDataRowValuesAsStrings(dataRowType), INIT_INDEX_TYPE, INIT_INDEX_SUBTYPE);
						break;
					case TYPE:
						dataField = new ComboBoxDataField(label, DataEntry.TYPE_NAMES, INIT_INDEX_TYPE, new TypeDataFieldAL());
						break;
					case SUBTYPE:
						dataField = new ComboBoxDataField(label, DataEntry.SUBTYPE_NAMES[INIT_INDEX_TYPE], INIT_INDEX_SUBTYPE, new SubtypeDataFieldAL());
						break;
					case DATE:
						dataField = new DateDataField(label, 100, 1000);
						break;
					case REPEAT:
						dataField = new ComboBoxDataField(label, Interval.getNames(), INIT_INDEX_REPEAT, new RepeatDataFieldAL());
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
			constraints.gridy = DataEntry.DataRowType.Data.length;
			add(button, constraints);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

}