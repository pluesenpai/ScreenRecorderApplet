package org.plue.screenrecorderapplet.threads.photo;

import org.plue.screenrecorderapplet.models.ffmpeg.FfmpegDevice;
import org.plue.screenrecorderapplet.models.ffmpeg.FfmpegDevices;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author p.cortis@sinossi.it
 */
public class LinuxPhotoThreadTest
{
	private static final String video = "USB2.0 PC CAMERA (usb-0000:00:1d.7-1):\n"
			+ "        /dev/video1\n"
			+ "\n"
			+ "UVC Camera (046d:0819) (usb-0000:00:1d.7-2):\n"
			+ "        /dev/video0";

	private static final String videoWithTabs = "USB2.0 PC CAMERA (usb-0000:00:1d.7-1):\n"
			+ "\t\t/dev/video1\n"
			+ "\n"
			+ "UVC Camera (046d:0819) (usb-0000:00:1d.7-2):\n"
			+ "\t\t/dev/video0";

	private static final String videoWithNoWebcams = "Video Device (usb-0000:00:1d.7-1):\n"
			+ "\t\t/dev/video0\n";

	@Test
	public void testSplitV4lCtlDeviceListOutput()
	{
		String[] v4lCtlSplittedOutput = video.split("\n\n");
		Assert.assertEquals(v4lCtlSplittedOutput.length, 2);
	}

	@Test
	public void testSplitV4lCtlDeviceListOutputWithTabs()
	{
		String[] v4lCtlSplittedOutput = videoWithTabs.split("\n\n");
		Assert.assertEquals(v4lCtlSplittedOutput.length, 2);
	}

	@Test
	public void testGetWebcamDevices()
	{
		LinuxPhotoThread linuxPhotoThread = new LinuxPhotoThread("");
		FfmpegDevices webcamDevices = linuxPhotoThread.getWebcamDevices(video);
		Assert.assertNotNull(webcamDevices);
		List<FfmpegDevice> videoDevices = webcamDevices.getVideoDevices();
		Assert.assertEquals(videoDevices.size(), 2);
		Assert.assertEquals(webcamDevices.getAudioDevices().size(), 0);

		Assert.assertEquals(videoDevices.get(0).getName(), "/dev/video1");
		Assert.assertEquals(videoDevices.get(1).getName(), "/dev/video0");
	}

	@Test
	public void testGetWebcamDevicesWithNoWebcamDevices()
	{
		LinuxPhotoThread linuxPhotoThread = new LinuxPhotoThread("");
		FfmpegDevices webcamDevices = linuxPhotoThread.getWebcamDevices(videoWithNoWebcams);
		Assert.assertNotNull(webcamDevices);
		List<FfmpegDevice> videoDevices = webcamDevices.getVideoDevices();
		Assert.assertEquals(videoDevices.size(), 1);
		Assert.assertEquals(webcamDevices.getAudioDevices().size(), 0);

		Assert.assertEquals(videoDevices.get(0).getName(), "/dev/video0");
	}
}
