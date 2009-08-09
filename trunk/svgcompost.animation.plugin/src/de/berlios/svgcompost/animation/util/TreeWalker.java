package de.berlios.svgcompost.animation.util;

import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.RootGraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;

import de.berlios.svgcompost.animation.canvas.CanvasNode;

public class TreeWalker {

	public static RootGraphicsNode buildDynamicGraphicsTree( SVGDocument doc ) {
		UserAgentAdapter agent = new UserAgentAdapter();
		BridgeContext ctx = new BridgeContext( agent );
		ctx.setDynamic( true );
		GVTBuilder builder = new GVTBuilder();
		return (RootGraphicsNode) builder.build( ctx, doc );
	}
	
	public static void traverseGraphicsTree( GraphicsNode node, int level ) {
		String space = "";
		for (int i = 0; i < level; i++)
			space += "  ";
		System.out.println( space+"+ "+node.getClass().getSimpleName()+" has symbol id "+CanvasNode.getCanvasNode( node, null ).getSymbolId()+" and label: "+CanvasNode.getCanvasNode( node, null ).getName() );
		if( node instanceof CompositeGraphicsNode ) {
			CompositeGraphicsNode group = (CompositeGraphicsNode) node;
			List children = group.getChildren();
			for (int i = 0; i < children.size(); i++) {
				GraphicsNode child = (GraphicsNode) children.get(i);
				traverseGraphicsTree( child, level+1 );
			}
		}
	}
	
	public static void traverseCanvasTree( CanvasNode node, int level ) {
		String space = "";
		for (int i = 0; i < level; i++)
			space += "  ";
		System.out.println( space+"+ "+node.getClass().getSimpleName()+" has symbol id "+node.getSymbolId()+" and label: "+node.getName() );
		for (int i = 0; i < node.getSize(); i++) {
			traverseCanvasTree( node.get(i), level+1 );
		}
	}
	
	public static void traverseDocumentTree( SVGElement element, BridgeContext ctx, int level ) {
		String space = "";
		for (int i = 0; i < level; i++)
			space += "  ";
		GraphicsNode node = (GraphicsNode) ctx.getGraphicsNode( element );
		String label = element.getAttributeNS( "http://www.inkscape.org/namespaces/inkscape", "label" );
		System.out.println( space+"+"+element.getNodeName()+" "+label+" has node: "+node );
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = (Node) children.item(i);
			if( child instanceof SVGElement )
				traverseDocumentTree( (SVGElement) child, ctx, level+1 );
		}

	}

}
