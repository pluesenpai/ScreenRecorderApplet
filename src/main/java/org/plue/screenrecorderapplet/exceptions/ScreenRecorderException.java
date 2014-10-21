package org.plue.screenrecorderapplet.exceptions;

/**
 * @author paolo86@altervista.org
 */
public abstract class ScreenRecorderException extends Exception
{
	public ScreenRecorderException()
	{
	}

	public ScreenRecorderException(String message)
	{
		super(message);
	}

	public ScreenRecorderException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ScreenRecorderException(Throwable cause)
	{
		super(cause);
	}
}
