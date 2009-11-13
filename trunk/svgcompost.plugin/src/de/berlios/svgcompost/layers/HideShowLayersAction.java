package de.berlios.svgcompost.layers;

import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.w3c.dom.Element;

import de.berlios.svgcompost.editor.SVGEditor;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.plugin.SVGCompostConstants;
import de.berlios.svgcompost.plugin.SVGCompostPlugin;
import de.berlios.svgcompost.util.ElementTraversalHelper;

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
		Element layer = part.getEditRoot();
		Element parent = (Element) layer.getParentNode();
		if( parent == null )
			return;
		changeSiblingVisibility(layer, parent, part.getBridgeContext());
		part.refresh();
	}

	public static void changeSiblingVisibility(Element layer, Element parent, BridgeContext ctx) {
		boolean visible = false;
		List<Element> siblings = ElementTraversalHelper.getChildElements(parent);
		for (Element sibling : siblings) {
			if( sibling != layer ) {
				GraphicsNode gNode = ctx.getGraphicsNode(sibling);
				visible = gNode == null ? false : gNode.isVisible();
				break;
			}
		}
		visible = ! visible;
		for (Element sibling : siblings) {
			if( sibling != layer ) {
				sibling.setAttribute("display", visible ? "inline" : "none");
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
