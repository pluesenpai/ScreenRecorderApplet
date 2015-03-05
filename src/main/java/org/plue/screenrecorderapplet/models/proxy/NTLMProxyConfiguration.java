package org.plue.screenrecorderapplet.models.proxy;

import org.plue.screenrecorderapplet.Applet;
import org.plue.screenrecorderapplet.exceptions.BinariesDownloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author paolo86@altervista.org
 */
public class NTLMProxyConfiguration extends BasicAuthProxyConfiguration
{
	private static final Logger logger = LoggerFactory.getLogger(NTLMProxyConfiguration.class);

	private String workstation;

	private String domain;

	public NTLMProxyConfiguration(Applet applet) throws BinariesDownloadException
	{
		super(applet);

		String workstation = applet.getParameter("workstation");
		this.workstation = validateWorkstation(workstation);
		logger.info("workstation = " + this.workstation);

		String domain = applet.getParameter("domain");
		this.domain = validateDomain(domain);
		logger.info("domain = " + this.domain);
	}

	private String validateWorkstation(String workstation) throws BinariesDownloadException
	{
		if(workstation == null) {
			throw new BinariesDownloadException("Workstation is null");
		}

		return workstation;
	}

	private String validateDomain(String domain) throws BinariesDownloadException
	{
		if(domain == null) {
			throw new BinariesDownloadException("Domain is null");
		}

		return domain;
	}

	public String getWorkstation()
	{
		return workstation;
	}

	public void setWorkstation(String workstation)
	{
		this.workstation = workstation;
	}

	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}
}
