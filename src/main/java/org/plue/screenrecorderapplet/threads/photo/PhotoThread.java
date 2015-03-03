package org.plue.screenrecorderapplet.threads.photo;

import org.apache.commons.lang.StringUtils;
import org.plue.screenrecorderapplet.exceptions.ScreenRecorderException;
import org.plue.screenrecorderapplet.exceptions.UnknownOperatingSystemException;
import org.plue.screenrecorderapplet.models.AppletParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

/**
 * @author paolo86@altervista.org
 */
public abstract class PhotoThread extends Thread
{
	private static final Logger logger = LoggerFactory.getLogger(PhotoThread.class);

	protected AppletParameters appletParameters;

	protected String outputFileFullPath;

	private Process recordingProcess;

	protected PhotoThread(String outputFileFullPath)
	{
		logger.debug("# called constructor");

		try {
			appletParameters = AppletParameters.getInstance();
			this.outputFileFullPath = outputFileFullPath;
		} catch(Exception e) {
			logger.error("Error while initializing PhotoThread", e);
		}

		logger.debug("# completed constructor");
	}

	public static PhotoThread newInstance(String outputFileFullPath) throws IOException, ScreenRecorderException
	{
		logger.debug("# called newInstance");

		AppletParameters appletParameters = AppletParameters.getInstance();
		Class<? extends PhotoThread> photoThreadClass = appletParameters.getPhotoThreadClass();
		if(photoThreadClass == null) {
			logger.error("Unknown or unsupported operating system: '" + appletParameters.getOperatingSystem().toString() + "'");
			throw new UnknownOperatingSystemException();
		}

		try {
			logger.debug("Using class " + photoThreadClass.getSimpleName());
			Constructor<? extends PhotoThread> constructor = photoThreadClass.getDeclaredConstructor(String.class);
			constructor.setAccessible(true);
			PhotoThread photoThread = constructor.newInstance(outputFileFullPath);
			logger.debug("# completed newInstance");

			return photoThread;
		} catch(NoSuchMethodException e) {
			logger.error("Error while initializing PhotoThread", e);
			throw new RuntimeException(e);
		} catch(IllegalAccessException e) {
			logger.error("Error while initializing PhotoThread", e);
			throw new RuntimeException(e);
		} catch(InstantiationException e) {
			logger.error("Error while initializing PhotoThread", e);
			throw new RuntimeException(e);
		} catch(InvocationTargetException e) {
			logger.error("Error while initializing PhotoThread", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run()
	{
		logger.debug("# called run");

		try {
			// can have problem with file permissions when methods are invoked via Javascript even if applet is signed,
			// thus some code needs to wrapped in a privileged block
			AccessController.doPrivileged(new PrivilegedAction<Object>()
			{
				@Override
				public Object run()
				{
					runInternal();

					return null;
				}

				private void runInternal()
				{
					try {
						String command = getFFmpegCommand();

						List<String> ffmpegArgs = Arrays.asList(StringUtils.split(command, " "));

						logger.info("Executing command: " + command);
						ProcessBuilder pb = new ProcessBuilder(ffmpegArgs);

						if(recordingProcess != null) {
							recordingProcess.destroy();
						}

						recordingProcess = pb.start();

						recordingProcess.waitFor();

						logger.info("Photo taken");
					} catch(Exception e) {
						logger.error("Cannot take photo", e);
					}
				}
			});
		} catch(Exception e) {
			logger.error("Cannot take photo", e);
			return;
		}

		logger.debug("# completed run");
	}

	protected abstract String getFFmpegCommand() throws ScreenRecorderException;
}
