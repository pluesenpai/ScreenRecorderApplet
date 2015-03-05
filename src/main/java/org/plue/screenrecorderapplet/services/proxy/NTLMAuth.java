package org.plue.screenrecorderapplet.services.proxy;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.plue.screenrecorderapplet.models.proxy.NTLMProxyConfiguration;

/**
 * @author paolo86@altervista.org
 */
public class NTLMAuth extends BaseProxy
{
	public NTLMAuth(NTLMProxyConfiguration configuration)
	{
		super(configuration);
	}

	@Override
	public HttpClientContext getContext()
	{
		NTLMProxyConfiguration configuration = (NTLMProxyConfiguration) proxyConfiguration;

		NTCredentials credentials = new NTCredentials(configuration.getUsername(), configuration.getPassword(),
				configuration.getWorkstation(), configuration.getDomain());
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, credentials);

		HttpHost targetHost = new HttpHost(configuration.getHost(), configuration.getPort(),
				configuration.getProtocol().getProtocol());
		AuthCache authCache = new BasicAuthCache();
		NTLMScheme ntlmScheme = new NTLMScheme();
		authCache.put(targetHost, ntlmScheme);

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
		NTLMProxyConfiguration configuration = (NTLMProxyConfiguration) proxyConfiguration;

		NTCredentials credentials = new NTCredentials(configuration.getUsername(), configuration.getPassword(),
				configuration.getWorkstation(), configuration.getDomain());
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, credentials);

		return credentialsProvider;
	}
}
