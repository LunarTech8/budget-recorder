package com.romanbrunner.apps.budgetrecorder;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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

import com.romanbrunner.apps.budgetrecorder.DataEntry.DataRowType;
import com.romanbrunner.apps.budgetrecorder.DataEntry.DataRowSorting;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
// import javax.swing.JMenuItem;


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
	private static final DataRowSorting DEFAULT_DATA_ROW_SORTING = new DataRowSorting(DataEntry.DataRowType.DATE, DataRowSorting.Mode.DOWNWARD);
	private static final int DEFAULT_VIEW = 0;
	private static final String[] SETTINGS_VIEW_NAMES = { "Complete", "Daily", "Weekly", "Monthly", "Yearly" };
	private static final int[] SETTINGS_VIEW_MNEMONICS = { KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_M, KeyEvent.VK_Y };


	// --------------------
	// Functional code
	// --------------------

	private DataRowSorting sorting;
	private int view;

	private class HeaderButtonAL implements ActionListener
	{
		private DataRowType dataRowType;

		public HeaderButtonAL(DataRowType dataRowType)
		{
			this.dataRowType = dataRowType;
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Adjust sorting:
				if (sorting.row == dataRowType)
				{
					switch (sorting.mode)
					{
						case UPWARD:
							sorting.mode = DataRowSorting.Mode.DOWNWARD;
							break;
						case DOWNWARD:
							sorting.mode = DataRowSorting.Mode.UPWARD;
							break;
					}
				}
				else
				{
					sorting.row = dataRowType;
					sorting.mode = DataRowSorting.Mode.UPWARD;
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
				// Change view based on the index of the selected radio button:
				view = index;
				// Refresh panel:
				refreshPanel();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	public DataPanel(DataRowSorting sorting, int view)
	{
		super(new BorderLayout());
		this.sorting = sorting;
		this.view = view;
		recreatePanel();
	}
	public DataPanel()
	{
		this(DEFAULT_DATA_ROW_SORTING, DEFAULT_VIEW);
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
			if (dataRowType == sorting.row)
			{
				switch (sorting.mode)
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
			button.addActionListener(new HeaderButtonAL(dataRowType));
			headerPanel.add(button, constraints);

			constraints.gridx++;
		}

		// Create data panel:
		var dataPanel = new JPanel(new GridBagLayout());
		constraints.gridx = 0;
		constraints.gridy = 0;
		// Create data field labels:
		for (var dataEntry : MainFrame.getDataEntries(sorting))
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

	private void createMonthlyPanel(GridBagConstraints constraints, CompoundBorder dataBorder, CompoundBorder headerBorder) throws Exception
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
			if (dataRowType == sorting.row)
			{
				switch (sorting.mode)
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
			button.addActionListener(new HeaderButtonAL(dataRowType));
			headerPanel.add(button, constraints);

			constraints.gridx++;
		}

		// Create data panel:
		var dataPanel = new JPanel(new GridBagLayout());
		constraints.gridx = 0;
		constraints.gridy = 0;
		// Create data field labels:
		for (var dataEntry : MainFrame.getDataEntries(sorting))
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
			else if (view == 3)
			{
				createMonthlyPanel(constraints, dataBorder, headerBorder);
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

		/* --- Placeholder ---
		// Add settings menu:
        var menu = new JMenu("Settings");
        menu.setMnemonic(KeyEvent.VK_S);
        menu.getAccessibleContext().setAccessibleDescription("General settings menu");
		menuBar.add(menu);
		// Create test item A:
		var menuItem = new JMenuItem("Test item A", KeyEvent.VK_A);
        menuItem.getAccessibleContext().setAccessibleDescription("Test item / placeholder for later use");
		menu.add(menuItem);
		// Add test submenu:
        menu.addSeparator();
        var submenu = new JMenu("Test submenu");
        submenu.setMnemonic(KeyEvent.VK_T);
        menu.add(submenu);
		// Create test item B:
		menuItem = new JMenuItem("Test item B", KeyEvent.VK_B);
		submenu.add(menuItem);
		// Create test item C:
		menuItem = new JMenuItem("Test item C", KeyEvent.VK_C);
		submenu.add(menuItem);
		*/

		// Add view menu:
        var menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        menu.getAccessibleContext().setAccessibleDescription("View selection menu");
		menuBar.add(menu);
		// Create options:
		var group = new ButtonGroup();
		for (int i = 0; i < SETTINGS_VIEW_NAMES.length; i++)
		{
			var menuItem = new JRadioButtonMenuItem(SETTINGS_VIEW_NAMES[i]);
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