package org.plue.screenrecorderapplet.models;

import org.apache.commons.io.FilenameUtils;
import org.plue.screenrecorderapplet.exceptions.UnknownOperatingSystemException;
import org.plue.screenrecorderapplet.utils.OperatingSystemUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * @author paolo86@altervista.org
 */
public abstract class AppletParameters
{
	private static final String PROPERTIES_FILENAME = "config.properties";

	protected final Properties properties;

	protected File baseFolder;

	protected File binFolder;

	protected File tmpFolder;

	protected File logPath;

	private static AppletParameters instance;

	public static AppletParameters getInstance() throws IOException, UnknownOperatingSystemException
	{
		if(instance == null) {
			instance = getAppletParametersInstance();
		}

		return instance;
	}

	protected AppletParameters() throws IOException
	{
		properties = new Properties();
		properties.load(AppletParameters.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));

		loadParametersInternal();
	}

	private static AppletParameters getAppletParametersInstance() throws UnknownOperatingSystemException, IOException
	{
		AppletParameters appletParameters;
		if(OperatingSystemUtils.isWindows()) {
			appletParameters = new WindowsAppletParameters();
		} else if(OperatingSystemUtils.isLinux()) {
			appletParameters = new LinuxAppletParameters();
		} else {
			throw new UnknownOperatingSystemException(MessageFormat.format("Unknown OS {0}", OperatingSystemUtils.getOSName()));
		}

		return appletParameters;
	}

	private void loadParametersInternal()
	{
		readBaseFolder();
		readLogPath();
		readBinFolder();
		readTmpFolder();
	}

	public File getBaseFolder()
	{
		return baseFolder;
	}

	public File getBinFolder()
	{
		return binFolder;
	}

	public File getLogPath()
	{
		return logPath;
	}

	public File getTmpFolder()
	{
		return tmpFolder;
	}

	protected void readLogPath()
	{
		this.logPath = new File(FilenameUtils.concat(getBaseFolder().getAbsolutePath(), "production.log"));
	}

	protected abstract void readBaseFolder();

	protected abstract void readBinFolder();

	protected abstract void readTmpFolder();

	public abstract File getFFmpegBinaryPath();

	public abstract OperatingSystem getOperatingSystem();

	public enum OperatingSystem
	{
		WINDOWS,
		LINUX,
		MAC
	}
}
