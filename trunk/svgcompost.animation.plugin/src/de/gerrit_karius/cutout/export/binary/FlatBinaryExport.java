package de.gerrit_karius.cutout.export.binary;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.bridge.PaintServer;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.gvt.ShapeNode;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.gerrit_karius.cutout.canvas.Canvas;
import de.gerrit_karius.cutout.canvas.CanvasNode;
import de.gerrit_karius.cutout.canvas.PathConverter;
import de.gerrit_karius.cutout.export.FlatExport;

public class FlatBinaryExport extends FlatExport {

	private static Logger log = Logger.getLogger(FlatBinaryExport.class);

	protected SwfWriter writer = new SwfWriter();
	protected SwfFile swfFile;
	protected SwfData firstFrame;
	protected SwfStruct resources;
	protected Map<String, Integer> swfIds = new HashMap<String, Integer>();
	protected int lastHighestDepth = 0;
	protected int shapeIdCount = 0;
	
	protected int currentDepth;
	
	protected int framesPerSecond = 12;
	
	public FlatBinaryExport( Canvas canvas ) {
		this.canvas = canvas;
		swfFile = new SwfFile( writer, 5, new Rectangle2D.Float( 0, 0, canvas.width, canvas.height ), framesPerSecond );
		swfFile.addChild( new SwfSetBackgroundColorTag( writer, Color.white ) );
		resources = new SwfStruct( writer );
		swfFile.addChild( resources );
	}

	@Override
	protected void captureShapeDefinition(Element shapeElement) {
		String shapeId = shapeElement.getAttribute("id");
		int swfId = swfIds.get( shapeId );
		ShapeNode shapeNode = (ShapeNode) canvas.getSourceBld().build( canvas.getSourceCtx(), shapeElement );
		ExtendedGeneralPath path = PathConverter.convertPath( new ExtendedGeneralPath( shapeNode.getShape() ) );
//		ShapePainter painter = shapeNode.getShapePainter();
//		Paint paint = PainterAccess.getPaint( painter );
//		Stroke stroke = PainterAccess.getStroke( painter );
		Paint paint = PaintServer.convertFillPaint(shapeElement, shapeNode, canvas.getSourceCtx());
		Stroke stroke = PaintServer.convertStroke(shapeElement);
		
		resources.addChild( new SwfDefineShape3Tag( writer, swfId, path, paint, stroke ) );
	}

	@Override
	protected void startFrame() {
		currentDepth = 1;
	}

	@Override
	protected void captureShapeInstance(ShapeNode shapeNode) {
		String shapeId = CanvasNode.getCanvasNode( shapeNode, canvas ).getSymbolId();
		
//		int swfId;
//		if( shapeImport != null ) {
//			swfId = shapeImport.getShapeId( shapeId );
//			swfIds.put( shapeId, swfId );
//		}
//		else swfId = getSwfId( shapeId );
		
		int swfId = getSwfId( shapeId );

		boolean replace = currentDepth < lastHighestDepth;
		AffineTransform globTrafo = shapeNode.getGlobalTransform();
		SwfData placeObject2 = new SwfPlaceObject2Tag( writer, currentDepth, swfId, globTrafo, replace );
		if( firstFrame == null )
			firstFrame = placeObject2;
		swfFile.addChild( placeObject2 );
		
		currentDepth++;
	}

	@Override
	protected void endFrame() {
		int highestDepth = currentDepth;

		for( int i=highestDepth; i<lastHighestDepth; i++ ) {
			SwfData removeObject2 = new SwfRemoveObject2Tag( writer, i );
			swfFile.addChild( removeObject2 );
		}
		swfFile.addFrame();
		lastHighestDepth = highestDepth;
	}

	public int getSwfId( String symbolId ) {
		Integer swfId = swfIds.get( symbolId );
		if( swfId == null ) {
			shapeIdCount++;
			swfId = shapeIdCount;
			swfIds.put( symbolId, swfId );
			shapeIds.add( symbolId );
		}
		return swfId;
	}
	
	public void writeOutput(String fileName) {
		writer.convertData( swfFile );
		log.info("Write output file \""+fileName+"\".");
		writer.writeFile( new File(fileName) );
	}

}
