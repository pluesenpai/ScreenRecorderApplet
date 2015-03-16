package org.plue.screenrecorderapplet.models;

import org.apache.commons.lang.StringUtils;
import org.plue.screenrecorderapplet.AfterRecording;
import org.plue.screenrecorderapplet.Applet;
import org.plue.screenrecorderapplet.AppletWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author paolo86@altervista.org
 */
public class Extensions extends ArrayList<AfterRecording>
{
	private static final String GROOVY_ENGINE_NAME = "groovy";

	private static final Logger logger = LoggerFactory.getLogger(Extensions.class);

	public void execute(File video)
	{
		logger.debug("# called execExtensions");

		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName(GROOVY_ENGINE_NAME);
		if(engine == null) {
			logger.error("Cannot load Groovy ScriptEngine");
			return;
		}
		engine.put("videoFile", video);
		engine.put("appletWrapper", new AppletWrapper(Applet.applet));

		for(AfterRecording extension : this) {
			logger.info(MessageFormat.format("Calling extension [{0}]", extension.getClass().getName()));
			String script = extension.onRecordComplete();
			if(StringUtils.isBlank(script)) {
				logger.info(MessageFormat.format("Extension [{0}] has no script", extension.getClass().getName()));
				continue;
			}

			try {
				engine.eval(script);
			} catch(ScriptException e) {
				logger.error(MessageFormat.format("Cannot evaluate script for [{0}]", extension.getClass().getName()), e);
			}
		}

		logger.debug("# completed execExtensions");
	}
}
