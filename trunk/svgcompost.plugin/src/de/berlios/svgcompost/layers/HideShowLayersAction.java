package de.berlios.svgcompost.layers;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;

import de.berlios.svgcompost.editor.SVGEditor;
import de.berlios.svgcompost.model.SVGNode;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.plugin.SVGCompostConstants;
import de.berlios.svgcompost.plugin.SVGCompostPlugin;

public class HideShowLayersAction extends SelectionAction {
	
	public static String HIDE_SHOW_LAYERS = "Action.HideShowLayers"; 

	public HideShowLayersAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		setText("Hide/Show other Layers");
		setId(HIDE_SHOW_LAYERS);
		setEnabled(calculateEnabled());
	}
	
	@Override
	protected boolean calculateEnabled() {
		return SVGCompostPlugin.getDefault().getPreferenceStore().getBoolean(SVGCompostConstants.FLIP_LAYERS);
	}
	
	@Override
	public void run() {
		BackgroundPart part = getBackground(getWorkbenchPart());
		if( part == null )
			return;
		SVGNode layer = part.getEditRoot();
		SVGNode parent = layer.getParent();
		if( parent == null )
			return;
		changeSiblingVisibility(layer, parent);
		part.refresh();
	}

	public static void changeSiblingVisibility(SVGNode layer, SVGNode parent) {
		boolean visible = false;
		List<SVGNode> siblings = parent.getChildElements();
		for (SVGNode sibling : siblings) {
			if( sibling != layer ) {
				visible = sibling.getGraphicsNode() == null ? false : sibling.getGraphicsNode().isVisible();
				break;
			}
		}
		visible = ! visible;
		for (SVGNode sibling : siblings) {
			if( sibling != layer ) {
				sibling.getElement().setAttribute("display", visible ? "inline" : "none");
			}
		}
	}

	public static BackgroundPart getBackground(IWorkbenchPart wbPart) {
		SVGEditor editor = (SVGEditor) wbPart;
		EditPart root = (EditPart) editor.getAdapter(EditPart.class);
		if( root != null && root.getChildren().get(0) instanceof BackgroundPart)
			return (BackgroundPart) root.getChildren().get(0);
		return null;
	}

}
