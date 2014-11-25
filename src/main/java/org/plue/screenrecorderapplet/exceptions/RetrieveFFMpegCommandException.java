package org.plue.screenrecorderapplet.exceptions;

/**
 * @author p.cortis@sinossi.it
 */
public class RetrieveFFMpegCommandException extends ScreenRecorderException
{
	private RetrieveFFMpegCommandException()
	{
	}

	public RetrieveFFMpegCommandException(String message)
	{
		super(message);
	}

	private RetrieveFFMpegCommandException(String message, Throwable cause)
	{
		super(message, cause);
	}

	private RetrieveFFMpegCommandException(Throwable cause)
	{
		super(cause);
	}
}
