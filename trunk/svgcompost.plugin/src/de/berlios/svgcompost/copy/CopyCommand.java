package de.berlios.svgcompost.copy;

import java.util.ArrayList;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;
import org.w3c.dom.Element;

public class CopyCommand extends Command {

	private ArrayList<Element> list = new ArrayList<Element>();
	
	public boolean addElement(Element node) {
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
		return node != null && node instanceof Element;
	}

}
