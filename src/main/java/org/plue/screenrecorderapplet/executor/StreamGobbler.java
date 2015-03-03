package org.plue.screenrecorderapplet.executor;

import java.io.*;

/**
 * @author paolo86@altervista.org
 */
public class StreamGobbler implements Runnable
{
	private final InputStream inputStream;

	private final OutputStream outputStream;

	public StreamGobbler(InputStream inputStream, OutputStream outputStream)
	{
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}

	public void run()
	{
		PrintWriter writer = new PrintWriter(outputStream);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		try {
			read(reader, writer);
		} catch(IOException e) {
			throw new CommandUncheckedException(e);
		}
	}

	private void read(BufferedReader reader, PrintWriter writer) throws IOException
	{
		String line;
		while((line = reader.readLine()) != null) {
			writer.println(line);
		}
		writer.flush();
	}
}
