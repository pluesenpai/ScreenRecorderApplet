package org.plue.screenrecorderapplet.exceptions;

/**
 * @author paolo86@altervista.org
 */
public class FFMpegException extends ScreenRecorderException
{
	public FFMpegException()
	{
	}

	public FFMpegException(String message)
	{
		super(message);
	}

	public FFMpegException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public FFMpegException(Throwable cause)
	{
		super(cause);
	}
}
