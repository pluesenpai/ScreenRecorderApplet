package org.plue.screenrecorderapplet.models;

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
public class StreamGobbler extends Thread
{
	private static Logger logger = Logger.getLogger(StreamGobbler.class);

	private InputStream is;

	private boolean discardOutput;

	private String prefix;

	private Map<String, EventListenerList> listeners;

	public StreamGobbler(InputStream is, boolean discard, String prefix)
	{
		this.is = is;
		this.discardOutput = discard;
		this.prefix = prefix;
		this.listeners = new HashMap<String, EventListenerList>();
	}

	@Override
	public void run()
	{
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while((line = br.readLine()) != null) {
				if(!discardOutput) {
					logger.info(prefix + ": " + line);
				}

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

	public void addActionListener(String word, ActionListener l)
	{
		if(!listeners.containsKey(word)) {
			listeners.put(word, new EventListenerList());
		}

		listeners.get(word).add(ActionListener.class, l);
	}

	public void removeActionListener(ActionListener l)
	{
		for(EventListenerList forWord : listeners.values()) {
			forWord.remove(ActionListener.class, l);
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
					e = new ActionEvent(StreamGobbler.this, ActionEvent.ACTION_PERFORMED, line);
				}

				((ActionListener) forWord[i + 1]).actionPerformed(e);
			}
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		is.close();
	}
}
