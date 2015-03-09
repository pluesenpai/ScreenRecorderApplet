package org.plue.screenrecorderapplet.models;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.plue.screenrecorderapplet.AfterRecording;
import org.plue.screenrecorderapplet.Applet;
import org.plue.screenrecorderapplet.exceptions.UnknownOperatingSystemException;
import org.plue.screenrecorderapplet.models.json.Extension;
import org.plue.screenrecorderapplet.models.json.JSONExtensions;
import org.plue.screenrecorderapplet.threads.photo.LinuxPhotoThread;
import org.plue.screenrecorderapplet.threads.photo.PhotoThread;
import org.plue.screenrecorderapplet.threads.photo.WindowsPhotoThread;
import org.plue.screenrecorderapplet.threads.record.RecorderThread;
import org.plue.screenrecorderapplet.threads.record.WindowsRecorderThread;
import org.plue.screenrecorderapplet.utils.OperatingSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author paolo86@altervista.org
 */
public abstract class AppletParameters
{
	private static final Logger logger = LoggerFactory.getLogger(AppletParameters.class);

	private static final String PROPERTIES_FILENAME = "config.properties";

	protected final Properties properties;

	protected File baseFolder;

	protected File binFolder;

	protected File tmpFolder;

	private File logPath;

	private Extensions extensions;

	private static AppletParameters instance;

	private static Class<? extends RecorderThread> recorderThreadClass = null;

	private static Class<? extends PhotoThread> photoThreadClass = null;

	public static AppletParameters getInstance() throws IOException, UnknownOperatingSystemException
	{
		logger.debug("# called getInstance");

		if(instance == null) {
			logger.debug("Applet parameters instance was null. Creating new one");
			instance = prepareOsSpecificClasses();
		}

		logger.debug("# completed getInstance");

		return instance;
	}

	private static AppletParameters prepareOsSpecificClasses() throws UnknownOperatingSystemException, IOException
	{
		logger.debug("# called prepareOsSpecificClasses");

		AppletParameters appletParameters;
		if(OperatingSystemUtils.isWindows()) {
			logger.info("Using WindowsAppletParameters");
			appletParameters = new WindowsAppletParameters();
			photoThreadClass = WindowsPhotoThread.class;
			recorderThreadClass = WindowsRecorderThread.class;
		} else if(OperatingSystemUtils.isLinux()) {
			logger.info("Using LinuxAppletParameters");
			appletParameters = new LinuxAppletParameters();
			photoThreadClass = LinuxPhotoThread.class;
			recorderThreadClass = WindowsRecorderThread.class;
		} else {
			logger.error("Unknown or unsupported operating system: '" + OperatingSystemUtils.getOSName() + "'");
			throw new UnknownOperatingSystemException(
					MessageFormat.format("Unknown OS {0}", OperatingSystemUtils.getOSName()));
		}

		logger.debug("# completed prepareOsSpecificClasses");

		return appletParameters;
	}

	protected AppletParameters() throws IOException
	{
		logger.debug("# called constructor");

		logger.info("Loading properties file '" + PROPERTIES_FILENAME + "'");
		properties = new Properties();
		properties.load(AppletParameters.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));

		loadParametersInternal();

		logger.debug("# completed constructor");
	}

	private void loadParametersInternal()
	{
		logger.debug("# called loadParametersInternal");

		readBaseFolder();
		readLogPath();
		readBinFolder();
		readTmpFolder();

		logger.debug("# completed loadParametersInternal");
	}

	public void init(Applet applet)
	{
		loadExtensions(applet);
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
		logger.debug("# called readLogPath");

		this.logPath = new File(FilenameUtils.concat(getBaseFolder().getAbsolutePath(), "production.log"));

		logger.debug("# completed readLogPath");
	}

	protected abstract void readBaseFolder();

	protected abstract void readBinFolder();

	protected abstract void readTmpFolder();

	public abstract File getFFmpegBinaryPath();

	public abstract OperatingSystem getOperatingSystem();

	public Class<? extends RecorderThread> getRecorderThreadClass()
	{
		return recorderThreadClass;
	}

	public Class<? extends PhotoThread> getPhotoThreadClass()
	{
		return photoThreadClass;
	}

	private void loadExtensions(Applet applet)
	{
		logger.debug("# called loadExtensions");

		extensions = new Extensions();

		String extensionsBase64String = applet.getParameter("extensions");
		if(StringUtils.isBlank(extensionsBase64String)) {
			logger.info("Parameter extensions does not exist. Ignoring");
			return;
		}

		logger.info("Converting json to object");
		String extensionsString = new String(Base64.decodeBase64(extensionsBase64String));
		JSONExtensions extensionsList = convertFromJSON(extensionsString);
		for(Extension extensionData : extensionsList) {
			if(StringUtils.isBlank(extensionData.getJarUrl())) {
				logger.info("jarUrl missing. Ignoring");
				continue;
			}
			if(StringUtils.isBlank(extensionData.getClassName())) {
				logger.info("className missing. Ignoring");
				continue;
			}

			try {
				logger.info(MessageFormat
						.format("Instantiating class [{0}] from jar [{1}]", extensionData.getClassName(),
								extensionData.getJarUrl()));
				AfterRecording classInstance = getClassInstance(extensionData);
				extensions.add(classInstance);
			} catch(Throwable t) {
				logger.error(MessageFormat
						.format("Cannot load class [{0}] from jar [{1}]", extensionData.getClassName(),
								extensionData.getJarUrl()), t);
			}
		}

		logger.info(MessageFormat.format("Loaded [{0}] extensions.", extensions.size()));

		logger.debug("# completed loadExtensions");
	}

	private AfterRecording getClassInstance(Extension extension)
			throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, InstantiationException
	{
		logger.debug("# called getClassInstance");

		ClassLoader loader = URLClassLoader.newInstance(
				new URL[] { new URL(extension.getJarUrl()) },
				getClass().getClassLoader()
		);

		Class<?> clazz = Class.forName(extension.getClassName(), true, loader);
		Constructor<? extends AfterRecording> constructor = (Constructor<? extends AfterRecording>) clazz
				.getConstructor();
		AfterRecording instance = constructor.newInstance();

		logger.debug("# completed loadExtensions");

		return instance;
	}

	private JSONExtensions convertFromJSON(String extensionsString)
	{
		logger.debug("# called convertFromJSON");
		logger.debug("extensionsString: ");
		logger.debug(extensionsString);

		JSONExtensions extensions;
		try {
			Gson gson = new Gson();
			extensions = gson.fromJson(extensionsString, JSONExtensions.class);
		} catch(Exception e) {
			logger.error("Cannot parse parameter extensions as json", e);
			extensions = new JSONExtensions();
		}

		logger.debug("# completed convertFromJSON");

		return extensions;
	}

	public Extensions getExtensions()
	{
		return extensions;
	}

	public enum OperatingSystem
	{
		WINDOWS,
		LINUX,
		MAC
	}
}
