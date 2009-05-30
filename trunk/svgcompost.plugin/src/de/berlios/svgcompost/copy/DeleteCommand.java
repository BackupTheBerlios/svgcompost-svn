package de.berlios.svgcompost.copy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;
import org.w3c.dom.Element;

import de.berlios.svgcompost.model.SVGNode;

public class DeleteCommand extends Command {

	private List<SVGNode> list = new ArrayList<SVGNode>();
	private List<Integer> indices = new ArrayList<Integer>();
	private List<SVGNode> parents = new ArrayList<SVGNode>();
	
	public void addNode( SVGNode node ) {
		SVGNode parent = node.getParent();
		if( parent == null )
			return;
		list.add(node);
		indices.add( parent.getChildElements().indexOf(node) );
		parents.add(parent);
	}

	@Override
	public boolean canExecute() {
		if( list == null || list.isEmpty() )
			return false;
		return true;
	}

	@Override
	public void execute() {
		if( !canExecute() )
			return;
		for (int i = 0; i < list.size(); i++) {
			parents.get(i).removeChild(list.get(i));
		}
	}

	@Override public boolean canUndo() {
		return ! list.isEmpty();
	}
	
	@Override
	public void undo() {
		for (int i = 0; i < list.size(); i++) {
			parents.get(i).addChild(indices.get(i), list.get(i));
		}
	}
}
