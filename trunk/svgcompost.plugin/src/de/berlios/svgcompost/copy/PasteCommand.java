package de.berlios.svgcompost.copy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;
import org.w3c.dom.Element;

import de.berlios.svgcompost.model.SVGNode;

public class PasteCommand extends Command {

	private HashMap<SVGNode, SVGNode> list = new HashMap<SVGNode, SVGNode>();
	
	private SVGNode parentElement;

	public void setParentElement(SVGNode parentElement) {
		this.parentElement = parentElement;
	}

	@Override
	public boolean canExecute() {
		ArrayList<SVGNode> bList = (ArrayList<SVGNode>) Clipboard.getDefault().getContents();
		if( bList == null || bList.isEmpty() )
			return false;
		Iterator<SVGNode> it = bList.iterator();
		while( it.hasNext() ) {
			SVGNode node = it.next();
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
		Iterator<SVGNode> it = list.keySet().iterator();
		while( it.hasNext() ) {
			SVGNode node = it.next();
			Element el = node.getElement();
			Element clone = (Element) el.cloneNode(true);
			clone.setAttribute("id", el.getAttribute("id")+"_clone");
			SVGNode cloneNode = new SVGNode(clone, node.getBridgeContext());
			cloneNode.setParent(node.getParent());
			list.put(node, cloneNode);
		}
		redo();
	}

	@Override
	public void redo() {
		Iterator<SVGNode> it = list.values().iterator();
		while( it.hasNext() ) {
			SVGNode node = it.next();
			if( isPastable(node) && canBePastedInto(parentElement) ) {
				parentElement.addChild(node);
			}
		}
	}
	
	@Override public boolean canUndo() {
		return ! list.isEmpty();
	}
	
	@Override
	public void undo() {
		Iterator<SVGNode> it = list.values().iterator();
		while( it.hasNext() ) {
			SVGNode node = it.next();
			if( isPastable(node) && canBePastedInto(parentElement) ) {
				node.getParent().removeChild(node);
			}
		}
	}
	
	public boolean isPastable(Object node) {
		return true;
	}

	public boolean canBePastedInto(Object node) {
		return (node != null && node instanceof SVGNode);
	}
}
