package org.plue.screenrecorderapplet.services.proxy;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
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
		HttpHost targetHost = new HttpHost(proxyConfiguration.getHost(), proxyConfiguration.getPort(), proxyConfiguration.getProtocol().name());

		HttpClientContext context = HttpClientContext.create();
		context.setTargetHost(targetHost);

		return context;
	}
}
