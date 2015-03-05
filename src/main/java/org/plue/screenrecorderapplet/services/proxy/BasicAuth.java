package org.plue.screenrecorderapplet.services.proxy;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.plue.screenrecorderapplet.models.proxy.BasicAuthProxyConfiguration;

/**
 * @author paolo86@altervista.org
 */
public class BasicAuth extends BaseProxy
{
	public BasicAuth(BasicAuthProxyConfiguration proxyConfiguration)
	{
		super(proxyConfiguration);
	}

	@Override
	public HttpClientContext getContext()
	{
		BasicAuthProxyConfiguration configuration = (BasicAuthProxyConfiguration) proxyConfiguration;

		HttpHost targetHost = new HttpHost(configuration.getHost(), configuration.getPort(),
				configuration.getProtocol().getProtocol());
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(
				new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(configuration.getUsername(), configuration.getPassword()));

		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(targetHost, basicAuth);

		HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(credentialsProvider);
		context.setAuthCache(authCache);

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
		BasicAuthProxyConfiguration configuration = (BasicAuthProxyConfiguration) proxyConfiguration;

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(
				new AuthScope(configuration.getHost(), configuration.getPort()),
				new UsernamePasswordCredentials(configuration.getUsername(), configuration.getPassword()));

		return credentialsProvider;
	}
}
