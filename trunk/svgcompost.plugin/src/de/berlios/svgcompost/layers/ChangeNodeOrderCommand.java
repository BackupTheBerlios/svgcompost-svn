package de.berlios.svgcompost.layers;

import org.eclipse.gef.commands.Command;
import org.w3c.dom.Element;

import de.berlios.svgcompost.util.ElementTraversalHelper;

public class ChangeNodeOrderCommand extends Command {

	private Element node;
	private int oldPosition;
	private int newPosition;
	
	public ChangeNodeOrderCommand(Element node, int direction) {
		this.node = node;
		oldPosition = ElementTraversalHelper.indexOf( node.getParentNode(), node );
		newPosition = oldPosition + direction;
	}
	
	@Override
	public boolean canExecute() {
		if( newPosition >= 0 && newPosition < node.getParentNode().getChildNodes().getLength() )
			return true;
		return false;
	}

	@Override
	public void execute() {
		if( canExecute() ) {
			ElementTraversalHelper.moveChild( node, newPosition );
		}
	}

	@Override public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		ElementTraversalHelper.moveChild( node, oldPosition );
	}

}
