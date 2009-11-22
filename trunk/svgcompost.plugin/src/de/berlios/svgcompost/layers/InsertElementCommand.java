package de.berlios.svgcompost.layers;

import org.apache.batik.dom.AbstractElement;
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
		this.newChild = newChild;
		newPosition = parent.getChildNodes().getLength();
	}
	
	public InsertElementCommand(Element parent, Element newChild, int index) {
		this.parent = parent;
		this.newChild = newChild;
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
			ElementTraversalHelper.insertAt(parent, newChild, newPosition );
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
