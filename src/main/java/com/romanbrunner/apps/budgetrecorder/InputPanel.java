package com.romanbrunner.apps.budgetrecorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

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


	// --------------------
	// Functional code
	// --------------------

	private DataField dataFields[] = new DataField[DataEntry.DataRowType.Data.length];
	private JButton addButton;

	public abstract static class DataField
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

	public static class CurrencyDataField extends DataField
	{
		private JFormattedTextField dataField;

		public CurrencyDataField(JLabel label, int fractionDigits, float initValue)
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

	public static class ComboBoxDataField extends DataField
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
			this(label, items, initSelectedIndex);
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

	public static class DateDataField extends DataField
	{
		private JSpinner dataField;

		public DateDataField(JLabel label, Date minDate, Date maxDate, Date initDate)
		{
			super(label);
			Comparable<java.util.Date> start = null;
			Comparable<java.util.Date> end = null;
			if (minDate != null)
			{
				start = Date.dateToCalendar(minDate).getTime();
			}
			if (maxDate != null)
			{
				end = Date.dateToCalendar(maxDate).getTime();
			}
			dataField = new JSpinner(new SpinnerDateModel(Date.dateToCalendar(initDate).getTime(), start, end, Calendar.YEAR));
			dataField.setEditor(new JSpinner.DateEditor(dataField, "dd.MM.yyyy"));
		}
		public DateDataField(JLabel label, Date minDate, Date maxDate, Date initDate, ChangeListener changeListener)
		{
			this(label, minDate, maxDate, initDate);
			dataField.addChangeListener(changeListener);
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

		public JComponent getTextField()
		{
			return ((JSpinner.DefaultEditor)dataField.getEditor()).getTextField();
		}

		public void setMinDate(Date minDate)
		{
			var spinnerDateModel = ((SpinnerDateModel)dataField.getModel());
			Comparable<java.util.Date> start = null;
			if (minDate != null)
			{
				start = Date.dateToCalendar(minDate).getTime();
				if (start.compareTo(spinnerDateModel.getDate()) > 0)
				{
					spinnerDateModel.setValue(start);
				}
			}
			spinnerDateModel.setStart(start);
		}
	}

	public static class CheckBoxDataField extends DataField
	{
		private JCheckBox dataField;

		public CheckBoxDataField(JLabel label, String text, boolean initIsChecked)
		{
			super(label);
			dataField = new JCheckBox(text, initIsChecked);
		}
		public CheckBoxDataField(JLabel label, String text, boolean initIsChecked, ActionListener actionListener)
		{
			this(label, text, initIsChecked);
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

	public static class TextDataField extends DataField
	{
		private static final String COMMIT_ACTION = "commit";
		private static final String COMMIT_KEY = "ENTER";

		private JTextField dataField;
		private Autocomplete autocompletes[][];
		private Autocomplete activatedAutocomplete;

		public TextDataField(JLabel label, LinkedList<String>[][] keywords, int initTypeIndex, int initSubtypeIndex, String initText)
		{
			super(label);
			dataField = new JTextField(initText);
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

	private class DateDataFieldCL implements ChangeListener
	{
		public void stateChanged(ChangeEvent event)
		{
			try
			{
				// Evaluate date data field:
				var dateDataField = dataFields[DataEntry.DataRowType.DATE.toInt()];
				var minDate = new Date(Stream.of(((String)dateDataField.getValue()).split("[.]")).mapToInt(Integer::parseInt).toArray());
				// Adjust min date for until data field:
				var untilDataField = dataFields[DataEntry.DataRowType.UNTIL.toInt()];
				((DateDataField)untilDataField).setMinDate(minDate);
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
				var type = (int)dataFields[i++].getValue();
				var subtype = (int)dataFields[i++].getValue();
				var name = (String)dataFields[i++].getValue();
				var location = (String)dataFields[i++].getValue();
				MainFrame.addDataEntry(new DataEntry(
					money,
					type,
					subtype,
					name,
					location,
					new Date(Stream.of(((String)dataFields[i++].getValue()).split("[.]")).mapToInt(Integer::parseInt).toArray()),
					Interval.byIndex((int)dataFields[i++].getValue()),
					(boolean)dataFields[i++].getValue(),
					new Date(Stream.of(((String)dataFields[i++].getValue()).split("[.]")).mapToInt(Integer::parseInt).toArray())
					));
				// Write database to json:
				MainFrame.writeDatabaseFile();
				// Refresh data panel:
				MainFrame.refreshDataPanel();
				MainFrame.disposeExcerptDataFrame();
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
						dataField = new CurrencyDataField(label, 2, DataEntry.DEFAULT_VALUE_MONEY);
						break;
					case TYPE:
						dataField = new ComboBoxDataField(label, DataEntry.TYPE_NAMES, DataEntry.DEFAULT_VALUE_TYPE, new TypeDataFieldAL());
						break;
					case SUBTYPE:
						dataField = new ComboBoxDataField(label, DataEntry.SUBTYPE_NAMES[DataEntry.DEFAULT_VALUE_TYPE], DataEntry.DEFAULT_VALUE_SUBTYPE, new SubtypeDataFieldAL());
						break;
					case NAME:
						dataField = new TextDataField(label, MainFrame.getDataRowValuesAsStrings(dataRowType), DataEntry.DEFAULT_VALUE_TYPE, DataEntry.DEFAULT_VALUE_SUBTYPE, DataEntry.DEFAULT_VALUE_NAME);
						break;
					case LOCATION:
						dataField = new TextDataField(label, MainFrame.getDataRowValuesAsStrings(dataRowType), DataEntry.DEFAULT_VALUE_TYPE, DataEntry.DEFAULT_VALUE_SUBTYPE, DataEntry.DEFAULT_VALUE_LOCATION);
						break;
					case DATE:
						dataField = new DateDataField(label, null, null, DataEntry.DEFAULT_VALUE_DATE, new DateDataFieldCL());
						break;
					case REPEAT:
						dataField = new ComboBoxDataField(label, Interval.getNames(), DataEntry.DEFAULT_VALUE_REPEAT.toInt(), new RepeatDataFieldAL());
						break;
					case DURATION:
						dataField = new CheckBoxDataField(label, DataEntry.DURATION_TEXT_ON, DataEntry.DEFAULT_VALUE_DURATION, new DurationDataFieldAL());
						dataField.setVisible(false);
						break;
					case UNTIL:
						dataField = new DateDataField(label, DataEntry.DEFAULT_VALUE_DATE, null, DataEntry.DEFAULT_VALUE_DATE);
						dataField.setVisible(false);
						break;
					default:
						throw new Exception("ERROR: Unaccounted data row type (" + dataRowType.toString() + ")");
				}
				var component = dataField.getJComponent();
				component.setToolTipText(DATA_FIELD_TOOLTIP.replace("<NAME>", name));
				component.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, DATA_ROW_HEIGHT));
				add(component, constraints);
				dataFields[constraints.gridy] = dataField;

				constraints.gridy++;
			}
			// Create add button:
			addButton = new JButton(ADD_BUTTON_TEXT);
			addButton.setToolTipText(ADD_BUTTON_TOOLTIP);
			addButton.setPreferredSize(new Dimension(NAME_LABEL_WIDTH + DATA_FIELD_WIDTH, ADD_BUTTON_HEIGHT));
			addButton.addActionListener(new AddButtonAL());
			constraints.gridwidth = 2;
			constraints.gridx = 0;
			constraints.gridy = DataEntry.DataRowType.Data.length;
			add(addButton, constraints);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public boolean reactOnKeyStroke(KeyboardFocusManager kfm, KeyEvent event) throws Exception
	{
		if (event.getKeyCode() == KeyEvent.VK_ENTER && event.getModifiersEx() == 0 && event.getID() == KeyEvent.KEY_PRESSED)
		{
			JComponent focusedComponent = (JComponent)kfm.getFocusOwner();
			// Reaction in data fields:
			for (var dataField : dataFields)
			{
				var component = dataField.getJComponent();
				if (component == focusedComponent)
				{
					if (dataField instanceof InputPanel.TextDataField)
					{
						event.consume();  // Stop tab spaces in text fields that are caused by enter key commands
					}
					else
					{
						kfm.redispatchEvent(component, event);
					}
					addButton.doClick();
					return true;
				}
				else if (dataField instanceof InputPanel.DateDataField)
				{
					component = ((InputPanel.DateDataField)dataField).getTextField();
					if (component == focusedComponent)
					{
						kfm.redispatchEvent(component, event);
						addButton.doClick();
						return true;
					}
				}
			}
			// Default reaction:
			event.consume();
			addButton.doClick();
			return true;
		}
		else if (event.getKeyCode() == KeyEvent.VK_ESCAPE && event.getID() == KeyEvent.KEY_RELEASED)
		{
			System.exit(0);
		}
		return false;
	}

}