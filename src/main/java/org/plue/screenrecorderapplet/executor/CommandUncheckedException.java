package org.plue.screenrecorderapplet.executor;

/**
 * @author paolo86@altervista.org
 */
public class CommandUncheckedException extends RuntimeException
{
	public CommandUncheckedException()
	{
	}

	public CommandUncheckedException(String message)
	{
		super(message);
	}

	public CommandUncheckedException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public CommandUncheckedException(Throwable cause)
	{
		super(cause);
	}
}
