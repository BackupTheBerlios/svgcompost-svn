package de.berlios.svgcompost.layers;

import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.gvt.GraphicsNode;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.w3c.dom.Element;

import de.berlios.svgcompost.editor.SVGEditor;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.part.EditablePart;
import de.berlios.svgcompost.plugin.SVGCompostConstants;
import de.berlios.svgcompost.plugin.SVGCompostPlugin;
import de.berlios.svgcompost.util.ElementTraversalHelper;

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
		BridgeContext ctx = bgPart.getBridgeContext();
		Element layer = bgPart.getEditRoot();
		Element parent = (Element) layer.getParentNode();
		if( parent == null )
			return;
		List<Element> childElements = ElementTraversalHelper.getChildElements(parent);
		int layerIndex = childElements.indexOf( layer );
		int flipIndex = layerIndex + getDirection();
		if( flipIndex >= 0 && flipIndex < childElements.size() ) {
			Element flipLayer = childElements.get(flipIndex);
			GraphicsNode gNode = ctx.getGraphicsNode(flipLayer);
			boolean visible = gNode == null ? false : gNode.isVisible();
			if( ! visible ) {
				// TODO: only flip selection, have visibility managed by the BackgroundElement.
				// problem with display:none is that no GVT nodes are generated if saved and reloaded.
				layer.setAttribute("display", "none");
				flipLayer.setAttribute("display", "inline");
				SVGEditor editor = (SVGEditor) getWorkbenchPart();
				GraphicalViewer viewer = (GraphicalViewer) editor.getAdapter(GraphicalViewer.class);
				EditablePart flipLayerPart = (EditablePart) viewer.getEditPartRegistry().get(flipLayer);
				viewer.setSelection( new StructuredSelection(flipLayerPart) );
				bgPart.setEditRoot( (SVGOMElement) flipLayer );
				bgPart.refreshVisuals();
			}
		}
	}

}
