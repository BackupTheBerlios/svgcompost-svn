package de.berlios.svgcompost.layers;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import org.eclipse.gef.commands.Command;

import de.berlios.svgcompost.model.SVGNode;

public class BreakApartCommand extends Command {

	SVGNode node;
	SVGNode parentNode;
	AffineTransform transform;
	
	public BreakApartCommand(SVGNode node) {
		this.node = node;
		this.parentNode = node.getParent();
		this.transform = node.getTransform();
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
		//TODO: undo
	}
}
