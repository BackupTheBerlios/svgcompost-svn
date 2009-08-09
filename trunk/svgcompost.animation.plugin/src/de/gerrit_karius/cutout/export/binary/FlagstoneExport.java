package de.gerrit_karius.cutout.export.binary;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import com.flagstone.transform.FSBounds;
import com.flagstone.transform.FSCoderException;
import com.flagstone.transform.FSColorTable;
import com.flagstone.transform.FSCoordTransform;
import com.flagstone.transform.FSDefineShape2;
import com.flagstone.transform.FSMovie;
import com.flagstone.transform.FSPlaceObject2;
import com.flagstone.transform.FSRemoveObject2;
import com.flagstone.transform.FSSetBackgroundColor;
import com.flagstone.transform.FSShowFrame;
import com.flagstone.transform.FSSolidFill;
import com.flagstone.transform.FSSolidLine;
import com.flagstone.transform.FSTransformObject;
import com.flagstone.transform.util.FSShapeConstructor;

import de.gerrit_karius.cutout.canvas.Canvas;
import de.gerrit_karius.cutout.canvas.CanvasNode;
import de.gerrit_karius.cutout.canvas.PathConverter;
import de.gerrit_karius.cutout.export.Export;

public class FlagstoneExport implements Export {
	
	private static Logger log = Logger.getLogger(FlagstoneExport.class);

	protected Canvas canvas;
	
	protected FSMovie movie;

	protected HashMap<String, Integer> swfIds = new HashMap<String, Integer>();
	protected ArrayList<String> elementIds = new ArrayList<String>();
	protected ArrayList<FSTransformObject> shapeDefs = new ArrayList<FSTransformObject>();
	protected ArrayList<FSTransformObject> frameDefs = new ArrayList<FSTransformObject>();
	protected int lastHighestDepth = 0;
	protected int shapeIdCount = 0;
	protected boolean shapesAreCaptured = false;
	
	protected int framesPerSecond = 12;
	
	protected boolean initialFrame = true;

	public FlagstoneExport( Canvas canvas ) {
		this.canvas = canvas;
		init();
	}
	
	public void init() {
	    movie = new FSMovie();

	    movie.setVersion(7);
	    movie.setFrameRate(framesPerSecond);
	    movie.setFrameSize(new FSBounds(0, 0, 8000, 8000));

	    movie.add(new FSSetBackgroundColor(FSColorTable.white()));

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
		
		log.debug("capture shape "+shapeId);
		
		int swfId = swfIds.get( shapeId );
		ShapeNode shapeNode = (ShapeNode) canvas.getSourceBld().build( canvas.getSourceCtx(), shapeElement );
		ExtendedGeneralPath path = PathConverter.convertPath( new ExtendedGeneralPath( shapeNode.getShape() ) );
//		ShapePainter painter = shapeNode.getShapePainter();
//		Paint paint = PainterAccess.getPaint( painter );
//		Stroke stroke = PainterAccess.getStroke( painter );

		FSShapeConstructor constructor = new FSShapeConstructor();
		constructor.COORDINATES_ARE_PIXELS = false;
		constructor.add(new FSSolidLine(20, FSColorTable.black()));
		constructor.add(new FSSolidFill(FSColorTable.lightgrey()));

		capturePath(path, constructor);
		FSDefineShape2 shapeDef = constructor.defineShape(swfId);
		shapeDefs.add(shapeDef);
		
	}
	
	protected void capturePath(ExtendedGeneralPath path, FSShapeConstructor constructor) { 
        ExtendedPathIterator iterator = path.getExtendedPathIterator(); 
        double[] points = new double[6]; 
        double[] current = new double[] { 0, 0 }; 

        constructor.newPath(); 
        constructor.selectStyle(0,0); 

        while(!iterator.isDone()) { 
                switch(iterator.currentSegment(points)) { 
                case ExtendedPathIterator.SEG_MOVETO: 
                        constructor.move((int)(points[0]-current[0])*20, (int)(points[1]-current[1])*20); 
                        current[0] = points[0]; 
                        current[1] = points[1]; 
                        break; 
                case ExtendedPathIterator.SEG_LINETO: 
                        constructor.rline((int)(points[0]-current[0])*20, (int)(points[1]-current[1])*20); 
                        current[0] = points[0]; 
                        current[1] = points[1]; 
                        break; 
                case ExtendedPathIterator.SEG_QUADTO: 
                        constructor.rcurve(
                        	(int)(points[0]-current[0])*20, (int)(points[1]-current[1])*20,
                        	(int)(points[2]-points[0])*20, (int)(points[3]-points[1])*20);

                        current[0] = points[2]; 
                        current[1] = points[3]; 
                        break; 
                case ExtendedPathIterator.SEG_ARCTO: 
                        System.err.println("Arc in path!"); 
                        break; 
                case ExtendedPathIterator.SEG_CUBICTO: 
                        System.err.println("Cubic curve in path!"); 
                        break; 
                case ExtendedPathIterator.SEG_CLOSE: 
                default:   
                        break; 
                } 
                iterator.next(); 
        } 

        constructor.closePath(); 
	}
	
	public void captureFrame() {
		
		log.debug("capture frame");
		
		int highestDepth = captureNode( canvas.getRoot().getGraphicsNode(), 1 );
	
		for( int i=highestDepth; i<lastHighestDepth; i++ ) {
			frameDefs.add( new FSRemoveObject2( i ) );
		}
		frameDefs.add(new FSShowFrame());
		lastHighestDepth = highestDepth;

		initialFrame = false;
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
			swfId = getSwfId( shapeId );
	
//			boolean replace = currentDepth < lastHighestDepth;
			AffineTransform globTrafo = shapeNode.getGlobalTransform();
			float[][] matrix = new float[][] {
					new float[] { (float)globTrafo.getScaleX(), (float)globTrafo.getShearX(), (float)globTrafo.getTranslateX()*20 },
					new float[] { (float)globTrafo.getShearY(), (float)globTrafo.getScaleY(), (float)globTrafo.getTranslateY()*20 },
					new float[] { 0, 0, 1 }
			};
//			frameDefs.add( new FSPlaceObject2(currentDepth, new FSCoordTransform(matrix)) );
//			frameDefs.add( new FSPlaceObject2(swfId, currentDepth, new FSCoordTransform(matrix)) );
//			frameDefs.add( new FSPlaceObject2(currentDepth, new FSCoordTransform(matrix)) );
			frameDefs.add( new FSPlaceObject2(initialFrame?FSPlaceObject2.New:FSPlaceObject2.Replace, swfId, currentDepth, new FSCoordTransform(matrix), null) );
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
		movie.add( shapeDefs );
		movie.add( frameDefs );
	}


	public void writeOutput(String fileName) {
		try
		{
		    movie.encodeToFile(fileName);
		}
		catch (FSCoderException e) {
		    e.printStackTrace();
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
	}

}
