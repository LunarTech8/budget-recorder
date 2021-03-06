package com.romanbrunner.apps.budgetrecorder;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;


public class Autocomplete implements DocumentListener
{
	// --------------------
	// Functional code
	// --------------------

	private static enum Mode
	{
		INSERT,	COMPLETION;
	};

	private List<String> keywords;
	private JTextField textField;
	private Mode mode = Mode.INSERT;
	private boolean isActive;

	/** Class for displaying found completion in the text field. */
	private class CompletionTask implements Runnable
	{
		private String completion;
		private int position;

		CompletionTask(String completion, int position)
		{
			this.completion = completion;
			this.position = position;
		}

		public void run()
		{
			StringBuffer sb = new StringBuffer(textField.getText());
			sb.insert(position, completion);
			textField.setText(sb.toString());
			textField.setCaretPosition(position + completion.length());
			textField.moveCaretPosition(position);
			mode = Mode.COMPLETION;
		}
	}

	/** Class for inserting chosen completion. */
	public class CommitAction extends AbstractAction
	{
		private static final long serialVersionUID = 5794543109646743416L;

		@Override
		public void actionPerformed(ActionEvent ev)
		{
			if (mode == Mode.COMPLETION)
			{
				StringBuffer sb = new StringBuffer(textField.getText());
				textField.setText(sb.toString());
				textField.setCaretPosition(textField.getSelectionEnd());
				mode = Mode.INSERT;
			}
			else
			{
				textField.replaceSelection("\t");
			}
		}
	}

	public Autocomplete(JTextField textField, List<String> keywords, boolean isActive)
	{
		this.textField = textField;
		this.keywords = keywords;
		Collections.sort(keywords);
		this.isActive = isActive;
	}

	@Override
	public void changedUpdate(DocumentEvent ev) {}

	@Override
	public void removeUpdate(DocumentEvent ev) {}

	@Override
	public void insertUpdate(DocumentEvent ev)
	{
		if (isActive == false || ev.getLength() != 1)
		{
			return;
		}

		int pos = ev.getOffset();
		String content = null;
		try
		{
			content = textField.getText(0, pos + 1);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}

		// Find where the word starts:
		int w;
		for (w = pos; w >= 0; w--)
		{
			if (!Character.isLetter(content.charAt(w)))
			{
				break;
			}
		}

		// Too few chars:
		if (pos - w < 1)
		{
			return;
		}

		String prefix = content.substring(w + 1);
		int n = Collections.binarySearch(keywords, prefix);
		if (n < 0 && -n <= keywords.size())
		{
			String match = keywords.get(-n - 1);
			if (match.startsWith(prefix))
			{
				// A completion is found:
				String completion = match.substring(pos - w);
				// We cannot modify Document from within notification, so we submit a task that does the change later:
				SwingUtilities.invokeLater(new CompletionTask(completion, pos + 1));
			}
		}
		else
		{
			// Nothing found:
			mode = Mode.INSERT;
		}
	}

	/** Sets keywords to the given list of strings. */
	public void setKeywords(List<String> keywords)
    {
		this.keywords = keywords;
		Collections.sort(this.keywords);
	}

	/** Adds given string to the current list of keywords if it isn't already included. */
	public void addKeyword(String keyword)
	{
		if (keywords.contains(keyword) == false)
		{
			keywords.add(keyword);
			Collections.sort(this.keywords);
		}
	}

	/** Turn active autocompletion on or off. */
	public void changeActivation(boolean turnOn)
	{
		isActive = turnOn;
	}

}