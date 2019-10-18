package com.romanbrunner.apps.budgetrecorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.romanbrunner.apps.budgetrecorder.Date.Interval;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;


@SuppressWarnings("serial")
class DataPanel extends JPanel
{
	// --------------------
	// Data code
	// --------------------

	private static final int DATA_FIELD_WIDTH = 120;
	private static final int DATA_FIELD_HEIGHT = 40;
	private static final int HEADER_HEIGHT = 50;
	private static final int DATA_PANEL_HIGHT = 400;
	private static final int BORDER_INNER_PADDING_SIZE = 10;
	private static final int BORDER_OUTER_PADDING_SIZE = 1;
	private static final String HEADER_TEXT = "<NAME>:";
	private static final String HEADER_TOOLTIP = "Shows the values for <NAME> in the fields below.";
	private static final DataEntry.DataRowSorting DEFAULT_DATA_ROW_SORTING_COMPLETE = new DataEntry.DataRowSorting(DataEntry.DataRowType.DATE, DataEntry.DataRowSorting.Mode.DOWNWARD);
	private static final DataBundle.DataRowSorting DEFAULT_DATA_ROW_SORTING_BUNDLED = new DataBundle.DataRowSorting(DataBundle.DataRowType.START, DataBundle.DataRowSorting.Mode.DOWNWARD);
	private static final Interval DEFAULT_VIEW = Interval.NEVER;
	private static final int VERTICAL_SCROLL_SPEED = 12;
	private static final int VERTICAL_SCROLL_SPEED_MULTIPLIER = 3;

	private static final int[] SETTINGS_VIEW_MNEMONICS = { KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_M, KeyEvent.VK_Y };
	private static final String SETTINGS_MENU_TEXT = "Settings";
	private static final String SETTINGS_MENU_DESCRIPTION = "General settings menu";
	private static final String VIEW_SUBMENU_TEXT = "View";
	private static final String SHOW_EMPTY_ENTRIES_TEXT = "Show empty entries";
	private static final int ENTRIES_LIMIT_MIN = 0;
	private static final int ENTRIES_LIMIT_MAX = 500;
	private static final int ENTRIES_LIMIT_DEFAULT = 100;
	private static final int ENTRIES_LIMITER_MINOR_SPACING = 10;
	private static final int ENTRIES_LIMITER_MAJOR_SPACING = 100;
	private static final int ENTRIES_LIMITER_FONT_SIZE = 10;
	private static final String ENTRIES_LIMITER_TEXT = "Displayed entries limit: <LIMIT>";
	private static final String VERSION_SUBMENU_TEXT = "Software Version";
	private static final int VERSION_PADDING_SIZE = 5;
	private static final String VERSION_TEXT_MAJOR = "Major: <VERSION>";
	private static final String VERSION_TEXT_MINOR = "Minor: <VERSION>";
	private static final String VERSION_TEXT_PATCH = "Patch: <VERSION>";
	private static final String BUNDLE_MENU_TEXT = "Bundle";
	private static final String BUNDLE_MENU_DESCRIPTION = "Bundle selection menu";


	// --------------------
	// Functional code
	// --------------------

	private boolean showEmptyEntries = true;
	private int displayedEntriesLimit = ENTRIES_LIMIT_DEFAULT;
	private DataEntry.DataRowSorting sortingComplete;
	private DataBundle.DataRowSorting sortingBundled;
	private Interval view;
	private GridBagConstraints constraints;
	private JScrollPane scroller;
	private DataFieldButtonAL activeDataField = null;
	private DualHashBidiMap<JComponent, JComponent> biMapTypeCompToSubtypeComp = new DualHashBidiMap<JComponent, JComponent>();
	private DualHashBidiMap<JComponent, JComponent> biMapDateCompToUntilComp = new DualHashBidiMap<JComponent, JComponent>();
	private DualHashBidiMap<JComponent, JComponent> biMapRepeatCompToDurationComp = new DualHashBidiMap<JComponent, JComponent>();
	private DualHashBidiMap<JComponent, JComponent> biMapDurationCompToUntilComp = new DualHashBidiMap<JComponent, JComponent>();

	private class HeaderButtonCompleteAL implements ActionListener
	{
		private DataEntry.DataRowType dataRowType;

		public HeaderButtonCompleteAL(DataEntry.DataRowType dataRowType)
		{
			this.dataRowType = dataRowType;
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Adjust sorting:
				if (sortingComplete.row == dataRowType)
				{
					switch (sortingComplete.mode)
					{
						case UPWARD:
							sortingComplete.mode = DataEntry.DataRowSorting.Mode.DOWNWARD;
							break;
						case DOWNWARD:
							sortingComplete.mode = DataEntry.DataRowSorting.Mode.UPWARD;
							break;
					}
				}
				else
				{
					sortingComplete.row = dataRowType;
					sortingComplete.mode = DataEntry.DataRowSorting.Mode.UPWARD;
				}
				// Refresh panel:
				refreshPanel();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private class HeaderButtonBundledAL implements ActionListener
	{
		private DataBundle.DataRowType dataRowType;

		public HeaderButtonBundledAL(DataBundle.DataRowType dataRowType)
		{
			this.dataRowType = dataRowType;
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Adjust sorting:
				if (sortingBundled.row == dataRowType)
				{
					switch (sortingBundled.mode)
					{
						case UPWARD:
						sortingBundled.mode = DataBundle.DataRowSorting.Mode.DOWNWARD;
							break;
						case DOWNWARD:
						sortingBundled.mode = DataBundle.DataRowSorting.Mode.UPWARD;
							break;
					}
				}
				else
				{
					sortingBundled.row = dataRowType;
					sortingBundled.mode = DataBundle.DataRowSorting.Mode.UPWARD;
				}
				// Refresh panel:
				refreshPanel();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private class DataFieldButtonAL implements ActionListener
	{
		private DataEntry.DataRowType dataRowType;
		private JButton button;
		private DataEntry dataEntry;
		private Border dataBorder;
		private int gridx;
		private int gridy;
		private InputPanel.DataField dataField;

		public DataFieldButtonAL(DataEntry.DataRowType dataRowType, JButton button, DataEntry dataEntry)
		{
			this.dataRowType = dataRowType;
			this.button = button;
			this.dataEntry = dataEntry;

			dataBorder = button.getBorder();
			// Store current grid position:
			gridx = constraints.gridx;
			gridy = constraints.gridy;
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Deactivate current active data field if existent:
				if (activeDataField != null)
				{
					deactivateActiveDataField(true);
				}
				// Create new active data field:
				switch (dataRowType)
				{
					case MONEY:
						dataField = new InputPanel.CurrencyDataField(null, 2, dataEntry.getMoney());
						break;
					case TYPE:
						dataField = new InputPanel.ComboBoxDataField(null, DataEntry.TYPE_NAMES, dataEntry.getType(), new DataFieldModificationAL(dataEntry, dataRowType));
						break;
					case SUBTYPE:
						dataField = new InputPanel.ComboBoxDataField(null, DataEntry.SUBTYPE_NAMES[dataEntry.getType()], dataEntry.getSubtype());
						break;
					case NAME:
						dataField = new InputPanel.TextDataField(null, MainFrame.getDataRowValuesAsStrings(dataRowType), dataEntry.getType(), dataEntry.getSubtype(), dataEntry.getName());
						break;
					case LOCATION:
						dataField = new InputPanel.TextDataField(null, MainFrame.getDataRowValuesAsStrings(dataRowType), dataEntry.getType(), dataEntry.getSubtype(), dataEntry.getLocation());
						break;
					case DATE:
						dataField = new InputPanel.DateDataField(null, null, null, dataEntry.getDate(), new DataFieldModificationCL(dataEntry, dataRowType));
						break;
					case REPEAT:
						dataField = new InputPanel.ComboBoxDataField(null, Interval.getNames(), dataEntry.getRepeat().toInt(), new DataFieldModificationAL(dataEntry, dataRowType));
						break;
					case DURATION:
						if (dataEntry.getRepeat() != Interval.NEVER)
						{
							dataField = new InputPanel.CheckBoxDataField(null, DataEntry.DURATION_TEXT_ON, dataEntry.getDuration(), new DataFieldModificationAL(dataEntry, dataRowType));
						}
						break;
					case UNTIL:
						if (dataEntry.getRepeat() != Interval.NEVER && dataEntry.getDuration() == false)
						{
							var minDate = dataEntry.getDate();
							var initDate = dataEntry.getUntil();
							if (initDate.compareTo(minDate) < 0)
							{
								initDate = minDate;
							}
							dataField = new InputPanel.DateDataField(null, minDate, null, initDate);
						}
						break;
					default:
						throw new Exception("ERROR: Unaccounted data row type (" + dataRowType.toString() + ")");
				}
				if (dataField != null)
				{
					// Remove button:
					var dataPanel = button.getParent();
					dataPanel.remove(button);
					// Add data field:
					constraints.gridx = gridx;
					constraints.gridy = gridy;
					var component = dataField.getJComponent();
					dataPanel.add(component, constraints);
					// Adjust global variables:
					adjustComponentMap(dataRowType, button, component);
					activeDataField = this;
					// Refresh data panel:
					revalidate();
					repaint();
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private class DataFieldModificationAL implements ActionListener
	{
		private DataEntry dataEntry;
		private DataEntry.DataRowType dataRowType;

		public DataFieldModificationAL(DataEntry dataEntry, DataEntry.DataRowType dataRowType)
		{
			this.dataEntry = dataEntry;
			this.dataRowType = dataRowType;
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				boolean changesRequired = false;
				boolean refreshDataPanel = false;
				JComponent component = null;
				Object newValueObject = null;

				// Check if changes are required:
				if (dataRowType == DataEntry.DataRowType.TYPE)
				{
					// Make changes if selected index is unequal to type of data entry:
					@SuppressWarnings("unchecked")
					var comboBox = (JComboBox<String>)event.getSource();
					var newValue = comboBox.getSelectedIndex();
					changesRequired = (newValue != dataEntry.getType());
					component = comboBox;
					newValueObject = newValue;
				}
				else if (dataRowType == DataEntry.DataRowType.REPEAT)
				{
					// Make changes if selected index is unequal to repeat of data entry relating to the interval never:
					@SuppressWarnings("unchecked")
					var comboBox = (JComboBox<String>)event.getSource();
					var newValue = comboBox.getSelectedIndex();
					changesRequired = ((Interval.byIndex(newValue) == Interval.NEVER) != (dataEntry.getRepeat() == Interval.NEVER));
					component = comboBox;
					newValueObject = newValue;
				}
				else if (dataRowType == DataEntry.DataRowType.DURATION)
				{
					// Make changes if selection is unequal to duration of data entry:
					var checkBox = (JCheckBox)event.getSource();
					var newValue = checkBox.isSelected();
					changesRequired = (newValue != dataEntry.getDuration());
					component = checkBox;
					newValueObject = newValue;
				}

				// Make adjustments if required:
				if (changesRequired)
				{
					if (dataRowType == DataEntry.DataRowType.TYPE)
					{
						// Adjust data entry values:
						dataEntry.setValue(DataEntry.DataRowType.TYPE, newValueObject);
						dataEntry.setValue(DataEntry.DataRowType.SUBTYPE, (Object)DataEntry.DEFAULT_VALUE_SUBTYPE);
						// Adjust button name:
						JButton button = (JButton)biMapTypeCompToSubtypeComp.get(component);
						button.setText(dataEntry.getDataRowValueAsText(DataEntry.DataRowType.SUBTYPE));
						refreshDataPanel = true;
					}
					else if (dataRowType == DataEntry.DataRowType.REPEAT)
					{
						// Adjust data entry value:
						dataEntry.setValue(DataEntry.DataRowType.REPEAT, newValueObject);
						// Adjust button names:
						JButton button = (JButton)biMapRepeatCompToDurationComp.get(component);
						button.setText(dataEntry.getDataRowValueAsText(DataEntry.DataRowType.DURATION));
						button = (JButton)biMapDurationCompToUntilComp.get(button);
						button.setText(dataEntry.getDataRowValueAsText(DataEntry.DataRowType.UNTIL));
						refreshDataPanel = true;
					}
					else if (dataRowType == DataEntry.DataRowType.DURATION)
					{
						// Adjust data entry value:
						dataEntry.setValue(DataEntry.DataRowType.DURATION, newValueObject);
						// Adjust button name:
						JButton button = (JButton)biMapDurationCompToUntilComp.get(component);
						button.setText(dataEntry.getDataRowValueAsText(DataEntry.DataRowType.UNTIL));
						refreshDataPanel = true;
					}
				}

				// Refresh data panel if required:
				if (refreshDataPanel)
				{
					revalidate();
					repaint();
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private class DataFieldModificationCL implements ChangeListener
	{
		private DataEntry dataEntry;
		private DataEntry.DataRowType dataRowType;

		public DataFieldModificationCL(DataEntry dataEntry, DataEntry.DataRowType dataRowType)
		{
			this.dataEntry = dataEntry;
			this.dataRowType = dataRowType;
		}

		public void stateChanged(ChangeEvent event)
		{
			try
			{
				boolean changesRequired = false;
				boolean refreshDataPanel = false;
				JComponent component = null;
				Object newValueObject = null;

				// Check if changes are required:
				if (dataRowType == DataEntry.DataRowType.DATE)
				{
					// Make changes if value is bigger than until of data entry:
					var spinner = (JSpinner)event.getSource();
					var newValue = DateFormat.getDateInstance(DateFormat.MEDIUM).format((java.util.Date)spinner.getModel().getValue());
					changesRequired = (new Date(Stream.of(newValue.split("[.]")).mapToInt(Integer::parseInt).toArray()).compareTo(dataEntry.getUntil()) > 0);
					component = spinner;
					newValueObject = newValue;
				}

				// Make adjustments if required:
				if (changesRequired)
				{
					if (dataRowType == DataEntry.DataRowType.DATE)
					{
						// Adjust data entry value:
						dataEntry.setValue(DataEntry.DataRowType.DATE, newValueObject);
						dataEntry.setValue(DataEntry.DataRowType.UNTIL, newValueObject);
						// Adjust button name:
						JButton button = (JButton)biMapDateCompToUntilComp.get(component);
						button.setText(dataEntry.getDataRowValueAsText(DataEntry.DataRowType.UNTIL));
						refreshDataPanel = true;
					}
				}

				// Refresh data panel if required:
				if (refreshDataPanel)
				{
					revalidate();
					repaint();
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private class SettingsMenuAL implements ActionListener
	{
		private String setting;

		public SettingsMenuAL(String setting)
		{
			this.setting = setting;
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Change selected setting:
				switch (setting)
				{
					case "ShowEmptyEntries":
						showEmptyEntries = !showEmptyEntries;
						refreshPanel();
						break;
					default:
						throw new Exception("ERROR: Invalid setting type");
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private class BundleMenuAL implements ActionListener
	{
		private Interval interval;

		public BundleMenuAL(Interval interval)
		{
			this.interval = interval;
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				view = interval;  // Change view based on the index of the selected radio button
				refreshPanel();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private class LimiterSliderCL implements ChangeListener
	{
		private JLabel label;

		public LimiterSliderCL(JLabel label)
		{
			this.label = label;
		}

		public void stateChanged(ChangeEvent event)
		{
			JSlider source = (JSlider)event.getSource();
			if (!source.getValueIsAdjusting())
			{
				displayedEntriesLimit = (int)source.getValue();
				label.setText(ENTRIES_LIMITER_TEXT.replace("<LIMIT>", String.valueOf(displayedEntriesLimit)));
				refreshPanel();
			}
		}
	}

	public DataPanel(DataEntry.DataRowSorting sortingComplete, DataBundle.DataRowSorting sortingBundled, Interval view)
	{
		super(new BorderLayout());
		this.sortingComplete = sortingComplete;
		this.sortingBundled = sortingBundled;
		this.view = view;
		recreatePanel();
	}
	public DataPanel()
	{
		this(DEFAULT_DATA_ROW_SORTING_COMPLETE, DEFAULT_DATA_ROW_SORTING_BUNDLED, DEFAULT_VIEW);
	}

	private void adjustComponentMap(DataEntry.DataRowType dataRowType, JComponent oldComponent, JComponent newComponent)
	{
		switch (dataRowType)
		{
			case TYPE:
				biMapTypeCompToSubtypeComp.put(newComponent, biMapTypeCompToSubtypeComp.get(oldComponent));
				break;
			case SUBTYPE:
				biMapTypeCompToSubtypeComp.put(biMapTypeCompToSubtypeComp.getKey(oldComponent), newComponent);
				break;
			case DATE:
				biMapDateCompToUntilComp.put(newComponent, biMapDateCompToUntilComp.get(oldComponent));
				break;
			case REPEAT:
				biMapRepeatCompToDurationComp.put(newComponent, biMapRepeatCompToDurationComp.get(oldComponent));
				break;
			case DURATION:
				biMapRepeatCompToDurationComp.put(biMapRepeatCompToDurationComp.getKey(oldComponent), newComponent);
				biMapDurationCompToUntilComp.put(newComponent, biMapDurationCompToUntilComp.get(oldComponent));
				break;
			case UNTIL:
				biMapDateCompToUntilComp.put(biMapDateCompToUntilComp.getKey(oldComponent), newComponent);
				biMapDurationCompToUntilComp.put(biMapDurationCompToUntilComp.getKey(oldComponent), newComponent);
				break;
			default:
				break;
		}
	}

	private JButton createDataFieldButton(DataEntry.DataRowType dataRowType, DataEntry dataEntry, Border dataBorder) throws Exception
	{
		int alignment;
		switch (dataRowType)
		{
			case MONEY:
				alignment = SwingConstants.RIGHT;
				break;
			default:
				alignment = SwingConstants.LEFT;
				break;
		}
		var name = dataRowType.toString();
		var text = dataEntry.getDataRowValueAsText(dataRowType);
		var button = new JButton(text);
		button.setHorizontalAlignment(alignment);
		button.setToolTipText(name + ": " + text);
		button.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, DATA_FIELD_HEIGHT));
		button.setBorder(dataBorder);
		button.setContentAreaFilled(false);
		button.addActionListener(new DataFieldButtonAL(dataRowType, button, dataEntry));
		return button;
	}

	private void deactivateActiveDataField(boolean overwriteData) throws Exception
	{
		// Overwrite data if required:
		if (overwriteData)
		{
			// Extract entered value and adjust data entry:
			activeDataField.dataEntry.setValue(activeDataField.dataRowType, activeDataField.dataField.getValue());
			// Write database to json:
			MainFrame.writeDatabaseFile();
		}
		// Remove data field:
		var component = activeDataField.dataField.getJComponent();
		var dataPanel = component.getParent();
		dataPanel.remove(component);
		// Add button:
		constraints.gridx = activeDataField.gridx;
		constraints.gridy = activeDataField.gridy;
		var button = createDataFieldButton(activeDataField.dataRowType, activeDataField.dataEntry, activeDataField.dataBorder);
		dataPanel.add(button, constraints);
		// Adjust global variables:
		adjustComponentMap(activeDataField.dataRowType, component, button);
		activeDataField = null;
		// Refresh data panel:
		revalidate();
		repaint();
	}

	private void createCompletePanel(GridBagConstraints constraints, CompoundBorder dataBorder, CompoundBorder headerBorder) throws Exception
	{
		// Create header panel:
		var headerPanel = new JPanel(new GridBagLayout());
		constraints.gridx = 0;
		constraints.gridy = 0;
		// Create header buttons:
		JButton button;
		for (var dataRowType : DataEntry.DataRowType.Data.values)
		{
			var name = dataRowType.toString();
			var text = HEADER_TEXT.replace("<NAME>", name);
			if (dataRowType == sortingComplete.row)
			{
				switch (sortingComplete.mode)
				{
					case UPWARD:
						text = "[^] " + text;
						break;
					case DOWNWARD:
						text = "[v] " + text;
						break;
				}
			}
			button = new JButton(text);
			button.setFocusPainted(false);
			button.setHorizontalAlignment(SwingConstants.LEFT);
			button.setToolTipText(HEADER_TOOLTIP.replace("<NAME>", name));
			button.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, HEADER_HEIGHT));
			button.setBorder(headerBorder);
			button.addActionListener(new HeaderButtonCompleteAL(dataRowType));
			headerPanel.add(button, constraints);

			constraints.gridx++;
		}
		button = new JButton("[x]:");
		button.setFocusPainted(false);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setToolTipText("Remove buttons");
		button.setPreferredSize(new Dimension(DATA_FIELD_HEIGHT, HEADER_HEIGHT));
		button.setBorder(headerBorder);
		headerPanel.add(button, constraints);

		// Create data panel:
		var dataPanel = new JPanel(new GridBagLayout());
		constraints.gridx = 0;
		constraints.gridy = 0;
		// Create data field buttons:
		JButton buttons[] = new JButton[DataEntry.DataRowType.Data.length];
		var dataEntryCounter = 0;
		for (var dataEntry : MainFrame.getDataEntries(sortingComplete))
		{
			if (dataEntryCounter >= displayedEntriesLimit)
			{
				break;
			}
			constraints.gridx = 0;
			for (var dataRowType : DataEntry.DataRowType.Data.values)
			{
				buttons[dataRowType.toInt()] = createDataFieldButton(dataRowType, dataEntry, dataBorder);
				dataPanel.add(buttons[dataRowType.toInt()], constraints);

				constraints.gridx++;
			}
			biMapTypeCompToSubtypeComp.put(buttons[DataEntry.DataRowType.TYPE.toInt()], buttons[DataEntry.DataRowType.SUBTYPE.toInt()]);
			biMapDateCompToUntilComp.put(buttons[DataEntry.DataRowType.DATE.toInt()], buttons[DataEntry.DataRowType.UNTIL.toInt()]);
			biMapRepeatCompToDurationComp.put(buttons[DataEntry.DataRowType.REPEAT.toInt()], buttons[DataEntry.DataRowType.DURATION.toInt()]);
			biMapDurationCompToUntilComp.put(buttons[DataEntry.DataRowType.DURATION.toInt()], buttons[DataEntry.DataRowType.UNTIL.toInt()]);

			// Create remove button:
			button = new JButton("[x]");
			button.setHorizontalAlignment(SwingConstants.LEFT);
			button.setToolTipText("Remove data entry");
			button.setPreferredSize(new Dimension(DATA_FIELD_HEIGHT, DATA_FIELD_HEIGHT));
			button.setBorder(dataBorder);
			// button.setContentAreaFilled(false);
			// TODO: add action listener for removal
			dataPanel.add(button, constraints);

			constraints.gridy++;
			dataEntryCounter++;
		}

		// Put the panels in a scroll pane:
		scroller = new JScrollPane();
		scroller.setViewportView(dataPanel);
		scroller.setColumnHeaderView(headerPanel);
		scroller.setPreferredSize(new Dimension(DATA_FIELD_WIDTH * (DataEntry.DataRowType.Data.length + 1), DATA_PANEL_HIGHT));  // Add one entry to width to avoid a width scrollbar
		scroller.getVerticalScrollBar().setUnitIncrement(VERTICAL_SCROLL_SPEED);
		add(scroller, BorderLayout.CENTER);
	}

	private void createBundledPanel(GridBagConstraints constraints, CompoundBorder dataBorder, CompoundBorder headerBorder) throws Exception
	{
		// Create header panel:
		var headerPanel = new JPanel(new GridBagLayout());
		constraints.gridx = 0;
		constraints.gridy = 0;
		// Create header buttons:
		for (var dataRowType : DataBundle.DataRowType.Data.values)
		{
			var name = dataRowType.toString();
			var text = HEADER_TEXT.replace("<NAME>", name);
			if (dataRowType == sortingBundled.row)
			{
				switch (sortingBundled.mode)
				{
					case UPWARD:
						text = "[^] " + text;
						break;
					case DOWNWARD:
						text = "[v] " + text;
						break;
				}
			}
			var button = new JButton(text);
			button.setFocusPainted(false);
			button.setHorizontalAlignment(SwingConstants.LEFT);
			button.setToolTipText(HEADER_TOOLTIP.replace("<NAME>", name));
			button.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, HEADER_HEIGHT));
			button.setBorder(headerBorder);
			button.addActionListener(new HeaderButtonBundledAL(dataRowType));
			headerPanel.add(button, constraints);

			constraints.gridx++;
		}

		// Create data panel:
		var dataPanel = new JPanel(new GridBagLayout());
		constraints.gridx = 0;
		constraints.gridy = 0;
		// Unpack repeate entries:
		var sorting = new DataEntry.DataRowSorting(DataEntry.DataRowType.DATE, DataEntry.DataRowSorting.Mode.UPWARD);
		var dataEntries = MainFrame.getDataEntries(sorting);
		var end = dataEntries.getLast().getDate();
		var unpackedAddList = new LinkedList<DataEntry>();
		for (var dataEntry : dataEntries)
		{
			dataEntry.unpackToList(unpackedAddList, end);
		}
		dataEntries.addAll(unpackedAddList);
		Collections.sort(dataEntries, new DataEntry.DataComparator(sorting));
		// Bundle entries based on view settings:
		var dataBundles = new LinkedList<DataBundle>();
		DataBundle dataBundle = dataEntries.pop().createNewDataBundle(view);
		dataBundles.add(dataBundle);
		for (var dataEntry : dataEntries)
		{
			while (dataEntry.tryAddToDataBundle(dataBundle) == false)
			{
				if (showEmptyEntries == false && dataBundle.hasEntries() == false)
				{
					dataBundles.removeLast();
				}
				dataBundle = dataEntry.createNextDataBundle(dataBundle, view);
				dataBundles.add(dataBundle);
			}
		}
        Collections.sort(dataBundles, new DataBundle.DataComparator(sortingBundled));
		// Create data field labels:
		var dataEntryCounter = 0;
		for (var sortedDataBundle : dataBundles)
		{
			if (dataEntryCounter >= displayedEntriesLimit)
			{
				break;
			}
			constraints.gridx = 0;
			for (var dataRowType : DataBundle.DataRowType.Data.values)
			{
				int alignment;
				switch (dataRowType)
				{
					case BALANCE:
						alignment = JLabel.RIGHT;
						break;
					default:
						alignment = JLabel.LEFT;
						break;
				}
				var name = dataRowType.toString();
				var text = sortedDataBundle.getDataRowValueAsText(dataRowType);
				var label = new JLabel(text, alignment);
				label.setToolTipText(name + ": " + text);
				label.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, DATA_FIELD_HEIGHT));
				label.setBorder(dataBorder);
				dataPanel.add(label, constraints);

				constraints.gridx++;
			}

			constraints.gridy++;
			dataEntryCounter++;
		}

		// Put the panels in a scroll pane:
		scroller = new JScrollPane();
		scroller.setViewportView(dataPanel);
		scroller.setColumnHeaderView(headerPanel);
		scroller.setPreferredSize(new Dimension(DATA_FIELD_WIDTH * (DataBundle.DataRowType.Data.length + 1), DATA_PANEL_HIGHT));  // Add one entry to width to avoid a width scrollbar
		scroller.getVerticalScrollBar().setUnitIncrement(VERTICAL_SCROLL_SPEED);
		add(scroller, BorderLayout.CENTER);
	}

	private void recreatePanel()
	{
		try
		{
			// Remove old content:
			this.removeAll();

			constraints = new GridBagConstraints();
			// Define default constraints:
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 0.5;
			constraints.weighty = 0.5;

			// Define borders:
			var innerPaddingBorder = new EmptyBorder(BORDER_INNER_PADDING_SIZE, BORDER_INNER_PADDING_SIZE, BORDER_INNER_PADDING_SIZE, BORDER_INNER_PADDING_SIZE);
			var outerPaddingBorder = new EmptyBorder(BORDER_OUTER_PADDING_SIZE, BORDER_OUTER_PADDING_SIZE, BORDER_OUTER_PADDING_SIZE, BORDER_OUTER_PADDING_SIZE);
			var dataBorder = BorderFactory.createCompoundBorder(outerPaddingBorder, BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.LOWERED), innerPaddingBorder));
			var headerBorder = BorderFactory.createCompoundBorder(outerPaddingBorder, BorderFactory.createCompoundBorder(new LineBorder(Color.black), innerPaddingBorder));

			// Create new content panel:
			if (view == Interval.NEVER)
			{
				createCompletePanel(constraints, dataBorder, headerBorder);
			}
			else
			{
				createBundledPanel(constraints, dataBorder, headerBorder);
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public void refreshPanel()
	{
		recreatePanel();
		revalidate();
		repaint();
	}

	public boolean reactOnKeyStroke(KeyboardFocusManager kfm, KeyEvent event) throws Exception
	{
		if (event.getKeyCode() == KeyEvent.VK_ENTER && event.getModifiersEx() == 0 && event.getID() == KeyEvent.KEY_PRESSED)
		{
			// Deactivate current active data field if existent and handle event appropriately:
			if (activeDataField != null)
			{
				if (activeDataField.dataField instanceof InputPanel.TextDataField)
				{
					event.consume();  // Stop tab spaces in text fields that are caused by enter key commands
				}
				else if (activeDataField.dataField instanceof InputPanel.DateDataField)
				{
					kfm.redispatchEvent(((InputPanel.DateDataField)activeDataField.dataField).getTextField(), event);
				}
				else
				{
					kfm.redispatchEvent(activeDataField.dataField.getJComponent(), event);
				}
				deactivateActiveDataField(true);
				return true;
			}
		}
		else if (event.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			System.exit(0);
		}
		else if (event.getKeyCode() == KeyEvent.VK_CONTROL)
		{
			if (event.getID() == KeyEvent.KEY_PRESSED)
			{
				scroller.getVerticalScrollBar().setUnitIncrement(VERTICAL_SCROLL_SPEED_MULTIPLIER * VERTICAL_SCROLL_SPEED);
			}
			else if (event.getID() == KeyEvent.KEY_RELEASED)
			{
				scroller.getVerticalScrollBar().setUnitIncrement(VERTICAL_SCROLL_SPEED);
			}
		}
		return false;
	}

	public JMenuBar createMenuBar()
	{
		// Create menu bar:
		var menuBar = new JMenuBar();

		// Add settings menu:
        var menu = new JMenu(SETTINGS_MENU_TEXT);
        menu.setMnemonic(KeyEvent.VK_S);
        menu.getAccessibleContext().setAccessibleDescription(SETTINGS_MENU_DESCRIPTION);
		menuBar.add(menu);
		// Add view submenu:
        menu.addSeparator();
        var submenu = new JMenu(VIEW_SUBMENU_TEXT);
        submenu.setMnemonic(KeyEvent.VK_V);
        menu.add(submenu);
		// Create show empty entries check box item:
		JMenuItem menuItem = new JCheckBoxMenuItem(SHOW_EMPTY_ENTRIES_TEXT, showEmptyEntries);
		menuItem.addActionListener(new SettingsMenuAL("ShowEmptyEntries"));
		submenu.add(menuItem);
		// Create limiter slider:
		submenu.addSeparator();
		var label = new JLabel(ENTRIES_LIMITER_TEXT.replace("<LIMIT>", String.valueOf(displayedEntriesLimit)), SwingConstants.LEFT);
		submenu.add(label);
		JSlider slider = new JSlider(JSlider.HORIZONTAL, ENTRIES_LIMIT_MIN, ENTRIES_LIMIT_MAX, ENTRIES_LIMIT_DEFAULT);
		slider.setMinorTickSpacing(ENTRIES_LIMITER_MINOR_SPACING);
		slider.setMajorTickSpacing(ENTRIES_LIMITER_MAJOR_SPACING);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setFont(new Font("Serif", Font.ITALIC, ENTRIES_LIMITER_FONT_SIZE));
		slider.addChangeListener(new LimiterSliderCL(label));
		submenu.add(slider);
		// Add software version submenu:
        menu.addSeparator();
        submenu = new JMenu(VERSION_SUBMENU_TEXT);
        submenu.setMnemonic(KeyEvent.VK_S);
        menu.add(submenu);
		// Create version labels:
		label = new JLabel(VERSION_TEXT_MAJOR.replace("<VERSION>", String.valueOf(MainFrame.VERSION_MAJOR)));
		label.setFont(label.getFont().deriveFont(Font.ITALIC));
        label.setBorder(BorderFactory.createEmptyBorder(VERSION_PADDING_SIZE, VERSION_PADDING_SIZE, VERSION_PADDING_SIZE, VERSION_PADDING_SIZE));
		submenu.add(label);
		label = new JLabel(VERSION_TEXT_MINOR.replace("<VERSION>", String.valueOf(MainFrame.VERSION_MINOR)));
		label.setFont(label.getFont().deriveFont(Font.ITALIC));
        label.setBorder(BorderFactory.createEmptyBorder(VERSION_PADDING_SIZE, VERSION_PADDING_SIZE, VERSION_PADDING_SIZE, VERSION_PADDING_SIZE));
		submenu.add(label);
		label = new JLabel(VERSION_TEXT_PATCH.replace("<VERSION>", String.valueOf(MainFrame.VERSION_PATCH)));
		label.setFont(label.getFont().deriveFont(Font.ITALIC));
        label.setBorder(BorderFactory.createEmptyBorder(VERSION_PADDING_SIZE, VERSION_PADDING_SIZE, VERSION_PADDING_SIZE, VERSION_PADDING_SIZE));
		submenu.add(label);

		// Add bundle menu:
        menu = new JMenu(BUNDLE_MENU_TEXT);
        menu.setMnemonic(KeyEvent.VK_B);
        menu.getAccessibleContext().setAccessibleDescription(BUNDLE_MENU_DESCRIPTION);
		menuBar.add(menu);
		// Create options:
		var group = new ButtonGroup();
		for (var interval : Interval.Data.values)
		{
			menuItem = new JRadioButtonMenuItem(interval.toString());
			if (interval == Interval.NEVER)
			{
				menuItem.setSelected(true);
			}
			menuItem.setMnemonic(SETTINGS_VIEW_MNEMONICS[interval.toInt()]);
			menuItem.addActionListener(new BundleMenuAL(interval));
			group.add(menuItem);
			menu.add(menuItem);
		}

		return menuBar;
	}
}