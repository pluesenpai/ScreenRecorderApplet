package org.plue.screenrecorderapplet.models.proxy;

import org.plue.screenrecorderapplet.Applet;
import org.plue.screenrecorderapplet.exceptions.BinariesDownloadException;
import org.plue.screenrecorderapplet.services.proxy.BaseProxy;

/**
 * @author paolo86@altervista.org
 */
public class ProxyConfiguration
{
	private BaseProxy.ProxyProtocol protocol;

	private String host;

	private Integer port;

	public ProxyConfiguration(Applet applet) throws BinariesDownloadException
	{
		String proxyProtocol = applet.getParameter("proxy_protocol");
		this.protocol = validateProxyProtocol(proxyProtocol);

		String host = applet.getParameter("host");
		this.host = validateHost(host);

		String portAsString = applet.getParameter("port");
		this.port = validatePort(portAsString);
	}

	private static BaseProxy.ProxyProtocol validateProxyProtocol(String proxyProtocol) throws BinariesDownloadException
	{
		BaseProxy.ProxyProtocol protocol = BaseProxy.ProxyProtocol.valueOf(proxyProtocol);
		if(protocol == null) {
			throw new BinariesDownloadException("Unknown proxy protocol " + proxyProtocol);
		}

		return protocol;
	}

	private static String validateHost(String host) throws BinariesDownloadException
	{
		if(host == null) {
			throw new BinariesDownloadException("Host is null");
		}

		return host;
	}

	private static Integer validatePort(String portAsString) throws BinariesDownloadException
	{
		int port;
		try {
			port = Integer.parseInt(portAsString);
		} catch(NumberFormatException e) {
			throw new BinariesDownloadException("Port is not a number: " + portAsString);
		}

		if(port < 1 || port > 65535) {
			throw new BinariesDownloadException("Port is out of range: " + portAsString);
		}

		return port;
	}

	public BaseProxy.ProxyProtocol getProtocol()
	{
		return protocol;
	}

	public void setProtocol(BaseProxy.ProxyProtocol protocol)
	{
		this.protocol = protocol;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public Integer getPort()
	{
		return port;
	}

	public void setPort(Integer port)
	{
		this.port = port;
	}
}
