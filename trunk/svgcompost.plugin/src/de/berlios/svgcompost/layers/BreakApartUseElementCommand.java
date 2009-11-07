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
import org.w3c.dom.svg.SVGUseElement;

import de.berlios.svgcompost.model.SVGNode;
import de.berlios.svgcompost.util.LinkHelper;

public class BreakApartUseElementCommand extends Command {

	private SVGNode node;
	private SVGNode parentNode;
	private SVGNode gNode;
	
	public BreakApartUseElementCommand(SVGNode node) {
		this.node = node;
		this.parentNode = node.getParent();
	}

	@Override
	public boolean canExecute() {
		return node.getElement().getNodeName().equals("use");
	}

	@Override
	public void execute() {
		if( canExecute() ) {
			breakApartUseElement( node );
		}
	}

	public void breakApartUseElement(SVGNode useNode) {
		SVGUseElement useElement = (SVGUseElement) useNode.getElement();
//		Element parentElement = (Element) useElement.getParentNode();
		
		BridgeContext ctx = useNode.getBridgeContext();
		Element refElement = ctx.getReferencedElement(useElement, useElement.getAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "href"));

		SVGOMDocument document = (SVGOMDocument)useElement.getOwnerDocument();
		SVGOMDocument refDocument = (SVGOMDocument)refElement.getOwnerDocument();
		
		SVGGElement gElement = (SVGGElement) document.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
		if( useElement.hasAttribute("transform") )
			gElement.setAttribute("transform", useElement.getAttribute("transform"));
		
		NodeList list = refElement.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node w3cNode = list.item(i);
			Node clone = document.importNode(w3cNode, true, true);
			LinkHelper.refactorLinks( clone, refDocument.getURL(), document.getURL() );
			gElement.appendChild(clone);
		}
		
		gNode = new SVGNode( gElement, parentNode.getBridgeContext() );
//		parentElement.insertBefore(gElement, useElement);
		parentNode.insertBefore( gNode, useNode );
//		parentElement.removeChild(useElement);
		parentNode.removeChild(useNode);
	}
	
	@Override public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		parentNode.insertBefore( node, gNode );
		parentNode.removeChild(gNode);
	}
}
