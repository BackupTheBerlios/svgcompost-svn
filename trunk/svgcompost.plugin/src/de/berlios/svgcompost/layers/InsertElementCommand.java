package de.berlios.svgcompost.layers;

import org.apache.batik.dom.AbstractElement;
import org.apache.batik.util.SVGConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.w3c.dom.Element;

import de.berlios.svgcompost.part.EditEvent;
import de.berlios.svgcompost.util.ElementTraversalHelper;

public class InsertElementCommand extends Command {

	private Element parent;
	private Element newChild;
	private int newPosition;
	
	public InsertElementCommand(Element parent, Element newChild) {
		this.parent = parent;
		newPosition = parent.getChildNodes().getLength();
		if( newChild.getOwnerDocument() == parent.getOwnerDocument() )
			this.newChild = newChild;
		else
			this.newChild = (Element) parent.getOwnerDocument().importNode(newChild, true);
	}
	
	public InsertElementCommand(Element parent, Element newChild, Rectangle bounds) {
		this( parent, newChild );
		if( newChild.getNodeName().equals( SVGConstants.SVG_RECT_TAG ) ) {
			newChild.setAttribute("width", ""+bounds.width);
			newChild.setAttribute("height", ""+bounds.height);
			newChild.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("+(bounds.x)+","+(bounds.y)+")");
		}
		else if( newChild.getNodeName().equals( SVGConstants.SVG_ELLIPSE_TAG ) ) {
			int rx = bounds.width/2;
			int ry = bounds.height/2;
			newChild.setAttribute("rx", Integer.toString(rx));
			newChild.setAttribute("ry", Integer.toString(ry));
			newChild.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("+(bounds.x+rx)+","+(bounds.y+ry)+")");
		}
	}
	
	public InsertElementCommand(Element parent, Element newChild, int index) {
		this( parent, newChild );
		newPosition = index;
	}
	
	@Override
	public boolean canExecute() {
		if( newPosition >= 0 && newPosition <= parent.getChildNodes().getLength() )
			return true;
		return false;
	}

	@Override
	public void execute() {
		if( canExecute() ) {
			ElementTraversalHelper.insertNodeAt(parent, newChild, newPosition );
			((AbstractElement)parent).dispatchEvent(new EditEvent(this, EditEvent.INSERT, null, newChild));
		}
	}

	@Override public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		parent.removeChild(newChild);
		((AbstractElement)parent).dispatchEvent(new EditEvent(this, EditEvent.REMOVE, newChild, null));
	}

}
