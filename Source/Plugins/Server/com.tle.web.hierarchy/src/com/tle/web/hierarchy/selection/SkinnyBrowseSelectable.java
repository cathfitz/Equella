package com.tle.web.hierarchy.selection;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.SelectionNavAction;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;

/**
 * identical to SkinnyFavouritesNavAction, except for literal strings
 * 
 * @author larry
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class SkinnyBrowseSelectable implements SelectionNavAction
{
	private static final String FORWARD_PATH = "/access/skinny/hierarchy.do";

	@PlugKey("selection.navaction")
	private static Label NAV_ACTION_LABEL;

	@Override
	public Label getLabelForNavAction(SectionInfo info)
	{
		return NAV_ACTION_LABEL;
	}

	@Override
	public SectionInfo createForwardForNavAction(SectionInfo fromInfo, SelectionSession session)
	{
		return fromInfo.createForward(FORWARD_PATH);
	}

	@Override
	public String getActionType()
	{
		return "skinnybrowse";
	}

	@Override
	public SectionInfo createSectionInfo(SectionInfo info, SelectionSession session)
	{
		return createForwardForNavAction(info, session);
	}

	@Override
	public boolean isActionAvailable(SectionInfo info, SelectionSession session)
	{
		Layout layout = session.getLayout();
		return layout == Layout.SKINNY || layout == Layout.COURSE;
	}

	@Override
	public boolean isShowBreadcrumbs()
	{
		return true;
	}
}
