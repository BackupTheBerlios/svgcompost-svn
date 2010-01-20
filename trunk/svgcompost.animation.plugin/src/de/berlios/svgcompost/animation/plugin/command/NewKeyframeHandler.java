package de.berlios.svgcompost.animation.plugin.command;

import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.gef.commands.Command;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.berlios.svgcompost.animation.canvas.Library;
import de.berlios.svgcompost.animation.util.xml.Classes;
import de.berlios.svgcompost.editor.SVGEditor;
import de.berlios.svgcompost.layers.InsertElementCommand;
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
		Element keyframe = HideAndShowHandler.getKeyFrame(part.getEditRoot());
		if( keyframe == null || keyframe.getParentNode() == null )
			return null;
		Element layer = (Element) keyframe.getParentNode();
		SVGGraphicsElement element = (SVGGraphicsElement) keyframe;
		Element clone = (Element) element.cloneNode(true);
		LinkHelper.changeIds(clone, element.getOwnerDocument());
		int index = ElementTraversalHelper.indexOf(layer,keyframe);
//		ElementTraversalHelper.insertAt(layer, clone, index+1);
		// TODO: use Command
		Command cmd = new InsertElementCommand(layer,clone,index+1);
		if( cmd != null && cmd.canExecute() ) {
			editor.getEditDomain().getCommandStack().execute(cmd);
		}
		return null;
	}

}
