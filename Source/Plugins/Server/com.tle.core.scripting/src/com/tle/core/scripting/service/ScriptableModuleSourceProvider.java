package com.tle.core.scripting.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.provider.ModuleSource;
import org.mozilla.javascript.commonjs.module.provider.ModuleSourceProvider;

public class ScriptableModuleSourceProvider implements ModuleSourceProvider
{
	private final Scriptable scriptable;

	public ScriptableModuleSourceProvider(Scriptable scriptable)
	{
		this.scriptable = scriptable;
	}

	@Override
	public ModuleSource loadSource(String moduleId, Scriptable paths, Object validator) throws IOException,
		URISyntaxException
	{
		return loadFromScriptableObject(moduleId);
	}

	@Override
	public ModuleSource loadSource(URI uri, URI baseUri, Object validator) throws IOException, URISyntaxException
	{
		return null;
	}
	
	@SuppressWarnings("nls")
	public ModuleSource loadFromScriptableObject(String moduleId) throws UnsupportedEncodingException,
		URISyntaxException
	{
		Object object = scriptable.get(moduleId, scriptable);
		if(object !=null)
		{
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(object.toString().getBytes("UTF-8"));
			Reader reader = new InputStreamReader(byteArrayInputStream);
			URI uri = new URI("#" + moduleId);
			
			return new ModuleSource(reader, null, uri, null, null);
		}
		return null;
	}
}
