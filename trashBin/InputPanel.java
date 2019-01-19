// package com.romanbrunner.apps.budgetrecorder;

// import java.awt.GridLayout;
// import java.awt.Dimension;
// import javax.swing.JLabel;
// import javax.swing.JPanel;
// import java.util.List;
// import static java.util.Arrays.asList;


// @SuppressWarnings("serial")
// public class InputPanel extends JPanel
// {
// 	final static int labelWidth = 200;
// 	final static int labelHeight = 50;
// 	final static List<String> labelNames = asList("Money", "Name", "Location", "Type", "Date", "Repeat");
// 	final static int rows = labelNames.size();
// 	final static int cols = 2;

// 	private static int getIndex(int row, int col)
// 	{
// 		return row * cols + col;
// 	}

// 	public InputPanel()
// 	{
// 		super(new GridLayout(rows, cols));

// 		// Add labels:
// 		var currentRow = 0;
// 		for (var name : labelNames)
// 		{
// 			var label = new JLabel(name, JLabel.CENTER);
// 			label.setToolTipText("A label containing only text");
// 			label.setPreferredSize(new Dimension(labelWidth, labelHeight));
// 			// label.setVerticalTextPosition(JLabel.BOTTOM);
// 			// label.setHorizontalTextPosition(JLabel.CENTER);
// 			System.out.println(getIndex(currentRow++, 0));
// 			add(label);
// 			// add(label, getIndex(currentRow++, 0));
// 		}
// 	}
// }