package org.plue.screenrecorderapplet.executor;

/**
 * @author paolo86@altervista.org
 */
public class CommandException extends Exception
{
	public CommandException()
	{
	}

	public CommandException(String message)
	{
		super(message);
	}

	public CommandException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public CommandException(Throwable cause)
	{
		super(cause);
	}
}
