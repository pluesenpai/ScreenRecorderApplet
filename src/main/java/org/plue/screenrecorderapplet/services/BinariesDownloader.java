package org.plue.screenrecorderapplet.services;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.plue.screenrecorderapplet.exceptions.BinariesDownloadException;
import org.plue.screenrecorderapplet.exceptions.OldBinariesCleanupException;
import org.plue.screenrecorderapplet.exceptions.ScreenRecorderException;
import org.plue.screenrecorderapplet.exceptions.UnknownOperatingSystemException;
import org.plue.screenrecorderapplet.models.AppletParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author paolo86@altervista.org
 */
public class BinariesDownloader
{
	private Logger logger = LoggerFactory.getLogger(BinariesDownloader.class);

	private AppletParameters appletParameters;

	private final URL documentBase;

	private final URL codeBase;

	private DownloadCompleteNotifier downloadCompleteNotifier;

	public BinariesDownloader(URL documentBase, URL codeBase) throws IOException, UnknownOperatingSystemException
	{
		logger.debug("# called constructor");

		this.appletParameters = AppletParameters.getInstance();
		this.documentBase = documentBase;
		this.codeBase = codeBase;

		logger.debug("# completed constructor");
	}

	public void download() throws IOException, ScreenRecorderException
	{
		logger.debug("# called download");

		if(!appletParameters.getBinFolder().exists()) {
			logger.info("Directory " + appletParameters.getBinFolder().getAbsolutePath()
					+ " does not exist. Creating it and downloading files");
			deleteOldBinaries();
			downloadBinaryFilesInternal();
		}

		if(downloadCompleteNotifier != null) {
			downloadCompleteNotifier.onDownloadComplete();
		}

		logger.debug("# completed download");
	}

	private void deleteOldBinaries() throws IOException, OldBinariesCleanupException
	{
		logger.debug("# called deleteOldBinaries");

		File[] filesArray = appletParameters.getBaseFolder().listFiles();
		if(filesArray == null) {
			logger.info("No files to delete");
			logger.debug("# completed deleteOldBinaries");
			return;
		}

		List<File> files = Arrays.asList(filesArray);
		for(File file : files) {
			if(StringUtils.startsWith(file.getName(), "bin") && file.isDirectory()) {
				logger.info("Deleting directory " + file.getAbsolutePath());
				FileUtils.deleteDirectory(file);
				if(file.exists()) {
					logger.error("Delete failed for directory " + file.getAbsolutePath());
					throw new OldBinariesCleanupException("Could not delete the old native extentions!");
				} else {
					logger.info("Delete completed for directory " + file.getAbsolutePath());
				}
			}
		}

		logger.debug("# completed deleteOldBinaries");
	}

	private void downloadBinaryFilesInternal() throws IOException, ScreenRecorderException
	{
		logger.debug("# called downloadBinaryFilesInternal");

		File binFolder = appletParameters.getBinFolder();
		URL documentBase = getDocumentBase();
		String hostUrl = documentBase.getProtocol() + "://" + documentBase.getHost();
		String zipFilename = binFolder.getName() + ".zip";

		String url = hostUrl + "/" + zipFilename;
		String savePath = FilenameUtils.concat(appletParameters.getTmpFolder().getAbsolutePath(), zipFilename);
		logger.info("Trying download from: " + url);
		doDownload(url, savePath);
		if(binFolder.exists()) {
			logger.debug("# completed downloadBinaryFilesInternal");
			return;
		}

		URL codeBase = getCodeBase();
		url = codeBase.toString() + zipFilename;
		logger.info("Trying download from: " + url);
		doDownload(url, savePath);
		if(binFolder.exists()) {
			logger.debug("# completed downloadBinaryFilesInternal");
			return;
		}

		url = documentBase.toString() + zipFilename;
		logger.info("Trying download from: " + url);
		doDownload(url, savePath);
		if(!binFolder.exists()) {
			logger.error("Cannot download binaries.");
			throw new BinariesDownloadException();
		}

		logger.debug("# completed downloadBinaryFilesInternal");
	}

	private void doDownload(String url, String saveTo) throws IOException, ScreenRecorderException
	{
		logger.debug("# called doDownload");

		logger.info("Downloading from URL " + url + " to path " + saveTo);
		String downloadURL = url + "?" + Math.random() * 10000;
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(downloadURL);

		HttpResponse response = httpClient.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();
		if(statusCode != HttpStatus.SC_OK) {
			logger.error("Response has status code " + statusCode);
			return;
		}

		logger.info("Saving downloaded file");
		IOUtils.copy(response.getEntity().getContent(), new FileOutputStream(saveTo));

		try {
			logger.info("Extracting zip file");
			extractZip(saveTo, appletParameters.getBaseFolder().getAbsolutePath());
		} catch(ZipException e) {
			logger.error("Error while unzipping file " + saveTo, e);
			throw new BinariesDownloadException(e);
		}

		logger.debug("# completed doDownload");
	}

	private void extractZip(String zipFilePath, String where)
			throws ZipException, BinariesDownloadException
	{
		logger.debug("# called extractZip");

		ZipFile zipFile = new ZipFile(zipFilePath);
		zipFile.extractAll(where);

		logger.debug("# completed extractZip");
	}

	private URL getDocumentBase()
	{
		return documentBase;
	}

	private URL getCodeBase()
	{
		return codeBase;
	}

	public void setDownloadCompleteNotifier(DownloadCompleteNotifier downloadCompleteNotifier)
	{
		this.downloadCompleteNotifier = downloadCompleteNotifier;
	}

	public interface DownloadCompleteNotifier
	{
		public void onDownloadComplete();
	}
}
