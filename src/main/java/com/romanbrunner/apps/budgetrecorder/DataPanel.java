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
	private static final int DATA_PANEL_HIGHT = 400;
	private static final String HEADER_TEXT = "<NAME>:";
	private static final String HEADER_TOOLTIP = "Shows the values for <NAME> in the fields below.";
	private static final String DATA_FIELD_TOOLTIP = "Data field value for <NAME>.";


	// --------------------
	// Functional code
	// --------------------

	public DataPanel()
	{
		super(new BorderLayout());
		try
		{
			GridBagConstraints constraints = new GridBagConstraints();
			// Define default constraints:
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 0.5;
			constraints.weighty = 0.5;

			// Create header panel:
			var headerPanel = new JPanel(new GridBagLayout());
			constraints.gridx = 0;
			constraints.gridy = 0;
			// Create header labels:
			for (var dataRowType : DataEntry.DataRowType.values())
			{
				var name = dataRowType.toString();
				var header = new JLabel(HEADER_TEXT.replace("<NAME>", name), JLabel.LEFT);
				header.setToolTipText(HEADER_TOOLTIP.replace("<NAME>", name));
				header.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, HEADER_HEIGHT));
				headerPanel.add(header, constraints);

				constraints.gridx++;
			}

			// Create data panel:
			var dataPanel = new JPanel(new GridBagLayout());
			constraints.gridx = 0;
			constraints.gridy = 0;
			// Create data field labels:
			for (var dataEntry : MainFrame.getDataEntries())
			{
				constraints.gridx = 0;
				for (var dataRowType : DataEntry.DataRowType.values())
				{
					var name = dataRowType.toString();
					var label = new JLabel(dataEntry.getDataRow(dataRowType), JLabel.LEFT);
					label.setToolTipText(DATA_FIELD_TOOLTIP.replace("<NAME>", name));
					label.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, DATA_FIELD_HEIGHT));
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
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
}
