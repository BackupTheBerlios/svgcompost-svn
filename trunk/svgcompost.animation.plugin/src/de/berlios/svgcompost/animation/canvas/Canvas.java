package de.berlios.svgcompost.animation.canvas;

import java.awt.geom.AffineTransform;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;


/**
 * The Canvas is a screen to draw the animations on. It aggregates a tree of CanvasNode objects,
 * together with the source SVGDocument and its BridgeContext to retrieve definitions.
 * It provides some utility functions to insert new nodes into the tree. 
 * @author Gerrit Karius
 *
 */
public class Canvas {
	
	public static final LabelKey KEY_SYMBOL_ID = new LabelKey(1024);
	public static final LabelKey KEY_LABEL = new LabelKey(1025);
	public static final LabelKey KEY_WRAPPER = new LabelKey(1026);
	public static final LabelKey KEY_SRC_ELEMENT = new LabelKey(1027);
	
	public static String inkscapeNs = "http://www.inkscape.org/namespaces/inkscape";
	public static String xlinkNs = "http://www.w3.org/1999/xlink";
	public static String inkscapePrefix = "inkscape";
	
	private SVGDocument sourceDoc;
	private BridgeContext sourceCtx;
	private GVTBuilder sourceBld;
	
	private CanvasGraphicsNode canvasNode;
	
	public Canvas( BridgeContext sourceCtx ) {
		this.sourceCtx = sourceCtx;
		
		if( sourceCtx != null ) {
			sourceDoc = (SVGDocument) sourceCtx.getDocument();
			sourceBld = sourceCtx.getGVTBuilder();
			
			Element svgEl = sourceDoc.getRootElement();
			UnitProcessor.Context upCtx = UnitProcessor.createContext( sourceCtx, svgEl );
//			width = (int) UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox( svgEl.getAttribute( "width" ), "width", upCtx );
//			height = (int) UnitProcessor.svgVerticalCoordinateToObjectBoundingBox( svgEl.getAttribute( "height" ), "height", upCtx );
		}
		
		canvasNode = new CanvasGraphicsNode(); // (CanvasGraphicsNode) rootNode.get( 0 );
		canvasNode.setRenderingHint( KEY_LABEL, "canvas" );

//		SVGRect bounds = sourceDoc.getRootElement().getBBox();
//		width = (int) bounds.getWidth();
//		height = (int) bounds.getWidth();
	}
	
	public SVGDocument getSourceDoc() {
		return sourceDoc;
	}
	
	public CanvasNode getRoot() {
		return CanvasNode.getCanvasNode( canvasNode, this );
	}
	
	public Element getElement(CanvasNode node) {
		return sourceCtx.getElement( node.getGraphicsNode() );
	}

//	public static GraphicsNode getChild( GraphicsNode parent, String name ) {
//		if( parent == null || ! (parent instanceof CompositeGraphicsNode) )
//			return null;
//		CompositeGraphicsNode group = (CompositeGraphicsNode) parent;
//		List<GraphicsNode> children = group.getChildren();
//		for (int i = 0; i < children.size(); i++) {
//			GraphicsNode child = children.get(i);
//			RenderingHints hints = child.getRenderingHints();
//			if( hints == null || hints.get( KEY_LABEL ) == null )
//				continue;
//			if( hints.get( KEY_LABEL ).equals( name ) ) {
//				return child;
//			}
//		}
//		return null;
//	}
	
//	public static CompositeGraphicsNode getParent( GraphicsNode gNode ) {
//		if( gNode == null ) {
//			return null;
//		}
//		return gNode.getParent();
//	}
	
//	public static void removeNode( GraphicsNode gNode ) {
//		if( gNode == null || gNode.getParent() == null ) {
//			return;
//		}
//		CompositeGraphicsNode group = (CompositeGraphicsNode) gNode.getParent();
//		group.remove( gNode );
//	}
	
	public static CanvasNode insertGroupNode( CanvasNode cNode, String name ) {
		GraphicsNode parent = cNode.getGraphicsNode();
		if( parent == null ) {
			return null;
		}
		if( parent instanceof CompositeGraphicsNode ) {
			CompositeGraphicsNode group = (CompositeGraphicsNode) parent;
			CompositeGraphicsNode gNode = new CompositeGraphicsNode();
			gNode.setRenderingHint( KEY_LABEL, name );
			group.add( gNode );
			return CanvasNode.getCanvasNode( gNode, cNode.getCanvas() );
		}
		return null;
	}
	
	public CanvasNode renderDocument( SVGDocument document ) {
		CanvasNode documentNode = insertSymbolNode( getRoot(), document.getRootElement(), document.getRootElement().getAttribute("id") );
		setChildLabels(documentNode.getGraphicsNode(), document.getRootElement());
		return documentNode;
	}
	
	public CanvasNode insertSymbolNode( CanvasNode cNode, String symbolId, String name ) {
		Element element = resolve(symbolId, cNode.getSourceElement());
		if( element == null ) {
			return null;
		}
		return insertSymbolNode(cNode, element, name);
	}
	
	private CanvasNode insertSymbolNode( CanvasNode cNode, Element element, String name ) {
		GraphicsNode parent = cNode.getGraphicsNode();
		if( parent == null ) {
			return null;
		}
		String symbolId = element.getAttribute("id");
		if( parent instanceof CompositeGraphicsNode ) {
			CompositeGraphicsNode group = (CompositeGraphicsNode) parent;
			if( element.getAttribute("display").equals("none") ) {
				element.setAttribute("display","inline");
			}
			GraphicsNode gNode = sourceBld.build( sourceCtx, element );
			if( gNode == null ) {
				return insertGroupNode(cNode, name);
			}
			group.add( gNode );
			gNode.setRenderingHint( KEY_SRC_ELEMENT, element );
			gNode.setRenderingHint( KEY_SYMBOL_ID, symbolId );
			gNode.setRenderingHint( KEY_LABEL, name );
			setChildLabels( gNode, element );
			return CanvasNode.getCanvasNode( gNode, this );
		}

		return null;
	}
	
	private void setChildLabels( GraphicsNode parent, Element element ) {
		if( ! (parent instanceof CompositeGraphicsNode) )
			return;
		List<GraphicsNode> childGNodes = ((CompositeGraphicsNode)parent).getChildren();
		for (int i = 0; i < childGNodes.size(); i++) {
			GraphicsNode childNode = childGNodes.get(i);
			Element childElement = sourceCtx.getElement(childNode);
			String type = childElement.getNodeName();
			// Label is taken from the original element.
			String label = childElement.getAttributeNS( inkscapeNs, "label" );
			if( type.endsWith("use")  ) {
				Element fromElement = childElement;
				String href = childElement.getAttributeNS( xlinkNs, "href" );
				childElement = resolve(href, childElement);
				if( childElement == null )
					System.out.println("\nCanvas.setChildLabels(): could not resolve "+href+" from "+((SVGOMDocument)fromElement.getOwnerDocument()).getURL());
				type = childElement.getNodeName();
				childNode = cutUseNode( childNode );
			}
			// ID is taken from original element, or from referenced element for use elements.
			String symbolId = childElement.getAttribute( "id" );
			childNode.setRenderingHint( KEY_SRC_ELEMENT, childElement );
			// FIXME: Sometimes, the label is already set. Need to improve setup.
			if( label != null && childNode.getRenderingHints().get(KEY_LABEL) == null )
				childNode.setRenderingHint( KEY_LABEL, label );
			if( symbolId != null ) 
				childNode.setRenderingHint( KEY_SYMBOL_ID, symbolId );
			setChildLabels( childNode, childElement );

		}
	}
	
	private GraphicsNode cutUseNode( GraphicsNode gNode ) {
		if( gNode == null || ! (gNode instanceof CompositeGraphicsNode) )
			return null;
		CompositeGraphicsNode useNode = (CompositeGraphicsNode) gNode;
		GraphicsNode child = (GraphicsNode)useNode.get(0);
		CompositeGraphicsNode parent = useNode.getParent();
		AffineTransform transform = child.getTransform();
		if( transform == null )
			transform = useNode.getTransform();
		else
			transform.concatenate( useNode.getTransform() );
		child.setTransform( transform );
		useNode.remove(0);
		parent.add( parent.indexOf( useNode ), child );
		parent.remove( useNode );
		return child;
	}
	
//	public static AffineTransform getTransform( GraphicsNode node ) {
//		if( node == null ) {
//			return null;
//		}
//		if( node.getTransform() == null ) {
//			return new AffineTransform();
//		}
//		return (AffineTransform) node.getTransform().clone();
//	}
	
	private Element resolve( String reference, Element fromElement ) {
		int index = reference.indexOf("#");
		if( fromElement == null )
			fromElement = sourceDoc.getDocumentElement();
		if( index == -1 )
			return fromElement.getOwnerDocument().getElementById(reference);
		if( index == 0 )
	    	return fromElement.getOwnerDocument().getElementById(reference.substring(1));
		Element element = null;
		try {
			element = sourceCtx.getReferencedElement(fromElement, reference);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if( element == null )
			System.out.println( "Could not resolve ID "+reference );
		return element;
	}

}
