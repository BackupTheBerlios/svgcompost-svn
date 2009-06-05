package de.berlios.svgcompost.plugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class SVGCompostPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private Button freeTransformCenter;
	private Button freeTransformOrigin;

	private Button hideSiblingLayers;

	@Override
	protected Control createContents(Composite parent) {
		Composite container = WidgetHelper.addContainer(parent,1);

		createFreeTransformGroup(container);
		createFlipGroup(container);
		
		initializeValues();

		return container;
	}

	private void createFreeTransformGroup(Composite container) {
		Group freeTransformGroup = WidgetHelper.addGroup(container, "Reference Point for Transformations", 1);
		WidgetHelper.addLabel(freeTransformGroup, "When editing SVG transformations, rotate, scale and skew");
		freeTransformCenter = WidgetHelper.addRadioButton(freeTransformGroup, "around the geometric center of the figure.", true);
		freeTransformOrigin = WidgetHelper.addRadioButton(freeTransformGroup, "around the origin coordinates of the SVG node.", false);
	}
	
	private void createFlipGroup(Composite container) {
		Group flipGroup = WidgetHelper.addGroup(container, "Edit layers as frames", 1);
		hideSiblingLayers = WidgetHelper.addCheckButton(flipGroup, "Allow hiding of sibling layers and flipping between them.", false);
	}
	
	public void init(IWorkbench arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return SVGCompostPlugin.getDefault().getPreferenceStore();
	}
	
	@Override
	public boolean performOk() {
		storeValues();
		return super.performOk();
	}

	@Override
	public void performApply() {
		storeValues();
	}

	private void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(SVGCompostConstants.FREETRANSFORM_CENTER, freeTransformCenter.getSelection());
		store.setValue(SVGCompostConstants.FLIP_LAYERS, hideSiblingLayers.getSelection());
	}
	
	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		freeTransformCenter.setSelection(store.getBoolean(SVGCompostConstants.FREETRANSFORM_CENTER));
		freeTransformOrigin.setSelection(!freeTransformCenter.getSelection());
		hideSiblingLayers.setSelection(store.getBoolean(SVGCompostConstants.FLIP_LAYERS));
	}
	
	
}
