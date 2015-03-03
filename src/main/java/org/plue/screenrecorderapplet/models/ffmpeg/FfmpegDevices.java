package org.plue.screenrecorderapplet.models.ffmpeg;

import java.util.ArrayList;
import java.util.List;

/**
 * @author paolo86@altervista.org
 */
public class FfmpegDevices
{
	private List<FfmpegDevice> audioDevices;

	private List<FfmpegDevice> videoDevices;

	public List<FfmpegDevice> getAudioDevices()
	{
		if(audioDevices == null) {
			audioDevices = new ArrayList<FfmpegDevice>();
		}

		return audioDevices;
	}

	public void setAudioDevices(List<FfmpegDevice> audioDevices)
	{
		this.audioDevices = audioDevices;
	}

	public List<FfmpegDevice> getVideoDevices()
	{
		if(videoDevices == null) {
			videoDevices = new ArrayList<FfmpegDevice>();
		}

		return videoDevices;
	}

	public void setVideoDevices(List<FfmpegDevice> videoDevices)
	{
		this.videoDevices = videoDevices;
	}
}
