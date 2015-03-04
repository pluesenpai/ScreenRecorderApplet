package org.plue.screenrecorderapplet.services.proxy;

import org.apache.http.client.protocol.HttpClientContext;
import org.plue.screenrecorderapplet.models.proxy.ProxyConfiguration;

/**
 * @author paolo86@altervista.org
 */
public abstract class BaseProxy
{
	protected ProxyConfiguration proxyConfiguration;

	public BaseProxy(ProxyConfiguration proxyConfiguration)
	{
		this.proxyConfiguration = proxyConfiguration;
	}

	public abstract HttpClientContext getContext();

	public enum ProxyProtocol
	{
		HTTP("http"),
		HTTPS("https");

		private String protocol;

		private ProxyProtocol(String protocol)
		{
			this.protocol = protocol;
		}
	}
}
