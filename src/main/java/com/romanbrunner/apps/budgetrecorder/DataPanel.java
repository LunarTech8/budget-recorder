package com.romanbrunner.apps.budgetrecorder;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import com.romanbrunner.apps.budgetrecorder.DataEntry.DataRowType;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
	private static final int BORDER_INNER_PADDING_SIZE = 10;
	private static final int BORDER_OUTER_PADDING_SIZE = 1;
	private static final String HEADER_TEXT = "<NAME>:";
	private static final String HEADER_TOOLTIP = "Shows the values for <NAME> in the fields below.";


	// --------------------
	// Functional code
	// --------------------

	private DataRowType filterRow;
	private int filterMode;

	private class HeaderButtonAL implements ActionListener
	{
		private DataPanel basePanel;
		private DataRowType filterRow;

		public HeaderButtonAL(DataPanel basePanel, DataRowType filterRow)
		{
			this.basePanel = basePanel;
			this.filterRow = filterRow;
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				// Adjust filter:
				if (basePanel.filterRow == filterRow)
				{
					if (basePanel.filterMode == 1)
					{
						basePanel.filterMode = 2;
					}
					else if (basePanel.filterMode == 2)
					{
						basePanel.filterMode = 1;
					}
				}
				else
				{
					basePanel.filterRow = filterRow;
					basePanel.filterMode = 1;
				}

				// Refresh panel:
				basePanel.recreate();
				basePanel.revalidate();
				basePanel.repaint();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	public DataPanel(DataRowType filterRow, int filterMode)
	{
		super(new BorderLayout());
		this.filterRow = filterRow;
		this.filterMode = filterMode;
		recreate();
	}
	public DataPanel()
	{
		this(DataEntry.DataRowType.DATE, 1);
	}

	private void recreate()
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

			// Create header panel:
			var headerPanel = new JPanel(new GridBagLayout());
			constraints.gridx = 0;
			constraints.gridy = 0;
			// Create header buttons:
			for (var dataRowType : DataEntry.DataRowType.values())
			{
				var name = dataRowType.toString();
				var text = HEADER_TEXT.replace("<NAME>", name);
				if (dataRowType == filterRow)
				{
					if (filterMode == 1)
					{
						text = "[^] " + text;
					}
					else if (filterMode == 2)
					{
						text = "[v] " + text;
					}
				}
				var button = new JButton(text);
				button.setFocusPainted(false);
				button.setHorizontalAlignment(SwingConstants.LEFT);
				button.setToolTipText(HEADER_TOOLTIP.replace("<NAME>", name));
				button.setPreferredSize(new Dimension(DATA_FIELD_WIDTH, HEADER_HEIGHT));
				button.setBorder(headerBorder);
				button.addActionListener(new HeaderButtonAL(this, dataRowType));
				headerPanel.add(button, constraints);

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
					var text = dataEntry.getDataRow(dataRowType);
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
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
}
