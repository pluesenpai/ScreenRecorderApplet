package org.plue.screenrecorderapplet;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.plue.screenrecorderapplet.constants.PropertyKeys;
import org.plue.screenrecorderapplet.constants.StandardMethodNames;
import org.plue.screenrecorderapplet.enums.NotificationType;
import org.plue.screenrecorderapplet.exceptions.ScreenRecorderException;
import org.plue.screenrecorderapplet.models.AppletParameters;
import org.plue.screenrecorderapplet.services.BinariesDownloader;
import org.plue.screenrecorderapplet.services.ScreenRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author paolo86@altervista.org
 */
public class Applet extends java.applet.Applet implements BinariesDownloader.DownloadCompleteNotifier, JavascriptAPIs
{
	private static final Logger logger = LoggerFactory.getLogger(Applet.class);

	public JSObject jsBridge;

	private AppletParameters appletParameters = null;

	private ScreenRecorder screenRecorder;

	public Applet()
	{
		try {
			appletParameters = AppletParameters.getInstance();
		} catch(Exception e) {
			System.out.println("Cannot load applet parameters");
		}

		setupLogFile();
	}

	private void setupLogFile()
	{
		System.setProperty(PropertyKeys.LOG_FILE_PATH, appletParameters.getLogPath().getAbsolutePath());
		PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
	}

	@Override
	public void init()
	{
		logger.debug("# called init");

		setupJsBridge();
		doInit();

		logger.debug("# completed init");
	}

	private void setupJsBridge()
	{
		logger.debug("# called setupJsBridge");

		try {
			jsBridge = JSObject.getWindow(this);
			logger.info("retrieved js bridge");
		} catch(JSException e) {
			logger.error("Could not retrieve js bridge.");
		}

		logger.debug("# completed setupJsBridge");
	}

	private void doInit()
	{
		logger.debug("# called doInit");

		try {
			if(appletParameters == null) {
				appletParameters = AppletParameters.getInstance();
			}

			createMissingDirectories();
			logAppletInfo();

			downloadBinaries();

			screenRecorder = new ScreenRecorder();
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			notifyView(NotificationType.FATAL, "Error initializing applet");
		}

		logger.debug("# completed doInit");
	}

	private void createMissingDirectories()
	{
		File baseFolder = appletParameters.getBaseFolder();
		if(!baseFolder.exists()) {
			baseFolder.mkdirs();
		}

		File logPath = appletParameters.getLogPath();
		if(!logPath.exists()) {
			logPath.mkdirs();
		}
	}

	private void logAppletInfo()
	{
		GraphicsDevice primaryScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		GraphicsDevice[] allScreens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		int myScreenIndex = -1, primaryScreenIndex = -1;
		for(int i = 0; i < allScreens.length; i++) {
			if(allScreens[i].equals(primaryScreen)) {
				primaryScreenIndex = i;
			}
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		DisplayMode displayMode = primaryScreen.getDisplayMode();
		logger.info(
				"\n\n\n\nAPPLET INFORMATION:\n" +
						"Date Time: \t" + dateFormat.format(date) + "\n" +
						"Java Version: \t" + System.getProperty("java.version") + "\n" +
						"Implementation-Build: \t" + Applet.class.getPackage().getImplementationVersion() + "\n" +
						"OS Name: \t" + System.getProperty("os.name") + "\n" +
						"OS Version: \t" + System.getProperty("os.version") + "\n" +
						"Run Directory: \t" + System.getProperty("user.dir") + "\n" +
						"User Home: \t" + System.getProperty("user.home") + "\n" +
						"User Name: \t" + System.getProperty("user.name") + "\n" +
						"Base Folder: \t" + appletParameters.getBaseFolder() + "\n" +
						"Bin Folder: \t" + appletParameters.getBinFolder() + "\n" +
						"Code Base: \t" + getCodeBase() + "\n" +
						"Document Base: \t" + getDocumentBase() + "\n" +
						"Multiple Monitors: \t" + (allScreens.length > 1) + "\n" +
						"Applet window is on screen " + myScreenIndex + "\n" +
						"Primary screen is index " + primaryScreenIndex + "\n" +
						"Primary screen resolution: " + displayMode.getWidth() + "x" + displayMode.getHeight() + "\n");
	}

	private void downloadBinaries() throws IOException, ScreenRecorderException
	{
		BinariesDownloader binariesDownloader = new BinariesDownloader(getDocumentBase(), getCodeBase());
		binariesDownloader.setDownloadCompleteNotifier(this);
		binariesDownloader.download();
	}

	private void notifyView(NotificationType state, String message)
	{
		if(StringUtils.isBlank(message)) {
			message = StringUtils.EMPTY;
		}

		jsCall(StandardMethodNames.SRA_STATUS_UPDATE, state, message);
	}

	private void jsCall(String method, Object... parameters)
	{
		logger.debug(MessageFormat
				.format("jsCall - method: {0}, parameters: {1}", method, StringUtils.join(parameters, ", ")));

		if(jsBridge == null) {
			logger.error("No JS Bridge exists.");
			return;
		}

		jsBridge.call(method, parameters);
	}

	@Override
	public void onDownloadComplete()
	{
		notifyView(NotificationType.READY, null);
	}

	/********************* JAVASCRIPT APIs START *********************/

	@Override
	public void startRecord(final String saveFolder, final String filename)
	{
		logger.debug("[JSAPI] startRecord");
		AccessController.doPrivileged(new PrivilegedAction<Object>()
		{
			@Override
			public Object run()
			{
				try {
					screenRecorder.setSaveFolder(saveFolder);
					screenRecorder.setFilename(filename);
					screenRecorder.recordScreen(new RecordingInfoNotifier());
				} catch(Exception e) {
					logger.error("Can't prepare and start the recording!", e);
					notifyView(NotificationType.FATAL, null);
				}

				return null;
			}
		});
	}

	@Override
	public void stopRecord()
	{
		logger.debug("[JSAPI] stopRecord");
		AccessController.doPrivileged(new PrivilegedAction<Object>()
		{
			@Override
			public Object run()
			{
				try {
					screenRecorder.stopRecord();
				} catch(Exception e) {
					logger.error("Can't stop the recording!", e);
					notifyView(NotificationType.FATAL, null);
				}

				return null;
			}
		});
	}

	/********************* JAVASCRIPT APIs END *********************/

	private class RecordingInfoNotifier implements ScreenRecorder.RecordingInfoNotifier
	{
		@Override
		public void onRecordUpdate(NotificationType notificationType, String message)
		{
			logger.debug("onRecordUpdate: " + notificationType.toString() + " - message: " + message);
			notifyView(notificationType, message);
		}
	}
}
