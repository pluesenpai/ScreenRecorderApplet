package org.plue.screenrecorderapplet.services.proxy;

import org.apache.http.client.protocol.HttpClientContext;

/**
 * @author paolo86@altervista.org
 */
public class NoProxy extends BaseProxy
{
	public NoProxy()
	{
		super(null);
	}

	@Override
	public HttpClientContext getContext()
	{
		return null;
	}
}
