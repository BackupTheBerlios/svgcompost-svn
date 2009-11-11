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
import de.berlios.svgcompost.model.SVGNode;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.util.LinkHelper;

public class NewKeyframeHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		SVGEditor editor = (SVGEditor) HandlerUtil.getActiveEditor(event);
		BackgroundPart part = FlipForwardHandler.getBackground(editor);
		if( part == null )
			return null;
		SVGNode node = part.getEditRoot();
		while( ! Library.hasClass( node.getElement(), "keyframe" ) ) {
			node = node.getParent();
			if( node == null )
				return null;
		}
		SVGNode layer = node.getParent();
		SVGGraphicsElement element = (SVGGraphicsElement) node.getElement();
		Element clone = (Element) element.cloneNode(true);
		LinkHelper.changeIds(clone, element.getOwnerDocument());
		SVGNode newNode = new SVGNode( clone, layer );
		int index = layer.getChildElements().indexOf(node);
		layer.addChild(index+1, newNode);
		return null;
	}

}
