package de.berlios.svgcompost.layers;

import java.util.List;

import org.apache.batik.dom.AbstractElement;
import org.apache.batik.util.SVGConstants;
import org.eclipse.gef.commands.Command;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.berlios.svgcompost.part.EditEvent;
import de.berlios.svgcompost.util.ElementTraversalHelper;

public class GroupCommand extends Command {

	private int[] indices;
	private Element parent;
	private Element newGroupElement;
	private List<Element> children;

	public GroupCommand(Element parent, List<Element> children) {
		this.parent = parent;
		this.children = children;
	}

	@Override
	public boolean canExecute() {
		return true;
	}

	@Override
	public void execute() {
		if( canExecute() ) {
			Document doc = parent.getOwnerDocument();
			newGroupElement = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
			indices = new int[children.size()];
			for (int i=0; i<children.size(); i++) {
				Element child = children.get(i);
				indices[i] = ElementTraversalHelper.indexOfNode(parent, child);
				parent.removeChild(child);
				newGroupElement.appendChild(child);
			}
			parent.appendChild(newGroupElement);
		}
		((AbstractElement)parent).dispatchEvent(new EditEvent(this, EditEvent.INSERT, parent, parent));
	}

	@Override public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		parent.removeChild(newGroupElement);
		for (int i=0; i<children.size(); i++) {
			Element child = children.get(i);
			newGroupElement.removeChild(child);
			ElementTraversalHelper.insertNodeAt(parent, child, indices[i]);
		}
		((AbstractElement)parent).dispatchEvent(new EditEvent(this, EditEvent.INSERT, parent, parent));
	}
}
