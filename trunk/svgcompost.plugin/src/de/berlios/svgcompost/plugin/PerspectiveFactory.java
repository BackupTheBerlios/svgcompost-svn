package de.berlios.svgcompost.plugin;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addView("de.berlios.svgcompost.navigator.view", IPageLayout.LEFT, 0.2f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(IPageLayout.ID_PROP_SHEET, IPageLayout.BOTTOM, 0.8f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.75f, IPageLayout.ID_EDITOR_AREA);
	}

}
