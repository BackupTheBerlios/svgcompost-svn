package de.berlios.svgcompost.animation.plugin.command;

import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Element;

import de.berlios.svgcompost.animation.canvas.Library;
import de.berlios.svgcompost.editor.SVGEditor;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.util.ElementTraversalHelper;
import de.berlios.svgcompost.util.LinkHelper;

public class NewKeyframeHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		SVGEditor editor = (SVGEditor) HandlerUtil.getActiveEditor(event);
		BackgroundPart part = FlipForwardHandler.getBackground(editor);
		if( part == null )
			return null;
		Element node = part.getEditRoot();
		while( ! Library.hasClass( node, "keyframe" ) ) {
			if( node.getParentNode() instanceof Element )
				node = (Element) node.getParentNode();
			else
				node = null;
			if( node == null )
				return null;
		}
		Element layer = (Element) node.getParentNode();
		SVGGraphicsElement element = (SVGGraphicsElement) node;
		Element clone = (Element) element.cloneNode(true);
		LinkHelper.changeIds(clone, element.getOwnerDocument());
		int index = ElementTraversalHelper.indexOf(layer,node);
		ElementTraversalHelper.insertAt(layer, clone, index+1);
		return null;
	}

}
