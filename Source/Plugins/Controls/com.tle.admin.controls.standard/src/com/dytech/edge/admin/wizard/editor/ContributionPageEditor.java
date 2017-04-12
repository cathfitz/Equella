package com.dytech.edge.admin.wizard.editor;

import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.edge.admin.wizard.model.AbstractPageModel;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

public class ContributionPageEditor extends Editor
{
	private static final long serialVersionUID = 1L;
	private I18nTextField title;

	public ContributionPageEditor(Control control, int wizardType, SchemaModel model)
	{
		super(control, wizardType, model);
		setup();
	}

	@Override
	protected void loadControl()
	{
		AbstractPageModel<?> control = (AbstractPageModel<?>) getControl();

		title.load(control.getTitle());
	}

	@Override
	protected void saveControl()
	{
		AbstractPageModel<?> control = (AbstractPageModel<?>) getControl();

		control.setTitle(title.save());
	}

	private void setup()
	{
		setShowScripting(true);

		JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title")); //$NON-NLS-1$

		title = new I18nTextField(BundleCache.getLanguages());

		final int width1 = titleLabel.getPreferredSize().width;
		final int height1 = title.getPreferredSize().height;

		final int[] rows = {height1,};
		final int[] cols = {width1, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
		all.add(titleLabel, new Rectangle(0, 0, 1, 1));
		all.add(title, new Rectangle(1, 0, 1, 1));

		addSection(all);
	}
}
