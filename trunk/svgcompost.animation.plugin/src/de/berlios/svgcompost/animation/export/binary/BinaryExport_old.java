package de.berlios.svgcompost.animation.export.binary;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import de.berlios.svgcompost.animation.canvas.Canvas;
import de.berlios.svgcompost.animation.canvas.CanvasNode;
import de.berlios.svgcompost.animation.canvas.PathConverter;
import de.berlios.svgcompost.animation.export.Export;
import de.berlios.svgcompost.animation.export.SwfmlShapeImport;
import de.berlios.svgcompost.animation.util.PainterAccess;

public class BinaryExport_old implements Export {
	
	private static Logger log = Logger.getLogger(BinaryExport_old.class);

	protected SwfWriter writer;
	protected SwfFile swfFile;
	protected Canvas canvas;
	protected SwfData firstFrame;
	protected SwfStruct resources;
	protected HashMap<String, Integer> swfIds;
	protected ArrayList<String> elementIds;
	protected int lastHighestDepth = 0;
	protected SwfmlShapeImport shapeImport;
	protected int shapeIdCount = 0;
	protected boolean shapesAreCaptured = false;
	
	protected int framesPerSecond = 12;
	
	public BinaryExport_old( Canvas canvas ) {
		this.canvas = canvas;
		swfIds = new HashMap<String, Integer>();
		elementIds = new ArrayList<String>();
		writer = new SwfWriter();
		swfFile = new SwfFile( writer, 5, new Rectangle2D.Float( 0, 0, canvas.width, canvas.height ), framesPerSecond );
		swfFile.addChild( new SwfSetBackgroundColorTag( writer, Color.white ) );
		resources = new SwfStruct( writer );
		swfFile.addChild( resources );
	}

	public void writeOutput(String fileName) {
		writer.convertData( swfFile );
		log.info("Write output file \""+fileName+"\".");
		writer.writeFile( new File(fileName) );
	}

	protected void captureShapes() {
		SVGDocument sourceDoc = canvas.getSourceDoc();
		for( Iterator<String> shapeIdIterator = swfIds.keySet().iterator(); shapeIdIterator.hasNext(); ) {
			String shapeId = shapeIdIterator.next();
			Element element = sourceDoc.getElementById( shapeId );
			if( element == null )
				continue;
			captureShape( element );
		}
		shapesAreCaptured = true;
	}

	protected void captureShape( Element shapeElement ) {
			
		String shapeId = shapeElement.getAttribute("id");
		int swfId = swfIds.get( shapeId );
		ShapeNode shapeNode = (ShapeNode) canvas.getSourceBld().build( canvas.getSourceCtx(), shapeElement );
		ExtendedGeneralPath path = PathConverter.convertPath( new ExtendedGeneralPath( shapeNode.getShape() ) );
		ShapePainter painter = shapeNode.getShapePainter();
		Paint paint = PainterAccess.getPaint( painter );
		Stroke stroke = PainterAccess.getStroke( painter );
		
		resources.addChild( new SwfDefineShape3Tag( writer, swfId, path, paint, stroke ) );
	}
	
	public void captureFrame() {
		int highestDepth = captureNode( canvas.getRoot().getGraphicsNode(), 1 );

		for( int i=highestDepth; i<lastHighestDepth; i++ ) {
			SwfData removeObject2 = new SwfRemoveObject2Tag( writer, i );
			swfFile.addChild( removeObject2 );
		}
		swfFile.addFrame();
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
		}
		else if( gNode instanceof ShapeNode ) {
			ShapeNode shapeNode = (ShapeNode) gNode;
			String shapeId = CanvasNode.getCanvasNode( shapeNode, canvas ).getSymbolId();
			
			int swfId;
			if( shapeImport != null ) {
				swfId = shapeImport.getShapeId( shapeId );
				swfIds.put( shapeId, swfId );
			}
			else swfId = getSwfId( shapeId );

			boolean replace = currentDepth < lastHighestDepth;
			AffineTransform globTrafo = shapeNode.getGlobalTransform();
//			log.debug("globalTransform: "+globTrafo);
//			log.debug("globalY: "+CanvasNode.getCanvasNode( shapeNode, canvas ).getGlobalXY().y);
			SwfData placeObject2 = new SwfPlaceObject2Tag( writer, currentDepth, swfId, globTrafo, replace );
			if( firstFrame == null )
				firstFrame = placeObject2;
			swfFile.addChild( placeObject2 );
			
//			log.debug("increase currentDepth for "+shapeId);
			currentDepth++;
		}
		return currentDepth;
	}

	public int getSwfId( String symbolId ) {
		Integer swfId = swfIds.get( symbolId );
		if( swfId == null ) {
			shapeIdCount++;
			swfId = shapeIdCount;
			swfIds.put( symbolId, swfId );
			elementIds.add( symbolId );
		}
		return swfId;
	}
	
	public void end() {
		if( ! shapesAreCaptured )
			captureShapes();
	}

}
