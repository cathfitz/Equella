package com.tle.web.mimetypes.search.section;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.web.mimetypes.MimeEditorUtils;
import com.tle.web.mimetypes.MimeSearchPrivilegeTreeProvider;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.settings.AbstractParentSettingsSection;

@Bind
public class MimeSettingsSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private MimeSearchPrivilegeTreeProvider securityProvider;

	@Override
	public boolean canView(SectionInfo info)
	{
		return securityProvider.isAuthorised();
	}

	@Override
	@SuppressWarnings("nls")
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		HtmlLinkState state = new HtmlLinkState();
		SectionInfo fwd = context.createForward("/" + MimeEditorUtils.MIME_BOOKMARK);
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return new Pair<HtmlLinkState, Label>(state, DESCRIPTION_LABEL);
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
