package org.plue.screenrecorderapplet.exceptions;

/**
 * @author paolo86@altervista.org
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

	public RetrieveFFMpegCommandException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public RetrieveFFMpegCommandException(Throwable cause)
	{
		super(cause);
	}
}
