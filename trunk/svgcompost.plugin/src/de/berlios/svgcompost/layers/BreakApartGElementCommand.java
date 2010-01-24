package de.berlios.svgcompost.layers;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.AbstractElement;
import org.eclipse.gef.commands.Command;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.berlios.svgcompost.part.EditEvent;
import de.berlios.svgcompost.util.ElementTraversalHelper;

public class BreakApartGElementCommand extends Command {

	private Element node;
	private int index;
	private Element parentNode;
	private AffineTransform transform;
	
	private List<Node> children = new ArrayList<Node>();
	private Map<Node,AffineTransform> transforms = new HashMap<Node,AffineTransform>();
	private BridgeContext ctx;

	public BreakApartGElementCommand(Element node, BridgeContext ctx) {
		this.node = node;
		this.parentNode = (Element) node.getParentNode();
		this.ctx = ctx;
		this.transform = ElementTraversalHelper.getTransform(node); // not necessary?
		if( parentNode != null ) {
			index = ElementTraversalHelper.getChildElements(parentNode).indexOf( node );
		}
	}

	@Override
	public boolean canExecute() {
		return node.getNodeName().equals("g");
	}

	@Override
	public void execute() {
		if( canExecute() ) {
			breakApartGElement( node );
		}
	}

	public void breakApartGElement(Element node) {
		AffineTransform parentTransformInverse = null;
		try {
			parentTransformInverse = ctx.getGraphicsNode(parentNode).getGlobalTransform().createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		int indexCount = index;
		while( node.getChildNodes().getLength() > 0 ) {
			Node childNode = node.getChildNodes().item(0);
			children.add(childNode);
			if( childNode instanceof Element ) {
				Element childElement = (Element) childNode;
				transforms.put(childElement,ElementTraversalHelper.getTransform(childElement));
				AffineTransform transform = ctx.getGraphicsNode(childElement).getGlobalTransform();
				node.removeChild(childElement);
				ElementTraversalHelper.insertNodeAt(parentNode,childElement,indexCount++);
				transform.preConcatenate( parentTransformInverse );
				ElementTraversalHelper.setTransform(childElement,transform,ctx);
			}
			else {
				node.removeChild(childNode);
				ElementTraversalHelper.insertNodeAt(parentNode,childNode,index++);
			}
		}
		parentNode.removeChild(node);
		((AbstractElement)parentNode).dispatchEvent(new EditEvent(this, EditEvent.INSERT, parentNode, parentNode));
	}
	
	@Override public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		for( Node child : children ) {
			child.getParentNode().removeChild(child);
		}
		ElementTraversalHelper.insertNodeAt(parentNode,node,index);
		ElementTraversalHelper.setTransform(node,transform,ctx);
		for (int i = 0; i < children.size(); i++) {
			Node child = children.get(i);
			AffineTransform childTransform = transforms.get(child);
			node.appendChild(child);
			if( child instanceof Element )
				ElementTraversalHelper.setTransform((Element)child,childTransform,ctx);
		}
		// FIXME: Has to be removed and added again to make the change visible
		parentNode.removeChild(node);
		ElementTraversalHelper.insertNodeAt(parentNode,node,index);
		ElementTraversalHelper.setTransform(node, transform, ctx);
		((AbstractElement)parentNode).dispatchEvent(new EditEvent(this, EditEvent.REMOVE, parentNode, parentNode));
	}
}
