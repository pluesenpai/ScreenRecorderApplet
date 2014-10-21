package org.plue.screenrecorderapplet.threads;

import org.apache.commons.lang.StringUtils;
import org.plue.screenrecorderapplet.enums.NotificationType;
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
	private static final Logger logger = LoggerFactory.getLogger(WindowsRecorderThread.class);

	private static final int ERRORS_WINDOW_IN_SECONDS = 15;

	protected static final String FPS = "10";

	protected AppletParameters appletParameters;

	protected String outputFileFullPath;

	protected ScreenRecorder.RecordingInfoNotifier recordingInfoNotifier;

	private Process recordingProcess;

	private StreamGobbler errorGobbler;

	private StreamGobbler inputGobbler;

	private Timer timer;

	private Long timerCount = 0L;

	private long[] errorHits = new long[90];

	protected RecorderThread(String outputFileFullPath, ScreenRecorder.RecordingInfoNotifier recordingInfoNotifier)
	{
		try {
			appletParameters = AppletParameters.getInstance();
			this.outputFileFullPath = outputFileFullPath;
			this.recordingInfoNotifier = recordingInfoNotifier;
		} catch(Exception e) {
			if(recordingInfoNotifier != null) {
				recordingInfoNotifier.onRecordUpdate(NotificationType.FATAL, "Cannot initialize recorder");
			}
		}
	}

	public static RecorderThread newInstance(String outputFileFullPath,
			ScreenRecorder.RecordingInfoNotifier recordingInfoNotifier)
			throws IOException, UnknownOperatingSystemException
	{
		AppletParameters appletParameters = AppletParameters.getInstance();
		AppletParameters.OperatingSystem operatingSystem = appletParameters.getOperatingSystem();
		if(operatingSystem == AppletParameters.OperatingSystem.WINDOWS) {
			return new WindowsRecorderThread(outputFileFullPath, recordingInfoNotifier);
		} else if(operatingSystem == AppletParameters.OperatingSystem.LINUX) {
			return new LinuxRecorderThread(outputFileFullPath, recordingInfoNotifier);
		}

		throw new UnknownOperatingSystemException();
	}

	@Override
	public void run()
	{
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

						logger.info("Executing this command: " + prettyCommand(ffmpegArgs));
						ProcessBuilder pb = new ProcessBuilder(ffmpegArgs);

						recordingProcess = pb.start();

						errorGobbler = new StreamGobbler(recordingProcess.getErrorStream(), false, "ffmpeg E");
						inputGobbler = new StreamGobbler(recordingProcess.getInputStream(), false, "ffmpeg O");

						logger.info("Starting listener threads...");
						errorGobbler.start();
						errorGobbler.addActionListener("Press [q] to stop", new ProcessActionListener());
						errorGobbler.addActionListener("    Last message repeated", new ProcessErrorsActionListener());
						inputGobbler.start();

						recordingProcess.waitFor();

						if(recordingInfoNotifier != null) {
							recordingInfoNotifier.onRecordUpdate(NotificationType.COMPLETED, "");
						}
					} catch(Exception e) {
						recordingInfoNotifier.onRecordUpdate(NotificationType.FATAL, "Registration failed");
					}
				}
			});
		} catch(Exception e) {
			recordingInfoNotifier.onRecordUpdate(NotificationType.FATAL, "Registration failed");
		}
	}

	public void stopRecording()
	{
		if(timer != null && timer.isRunning()) {
			timer.stop();
			timerCount = 0L;
		}

		PrintWriter pw = new PrintWriter(recordingProcess.getOutputStream());
		pw.print("q");
		pw.flush();
		logger.info("Screen recording should be stopped.");

		if(recordingProcess != null) {
			recordingProcess.destroy();
		}
	}

	private String prettyCommand(List<String> args)
	{
		return StringUtils.join(args, " ");
	}

	protected abstract String getFFmpegCommand();

	@Override
	protected void finalize() throws Throwable
	{
		logger.info("Finalizing ScreenRecorder...");
		super.finalize();
		stopRecording();
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
				logger.debug(MessageFormat
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
