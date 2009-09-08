package de.berlios.svgcompost.layers;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.w3c.dom.svg.SVGGElement;

import de.berlios.svgcompost.editor.SVGEditor;
import de.berlios.svgcompost.model.SVGNode;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.part.EditablePart;
import de.berlios.svgcompost.plugin.SVGCompostConstants;
import de.berlios.svgcompost.plugin.SVGCompostPlugin;

public class FlipForwardAction extends SelectionAction {
	
	public static String FLIP_FORWARD = "Action.FlipForward"; 

	public FlipForwardAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		setText("Flip Forward");
		setId(FLIP_FORWARD);
		setEnabled(false);
	}
	
	protected int getDirection() {
		return -1;
	}

	@Override
	protected boolean calculateEnabled() {
		return SVGCompostPlugin.getDefault().getPreferenceStore().getBoolean(SVGCompostConstants.FLIP_LAYERS);
	}
	
	@Override
	public void run() {
		BackgroundPart bgPart = HideShowLayersAction.getBackground(getWorkbenchPart());
		if( bgPart == null )
			return;
		SVGNode layer = bgPart.getEditRoot();
		SVGNode parent = layer.getParent();
		if( parent == null )
			return;
		int layerIndex = parent.getChildElements().indexOf(layer);
		int flipIndex = layerIndex + getDirection();
		if( flipIndex >= 0 && flipIndex < parent.getChildElements().size() ) {
			SVGNode flipLayer = parent.getChildElements().get(flipIndex);
			boolean visible = flipLayer.getGraphicsNode() == null ? false : flipLayer.getGraphicsNode().isVisible();
			if( ! visible ) {
				// TODO: only flip selection, have visibility managed by the BackgroundElement.
				// problem with display:none is that no GVT nodes are generated if saved and reloaded.
				layer.getElement().setAttribute("display", "none");
				flipLayer.getElement().setAttribute("display", "inline");
				SVGEditor editor = (SVGEditor) getWorkbenchPart();
				GraphicalViewer viewer = (GraphicalViewer) editor.getAdapter(GraphicalViewer.class);
				EditablePart flipLayerPart = (EditablePart) viewer.getEditPartRegistry().get(flipLayer.getElement());
				viewer.setSelection( new StructuredSelection(flipLayerPart) );
				bgPart.setEditRoot( flipLayer );
				bgPart.refreshVisuals();
			}
		}
	}

}
