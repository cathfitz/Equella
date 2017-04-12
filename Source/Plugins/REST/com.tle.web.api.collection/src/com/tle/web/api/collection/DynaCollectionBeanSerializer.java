package com.tle.web.api.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.api.baseentity.serializer.AbstractEquellaBaseEntitySerializer;
import com.tle.web.api.collection.beans.DynaCollectionBean;
import com.tle.web.api.collection.impl.DynaCollectionEditorImpl.DynaCollectionEditorFactory;
import com.tle.web.api.item.equella.interfaces.beans.ItemDefinitionScriptBean;
import com.tle.web.api.item.equella.interfaces.beans.SchemaScriptBean;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class DynaCollectionBeanSerializer
	extends
		AbstractEquellaBaseEntitySerializer<DynaCollection, DynaCollectionBean, DynaCollectionEditor>
{
	@Inject
	private DynaCollectionService dynaCollectionService;
	@Inject
	private DynaCollectionEditorFactory editorFactory;

	@Override
	protected DynaCollectionBean createBean()
	{
		return new DynaCollectionBean();
	}

	@Override
	protected DynaCollection createEntity()
	{
		return new DynaCollection();
	}

	@Nullable
	@Override
	protected DynaCollectionEditor createExistingEditor(DynaCollection entity, @Nullable String stagingUuid,
		@Nullable String lockId)
	{
		return editorFactory.createExistingEditor(entity, stagingUuid, lockId, true);
	}

	@Override
	protected DynaCollectionEditor createNewEditor(DynaCollection entity, @Nullable String stagingUuid)
	{
		return editorFactory.createNewEditor(entity, stagingUuid);
	}

	@Override
	protected void copyCustomLightweightFields(DynaCollection dynaColl, DynaCollectionBean bean, Object data)
	{
		super.copyCustomLightweightFields(dynaColl, bean, data);

		final String virtualValue = (String) data;
		bean.setVirtualisationValue(virtualValue);
		if( Check.isEmpty(virtualValue) )
		{
			bean.setCompoundId(dynaColl.getUuid());
		}
		else
		{
			bean.setCompoundId(dynaColl.getUuid() + ":" + URLUtils.basicUrlEncode(virtualValue));
		}
	}

	@Override
	protected void copyCustomFields(DynaCollection dynaColl, DynaCollectionBean bean, Object data)
	{
		bean.setVirtualisationId(dynaColl.getVirtualisationId());
		bean.setVirtualisationPath(dynaColl.getVirtualisationPath());
		bean.setFreetext(dynaColl.getFreetextQuery());

		Set<String> usages = dynaColl.getUsageIds();
		if( !Check.isEmpty(usages) )
		{
			List<String> usagesList = new ArrayList<String>(usages.size());
			usagesList.addAll(usages);
			bean.setUsages(usagesList);
		}

		List<SchemaScript> dynaSchemas = dynaColl.getSchemas();
		if( !Check.isEmpty(dynaSchemas) )
		{
			List<SchemaScriptBean> scripts = new ArrayList<SchemaScriptBean>(dynaSchemas.size());
			for( SchemaScript scriptObj : dynaSchemas )
			{
				SchemaScriptBean schemaScriptBean = new SchemaScriptBean();
				schemaScriptBean.setSchema(new BaseEntityReference(scriptObj.getEntity().getUuid()));
				schemaScriptBean.setScript(scriptObj.getScript());
				scripts.add(schemaScriptBean);
			}
			bean.setSchemaScripts(scripts);
		}
		List<ItemDefinitionScript> dynaItemDefScripts = dynaColl.getItemDefs();
		if( !Check.isEmpty(dynaItemDefScripts) )
		{
			List<ItemDefinitionScriptBean> scripts = new ArrayList<ItemDefinitionScriptBean>(dynaItemDefScripts.size());
			for( ItemDefinitionScript itemDefScriptObj : dynaItemDefScripts )
			{
				ItemDefinitionScriptBean itemDefBean = new ItemDefinitionScriptBean();
				itemDefBean.setCollection(new BaseEntityReference(itemDefScriptObj.getEntity().getUuid()));
				itemDefBean.setScript(itemDefScriptObj.getScript());
				scripts.add(itemDefBean);
			}
			bean.setCollectionScripts(scripts);
		}
	}

	@Override
	protected AbstractEntityService<?, DynaCollection> getEntityService()
	{
		return dynaCollectionService;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.DYNA_COLLECTION;
	}
}
