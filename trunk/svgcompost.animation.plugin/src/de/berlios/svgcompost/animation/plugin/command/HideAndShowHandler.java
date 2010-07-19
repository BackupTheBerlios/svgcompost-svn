package de.berlios.svgcompost.animation.plugin.command;

import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.berlios.svgcompost.animation.canvas.Library;
import de.berlios.svgcompost.editor.SVGEditor;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.util.ElementTraversalHelper;
import de.berlios.svgcompost.util.VisibilityHelper;
import de.berlios.svgcompost.xmlconstants.Classes;

public class HideAndShowHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event)
			throws org.eclipse.core.commands.ExecutionException {
		SVGEditor editor = (SVGEditor) HandlerUtil.getActiveEditor(event);
		BackgroundPart part = FlipForwardHandler.getBackground(editor);
		if( part == null )
			return null;
		Element keyframe = getKeyFrame(part.getEditRoot());
		if( keyframe == null || keyframe.getParentNode() == null )
			return null;
		Element parent = (Element) keyframe.getParentNode();
		changeSiblingVisibility(keyframe, parent, part.getBridgeContext());
		part.refresh();
		return null;
	}

	public static Element getKeyFrame(Element node) {
		while( ! Library.hasClass( node, Classes.FRAME ) ) {
			if( node.getParentNode().getNodeType() == Node.ELEMENT_NODE )
				node = (Element) node.getParentNode();
			else
				node = null;
			if( node == null )
				return null;
		}
		return node;
	}

	public static void changeSiblingVisibility(Element layer, Element parent, BridgeContext ctx) {
		boolean visible = false;
		List<Element> siblings = ElementTraversalHelper.getChildElements(parent);
		for (Element sibling : siblings) {
			if( sibling != layer ) {
				visible = ctx.getGraphicsNode(sibling) == null ? false : ctx.getGraphicsNode(sibling).isVisible();
				break;
			}
		}
		visible = ! visible;
		for (Element sibling : siblings) {
			if( sibling != layer ) {
//				FreeTransformHelper.setDisplayValue( sibling.getElement(), visible );
				VisibilityHelper.setVisibility( sibling, visible );
			}
		}
	}

}
