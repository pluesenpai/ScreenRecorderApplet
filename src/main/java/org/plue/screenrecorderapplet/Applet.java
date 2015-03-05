package org.plue.screenrecorderapplet;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.plue.screenrecorderapplet.constants.PropertyKeys;
import org.plue.screenrecorderapplet.constants.StandardMethodNames;
import org.plue.screenrecorderapplet.enums.NotificationType;
import org.plue.screenrecorderapplet.exceptions.BinariesDownloadException;
import org.plue.screenrecorderapplet.exceptions.ScreenRecorderException;
import org.plue.screenrecorderapplet.models.AppletParameters;
import org.plue.screenrecorderapplet.models.proxy.BasicAuthProxyConfiguration;
import org.plue.screenrecorderapplet.models.proxy.NTLMProxyConfiguration;
import org.plue.screenrecorderapplet.models.proxy.ProxyConfiguration;
import org.plue.screenrecorderapplet.services.BinariesDownloader;
import org.plue.screenrecorderapplet.services.ScreenRecorder;
import org.plue.screenrecorderapplet.services.proxy.*;
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

	private JSObject jsBridge;

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
		String logPath = appletParameters.getLogPath().getAbsolutePath();
		System.setProperty(PropertyKeys.LOG_FILE_PATH, logPath);
		PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));

		logger.info("Logger setup completed");
		logger.info("Logging to " + logPath);
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
		logger.debug("# called createMissingDirectories");

		File baseFolder = appletParameters.getBaseFolder();
		if(!baseFolder.exists()) {
			logger.info("Path " + baseFolder.getAbsolutePath() + " does not exist. Creating.");
			boolean result = baseFolder.mkdirs();
			logger.info("Creating directory " + baseFolder.getAbsolutePath() + " result: " + Boolean.toString(result));
		}

		File logPath = appletParameters.getLogPath();
		if(!logPath.exists()) {
			logger.info("Path " + logPath.getAbsolutePath() + " does not exist. Creating.");
			boolean result = logPath.mkdirs();
			logger.info("Creating directory " + logPath.getAbsolutePath() + " result: " + Boolean.toString(result));
		}

		logger.debug("# completed createMissingDirectories");
	}

	private void logAppletInfo()
	{
		logger.debug("# called logAppletInfo");

		GraphicsDevice primaryScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		GraphicsDevice[] allScreens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		int myScreenIndex = -1, primaryScreenIndex = -1;
		for(int i = 0; i < allScreens.length; i++) {
			if(allScreens[i].equals(primaryScreen)) {
				primaryScreenIndex = i;
			}
		}

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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

		logger.debug("# completed logAppletInfo");
	}

	private void downloadBinaries() throws IOException, ScreenRecorderException
	{
		logger.debug("# called downloadBinaries");

		BaseProxy proxy = createProxy();

		BinariesDownloader binariesDownloader = new BinariesDownloader(getDocumentBase(), getCodeBase(), proxy);
		binariesDownloader.setDownloadCompleteNotifier(this);
		binariesDownloader.download();

		logger.debug("# completed downloadBinaries");
	}

	private BaseProxy createProxy() throws BinariesDownloadException
	{
		String proxyType = getParameter("proxy_type");
		logger.info("proxyType = " + proxyType);

		if(StringUtils.equals(proxyType, "BASIC")) {
			logger.info("Using Basic Auth");
			return createBasicAuthProxy();
		} else if(StringUtils.equals(proxyType, "NOAUTH")) {
			logger.info("Using No Auth");
			return createNoAuthProxy();
		} else if(StringUtils.equals(proxyType, "NTLM")) {
			logger.info("Using NTLM Auth");
			return createNTLMAuthProxy();
		}

		logger.info("Using Direct connection");
		return new NoProxy();
	}

	private BaseProxy createNoAuthProxy() throws BinariesDownloadException
	{
		ProxyConfiguration configuration = new ProxyConfiguration(this);
		return new NoAuth(configuration);
	}

	private BaseProxy createBasicAuthProxy() throws BinariesDownloadException
	{
		BasicAuthProxyConfiguration configuration = new BasicAuthProxyConfiguration(this);
		return new BasicAuth(configuration);
	}

	private BaseProxy createNTLMAuthProxy() throws BinariesDownloadException
	{
		NTLMProxyConfiguration configuration = new NTLMProxyConfiguration(this);
		return new NTLMAuth(configuration);
	}

	private void notifyView(NotificationType state, String message)
	{
		logger.debug("# called notifyView");

		if(StringUtils.isBlank(message)) {
			message = StringUtils.EMPTY;
		}

		jsCall(StandardMethodNames.SRA_STATUS_UPDATE, state, message);

		logger.debug("# completed notifyView");
	}

	private void jsCall(String method, Object... parameters)
	{
		logger.debug("# called jsCall");

		logger.debug(MessageFormat
				.format("jsCall - method: {0}, parameters: {1}", method, StringUtils.join(parameters, ", ")));

		if(jsBridge == null) {
			logger.error("No JS Bridge exists.");
			return;
		}

		jsBridge.call(method, parameters);

		logger.debug("# completed jsCall");
	}

	@Override
	public void onDownloadComplete()
	{
		logger.debug("# called onDownloadComplete");

		notifyView(NotificationType.READY, null);

		logger.debug("# completed onDownloadComplete");
	}

	/********************* JAVASCRIPT APIs START *********************/

	@Override
	public void startRecord(final String saveFolder, final String filename)
	{
		logger.debug("# called startRecord");

		logger.info("[JSAPI] startRecord");
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
					logger.error("Can't prepare and start the recording", e);
					notifyView(NotificationType.FATAL, null);
				}

				return null;
			}
		});

		logger.debug("# completed startRecord");
	}

	@Override
	public void stopRecord()
	{
		logger.debug("# called stopRecord");

		logger.info("[JSAPI] stopRecord");
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

		logger.debug("# completed stopRecord");
	}

	@Override
	public void takePhotoFromWebcam(final String saveFolder, final String filename)
	{
		logger.debug("# called takePhotoFromWebcam");

		logger.info("[JSAPI] takePhotoFromWebcam");
		AccessController.doPrivileged(new PrivilegedAction<Object>()
		{
			@Override
			public Object run()
			{
				try {
					screenRecorder.takePhotoFromWebcam(saveFolder, filename);
				} catch(Exception e) {
					logger.error("Can't prepare and start the recording", e);
					notifyView(NotificationType.FATAL, null);
				}

				return null;
			}
		});

		logger.debug("# completed takePhotoFromWebcam");
	}

	@Override
	public void takeScreenshot(String saveFolder, String filename)
	{
		// TODO
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
