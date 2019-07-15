package com.romanbrunner.apps.budgetrecorder;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


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
	private static final int DEFAULT_VIEW = 0;
	private static final String[] SETTINGS_VIEW_NAMES = { "Complete", "Daily", "Weekly", "Monthly", "Yearly" };
	private static final int[] SETTINGS_VIEW_MNEMONICS = { KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_M, KeyEvent.VK_Y };


	// --------------------
	// Functional code
	// --------------------

	private static boolean showEmptyEntries = true;

	private DataEntry.DataRowSorting sortingComplete;
	private DataBundle.DataRowSorting sortingBundled;
	private int view;

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

	private class ViewMenuAL implements ActionListener
	{
		private int index;

		public ViewMenuAL(int index)
		{
			this.index = index;
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				view = index;  // Change view based on the index of the selected radio button
				refreshPanel();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	public DataPanel(DataEntry.DataRowSorting sortingComplete, DataBundle.DataRowSorting sortingBundled, int view)
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

	private void createCompletePanel(GridBagConstraints constraints, CompoundBorder dataBorder, CompoundBorder headerBorder) throws Exception
	{
		// Create header panel:
		var headerPanel = new JPanel(new GridBagLayout());
		constraints.gridx = 0;
		constraints.gridy = 0;
		// Create header buttons:
		for (var dataRowType : DataEntry.DataRowType.values())
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
			var button = new JButton(text);
			button.setFocusPainted(false);
			button.setHorizontalAlignment(SwingConstants.LEFT);
			button.setToolTipText(HEADER_TOOLTIP.replace("<NAME>", name));
			button.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, HEADER_HEIGHT));
			button.setBorder(headerBorder);
			button.addActionListener(new HeaderButtonCompleteAL(dataRowType));
			headerPanel.add(button, constraints);

			constraints.gridx++;
		}

		// Create data panel:
		var dataPanel = new JPanel(new GridBagLayout());
		constraints.gridx = 0;
		constraints.gridy = 0;
		// Create data field labels:
		for (var dataEntry : MainFrame.getDataEntries(sortingComplete))
		{
			constraints.gridx = 0;
			for (var dataRowType : DataEntry.DataRowType.values())
			{
				int alignment;
				switch (dataRowType)
				{
					case MONEY:
						alignment = JLabel.RIGHT;
						break;
					default:
						alignment = JLabel.LEFT;
						break;
				}
				var name = dataRowType.toString();
				var text = dataEntry.getDataRowValueAsText(dataRowType);
				var label = new JLabel(text, alignment);
				label.setToolTipText(name + ": " + text);
				label.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, DATA_FIELD_HEIGHT));
				label.setBorder(dataBorder);
				dataPanel.add(label, constraints);

				constraints.gridx++;
			}

			constraints.gridy++;
		}

		// Put the panels in a scroll pane:
		var scroller = new JScrollPane();
		scroller.setViewportView(dataPanel);
		scroller.setColumnHeaderView(headerPanel);
		scroller.setPreferredSize(new Dimension(DATA_FIELD_WIDTH * (DataEntry.DATA_ROW_TYPE_COUNT + 1), DATA_PANEL_HIGHT));  // Add one entry to width to avoid a width scrollbar
		add(scroller, BorderLayout.CENTER);
	}

	private void createBundledPanel(GridBagConstraints constraints, CompoundBorder dataBorder, CompoundBorder headerBorder) throws Exception
	{
		// Create header panel:
		var headerPanel = new JPanel(new GridBagLayout());
		constraints.gridx = 0;
		constraints.gridy = 0;
		// Create header buttons:
		for (var dataRowType : DataBundle.DataRowType.values())
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
		// Create data field labels:
		var sorting = new DataEntry.DataRowSorting(DataEntry.DataRowType.DATE, DataEntry.DataRowSorting.Mode.UPWARD);
		var dataEntries = MainFrame.getDataEntries(sorting);
		var calendarEnd = DataBundle.dateToCalendar(dataEntries.getLast().getDate());  // TODO: test
		var unpackedAddList = new LinkedList<DataEntry>();
		for (var dataEntry : dataEntries)
		{
			dataEntry.unpackToList(unpackedAddList, calendarEnd);
		}
		dataEntries.addAll(unpackedAddList);
		Collections.sort(dataEntries, new DataEntry.DataComparator(sorting));
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
		for (var sortedDataBundle : dataBundles)
		{
			constraints.gridx = 0;
			for (var dataRowType : DataBundle.DataRowType.values())
			{
				int alignment;
				switch (dataRowType)
				{
					case MONEY:
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
		}

		// Put the panels in a scroll pane:
		var scroller = new JScrollPane();
		scroller.setViewportView(dataPanel);
		scroller.setColumnHeaderView(headerPanel);
		scroller.setPreferredSize(new Dimension(DATA_FIELD_WIDTH * (DataBundle.DATA_ROW_TYPE_COUNT + 1), DATA_PANEL_HIGHT));  // Add one entry to width to avoid a width scrollbar
		add(scroller, BorderLayout.CENTER);
	}

	private void recreatePanel()
	{
		try
		{
			// Remove old content:
			this.removeAll();

			GridBagConstraints constraints = new GridBagConstraints();
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
			if (view == 0)
			{
				createCompletePanel(constraints, dataBorder, headerBorder);
			}
			else if (view < SETTINGS_VIEW_NAMES.length)
			{
				createBundledPanel(constraints, dataBorder, headerBorder);
			}
			else
			{
				throw new Exception("ERROR: Invalid view selection");
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

	public JMenuBar createMenuBar()
	{
		// Create menu bar:
		var menuBar = new JMenuBar();

		// Add settings menu:
        var menu = new JMenu("Settings");
        menu.setMnemonic(KeyEvent.VK_S);
        menu.getAccessibleContext().setAccessibleDescription("General settings menu");
		menuBar.add(menu);
		// Add view submenu:
        menu.addSeparator();
        var submenu = new JMenu("View");
        submenu.setMnemonic(KeyEvent.VK_V);
        menu.add(submenu);
		// Create version items:
		JMenuItem menuItem = new JCheckBoxMenuItem("Show empty entries", showEmptyEntries);
		menuItem.addActionListener(new SettingsMenuAL("ShowEmptyEntries"));
		submenu.add(menuItem);
		// Add software version submenu:
        menu.addSeparator();
        submenu = new JMenu("Software Version");
        submenu.setMnemonic(KeyEvent.VK_S);
        menu.add(submenu);
		// Create version items:
		submenu.add(new JMenuItem("Major: " + MainFrame.VERSION_MAJOR));
		submenu.add(new JMenuItem("Minor: " + MainFrame.VERSION_MINOR));
		submenu.add(new JMenuItem("Patch: " + MainFrame.VERSION_PATCH));

		// Add view menu:
        menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        menu.getAccessibleContext().setAccessibleDescription("View selection menu");
		menuBar.add(menu);
		// Create options:
		var group = new ButtonGroup();
		for (int i = 0; i < SETTINGS_VIEW_NAMES.length; i++)
		{
			menuItem = new JRadioButtonMenuItem(SETTINGS_VIEW_NAMES[i]);
			if (i == 0)
			{
				menuItem.setSelected(true);
			}
			menuItem.setMnemonic(SETTINGS_VIEW_MNEMONICS[i]);
			menuItem.addActionListener(new ViewMenuAL(i));
			group.add(menuItem);
			menu.add(menuItem);
		}

		return menuBar;
	}
}