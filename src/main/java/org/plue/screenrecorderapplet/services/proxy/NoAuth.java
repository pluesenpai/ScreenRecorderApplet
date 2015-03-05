package org.plue.screenrecorderapplet.services.proxy;

import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.plue.screenrecorderapplet.models.proxy.ProxyConfiguration;

/**
 * @author paolo86@altervista.org
 */
public class NoAuth extends BaseProxy
{
	public NoAuth(ProxyConfiguration proxyConfiguration)
	{
		super(proxyConfiguration);
	}

	@Override
	public HttpClientContext getContext()
	{
		HttpHost targetHost = new HttpHost(proxyConfiguration.getHost(), proxyConfiguration.getPort(),
				proxyConfiguration.getProtocol().getProtocol());

		HttpClientContext context = HttpClientContext.create();
		context.setTargetHost(targetHost);

		return context;
	}

	@Override
	public RequestConfig getRequestConfig()
	{
		HttpHost proxy = new HttpHost(proxyConfiguration.getHost(), proxyConfiguration.getPort(),
				proxyConfiguration.getProtocol().getProtocol());

		return RequestConfig.custom()
				.setProxy(proxy)
				.build();
	}

	@Override
	public CredentialsProvider getCredentialsProvider()
	{
		return null;
	}
}
