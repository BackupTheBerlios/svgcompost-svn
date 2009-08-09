package de.berlios.svgcompost.animation.canvas;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.RootGraphicsNode;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGRect;


public class Canvas {
	
	private static Logger log = Logger.getLogger(Canvas.class);
	
	public int width = 400;
	public int height = 300;
	
	protected static final double[] matrixDummy = new double[6];
	protected static final Point2D.Float pointDummy = new Point2D.Float();
	
	public static final LabelKey KEY_SYMBOL_ID = new LabelKey(1024);
	public static final LabelKey KEY_LABEL = new LabelKey(1025);
	
	public static String inkscapeNs = "http://www.inkscape.org/namespaces/inkscape";
	public static String xlinkNs = "http://www.w3.org/1999/xlink";
	public static String inkscapePrefix = "inkscape";
	
	private SVGDocument sourceDoc;
	private BridgeContext sourceCtx;
	private GVTBuilder sourceBld;

//	private SVGDocument canvasDoc;
//	private BridgeContext canvasCtx;

	RootGraphicsNode rootNode;
	CanvasGraphicsNode canvasNode;
	
	String inkscapeNamespaceURI;
	String inkspaceLabel;
	
	protected Library library;
	
	// TODO: GVTBuilder, BridgeContext, Bridge, getBridgeContext
	
	public Canvas( BridgeContext sourceCtx ) {
		this.sourceCtx = sourceCtx;
		
		if( sourceCtx != null ) {
			sourceDoc = (SVGDocument) sourceCtx.getDocument();
			sourceBld = sourceCtx.getGVTBuilder();
			
			Element svgEl = sourceDoc.getRootElement();
			UnitProcessor.Context upCtx = UnitProcessor.createContext( sourceCtx, svgEl );
			width = (int) UnitProcessor.svgHorizontalCoordinateToObjectBoundingBox( svgEl.getAttribute( "width" ), "width", upCtx );
			height = (int) UnitProcessor.svgVerticalCoordinateToObjectBoundingBox( svgEl.getAttribute( "height" ), "height", upCtx );
		}
		
		canvasNode = new CanvasGraphicsNode(); // (CanvasGraphicsNode) rootNode.get( 0 );
		canvasNode.setRenderingHint( KEY_LABEL, "canvas" );

		SVGOMElement rootEl = (SVGOMElement) sourceDoc.getRootElement();
		log.debug("sourceDoc.getRootElement() = "+rootEl);
		log.debug("rootEl.getSVGContext() = "+rootEl.getSVGContext());
		SVGRect bounds = sourceDoc.getRootElement().getBBox();
		log.debug("bounds = "+bounds);
		width = (int) bounds.getWidth();
		height = (int) bounds.getWidth();
	}
	
	public SVGDocument getSourceDoc() {
		return sourceDoc;
	}
	
	public BridgeContext getSourceCtx() {
		return sourceCtx;
	}
	
	public GVTBuilder getSourceBld() {
		return sourceBld;
	}

	public CanvasNode getRoot() {
		return CanvasNode.getCanvasNode( canvasNode, this );
	}
	
	public Library getLibrary() {
		if( library == null )
			log.error( "Library is requested, but none was attached with addLibrary(BridgeContext)." );
		return library;
	}

	public void addLibrary( BridgeContext sourceCtx ) {
		library = new Library( new Canvas( sourceCtx ) );
	}
	
//	public CanvasGraphicsNode getCanvas() {
//		return canvasNode;
//	}
	
	public static Iterator getChildIterator( GraphicsNode parent ) {
		if( parent == null || ! (parent instanceof CompositeGraphicsNode) )
			return null;
		CompositeGraphicsNode group = (CompositeGraphicsNode) parent;
		return group.iterator();
	}
	
	public static GraphicsNode getChild( GraphicsNode parent, String name ) {
		//TODO: use RenderingHints, try a custom hint with custom key.
		if( parent == null || ! (parent instanceof CompositeGraphicsNode) )
			return null;
		CompositeGraphicsNode group = (CompositeGraphicsNode) parent;
		List children = group.getChildren();
		for (int i = 0; i < children.size(); i++) {
			GraphicsNode child = (GraphicsNode) children.get(i);
			RenderingHints hints = child.getRenderingHints();
			if( hints == null || hints.get( KEY_LABEL ) == null )
				continue;
			if( hints.get( KEY_LABEL ).equals( name ) ) {
				return child;
			}
		}
		return null;
	}
	
	public static CompositeGraphicsNode getParent( GraphicsNode gNode ) {
		if( gNode == null ) {
			return null;
		}
		return gNode.getParent();
	}
	
	public static void removeNode( GraphicsNode gNode ) {
		if( gNode == null || gNode.getParent() == null ) {
			return;
		}
		CompositeGraphicsNode group = (CompositeGraphicsNode) gNode.getParent();
		group.remove( gNode );
	}
	
	public static CanvasNode groupNode( CanvasNode cNode, String name ) {
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
	
	public CanvasNode symbolNode( CanvasNode cNode, String symbolId, String name ) {
		GraphicsNode parent = cNode.getGraphicsNode();
		if( parent == null ) {
			return null;
		}
		if( parent instanceof CompositeGraphicsNode ) {
			CompositeGraphicsNode group = (CompositeGraphicsNode) parent;
			Element element = sourceDoc.getElementById( symbolId );
			if( element == null ) {
				log.error( "Couldn't find element for ID: "+symbolId );
				return null;
			}
			if( element.getAttribute("display").equals("none") ) {
				log.warn("Display was set to 'none' for symbol: "+symbolId+". Set to 'inline'.");
				element.setAttribute("display","inline");
			}
			GraphicsNode gNode = sourceBld.build( sourceCtx, element );
			if( gNode == null ) {
				log.warn( "Null node built for symbol: "+symbolId+". (Empty or invisible element?) Using empty group node instead." );
				return groupNode(cNode, name);
			}
			group.add( gNode );
			gNode.setRenderingHint( KEY_SYMBOL_ID, symbolId );
			gNode.setRenderingHint( KEY_LABEL, name );
			setChildLabels( gNode, element );
			return CanvasNode.getCanvasNode( gNode, this );
		}
		else
			log.error("Cannot add child to non-group node "+cNode.getName());
		return null;
	}
	
	protected void setChildLabels( GraphicsNode parent, Element element ) {
		if( ! (parent instanceof CompositeGraphicsNode) )
			return;
		List childGNodes = ((CompositeGraphicsNode)parent).getChildren();
		NodeList childNodes = element.getChildNodes();
		int j = 0;
		for (int i = 0; i < childGNodes.size(); i++) {
			GraphicsNode gNode = (GraphicsNode) childGNodes.get(i);
			Node childNode = null;
			boolean isGraphicalSvgElement = false;
			String label = null;
			String symbolId = null;
			// Search for an element that matches the GraphicsNode.
			while( ! isGraphicalSvgElement && j < childNodes.getLength() ) {
				childNode = childNodes.item(j);
				j++;
				String type = childNode.getNodeName();
				if( childNode instanceof Element ) {
					label = ((Element)childNode).getAttributeNS( inkscapeNs, "label" );
					symbolId = ((Element)childNode).getAttribute( "id" );
				}
				if( type.endsWith("use")  ) {
					String href = ((Element)childNode).getAttributeNS( xlinkNs, "href" ).replaceAll( "#", "" );
					childNode = sourceDoc.getElementById( href );
					type = childNode.getNodeName();
					symbolId = href;
					gNode = cutUseNode( gNode );
				}
				isGraphicalSvgElement = (type.endsWith("g") || type.endsWith("path") || type.endsWith("rect") || type.endsWith("symbol"));
			}
			if( isGraphicalSvgElement ) {
				if( label != null )
					gNode.setRenderingHint( KEY_LABEL, label );
				if( symbolId != null ) 
					gNode.setRenderingHint( KEY_SYMBOL_ID, symbolId );
				setChildLabels( gNode, (Element) childNode );
			}
			else if( ! childNode.getNodeName().endsWith("use") )
				log.debug( "Found non-graphical Svg element: "+childNode.getNodeName() );
		}
	}
	
	protected GraphicsNode cutUseNode( GraphicsNode gNode ) {
		if( gNode == null || ! (gNode instanceof CompositeGraphicsNode) )
			return null;
		CompositeGraphicsNode useNode = (CompositeGraphicsNode) gNode;
		GraphicsNode child = (GraphicsNode)useNode.get(0);
		CompositeGraphicsNode parent = useNode.getParent();
		AffineTransform transform = getTransform( child );
		transform.concatenate( useNode.getTransform() );
		child.setTransform( transform );
		useNode.remove(0);
		parent.add( parent.indexOf( useNode ), child );
		parent.remove( useNode );
		return child;
	}
	
	public static AffineTransform getTransform( GraphicsNode node ) {
		if( node == null ) {
			return null;
		}
		if( node.getTransform() == null ) {
			return new AffineTransform();
		}
//		else if( ! node.getTransform().isIdentity() )
//			System.out.println( "has transform: "+getName( node ) );
		return (AffineTransform) node.getTransform().clone();
	}
	

}