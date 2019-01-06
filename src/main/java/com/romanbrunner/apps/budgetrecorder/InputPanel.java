package com.romanbrunner.apps.budgetrecorder;

import java.awt.GridLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.List;
import static java.util.Arrays.asList;


@SuppressWarnings("serial")
public class InputPanel extends JPanel
{
	final static int labelWidth = 200;
	final static int labelHeight = 50;
	final static List<String> labelNames = asList("Money", "Name", "Type", "Date", "Repeat");

	public InputPanel()
	{
		super(new GridLayout(labelNames.size(), 1));

		// Add labels:
		for (var name : labelNames)
		{
			var label = new JLabel(name, JLabel.CENTER);
			label.setToolTipText("A label containing only text");
			label.setPreferredSize(new Dimension(labelWidth, labelHeight));
			// label.setVerticalTextPosition(JLabel.BOTTOM);
			// label.setHorizontalTextPosition(JLabel.CENTER);
			add(label);
		}
	}
}