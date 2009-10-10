package de.berlios.svgcompost.animation.plugin.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.berlios.svgcompost.editor.SVGEditor;
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
		SVGNode layer = bgPart.getEditRoot();
		SVGNode parent = layer.getParent();
		if( parent == null )
			return null;
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
//				SVGEditor editor = (SVGEditor) getWorkbenchPart();
				GraphicalViewer viewer = (GraphicalViewer) editor.getAdapter(GraphicalViewer.class);
				EditablePart flipLayerPart = (EditablePart) viewer.getEditPartRegistry().get(flipLayer.getElement());
				viewer.setSelection( new StructuredSelection(flipLayerPart) );
				bgPart.setEditRoot( flipLayer );
				bgPart.refreshVisuals();
			}
		}
		return null;
	}

	public static BackgroundPart getBackground(IWorkbenchPart wbPart) {
		SVGEditor editor = (SVGEditor) wbPart;
		EditPart root = (EditPart) editor.getAdapter(EditPart.class);
		if( root != null && root.getChildren().get(0) instanceof BackgroundPart)
			return (BackgroundPart) root.getChildren().get(0);
		return null;
	}


}
