package org.plue.screenrecorderapplet.models;

import org.apache.commons.io.FilenameUtils;
import org.plue.screenrecorderapplet.constants.PropertyKeys;

import java.io.File;
import java.io.IOException;

/**
 * @author paolo86@altervista.org
 */
public class LinuxAppletParameters extends AppletParameters
{
	LinuxAppletParameters() throws IOException
	{
		super();
	}

	@Override
	protected void readBaseFolder()
	{
		String folderName = properties.getProperty(PropertyKeys.BASE_FOLDER);

		String homeFolder = System.getProperty("user.home");
		this.baseFolder = new File(FilenameUtils.concat(homeFolder, "." + folderName));
	}

	@Override
	protected void readBinFolder()
	{
		this.binFolder = new File(FilenameUtils.concat(getBaseFolder().getAbsolutePath(), "bin-linux-1.0"));
	}

	@Override
	protected void readTmpFolder()
	{
		this.tmpFolder = new File(System.getenv("TEMP"));
	}

	@Override
	public File getFFmpegBinaryPath()
	{
		return new File(FilenameUtils.concat(getBinFolder().getAbsolutePath(), "ffmpeg"));
	}

	@Override
	public OperatingSystem getOperatingSystem()
	{
		return OperatingSystem.LINUX;
	}
}
