package org.plue.screenrecorderapplet.executor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author paolo86@altervista.org
 */
public class CommandExecutor
{
	private final String[] command;

	public CommandExecutor(String[] command)
	{
		this.command = command;
	}

	public ProcessResult run() throws InterruptedException, IOException
	{
		return run(null, null);
	}

	public ProcessResult run(File executionDirectory, String[] environmentVariables) throws InterruptedException, IOException
	{
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(command, environmentVariables, executionDirectory);

		OutputStream outputStream = new ByteArrayOutputStream();
		Thread outputThread = new Thread(new StreamGobbler(process.getInputStream(), outputStream));
		outputThread.start();

		OutputStream errorStream = new ByteArrayOutputStream();
		Thread errorThread = new Thread(new StreamGobbler(process.getErrorStream(), errorStream));
		errorThread.start();

		int exitCode = process.waitFor();

		outputThread.join();
		outputStream.close();

		errorThread.join();
		errorStream.close();

		ProcessResult processResult = new ProcessResult();
		processResult.setReturnCode(exitCode);
		processResult.setStdout(outputStream.toString());
		processResult.setStderr(errorStream.toString());

		return processResult;
	}
}
