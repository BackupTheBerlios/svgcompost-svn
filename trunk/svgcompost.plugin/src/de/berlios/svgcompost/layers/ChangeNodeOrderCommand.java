package de.berlios.svgcompost.layers;

import org.eclipse.gef.commands.Command;

import de.berlios.svgcompost.model.SVGNode;

public class ChangeNodeOrderCommand extends Command {

	private SVGNode node;
	private int oldPosition;
	private int newPosition;
	
	public ChangeNodeOrderCommand(SVGNode node, int direction) {
		this.node = node;
		oldPosition = node.getParent().getChildElements().indexOf(node);
		newPosition = oldPosition + direction;
	}
	
	@Override
	public boolean canExecute() {
		if( newPosition >= 0 && newPosition < node.getParent().getChildElements().size() )
			return true;
		return false;
	}

	@Override
	public void execute() {
		if( canExecute() ) {
			node.getParent().moveChild( node, oldPosition, newPosition );
		}
	}

	@Override public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		node.getParent().moveChild( node, newPosition, oldPosition );
	}

}
