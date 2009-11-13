package de.berlios.svgcompost.copy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.w3c.dom.Element;

import de.berlios.svgcompost.util.ElementTraversalHelper;

public class DeleteCommand extends Command {

	private List<Element> list = new ArrayList<Element>();
	private List<Integer> indices = new ArrayList<Integer>();
	private List<Element> parents = new ArrayList<Element>();
	
	public void addNode( Element node ) {
		Element parent = (Element) node.getParentNode();
		if( parent == null )
			return;
		list.add(node);
		indices.add( ElementTraversalHelper.indexOf(parent,node) );
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
			ElementTraversalHelper.insertAt( parents.get(i), list.get(i), indices.get(i) );
		}
	}
}
