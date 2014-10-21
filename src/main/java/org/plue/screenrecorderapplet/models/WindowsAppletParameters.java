package org.plue.screenrecorderapplet.models;

import org.apache.commons.io.FilenameUtils;
import org.plue.screenrecorderapplet.constants.PropertyKeys;

import java.io.File;
import java.io.IOException;

/**
 * @author paolo86@altervista.org
 */
public class WindowsAppletParameters extends AppletParameters
{
	WindowsAppletParameters() throws IOException
	{
		super();
	}

	@Override
	protected void readBaseFolder()
	{
		String folderName = properties.getProperty(PropertyKeys.BASE_FOLDER);

		String tempFolder = System.getenv("TEMP");
		this.baseFolder = new File(FilenameUtils.concat(tempFolder, folderName));
	}

	@Override
	protected void readBinFolder()
	{
		this.binFolder = new File(FilenameUtils.concat(getBaseFolder().getAbsolutePath(), "bin-windows-1.0"));
	}

	@Override
	protected void readTmpFolder()
	{
		this.tmpFolder = new File(System.getenv("TEMP"));
	}

	@Override
	public File getFFmpegBinaryPath()
	{
		return new File(FilenameUtils.concat(getBinFolder().getAbsolutePath(), "ffmpeg.exe"));
	}

	@Override
	public OperatingSystem getOperatingSystem()
	{
		return OperatingSystem.WINDOWS;
	}
}
