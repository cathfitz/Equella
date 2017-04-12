package com.tle.web.favourites;

import javax.inject.Inject;

import com.tle.common.search.DefaultSearch;
import com.tle.core.guice.Bind;
import com.tle.web.favourites.itemlist.FavouritesItemList;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.searching.StandardSearchResultType;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.generic.AbstractPrototypeSection;

@Bind
public class FavouritesSearchResults extends AbstractPrototypeSection<SearchResultsModel>
	implements
		StandardSearchResultType
{
	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(FavouritesSearchResults.class);

	@Inject
	private FavouritesItemList favouritesItemList;

	@Override
	public AbstractItemList<StandardItemListEntry, ?> getCustomItemList()
	{
		return favouritesItemList;
	}

	@Override
	public String getKey()
	{
		return helper.key("result.type.standard");
	}

	@Override
	public String getValue()
	{
		return "standard";
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
		tree.registerInnerSection(favouritesItemList, parentId);
	}

	@Override
	public void addResultTypeDefaultRestrictions(DefaultSearch defaultSearch)
	{
		// nothing
	}

	@Override
	public boolean isDisabled()
	{
		return false;
	}

}
