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
		this.appletParameters = AppletParameters.getInstance();
		this.documentBase = documentBase;
		this.codeBase = codeBase;
	}

	public void download() throws IOException, ScreenRecorderException
	{
		if(!appletParameters.getBinFolder().exists()) {
			deleteOldBinaries();
			downloadBinaryFilesInternal();
		}

		if(downloadCompleteNotifier != null) {
			downloadCompleteNotifier.onDownloadComplete();
		}
	}

	private void deleteOldBinaries() throws IOException, OldBinariesCleanupException
	{
		for(File file : appletParameters.getBaseFolder().listFiles()) {
			if(StringUtils.startsWith(file.getName(), "bin") && file.isDirectory()) {
				FileUtils.deleteDirectory(file);
				if(file.exists()) {
					throw new OldBinariesCleanupException("Could not delete the old native extentions!");
				}
			}
		}
	}

	private void downloadBinaryFilesInternal() throws IOException, ScreenRecorderException
	{
		File binFolder = appletParameters.getBinFolder();
		URL documentBase = getDocumentBase();
		String hostUrl = documentBase.getProtocol() + "://" + documentBase.getHost();
		String zipFilename = binFolder.getName() + ".zip";

		String url = hostUrl + "/" + zipFilename;
		String savePath = FilenameUtils.concat(appletParameters.getTmpFolder().getAbsolutePath(), zipFilename);
		doDownload(url, savePath);
		if(binFolder.exists()) {
			return;
		}

		URL codeBase = getCodeBase();
		url = codeBase.toString() + zipFilename;
		doDownload(url, savePath);
		if(binFolder.exists()) {
			return;
		}

		url = documentBase.toString() + zipFilename;
		doDownload(url, savePath);
		if(!binFolder.exists()) {
			throw new BinariesDownloadException();
		}
	}

	private void doDownload(String url, String saveTo) throws IOException, ScreenRecorderException
	{
		String downloadURL = url + "?" + Math.random() * 10000;
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(downloadURL);

		HttpResponse response = httpClient.execute(request);
		if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			return;
		}

		IOUtils.copy(response.getEntity().getContent(), new FileOutputStream(saveTo));

		try {
			extractZip(saveTo, appletParameters.getBaseFolder().getAbsolutePath());
		} catch(ZipException e) {
			throw new BinariesDownloadException(e);
		}
	}

	private void extractZip(String zipFilePath, String where)
			throws ZipException, BinariesDownloadException
	{
		ZipFile zipFile = new ZipFile(zipFilePath);
		zipFile.extractAll(where);
	}

//	private void extractZip(String zipFilePath, String where) throws IOException
//	{
//		byte[] buffer = new byte[1024];
//
//		ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zipFilePath));
//
//		ZipEntry entry = inputStream.getNextEntry();
//		while(entry != null) {
//			String fileName = entry.getName();
//			File newFile = new File(where + File.separator + fileName);
//
//			new File(newFile.getParent()).mkdirs();
//
//			FileOutputStream fos = new FileOutputStream(newFile);
//
//			int len;
//			while ((len = inputStream.read(buffer)) > 0) {
//				fos.write(buffer, 0, len);
//			}
//
//			fos.close();
//			entry = inputStream.getNextEntry();
//		}
//
//		inputStream.closeEntry();
//		inputStream.close();
//	}

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
		this.downloadCompleteNotifier = this.downloadCompleteNotifier;
	}

	public interface DownloadCompleteNotifier
	{
		public void onDownloadComplete();

	}
}
