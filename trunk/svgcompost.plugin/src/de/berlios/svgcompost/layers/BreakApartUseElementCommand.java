package de.berlios.svgcompost.layers;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.util.SVGConstants;
import org.eclipse.gef.commands.Command;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGGElement;

import de.berlios.svgcompost.util.LinkHelper;

public class BreakApartUseElementCommand extends Command {

	private Element node;
	private Element parentNode;
	private Element gElement;
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
		
		Element refElement = ctx.getReferencedElement(useElement, useElement.getAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "href"));

		SVGOMDocument document = (SVGOMDocument)useElement.getOwnerDocument();
		SVGOMDocument refDocument = (SVGOMDocument)refElement.getOwnerDocument();
		
		gElement = (SVGGElement) document.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
		if( useElement.hasAttribute("transform") )
			gElement.setAttribute("transform", useElement.getAttribute("transform"));
		
		NodeList list = refElement.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node w3cNode = list.item(i);
			Node clone = document.importNode(w3cNode, true, true);
			LinkHelper.refactorLinks( clone, refDocument.getURL(), document.getURL() );
			gElement.appendChild(clone);
		}
		
//		gNode = new Element( gElement, parentNode.getBridgeContext() );
//		parentElement.insertBefore(gElement, useElement);
		parentNode.insertBefore( gElement, useElement );
//		parentElement.removeChild(useElement);
		parentNode.removeChild(useElement);
	}
	
	@Override public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		parentNode.insertBefore( node, gElement );
		parentNode.removeChild(gElement);
	}
}
