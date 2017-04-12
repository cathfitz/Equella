/*
 * Created on 4/05/2006
 */
package com.tle.core.institution.convert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.tle.beans.Institution;
import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.core.dao.AccessExpressionDao;
import com.tle.core.dao.AclDao;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.migration.PostReadMigrator;
import com.tle.core.util.DefaultMessageCallback;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AclConverter extends AbstractConverter<AclConverter.AclPostReadMigratorParams>
{
	private static final Logger LOGGER = Logger.getLogger(AclConverter.class);

	@Inject
	private AclDao aclDao;
	@Inject
	private AccessExpressionDao accessExpressionDao;

	private static final String EXPRESSION_FILENAME = "acls/expressions.xml";
	private static final String ENTRY_FILENAME = "acls/entries.xml";

	private boolean convertAcl(AccessEntry entry, ConverterParams params)
	{
		String targetObject = entry.getTargetObject();
		String[] parts = targetObject.split(":");

		Map<Long, Long> old2new = params.getOld2new();
		int uuidnum = -1;
		char ch = parts[0].charAt(0);
		switch( ch )
		{
			case 'H':
				uuidnum = 1;
				old2new = params.getHierarchies();
				break;
			case 'D':
			case 'I':
				uuidnum = 1;
				old2new = params.getItems();
				break;
			case 'S':
				if( parts.length != 3 )
				{
					break;
				}
				//$FALL-THROUGH$
			case 'T':
			case 'B':
			case 'M':
				uuidnum = 1;
				break;

			default:
				break;
		}

		if( uuidnum >= 0 )
		{
			if( parts.length <= uuidnum )
			{
				LOGGER.error("Invalid targetObject " + targetObject);
				return false;
			}

			try
			{
				long oldId = Long.parseLong(parts[uuidnum]);
				Long newId = old2new.get(oldId);
				if( newId == null )
				{
					LOGGER.error("Can't find new ID for old ID:'" + oldId + "' Expression:" + targetObject);
					return false;
				}
				parts[uuidnum] = Long.toString(newId);
			}
			catch( NumberFormatException ex )
			{
				LOGGER.error("Error parsing number '" + parts[uuidnum] + "' from expression:" + targetObject);
				return false;
			}

			StringBuilder sbuf = new StringBuilder();
			for( int i = 0; i < parts.length; i++ )
			{
				if( i > 0 )
				{
					sbuf.append(':');
				}
				sbuf.append(parts[i]);
			}
			entry.setTargetObject(sbuf.toString());
		}
		return true;
	}

	@Override
	public void doDelete(Institution institution, ConverterParams params)
	{
		aclDao.deleteAll();
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		final List<AccessEntry> acls = aclDao.listAll();
		final Set<AccessExpression> exprs = new HashSet<AccessExpression>();

		final DefaultMessageCallback message = new DefaultMessageCallback("institutions.converter.generic.genericmsg");
		message.setType("ACLs and Access Expressions");
		message.setTotal(acls.size() * 2);

		callback.setMessageCallback(message);

		final List<AccessEntry> newAcls = new ArrayList<AccessEntry>();
		for( AccessEntry entry : acls )
		{
			AccessEntry newEntry = (AccessEntry) entry.clone();
			newEntry.setInstitution(null);
			AccessExpression newExpr = new AccessExpression();
			AccessExpression expr = entry.getExpression();
			exprs.add(expr);
			newExpr.setId(expr.getId());
			newEntry.setExpression(newExpr);
			newAcls.add(newEntry);
			aclDao.unlinkFromSession(expr);
			aclDao.unlinkFromSession(entry);
			message.incrementCurrent();
		}
		xmlHelper.writeXmlFile(staging, ENTRY_FILENAME, newAcls);

		final List<AccessExpression> newExprs = new ArrayList<AccessExpression>();
		for( AccessExpression expression : exprs )
		{
			AccessExpression newExpr = (AccessExpression) expression.clone();
			newExpr.setExpressionParts(null);
			newExprs.add(newExpr);
			message.incrementCurrent();
		}
		aclDao.clear();
		xmlHelper.writeXmlFile(staging, EXPRESSION_FILENAME, newExprs);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		if( !fileSystemService.fileExists(staging, EXPRESSION_FILENAME) )
		{
			return;
		}

		final DefaultMessageCallback message = new DefaultMessageCallback("institutions.converter.generic.genericmsg");
		params.setMessageCallback(message);

		List<AccessExpression> exprs = (List<AccessExpression>) xmlHelper.readXmlFile(staging, EXPRESSION_FILENAME);
		Map<Long, AccessExpression> oldIdToNewExpression = new HashMap<Long, AccessExpression>();
		message.setType("Access Expressions");
		message.setTotal(exprs.size());

		for( AccessExpression expression : exprs )
		{
			oldIdToNewExpression.put(expression.getId(),
				accessExpressionDao.retrieveOrCreate(expression.getExpression()));
			message.incrementCurrent();
		}

		List<AccessEntry> entries = (List<AccessEntry>) xmlHelper.readXmlFile(staging, ENTRY_FILENAME);
		AclPostReadMigratorParams postReadParams = new AclPostReadMigratorParams(entries, oldIdToNewExpression);
		Collection<PostReadMigrator<AclPostReadMigratorParams>> migrations = getMigrations(params);
		runMigrations(migrations, postReadParams);
		List<AccessEntry> xmlAcls = postReadParams.getEntriesFromXml();
		List<AccessEntry> additionalAcls = postReadParams.getAdditionalEntries();

		aclDao.flush();
		aclDao.clear();
		message.setType("ACLs");
		message.setCurrent(0);
		message.setTotal(xmlAcls.size() + additionalAcls.size());

		for( AccessEntry entry : xmlAcls )
		{
			entry.setExpression(oldIdToNewExpression.get(entry.getExpression().getId()));

			if( convertAcl(entry, params) )
			{
				entry.setInstitution(institution);
				aclDao.save(entry);
				aclDao.flush();
			}
			aclDao.clear();
			message.incrementCurrent();
		}
		for( AccessEntry entry : additionalAcls )
		{
			entry.setInstitution(institution);
			aclDao.save(entry);
			aclDao.flush();
			aclDao.clear();
			message.incrementCurrent();
		}
	}

	@Override
	public ConverterId getConverterId()
	{
		return ConverterId.ACLS;
	}

	public static class AclPostReadMigratorParams implements Iterable<AccessEntry>
	{
		private final List<AccessEntry> entriesFromXml;
		private final List<AccessEntry> additionalEntries;
		private final Map<Long, AccessExpression> expressionsFromXml;

		public AclPostReadMigratorParams(List<AccessEntry> entriesFromXml,
			Map<Long, AccessExpression> expressionsFromXml)
		{
			this.entriesFromXml = entriesFromXml;
			this.expressionsFromXml = expressionsFromXml;
			this.additionalEntries = new ArrayList<AccessEntry>();
		}

		/**
		 * Important! Make sure the AccessExpresion you reference is already
		 * saved. The institution field should be left null. Also, your target
		 * object is required to be new as well (ie. it won't be converted to
		 * look up from the old ID)
		 * 
		 * @param entry
		 */
		public void addAdditionalEntry(AccessEntry entry)
		{
			additionalEntries.add(entry);
		}

		protected List<AccessEntry> getAdditionalEntries()
		{
			return additionalEntries;
		}

		protected List<AccessEntry> getEntriesFromXml()
		{
			return entriesFromXml;
		}

		public Map<Long, AccessExpression> getExpressionsFromXml()
		{
			return expressionsFromXml;
		}

		@Override
		public Iterator<AccessEntry> iterator()
		{
			return new EntryIterator();
		}

		public class EntryIterator implements Iterator<AccessEntry>
		{
			private final Iterator<AccessEntry> fromXmlIterator = entriesFromXml.iterator();
			private final Iterator<AccessEntry> additionalIterator = additionalEntries.iterator();
			private boolean usingFromXml = fromXmlIterator.hasNext();

			@Override
			public boolean hasNext()
			{
				return (usingFromXml && fromXmlIterator.hasNext()) || additionalIterator.hasNext();
			}

			@Override
			public AccessEntry next()
			{
				if( !fromXmlIterator.hasNext() )
				{
					usingFromXml = false;
				}
				if( !usingFromXml )
				{
					return additionalIterator.next();
				}
				return fromXmlIterator.next();
			}

			@Override
			public void remove()
			{
				if( usingFromXml )
				{
					fromXmlIterator.remove();
				}
				else
				{
					additionalIterator.remove();
				}
			}
		}
	}
}
