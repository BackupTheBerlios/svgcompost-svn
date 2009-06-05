package de.berlios.svgcompost.layers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;

import de.berlios.svgcompost.model.SVGNode;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.part.EditablePart;
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
		setEnabled(false);
	}
	
	@Override
	protected boolean calculateEnabled() {
		return SVGCompostPlugin.getDefault().getPreferenceStore().getBoolean(SVGCompostConstants.FLIP_LAYERS);
	}
	
	@Override
	public void run() {
		List<Object> selectedObjects = getSelectedObjects();
		if( selectedObjects == null || selectedObjects.isEmpty() )
			return;
		BackgroundPart part = searchForRoot(selectedObjects);
		if( part == null )
			return;
		SVGNode layer = part.getEditRoot();
		SVGNode parent = layer.getParent();
		if( parent == null )
			return;
		changeSiblingVisibility(layer, parent);
		part.refresh();
	}

	protected void changeSiblingVisibility(SVGNode layer, SVGNode parent) {
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

	protected BackgroundPart searchForRoot(List<Object> selectedObjects) {
		Iterator<Object> it = selectedObjects.iterator();
		while( it.hasNext() ) {
			Object nextSelected = it.next();
			if( nextSelected instanceof BackgroundPart ) {
				return (BackgroundPart) nextSelected;
			}
			else if( nextSelected instanceof EditablePart ) {
				RootEditPart root = ((EditablePart)nextSelected).getRoot();
				if( root != null && root.getChildren().get(0) instanceof BackgroundPart) {
					return (BackgroundPart) root.getChildren().get(0);
				}
			}
		}
		return null;
	}
}
