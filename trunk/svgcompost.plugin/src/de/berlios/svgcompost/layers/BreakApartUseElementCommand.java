package de.berlios.svgcompost.layers;

import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.SVGConstants;
import org.eclipse.gef.commands.Command;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGGElement;

import de.berlios.svgcompost.part.EditEvent;
import de.berlios.svgcompost.util.ElementTraversalHelper;
import de.berlios.svgcompost.util.LinkHelper;

public class BreakApartUseElementCommand extends Command {

	private Element node;
	private Element parentNode;
	private Element newElement;
	private BridgeContext ctx;
	
	public BreakApartUseElementCommand(Element node, BridgeContext ctx) {
		this.node = node;
		this.ctx = ctx;
		this.parentNode = (Element) node.getParentNode();
	}

	@Override
	public boolean canExecute() {
		return node.getNodeName().equals("use");
	}

	@Override
	public void execute() {
		if( canExecute() ) {
			breakApartUseElement( node );
		}
	}

	public void breakApartUseElement(Element useElement) {
//		SVGUseElement useElement = (SVGUseElement) useNode.getElement();
//		Element parentElement = (Element) useElement.getParentNode();
		
		GraphicsNode useNode = ctx.getGraphicsNode(useElement);
		GraphicsNode useChild = (GraphicsNode) ((CompositeGraphicsNode)useNode).getChildren().get(0);
		AffineTransform transform = useChild.getGlobalTransform();
		
		Element refElement = ctx.getReferencedElement(useElement, useElement.getAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "href"));

		SVGOMDocument document = (SVGOMDocument)useElement.getOwnerDocument();
		SVGOMDocument refDocument = (SVGOMDocument)refElement.getOwnerDocument();
		
//		newElement = (SVGGElement) document.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
		
		newElement = (Element) document.importNode(refElement, true, true);
		LinkHelper.refactorLinks( newElement, refDocument.getURL(), document.getURL() );
		
//		if( useElement.hasAttribute("transform") )
		ElementTraversalHelper.setGlobalTransform(newElement, transform, ctx);
//		newElement.setAttribute("transform", useElement.getAttribute("transform"));
		
//		NodeList list = refElement.getChildNodes();
//		for (int i = 0; i < list.getLength(); i++) {
//			Node w3cNode = list.item(i);
//			Node clone = document.importNode(w3cNode, true, true);
//			LinkHelper.refactorLinks( clone, refDocument.getURL(), document.getURL() );
//			newElement.appendChild(clone);
//		}
		
//		gNode = new Element( newElement, parentNode.getBridgeContext() );
//		parentElement.insertBefore(newElement, useElement);
		parentNode.insertBefore( newElement, useElement );
//		parentElement.removeChild(useElement);
		parentNode.removeChild(useElement);
		((AbstractElement)parentNode).dispatchEvent(new EditEvent(this, EditEvent.INSERT, parentNode, parentNode));
	}
	
	@Override public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		parentNode.insertBefore( node, newElement );
		parentNode.removeChild(newElement);
		((AbstractElement)parentNode).dispatchEvent(new EditEvent(this, EditEvent.REMOVE, parentNode, parentNode));
	}
}
