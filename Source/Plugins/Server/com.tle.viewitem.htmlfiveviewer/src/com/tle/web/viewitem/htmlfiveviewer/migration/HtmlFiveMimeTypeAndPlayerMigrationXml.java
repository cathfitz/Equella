package com.tle.web.viewitem.htmlfiveviewer.migration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.mimetypes.institution.MimeEntryConverter;
import com.tle.core.xstream.XmlService;

@SuppressWarnings("nls")
@Bind
@Singleton
public class HtmlFiveMimeTypeAndPlayerMigrationXml extends XmlMigrator
{
	private static final String HTML5_VIEWER_ID = "htmlFiveViewer";

	@Inject
	private XmlService xmlService;
	@Inject
	private MimeTypeService mimeService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		// Add webm + ogg
		addOggAndWebM(staging);
		// Set default/enabled player
		final SubTemporaryFile folder = new SubTemporaryFile(staging, "mimetypes");
		final List<String> entries = xmlHelper.getXmlFileList(folder);
		for( String entry : entries )
		{
			final MimeEntry mime = xmlHelper.readXmlFile(folder, entry);
			String mimeType = mime.getType();
			if( mimeType.contains("video")
				&& (mimeType.contains("mp4") || mimeType.contains("ogg") || mimeType.contains("webm")) )
			{
				mime.getAttributes().put(MimeTypeConstants.KEY_DEFAULT_VIEWERID, HTML5_VIEWER_ID);
				mime.getAttributes().put(MimeTypeConstants.KEY_ICON_PLUGINICON, "icons/video.png");
				List<String> enabledViewers = mimeService.getListFromAttribute(mime,
					MimeTypeConstants.KEY_ENABLED_VIEWERS, String.class);
				if( Check.isEmpty(enabledViewers) )
				{
					enabledViewers = new ArrayList<String>();
				}
				enabledViewers.add(HTML5_VIEWER_ID);
				mimeService.setListAttribute(mime, MimeTypeConstants.KEY_ENABLED_VIEWERS, enabledViewers);
				xmlHelper.writeXmlFile(folder, entry, mime);
			}
		}

	}

	private void addOggAndWebM(TemporaryFileHandle staging)
	{
		SubTemporaryFile mimeFolder = MimeEntryConverter.getMimeFolder(staging);
		// ogg
		MimeEntry ogg = new MimeEntry();
		ogg.setDescription("Video");
		ogg.setType("video/ogg");
		ArrayList<String> oggExtensions = new ArrayList<String>();
		oggExtensions.add("ogv");
		oggExtensions.add("ogg");
		ogg.setExtensions(oggExtensions);
		String oggFilename = MimeEntryConverter.getFilenameForEntry(ogg);
		if( !fileExists(mimeFolder, oggFilename) )
		{
			xmlHelper.writeFile(mimeFolder, oggFilename, xmlService.serialiseToXml(ogg));
		}

		// webm
		MimeEntry webm = new MimeEntry();
		webm.setDescription("Video");
		webm.setType("video/webm");
		ArrayList<String> webmExtensions = new ArrayList<String>();
		webmExtensions.add("webm");
		webm.setExtensions(webmExtensions);
		String webmFilename = MimeEntryConverter.getFilenameForEntry(webm);
		if( !fileExists(mimeFolder, webmFilename) )
		{
			xmlHelper.writeFile(mimeFolder, webmFilename, xmlService.serialiseToXml(webm));
		}
	}

}
