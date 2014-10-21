package org.plue.screenrecorderapplet.exceptions;

/**
 * @author paolo86@altervista.org
 */
public class BinariesDownloadException extends ScreenRecorderException
{
	public BinariesDownloadException()
	{
	}

	public BinariesDownloadException(String message)
	{
		super(message);
	}

	public BinariesDownloadException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public BinariesDownloadException(Throwable cause)
	{
		super(cause);
	}
}
