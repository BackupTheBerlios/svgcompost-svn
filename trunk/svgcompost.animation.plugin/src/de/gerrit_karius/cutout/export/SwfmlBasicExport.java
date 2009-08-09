package de.gerrit_karius.cutout.export;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.batik.dom.GenericComment;
import org.apache.batik.dom.GenericElement;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;

import de.gerrit_karius.cutout.canvas.Canvas;
import de.gerrit_karius.cutout.canvas.CanvasNode;

public class SwfmlBasicExport extends SwfmlShapeExport {
	
	private static Logger log = Logger.getLogger(SwfmlBasicExport.class);

	protected Element header;
	protected Element tags;
	protected Element firstFrame;
	protected int frameCount = 0;
	protected int lastHighestDepth = 0;
	
	protected SwfmlShapeImport shapeImport;
	
	public SwfmlBasicExport( Canvas canvas ) {
		this.canvas = canvas;
		swfIds = new HashMap<String, Integer>();
		elementIds = new ArrayList<String>();
		Element swf = createDocumentWithRoot("swf");
		swf.setAttribute( "version", "7" );
		swf.setAttribute( "compressed", "1" );

		header = addElement( swf, "Header" );
		header.setAttribute( "framerate", ""+Math.floor(1000/timePerFrame) );
		Element size = addElement( header, "size" );
		size.appendChild( rectangleTag( new Rectangle2D.Float( 0, 0, canvas.width, canvas.height ) ) );
		
		tags = addElement( header, "tags" );
	}
	public void setShapeImport( SwfmlShapeImport shapeImport ) {
		this.shapeImport = shapeImport;
	}
	
	protected void captureShapes() {
		for( Iterator<String> shapeIdIterator = swfIds.keySet().iterator(); shapeIdIterator.hasNext(); ) {
//		for( int i = 0; i<elementIds.size(); i++ ) {
			SVGDocument sourceDoc = canvas.getSourceDoc();
			String shapeId = shapeIdIterator.next();
//			String shapeId = elementIds.get(i);
//			System.out.println( "shapeId: "+shapeId );
			if( log.isTraceEnabled() )
				log.trace( "shapeId: "+shapeId );
			Element element = sourceDoc.getElementById( shapeId );
			if( element == null )
				continue;
			captureShape( element );
		}
		shapesAreCaptured = true;
	}

	protected void captureShape( Element shapeElement ) {
			
		SVGElement pathElement = (SVGElement) shapeElement;
		String shapeId = pathElement.getAttribute("id");
		
		if( shapeImport != null ) {
			Element shapeSwfmlElement = shapeImport.getShapeElement( shapeId );
//			Element clone = (Element) shapeSwfmlElement.cloneNode( true );
			tags.insertBefore( cloneWithAttributes( shapeSwfmlElement ), firstFrame );
			return;
		}
		
		Element defineShape = new GenericElement( "DefineShape2", document );
		tags.insertBefore( defineShape, firstFrame );
		defineShape.appendChild( new GenericComment( "shape "+pathElement.getAttribute("id"), document ) );
		defineShape.setAttribute( "objectID", ""+getSwfId( shapeId ) );
		capturePath( pathElement, defineShape );
	}
	
	/* (non-Javadoc)
	 * @see de.gerrit_karius.cutout.canvas.Export#captureFrame()
	 */
	public void captureFrame() {
//		if( frameCount > 0 ) // only export one single frame
//			return;
		int highestDepth = captureNode( canvas.getRoot().getGraphicsNode(), 1 );

		log.debug("highestDepth: "+highestDepth);
		for( int i=highestDepth; i<lastHighestDepth; i++ ) {
			Element removeObject2 = new GenericElement( "RemoveObject2", document );
			tags.appendChild( removeObject2 );
			removeObject2.setAttribute( "depth", ""+i );
			log.debug("Remove object at depth "+i);
		}
		tags.appendChild( new GenericComment("frame "+frameCount+" with "+(highestDepth-1)+" nodes",document) );
		addFrame();
		lastHighestDepth = highestDepth;
	}
	
	protected int captureNode( GraphicsNode gNode, int currentDepth ) {
		if( ! gNode.isVisible() )
			return currentDepth;
		if( gNode instanceof CompositeGraphicsNode ) {
			CompositeGraphicsNode group = (CompositeGraphicsNode) gNode;
			for (int i = 0; i < group.size(); i++) {
				currentDepth = captureNode( (GraphicsNode) group.get(i), currentDepth );
			}
			return currentDepth;
		}
		if( gNode instanceof ShapeNode ) {
			ShapeNode shapeNode = (ShapeNode) gNode;
			String shapeId = CanvasNode.getCanvasNode( shapeNode, canvas ).getSymbolId();
			
			int swfId;
			if( shapeImport != null ) {
				swfId = shapeImport.getShapeId( shapeId );
				swfIds.put( shapeId, swfId );
			}
			else swfId = getSwfId( shapeId );

			Element placeObject2 = new GenericElement( "PlaceObject2", document );
			if( firstFrame == null )
				firstFrame = placeObject2;

			tags.appendChild( placeObject2 );
			placeObject2.setAttribute( "replace", currentDepth < lastHighestDepth ? "1" : "0" );
			placeObject2.setAttribute( "depth", ""+currentDepth );
			placeObject2.setAttribute( "objectID", ""+swfId );
			Element transform = new GenericElement( "transform", document );
			placeObject2.appendChild( transform );
			
//			String label = Canvas.getName( shapeNode );
			AffineTransform globTrafo = shapeNode.getGlobalTransform();
//			System.out.println( "shape "+label+" has trafo: "+Canvas.getTransform( shapeNode ) );
//			System.out.println( "shape "+label+" has globTrafo: "+globTrafo );
			Element matrix = matrix(globTrafo);//new GenericElement( "Transform", document );
			transform.appendChild( matrix );
			
			log.debug("increase currentDepth for "+shapeId);
			currentDepth++;
		}
		return currentDepth;
	}
	
	public Element matrix( AffineTransform trafo ) {
		Element matrix = new GenericElement( "Transform", document );
		matrix.setAttribute( "scaleX", ""+round(trafo.getScaleX()) );
		matrix.setAttribute( "scaleY", ""+round(trafo.getScaleY()) );
		// IMPORTANT: swfml skewX is java shearY and vice versa!!!
		matrix.setAttribute( "skewX", ""+round(trafo.getShearY()) );
		matrix.setAttribute( "skewY", ""+round(trafo.getShearX()) );
		matrix.setAttribute( "transX", ""+coord(trafo.getTranslateX()) );
		matrix.setAttribute( "transY", ""+coord(trafo.getTranslateY()) );
		return matrix;
	}
	
	/* (non-Javadoc)
	 * @see de.gerrit_karius.cutout.canvas.Export#end()
	 */
	public void end() {
		tags.appendChild( new GenericComment("end frame",document) );
		header.setAttribute( "frames", ""+frameCount );
		addElement( tags, "End" );
		if( ! shapesAreCaptured )
			captureShapes();
	}
	
	protected void addFrame() {
		addElement( tags, "ShowFrame" );
		frameCount++;
		return;
	}
	
}
