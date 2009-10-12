package de.berlios.svgcompost.animation.plugin.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.berlios.svgcompost.editor.SVGEditor;
import de.berlios.svgcompost.freetransform.FreeTransformHelper;
import de.berlios.svgcompost.model.SVGNode;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.part.EditablePart;

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
		SVGNode keyframe = bgPart.getEditRoot();
		SVGNode parent = keyframe.getParent();
		if( parent == null )
			return null;
		int layerIndex = parent.getChildElements().indexOf(keyframe);
		int flipIndex = layerIndex + getDirection();
		if( flipIndex >= 0 && flipIndex < parent.getChildElements().size() ) {
			SVGNode nextKeyframe = parent.getChildElements().get(flipIndex);
			boolean visible = nextKeyframe.getGraphicsNode() == null ? false : nextKeyframe.getGraphicsNode().isVisible();
			if( visible ) {
				// Next keyframe is already visible.
				// This means sibling visibility is still enabled.
				// Change visibility.
				changeSiblingVisibility(nextKeyframe);
			}
			// TODO: only flip selection, have visibility managed by the BackgroundElement.
			// problem with display:none is that no GVT nodes are generated if saved and reloaded.
//			keyframe.getElement().setAttribute("display", "none");
//			nextKeyframe.getElement().setAttribute("display", "inline");
			FreeTransformHelper.setDisplayValue(keyframe.getElement(), false);
			FreeTransformHelper.setDisplayValue(nextKeyframe.getElement(), true);
			GraphicalViewer viewer = (GraphicalViewer) editor.getAdapter(GraphicalViewer.class);
			EditablePart flipLayerPart = (EditablePart) viewer.getEditPartRegistry().get(nextKeyframe.getElement());
			viewer.setSelection( new StructuredSelection(flipLayerPart) );
			bgPart.setEditRoot( nextKeyframe );
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

	public static void changeSiblingVisibility(SVGNode layer) {
		SVGNode parent = layer.getParent();
		if(parent == null)
			return;
		boolean visible = false;
		List<SVGNode> siblings = parent.getChildElements();
		// Search for the first sibling and check whether it's visible.
		for (SVGNode sibling : siblings) {
			if( sibling != layer ) {
				visible = sibling.getGraphicsNode() == null ? false : sibling.getGraphicsNode().isVisible();
				break;
			}
		}
		// Change the visibility of all siblings.
		visible = ! visible;
		for (SVGNode sibling : siblings) {
			if( sibling != layer ) {
				sibling.getElement().setAttribute("display", visible ? "inline" : "none");
			}
		}
	}

}
