package de.berlios.svgcompost.animation.plugin.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.handlers.HandlerUtil;

import de.berlios.svgcompost.editor.SVGEditor;
import de.berlios.svgcompost.freetransform.FreeTransformHelper;
import de.berlios.svgcompost.model.SVGNode;
import de.berlios.svgcompost.part.BackgroundPart;

public class HideAndShowHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event)
			throws org.eclipse.core.commands.ExecutionException {
		SVGEditor editor = (SVGEditor) HandlerUtil.getActiveEditor(event);
		BackgroundPart part = FlipForwardHandler.getBackground(editor);
		if( part == null )
			return null;
		SVGNode layer = part.getEditRoot();
		SVGNode parent = layer.getParent();
		if( parent == null )
			return null;
		changeSiblingVisibility(layer, parent);
		part.refresh();
		return null;
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
				FreeTransformHelper.setDisplayValue( sibling.getElement(), visible );
			}
		}
	}

}
