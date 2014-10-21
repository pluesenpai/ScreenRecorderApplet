package org.plue.screenrecorderapplet.exceptions;

/**
 * @author paolo86@altervista.org
 */
public class UnknownOperatingSystemException extends ScreenRecorderException
{
	public UnknownOperatingSystemException()
	{
	}

	public UnknownOperatingSystemException(String message)
	{
		super(message);
	}

	public UnknownOperatingSystemException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public UnknownOperatingSystemException(Throwable cause)
	{
		super(cause);
	}
}
