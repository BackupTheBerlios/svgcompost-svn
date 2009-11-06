package de.berlios.svgcompost.layers;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;

import de.berlios.svgcompost.model.SVGNode;

public class BreakApartGElementCommand extends Command {

	private SVGNode node;
	private int index;
	private SVGNode parentNode;
	private AffineTransform transform;
	
	private List<SVGNode> children = new ArrayList<SVGNode>();
	private List<AffineTransform> transforms = new ArrayList<AffineTransform>();

	public BreakApartGElementCommand(SVGNode node) {
		this.node = node;
		this.parentNode = node.getParent();
		this.transform = node.getTransform(); // not necessary?
		if( parentNode != null ) {
			index = parentNode.getChildElements().indexOf( node );
		}
	}

	@Override
	public boolean canExecute() {
		return node.getElement().getNodeName().equals("g");
	}

	@Override
	public void execute() {
		if( canExecute() ) {
			breakApartGElement( node );
		}
	}

	public void breakApartGElement(SVGNode node) {
		SVGNode parentNode = node.getParent();
		AffineTransform parentTransformInverse = null;
		try {
			parentTransformInverse = parentNode.getGraphicsNode().getGlobalTransform().createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		while( node.getChildElements().size() > 0 ) {
			SVGNode childNode = node.getChildElements().get(0);
			children.add(childNode);
			transforms.add(childNode.getTransform());
			AffineTransform transform = childNode.getGraphicsNode().getGlobalTransform();
			node.removeChild(childNode);
			parentNode.addChild(childNode);
			transform.preConcatenate( parentTransformInverse );
			childNode.setTransform(transform);
		}
		parentNode.removeChild(node);
	}
	
	@Override public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		for( SVGNode child : children ) {
			child.getParent().removeChild(child);
		}
		parentNode.addChild(index,node);
		node.setTransform(transform);
		for (int i = 0; i < children.size(); i++) {
			SVGNode child = children.get(i);
			AffineTransform childTransform = transforms.get(i);
			node.addChild(child);
			child.setTransform(childTransform);
		}
		// FIXME: Has to be removed and added again to make the change visible
		parentNode.removeChild(node);
		parentNode.addChild(index,node);
	}
}
