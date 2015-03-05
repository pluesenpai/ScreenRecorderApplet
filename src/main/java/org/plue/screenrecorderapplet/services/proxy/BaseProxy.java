package org.plue.screenrecorderapplet.services.proxy;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
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

	public abstract RequestConfig getRequestConfig();

	public abstract CredentialsProvider getCredentialsProvider();

	public enum ProxyProtocol
	{
		HTTP("http"),
		HTTPS("https");

		private String protocol;

		private ProxyProtocol(String protocol)
		{
			this.protocol = protocol;
		}

		public String getProtocol()
		{
			return protocol;
		}

		public static ProxyProtocol fromString(String protocol)
		{
			for(ProxyProtocol proxyProtocol : ProxyProtocol.values()) {
				if(StringUtils.equalsIgnoreCase(proxyProtocol.getProtocol(), protocol)) {
					return proxyProtocol;
				}
			}

			throw new IllegalArgumentException("No enum constant " + protocol);
		}
	}
}
