/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html Contributors: Actuate Corporation -
 * initial API and implementation
 *******************************************************************************/

package com.tle.reporting.oda.ui.jdbc.ui.editors;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.tle.reporting.oda.ui.TLEOdaPlugin;

/**
 * Text menu manager contains 'undo,redo,cut,copy,paste,select all' menuItem. It
 * displays on textVeiwer.
 */
class TextMenuManager
{

	private final Hashtable htActions = new Hashtable();
	private final MenuManager manager;

	/**
	 * Constructor to specify the textMenuManager for a text viewer.
	 * 
	 * @param viewer
	 */
	TextMenuManager(TextViewer viewer)
	{
		manager = new MenuManager();
		Separator separator = new Separator("undo");//$NON-NLS-1$
		manager.add(separator);
		separator = new Separator("copy");//$NON-NLS-1$
		manager.add(separator);
		separator = new Separator("select");//$NON-NLS-1$
		manager.add(separator);
		manager
			.appendToGroup(
				"undo", getAction("undo", viewer, TLEOdaPlugin.getResourceString2("sqleditor.action.undo"), ITextOperationTarget.UNDO));//$NON-NLS-1$
		manager
			.appendToGroup(
				"undo", getAction("redo", viewer, TLEOdaPlugin.getResourceString2("sqleditor.action.redo"), ITextOperationTarget.REDO));//$NON-NLS-1$
		manager
			.appendToGroup(
				"copy", getAction("cut", viewer, TLEOdaPlugin.getResourceString2("sqleditor.action.cut"), ITextOperationTarget.CUT));//$NON-NLS-1$
		manager
			.appendToGroup(
				"copy", getAction("copy", viewer, TLEOdaPlugin.getResourceString2("sqleditor.action.copy"), ITextOperationTarget.COPY));//$NON-NLS-1$
		manager
			.appendToGroup(
				"copy", getAction("paste", viewer, TLEOdaPlugin.getResourceString2("sqleditor.action.paste"), ITextOperationTarget.PASTE));//$NON-NLS-1$
		manager
			.appendToGroup(
				"select", getAction("selectall", viewer, TLEOdaPlugin.getResourceString2("sqleditor.action.selectAll"), ITextOperationTarget.SELECT_ALL));//$NON-NLS-1$

		manager.addMenuListener(new IMenuListener()
		{

			public void menuAboutToShow(IMenuManager manager)
			{
				Enumeration elements = htActions.elements();
				while( elements.hasMoreElements() )
				{
					SQLEditorAction action = (SQLEditorAction) elements.nextElement();
					action.update();
				}
			}
		});
	}

	/**
	 * @param control
	 * @return
	 */
	public Menu getContextMenu(Control control)
	{
		return manager.createContextMenu(control);
	}

	/**
	 * @param id
	 * @param viewer
	 * @param name
	 * @param operation
	 * @return
	 */
	private final SQLEditorAction getAction(String id, TextViewer viewer, String name, int operation)
	{
		SQLEditorAction action = (SQLEditorAction) htActions.get(id);
		if( action == null )
		{
			action = new SQLEditorAction(viewer, name, operation);
			htActions.put(id, action);
		}
		return action;
	}

	/**
	 * SQL editor action set
	 */
	class SQLEditorAction extends Action
	{

		private int operationCode = -1;
		private TextViewer viewer = null;

		public SQLEditorAction(TextViewer viewer, String text, int operationCode)
		{
			super(text);
			this.operationCode = operationCode;
			this.viewer = viewer;
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		@Override
		public void run()
		{
			viewer.doOperation(operationCode);
		}

		/**
		 * update the operation
		 */
		public void update()
		{
			setEnabled(viewer.canDoOperation(operationCode));
		}

	}

}