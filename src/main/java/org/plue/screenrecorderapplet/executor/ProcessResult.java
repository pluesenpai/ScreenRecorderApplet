package org.plue.screenrecorderapplet.executor;

/**
 * @author paolo86@altervista.org
 */
public class ProcessResult
{
	private Integer returnCode;

	private String stdout;

	private String stderr;

	public ProcessResult()
	{
	}

	public ProcessResult(Integer returnCode, String stdout, String stderr)
	{
		this.returnCode = returnCode;
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public Integer getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(Integer returnCode)
	{
		this.returnCode = returnCode;
	}

	public String getStdout()
	{
		return stdout;
	}

	public void setStdout(String stdout)
	{
		this.stdout = stdout;
	}

	public String getStderr()
	{
		return stderr;
	}

	public void setStderr(String stderr)
	{
		this.stderr = stderr;
	}
}
