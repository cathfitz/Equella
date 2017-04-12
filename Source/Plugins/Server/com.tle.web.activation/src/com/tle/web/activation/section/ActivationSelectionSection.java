package com.tle.web.activation.section;

import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.item.ItemActivationId;
import com.tle.common.search.DefaultSearch;
import com.tle.core.activation.ActivationResult;
import com.tle.core.services.item.FreeTextService;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.web.bulk.section.AbstractBulkResultsDialog;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.annotations.Component;

public class ActivationSelectionSection extends AbstractBulkSelectionSection<ItemActivationId>
{
	private static final String KEY_SELECTIONS = "activationSelections"; //$NON-NLS-1$

	@PlugKey("selectionsbox.selectall")
	private static Label LABEL_SELECTALL;
	@PlugKey("selectionsbox.unselect")
	private static Label LABEL_UNSELECTALL;
	@PlugKey("selectionsbox.viewselected")
	private static Label LABEL_VIEWSELECTED;
	@PlugKey("selectionsbox.pleaseselect")
	private static Label LABEL_PLEASE;
	@PlugKey("selectionsbox.count")
	private static String LABEL_COUNT;

	@Inject
	@Component
	private ActivationResultsDialog bulkDialog;
	@Inject
	private FreeTextService freeTextService;

	@TreeLookup
	protected AbstractFreetextResultsSection<?, ?> resultsSection;

	@Override
	@EventHandlerMethod
	public void selectAll(SectionInfo info)
	{
		FreetextSearchEvent searchEvent = resultsSection.createSearchEvent(info);
		info.processEvent(searchEvent);
		DefaultSearch search = searchEvent.getFinalSearch();
		FreetextSearchResults<ActivationResult> results = freeTextService.search(search, 0, Integer.MAX_VALUE);
		Model<ItemActivationId> model = getModel(info);
		Set<ItemActivationId> selections = model.getSelections();

		int count = results.getCount();
		for( int i = 0; i < count; i++ )
		{
			ActivationResult itemId = results.getResultData(i);
			selections.add(new ItemActivationId(itemId.getItemIdKey(), itemId.getActivationId()));
		}
		model.setModifiedSelection(true);
	}

	@Override
	protected String getKeySelections()
	{
		return KEY_SELECTIONS;
	}

	@Override
	protected Label getLabelSelectAll()
	{
		return LABEL_SELECTALL;
	}

	@Override
	protected Label getLabelUnselectAll()
	{
		return LABEL_UNSELECTALL;
	}

	@Override
	protected Label getLabelViewSelected()
	{
		return LABEL_VIEWSELECTED;
	}

	@Override
	protected Label getPleaseSelectLabel()
	{
		return LABEL_PLEASE;
	}

	@Override
	protected Label getSelectionBoxCountLabel(int selectionCount)
	{
		return new PluralKeyLabel(LABEL_COUNT, selectionCount);
	}

	@Override
	protected AbstractBulkResultsDialog<ItemActivationId> getBulkDialog()
	{
		return bulkDialog;
	}

	@Override
	protected boolean useBitSet()
	{
		return false;
	}
}
