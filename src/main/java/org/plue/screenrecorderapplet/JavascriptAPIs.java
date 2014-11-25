package org.plue.screenrecorderapplet;

/**
 * @author paolo86@altervista.org
 */
public interface JavascriptAPIs
{
	void startRecord(String saveFolder, String filename);

	void stopRecord();

	void takePhotoFromWebcam(String saveFolder, String filename);

	void takeScreenshot(String saveFolder, String filename);
}
