package de.berlios.svgcompost.copy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;
import org.w3c.dom.Element;

import de.berlios.svgcompost.model.EditableElement;

public class PasteCommand extends Command {

	private HashMap<EditableElement, EditableElement> list = new HashMap<EditableElement, EditableElement>();

	@Override
	public boolean canExecute() {
		ArrayList<EditableElement> bList = (ArrayList<EditableElement>) Clipboard.getDefault().getContents();
		if( bList == null || bList.isEmpty() )
			return false;
		Iterator<EditableElement> it = bList.iterator();
		while( it.hasNext() ) {
			EditableElement node = it.next();
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
		Iterator<EditableElement> it = list.keySet().iterator();
		while( it.hasNext() ) {
			EditableElement node = it.next();
			Element el = node.getElement();
			Element clone = (Element) el.cloneNode(true);
			clone.setAttribute("id", el.getAttribute("id")+"_clone");
			EditableElement cloneNode = new EditableElement(clone, node.getBridgeContext());
			cloneNode.setParent(node.getParent());
			list.put(node, cloneNode);
		}
		redo();
	}

	@Override
	public void redo() {
		Iterator<EditableElement> it = list.values().iterator();
		while( it.hasNext() ) {
			EditableElement node = it.next();
			if( isPastable(node) ) {
				node.getParent().addChild(node);
			}
		}
	}
	
	@Override public boolean canUndo() {
		return ! list.isEmpty();
	}
	
	@Override
	public void undo() {
		Iterator<EditableElement> it = list.values().iterator();
		while( it.hasNext() ) {
			EditableElement node = it.next();
			if( isPastable(node) ) {
				node.getParent().removeChild(node);
			}
		}
	}
	
	public boolean isPastable(EditableElement node) {
		return true;
	}
}
