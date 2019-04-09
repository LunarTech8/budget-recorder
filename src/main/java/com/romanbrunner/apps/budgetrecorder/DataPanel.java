package com.romanbrunner.apps.budgetrecorder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JLabel;


@SuppressWarnings("serial")
class DataPanel extends JPanel
{
	// --------------------
	// Data code
	// --------------------

	private static final int DATA_FIELD_WIDTH = 120;
	private static final int DATA_FIELD_HEIGHT = 40;
	private static final int HEADER_HEIGHT = 50;
	private static final int DATA_PANEL_HIGHT = 0;//400;
	private static final String HEADER_TEXT = "<NAME>:";
	private static final String HEADER_TOOLTIP = "Shows the values for <NAME> in the fields below.";
	private static final String DATA_FIELD_TOOLTIP = "Data field value for <NAME>.";


	// --------------------
	// Functional code
	// --------------------

	public DataPanel()
	{
		super(new GridBagLayout());
		try
		{
			GridBagConstraints mainConstrains = new GridBagConstraints();
			// Define default constrains:
			mainConstrains.weightx = 0.5;
			mainConstrains.weighty = 0.5;

			mainConstrains.fill = GridBagConstraints.HORIZONTAL;
			mainConstrains.weighty = 0;
			mainConstrains.gridx = 0;
			mainConstrains.gridy = 0;
			// Create header labels:
			for (var dataRowType : DataEntry.DataRowType.values())
			{
				var name = dataRowType.toString();
				var header = new JLabel(HEADER_TEXT.replace("<NAME>", name), JLabel.LEFT);
				header.setToolTipText(HEADER_TOOLTIP.replace("<NAME>", name));
				header.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, HEADER_HEIGHT));
				add(header, mainConstrains);

				mainConstrains.gridx++;
			}

			mainConstrains.fill = GridBagConstraints.BOTH;
			mainConstrains.weighty = 1;
			mainConstrains.gridx = 0;
			mainConstrains.gridy = 1;
			mainConstrains.gridwidth = DataEntry.DATA_ROW_TYPE_COUNT;
			// Create data field labels:
			var dataRowsPanel = new JPanel(new GridBagLayout());
			GridBagConstraints dataRowsConstrains = new GridBagConstraints();
			dataRowsConstrains.fill = GridBagConstraints.BOTH;
			dataRowsConstrains.weightx = 0.5;
			dataRowsConstrains.weighty = 0.5;
			dataRowsConstrains.gridy = 0;
			for (var dataEntry : MainFrame.getDataEntries())
			{
				dataRowsConstrains.gridx = 0;
				for (var dataRowType : DataEntry.DataRowType.values())
				{
					var name = dataRowType.toString();
					var label = new JLabel(dataEntry.getDataRow(dataRowType), JLabel.LEFT);
					label.setToolTipText(DATA_FIELD_TOOLTIP.replace("<NAME>", name));
					label.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, DATA_FIELD_HEIGHT));
					dataRowsPanel.add(label, dataRowsConstrains);

					dataRowsConstrains.gridx++;
				}

				dataRowsConstrains.gridy++;
			}
			// Put the data rows panel in a scroll pane:
			var dataRowsScroller = new JScrollPane(dataRowsPanel);
			dataRowsScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			dataRowsScroller.setPreferredSize(new Dimension(DATA_FIELD_WIDTH * (DataEntry.DATA_ROW_TYPE_COUNT + 1), DATA_PANEL_HIGHT));  // Add one entry to width to avoid a width scrollbar
			add(dataRowsScroller, mainConstrains);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
}
