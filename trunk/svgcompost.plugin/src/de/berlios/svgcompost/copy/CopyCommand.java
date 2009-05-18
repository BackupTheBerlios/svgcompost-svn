package de.berlios.svgcompost.copy;

import java.util.ArrayList;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import de.berlios.svgcompost.model.SVGNode;

public class CopyCommand extends Command {

	private ArrayList<SVGNode> list = new ArrayList<SVGNode>();
	
	public boolean addElement(SVGNode node) {
		if( !list.contains(node) ) {
			return list.add(node);
		}
		return false;
	}
	
	@Override
	public boolean canExecute() {
		if( list == null || list.isEmpty() )
			return false;
		return true;
	}

	@Override
	public boolean canUndo() {
		return false;
	}

	@Override
	public void execute() {
		if( canExecute() )
			Clipboard.getDefault().setContents(list);
	}

	public boolean isCopyable(Object node) {
		return node != null && node instanceof SVGNode;
	}

}
