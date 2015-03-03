package org.plue.screenrecorderapplet.executor;

import org.apache.log4j.Logger;

import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles output from processes.
 *
 * @author Daniel Dixon (http://www.danieldixon.com)
 * @author paolo86@altervista.org
 */
public class StreamEventGobbler extends Thread
{
	private static Logger logger = Logger.getLogger(StreamEventGobbler.class);

	private InputStream inputStream;

	private String prefix;

	private Map<String, EventListenerList> listeners;

	public StreamEventGobbler(InputStream inputStream, String prefix)
	{
		this.inputStream = inputStream;
		this.prefix = prefix;
		this.listeners = new HashMap<String, EventListenerList>();
	}

	@Override
	public void run()
	{
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while((line = bufferedReader.readLine()) != null) {
				logger.info(prefix + ": " + line);

				for(String word : listeners.keySet()) {
					if(line.contains(word)) {
						fireActionPerformed(word, line);
					}
				}
			}
		} catch(IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void addActionListener(String word, ActionListener actionListener)
	{
		if(!listeners.containsKey(word)) {
			listeners.put(word, new EventListenerList());
		}

		listeners.get(word).add(ActionListener.class, actionListener);
	}

	public void removeActionListener(ActionListener actionListener)
	{
		for(EventListenerList eventListener : listeners.values()) {
			eventListener.remove(ActionListener.class, actionListener);
		}
	}

	private void fireActionPerformed(String word, String line)
	{
		// Guaranteed to return a non-null array
		Object[] forWord = listeners.get(word).getListenerList();
		ActionEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for(int i = forWord.length - 2; i >= 0; i -= 2) {
			if(forWord[i] == ActionListener.class) {
				// Lazily create the event:
				if(e == null) {
					e = new ActionEvent(StreamEventGobbler.this, ActionEvent.ACTION_PERFORMED, line);
				}

				((ActionListener) forWord[i + 1]).actionPerformed(e);
			}
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		inputStream.close();
	}
}
