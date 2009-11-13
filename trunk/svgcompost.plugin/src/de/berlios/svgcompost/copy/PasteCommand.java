package de.berlios.svgcompost.copy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;
import org.w3c.dom.Element;

public class PasteCommand extends Command {

	private HashMap<Element, Element> list = new HashMap<Element, Element>();
	
	private Element parentElement;

	public void setParentElement(Element parentElement) {
		this.parentElement = parentElement;
	}

	@Override
	public boolean canExecute() {
		ArrayList<Element> bList = (ArrayList<Element>) Clipboard.getDefault().getContents();
		if( bList == null || bList.isEmpty() )
			return false;
		Iterator<Element> it = bList.iterator();
		while( it.hasNext() ) {
			Element node = it.next();
			if( isPastable(node) ) {
				list.put(node, null);
			}
		}
		return true;
	}

	@Override
	public void execute() {
		if( !canExecute() )
			return;
		Iterator<Element> it = list.keySet().iterator();
		while( it.hasNext() ) {
			Element el = it.next();
			Element clone = (Element) el.cloneNode(true);
			clone.setAttribute("id", el.getAttribute("id")+"_clone");
			list.put(el, clone);
		}
		redo();
	}

	@Override
	public void redo() {
		Iterator<Element> it = list.values().iterator();
		while( it.hasNext() ) {
			Element node = it.next();
			if( isPastable(node) && canBePastedInto(parentElement) ) {
				parentElement.appendChild(node);
			}
		}
	}
	
	@Override public boolean canUndo() {
		return ! list.isEmpty();
	}
	
	@Override
	public void undo() {
		Iterator<Element> it = list.values().iterator();
		while( it.hasNext() ) {
			Element node = it.next();
			if( isPastable(node) && canBePastedInto(parentElement) ) {
				node.getParentNode().removeChild(node);
			}
		}
	}
	
	public boolean isPastable(Object node) {
		return true;
	}

	public boolean canBePastedInto(Object node) {
		return (node != null && node instanceof Element);
	}
}
