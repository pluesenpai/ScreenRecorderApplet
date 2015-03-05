package org.plue.screenrecorderapplet.models.proxy;

import org.plue.screenrecorderapplet.Applet;
import org.plue.screenrecorderapplet.exceptions.BinariesDownloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author paolo86@altervista.org
 */
public class BasicAuthProxyConfiguration extends ProxyConfiguration
{
	private static final Logger logger = LoggerFactory.getLogger(BasicAuthProxyConfiguration.class);

	private String username;

	private String password;

	public BasicAuthProxyConfiguration(Applet applet) throws BinariesDownloadException
	{
		super(applet);
		String username = applet.getParameter("username");
		this.username = validateUsername(username);
		logger.info("username = " + this.username);

		String password = applet.getParameter("password");
		this.password = validatePassword(password);
		logger.info("password = " + this.password);
	}

	private String validateUsername(String username) throws BinariesDownloadException
	{
		if(username == null) {
			throw new BinariesDownloadException("Username is null");
		}

		return username;
	}

	private String validatePassword(String password) throws BinariesDownloadException
	{
		if(password == null) {
			throw new BinariesDownloadException("Password is null");
		}

		return password;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}
}
