package com.romanbrunner.apps.budgetrecorder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;

import javax.swing.JPanel;

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
			GridBagConstraints c = new GridBagConstraints();
			// Define default constrains:
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0.5;
			c.weighty = 0.5;

			// Create header labels:
			c.gridx = 0;
			c.gridy = 0;
			for (var dataRowType : DataEntry.DataRowType.values())
			{
				var name = dataRowType.toString();
				var header = new JLabel(HEADER_TEXT.replace("<NAME>", name), JLabel.CENTER);
				header.setToolTipText(HEADER_TOOLTIP.replace("<NAME>", name));
				header.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, HEADER_HEIGHT));
				add(header, c);

				c.gridx++;
			}
			// Create data field labels:
			for (var dataEntry : MainFrame.getDataEntries())
			{
				c.gridx = 0;
				for (var dataRowType : DataEntry.DataRowType.values())
				{
					var name = dataRowType.toString();
					var label = new JLabel(dataEntry.getDataRow(dataRowType), JLabel.CENTER);
					label.setToolTipText(DATA_FIELD_TOOLTIP.replace("<NAME>", name));
					label.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, DATA_FIELD_HEIGHT));
					add(label, c);

					c.gridx++;
				}

				c.gridy++;
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
}
