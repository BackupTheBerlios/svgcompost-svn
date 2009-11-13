package de.berlios.svgcompost.animation.plugin.command;

import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.svg.SVGOMElement;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Element;

import de.berlios.svgcompost.editor.SVGEditor;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.part.EditablePart;
import de.berlios.svgcompost.util.ElementTraversalHelper;
import de.berlios.svgcompost.util.VisibilityHelper;

public class FlipForwardHandler extends AbstractHandler {

	protected int getDirection() {
		return -1;
	}

	@Override
	public Object execute(ExecutionEvent event)
			throws org.eclipse.core.commands.ExecutionException {
		SVGEditor editor = (SVGEditor) HandlerUtil.getActiveEditor(event);
		BackgroundPart bgPart = getBackground(editor);
		if( bgPart == null )
			return null;
		BridgeContext ctx = bgPart.getBridgeContext();
		Element keyframe = bgPart.getEditRoot();
		Element parent = (Element) keyframe.getParentNode();
		if( parent == null )
			return null;
		List<Element> childElements = ElementTraversalHelper.getChildElements(parent);
		int layerIndex = childElements.indexOf(keyframe);
		int flipIndex = layerIndex + getDirection();
		if( flipIndex >= 0 && flipIndex < childElements.size() ) {
			Element nextKeyframe = childElements.get(flipIndex);
			boolean visible = ctx.getGraphicsNode(nextKeyframe) == null ? false : ctx.getGraphicsNode(nextKeyframe).isVisible();
			if( visible ) {
				// Next keyframe is already visible.
				// This means sibling visibility is still enabled.
				// Change visibility.
				changeSiblingVisibility(nextKeyframe, bgPart.getBridgeContext());
			}
			VisibilityHelper.setVisibility(keyframe, false);
			VisibilityHelper.setVisibility(nextKeyframe, true);
			GraphicalViewer viewer = (GraphicalViewer) editor.getAdapter(GraphicalViewer.class);
			EditablePart flipLayerPart = (EditablePart) viewer.getEditPartRegistry().get(nextKeyframe);
			viewer.setSelection( new StructuredSelection(flipLayerPart) );
			bgPart.setEditRoot( (SVGOMElement) nextKeyframe );
			bgPart.refreshVisuals();
		}
		return null;
	}

	protected static BackgroundPart getBackground(IWorkbenchPart wbPart) {
		SVGEditor editor = (SVGEditor) wbPart;
		EditPart root = (EditPart) editor.getAdapter(EditPart.class);
		if( root != null && root.getChildren().get(0) instanceof BackgroundPart)
			return (BackgroundPart) root.getChildren().get(0);
		return null;
	}

	public static void changeSiblingVisibility(Element layer, BridgeContext ctx) {
		Element parent = (Element) layer.getParentNode();
		if(parent == null)
			return;
		boolean visible = false;
		List<Element> siblings = ElementTraversalHelper.getChildElements(parent);
		// Search for the first sibling and check whether it's visible.
		for (Element sibling : siblings) {
			if( sibling != layer ) {
				visible = ctx.getGraphicsNode(sibling) == null ? false : ctx.getGraphicsNode(sibling).isVisible();
				break;
			}
		}
		// Change the visibility of all siblings.
		visible = ! visible;
		for (Element sibling : siblings) {
			if( sibling != layer ) {
				VisibilityHelper.setVisibility(sibling, visible);
			}
		}
	}

}
