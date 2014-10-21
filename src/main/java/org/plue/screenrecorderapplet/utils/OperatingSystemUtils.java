package org.plue.screenrecorderapplet.utils;

import org.apache.commons.lang.StringUtils;

/**
 * @author paolo86@altervista.org
 */
public class OperatingSystemUtils
{
	private OperatingSystemUtils()
	{
	}

	public static String getOSName()
	{
		String osName = System.getProperty("os.name");
		if(osName == null) {
			return StringUtils.EMPTY;
		}

		return osName.toLowerCase();
	}

	public static boolean isMac()
	{
		return StringUtils.contains(getOSName(), "mac");
	}

	public static boolean isLinux()
	{
		return StringUtils.contains(getOSName(), "linux");
	}

	public static boolean isWindows()
	{
		return StringUtils.contains(getOSName(), "windows");
	}
}
