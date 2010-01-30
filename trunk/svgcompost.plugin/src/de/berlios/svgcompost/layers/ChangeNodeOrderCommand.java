package de.berlios.svgcompost.layers;

import java.util.List;

import org.apache.batik.dom.AbstractElement;
import org.eclipse.gef.commands.Command;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.berlios.svgcompost.part.EditEvent;
import de.berlios.svgcompost.util.ElementTraversalHelper;

public class ChangeNodeOrderCommand extends Command {

	private Element node;
	private int oldPosition;
	private int newPosition;
	private List<Element> siblings;
	private int oldElementPosition;
	private int newElementPosition;
	
	public ChangeNodeOrderCommand(Element node, int direction) {
		this.node = node;
		Node parent = node.getParentNode();
		siblings = ElementTraversalHelper.getChildElements(parent);
		oldElementPosition = siblings.indexOf(node);
		oldPosition = ElementTraversalHelper.indexOfNode( parent, node );
		newElementPosition = oldElementPosition + direction;
		if( newElementPosition >= siblings.size() )
			newPosition = parent.getChildNodes().getLength();
		else if( newElementPosition < 0 )
			newPosition = newElementPosition;
		else
			newPosition = ElementTraversalHelper.indexOfNode( parent, siblings.get(newElementPosition) );
	}
	
	@Override
	public boolean canExecute() {
		if( newElementPosition >= 0 && newElementPosition < siblings.size() )
			return true;
		return false;
	}

	@Override
	public void execute() {
		if( canExecute() ) {
			ElementTraversalHelper.moveChild( node, newPosition );
			((AbstractElement)node).dispatchEvent(new EditEvent(this, EditEvent.CHANGE_ORDER, node, node));
		}
	}

	@Override public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		ElementTraversalHelper.moveChild( node, oldPosition );
		((AbstractElement)node).dispatchEvent(new EditEvent(this, EditEvent.CHANGE_ORDER, node, node));
	}

}
