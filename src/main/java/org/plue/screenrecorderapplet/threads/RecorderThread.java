package org.plue.screenrecorderapplet.threads;

import org.apache.commons.lang.StringUtils;
import org.plue.screenrecorderapplet.enums.NotificationType;
import org.plue.screenrecorderapplet.exceptions.ScreenRecorderException;
import org.plue.screenrecorderapplet.exceptions.UnknownOperatingSystemException;
import org.plue.screenrecorderapplet.models.AppletParameters;
import org.plue.screenrecorderapplet.models.StreamGobbler;
import org.plue.screenrecorderapplet.services.ScreenRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author paolo86@altervista.org
 */
public abstract class RecorderThread extends Thread
{
	private static final Logger logger = LoggerFactory.getLogger(RecorderThread.class);

	private static final int ERRORS_WINDOW_IN_SECONDS = 15;

	private static final long[] errorHits = new long[90];

	protected static final String FPS = "10";

	protected AppletParameters appletParameters;

	protected String outputFileFullPath;

	private ScreenRecorder.RecordingInfoNotifier recordingInfoNotifier;

	private Process recordingProcess;

	private StreamGobbler errorGobbler;

	private StreamGobbler inputGobbler;

	private Timer timer;

	private Long timerCount = 0L;

	protected RecorderThread(String outputFileFullPath, ScreenRecorder.RecordingInfoNotifier recordingInfoNotifier)
	{
		logger.debug("# called constructor");

		try {
			appletParameters = AppletParameters.getInstance();
			this.outputFileFullPath = outputFileFullPath;
			this.recordingInfoNotifier = recordingInfoNotifier;
		} catch(Exception e) {
			logger.error("Error while initializing RecorderThread", e);
			if(recordingInfoNotifier != null) {
				recordingInfoNotifier.onRecordUpdate(NotificationType.FATAL, "Cannot initialize recorder");
			} else {
				logger.info("Recording Notifier is not set. Cannot notify view.");
			}
		}

		logger.debug("# completed constructor");
	}

	public static RecorderThread newInstance(String outputFileFullPath,
			ScreenRecorder.RecordingInfoNotifier recordingInfoNotifier)
			throws IOException, UnknownOperatingSystemException
	{
		logger.debug("# called newInstance");

		AppletParameters appletParameters = AppletParameters.getInstance();
		AppletParameters.OperatingSystem operatingSystem = appletParameters.getOperatingSystem();
		if(operatingSystem == AppletParameters.OperatingSystem.WINDOWS) {
			logger.info("Using WindowsRecorderThread");
			logger.debug("# completed newInstance");
			return new WindowsRecorderThread(outputFileFullPath, recordingInfoNotifier);
		} else if(operatingSystem == AppletParameters.OperatingSystem.LINUX) {
			logger.info("Using LinuxRecorderThread");
			logger.debug("# completed newInstance");
			return new LinuxRecorderThread(outputFileFullPath, recordingInfoNotifier);
		}

		logger.error("Unknown or unsupported operating system: '" + operatingSystem.toString() + "'");
		throw new UnknownOperatingSystemException();
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
					if(recordingInfoNotifier != null) {
						recordingInfoNotifier.onRecordUpdate(NotificationType.PRE_RECORDING, "Ready");
					}

					timer = new Timer(1000, new TimerActionListener());

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

						errorGobbler = new StreamGobbler(recordingProcess.getErrorStream(), false, "ffmpeg E");
						inputGobbler = new StreamGobbler(recordingProcess.getInputStream(), false, "ffmpeg O");

						logger.info("Starting listener threads...");
						errorGobbler.start();
						errorGobbler.addActionListener("Press [q] to stop", new ProcessActionListener());
						errorGobbler.addActionListener("    Last message repeated", new ProcessErrorsActionListener());
						inputGobbler.start();

						recordingProcess.waitFor();

						logger.info("Registration completed");

						if(recordingInfoNotifier != null) {
							recordingInfoNotifier.onRecordUpdate(NotificationType.COMPLETED, "");
						}
					} catch(Exception e) {
						logger.error("Registration failed", e);
						recordingInfoNotifier.onRecordUpdate(NotificationType.FATAL, "Registration failed");
					}
				}
			});
		} catch(Exception e) {
			logger.error("Registration failed", e);
			recordingInfoNotifier.onRecordUpdate(NotificationType.FATAL, "Registration failed");
			return;
		}

		logger.debug("# completed run");
	}

	public void stopRecording()
	{
		logger.debug("# called stopRecording");

		if(timer != null && timer.isRunning()) {
			timer.stop();
			timerCount = 0L;
		}

		logger.info("Stopping ffmpeg.");
		PrintWriter pw = new PrintWriter(recordingProcess.getOutputStream());
		pw.print("q");
		pw.flush();

		logger.debug("# completed stopRecording");
	}

	protected abstract String getFFmpegCommand() throws ScreenRecorderException;

	@Override
	protected void finalize() throws Throwable
	{
		logger.info("Finalizing ScreenRecorder...");
		super.finalize();
		stopRecording();

		if(recordingProcess != null) {
			recordingProcess.destroy();
		}
	}

	private class TimerActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String statusText = (timerCount / 60 < 10 ? "0" : "") + timerCount
					/ 60 + ":" + (timerCount % 60 < 10 ? "0" : "")
					+ timerCount % 60;
			timerCount++;

			if(recordingInfoNotifier != null) {
				recordingInfoNotifier.onRecordUpdate(NotificationType.RECORDING, statusText);
			}
		}
	}

	private class ProcessActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			logger.debug("Record started");
			if(recordingInfoNotifier != null) {
				recordingInfoNotifier.onRecordUpdate(NotificationType.RECORDING, "");
			}

			logger.debug("Starting timer");
			timer.start();
		}
	}

	private class ProcessErrorsActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			logger.debug("Error notified");

			System.arraycopy(errorHits, 1, errorHits, 0, errorHits.length - 1);
			long now = GregorianCalendar.getInstance().getTimeInMillis();
			errorHits[errorHits.length - 1] = now;
			if(errorHits[0] >= (now - (ERRORS_WINDOW_IN_SECONDS * 1000))) {
				logger.warn(MessageFormat
						.format("Too many errors in last {0} seconds (counted {1} errors). Stopping video",
								ERRORS_WINDOW_IN_SECONDS, errorHits.length));
				stopRecording();

				if(recordingInfoNotifier != null) {
					recordingInfoNotifier.onRecordUpdate(NotificationType.FATAL, "Registration failed");
				}
			}
		}
	}
}
