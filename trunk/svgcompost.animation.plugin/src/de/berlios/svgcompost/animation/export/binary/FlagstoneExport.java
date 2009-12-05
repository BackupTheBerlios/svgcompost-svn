package de.berlios.svgcompost.animation.export.binary;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.CompositeShapePainter;
import org.apache.batik.gvt.FillShapePainter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.apache.batik.gvt.StrokeShapePainter;
import org.w3c.dom.Element;

import com.flagstone.transform.FSBounds;
import com.flagstone.transform.FSCoderException;
import com.flagstone.transform.FSColor;
import com.flagstone.transform.FSColorTable;
import com.flagstone.transform.FSCoordTransform;
import com.flagstone.transform.FSDefineShape2;
import com.flagstone.transform.FSFillStyle;
import com.flagstone.transform.FSLineStyle;
import com.flagstone.transform.FSMovie;
import com.flagstone.transform.FSPlaceObject2;
import com.flagstone.transform.FSRemoveObject2;
import com.flagstone.transform.FSSetBackgroundColor;
import com.flagstone.transform.FSShowFrame;
import com.flagstone.transform.FSSolidFill;
import com.flagstone.transform.FSSolidLine;
import com.flagstone.transform.FSTransformObject;
import com.flagstone.transform.util.FSShapeConstructor;

import de.berlios.svgcompost.animation.canvas.Canvas;
import de.berlios.svgcompost.animation.canvas.PathConverter;
import de.berlios.svgcompost.animation.export.Export;

public class FlagstoneExport implements Export {
	
	protected Canvas canvas;
	
	protected FSMovie movie;

	protected HashMap<Element, Integer> swfIds = new HashMap<Element, Integer>();
	protected ArrayList<Element> elements = new ArrayList<Element>();
	protected ArrayList<FSTransformObject> shapeDefs = new ArrayList<FSTransformObject>();
	protected ArrayList<FSTransformObject> frameDefs = new ArrayList<FSTransformObject>();
	protected int lastHighestDepth = 0;
	protected int shapeIdCount = 0;
	protected boolean shapesAreCaptured = false;
	
	protected int framesPerSecond = 12;
	Rectangle2D screenSize;
	
	protected boolean initialFrame = true;

	public FlagstoneExport( Canvas canvas ) {
		this.canvas = canvas;
		init();
	}
	
	public void init() {
	    movie = new FSMovie();

	    movie.setVersion(7);
	    movie.setFrameRate(framesPerSecond);
	    screenSize = new Rectangle2D.Float( 0, 0, canvas.getWidth(), canvas.getHeight() );
	    movie.setFrameSize(new FSBounds(0, 0, (int)(screenSize.getWidth()*20), (int)(screenSize.getHeight()*20)));

	    movie.add(new FSSetBackgroundColor(FSColorTable.white()));

	}

	protected void captureShapes() {
//		SVGDocument sourceDoc = canvas.getSourceDoc();
		for( Iterator<Element> shapeElementIterator = swfIds.keySet().iterator(); shapeElementIterator.hasNext(); ) {
			Element element = shapeElementIterator.next();
			if( element == null ) {
				System.out.println( "Warning: element not found." );
				continue;
			}
			captureShape( element );
		}
		shapesAreCaptured = true;
	}
	
	protected void captureStyles( ShapeNode shapeNode, FSShapeConstructor constructor ) {
		ShapePainter painter = shapeNode.getShapePainter();
		FillShapePainter fillPainter = null;
		StrokeShapePainter strokePainter = null;
		if( painter instanceof CompositeShapePainter ) {
			CompositeShapePainter compositePainter = (CompositeShapePainter) painter;
			for (int i = 0; i < compositePainter.getShapePainterCount(); i++) {
				ShapePainter childPainter = compositePainter.getShapePainter(i);
				if( childPainter instanceof FillShapePainter )
					fillPainter = (FillShapePainter) childPainter;
				else if( childPainter instanceof StrokeShapePainter )
					strokePainter = (StrokeShapePainter) childPainter;
				else
					System.out.println( "Unimplemented: "+childPainter );
			}
		}
		else if(painter instanceof FillShapePainter) {
			fillPainter = (FillShapePainter) painter;
		}
		else if(painter instanceof StrokeShapePainter) {
			strokePainter = (StrokeShapePainter) painter;
		}
		
		if( strokePainter != null ) {
			Paint strokePaint = PainterAccess.getPaint( strokePainter );
			Stroke stroke = PainterAccess.getStroke( strokePainter );
			if( strokePaint instanceof Color && stroke instanceof BasicStroke ) {
				Color color = (Color) strokePaint;
				FSColor fsColor = new FSColor( color.getRed(), color.getGreen(), color.getBlue() );
				int width = (int) (((BasicStroke)stroke).getLineWidth()*20);
				constructor.add(new FSSolidLine( width, fsColor ));
			}
			else if( strokePaint != null || stroke != null ) {
				System.out.println( "Unimplemented: "+stroke+" with "+strokePaint );
				constructor.add(new FSSolidLine(20, FSColorTable.black()));
			}
		}
		if( fillPainter != null ) {
			Paint fillPaint = PainterAccess.getPaint( fillPainter );
			if( fillPaint instanceof Color ) {
				Color color = (Color) fillPaint;
				FSColor fsColor = new FSColor( color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() );
				constructor.add(new FSSolidFill( fsColor ));
			}
			else if( fillPaint != null ) {
				System.out.println( "Unimplemented: "+fillPaint.getClass() );
				constructor.add(new FSSolidFill(FSColorTable.lightgrey()));
			}
		}
		
	}

	protected void captureShape( Element shapeElement ) {
			
		Integer swfId = swfIds.get( shapeElement );
		ShapeNode shapeNode = (ShapeNode) canvas.getSourceBld().build( canvas.getSourceCtx(), shapeElement );
		ExtendedGeneralPath path = PathConverter.convertPath( new ExtendedGeneralPath( shapeNode.getShape() ) );

		FSShapeConstructor constructor = new FSShapeConstructor();
		constructor.COORDINATES_ARE_PIXELS = false;
		
		captureStyles(shapeNode, constructor);

		capturePath(path, constructor);
		FSDefineShape2 shapeDef = constructor.defineShape(swfId);
		shapeDefs.add(shapeDef);
		
	}
	
	protected void capturePath(ExtendedGeneralPath path, FSShapeConstructor constructor) { 
        ExtendedPathIterator iterator = path.getExtendedPathIterator(); 
        double[] points = new double[6]; 
        double[] current = new double[] { 0, 0 }; 

        constructor.newPath(); 
        
        if( constructor.getLineStyles().size() > 0 )
        	constructor.selectLineStyle(0);
        else
        	constructor.setLineStyles(new ArrayList<FSLineStyle>());
        if( constructor.getFillStyles().size() > 0 )
        	constructor.selectFillStyle(0);
        else
        	constructor.setFillStyles(new ArrayList<FSFillStyle>());

        while(!iterator.isDone()) { 
                switch(iterator.currentSegment(points)) { 
                case ExtendedPathIterator.SEG_MOVETO: 
                        constructor.move((int)((points[0]-current[0])*20), (int)((points[1]-current[1])*20)); 
                        current[0] = points[0]; 
                        current[1] = points[1]; 
                        break; 
                case ExtendedPathIterator.SEG_LINETO: 
                        constructor.rline((int)((points[0]-current[0])*20), (int)((points[1]-current[1])*20)); 
                        current[0] = points[0]; 
                        current[1] = points[1]; 
                        break; 
                case ExtendedPathIterator.SEG_QUADTO: 
                        constructor.rcurve(
                        	(int)((points[0]-current[0])*20), (int)((points[1]-current[1])*20),
                        	(int)((points[2]-points[0])*20), (int)((points[3]-points[1])*20));

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
		
		Rectangle2D bounds = canvas.getRoot().getGraphicsNode().getBounds();
		if( bounds.getWidth() == 0 || bounds.getHeight() == 0 ) {
			System.out.println( "Warning: frame "+(frameDefs.size()+1)+" is empty." );
		}
		else {
			Rectangle2D intersection = bounds.createIntersection(screenSize);
			if( intersection.getWidth() == 0 || intersection.getHeight() == 0 ) {
				System.out.println( "Warning: bounds of frame "+(frameDefs.size()+1)+" are off screen:"+bounds );
			}
		}
		
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
			Element element;
			if( shapeNode.getRenderingHints() != null )
				element = (Element) shapeNode.getRenderingHints().get(Canvas.KEY_SRC_ELEMENT);
			else
				element = canvas.getSourceCtx().getElement(shapeNode);
			
			int swfId;
			swfId = getSwfId( element );
	
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
	
	public int getSwfId( Element symbolId ) {
		Integer swfId = swfIds.get( symbolId );
		if( swfId == null ) {
			shapeIdCount++;
			swfId = shapeIdCount;
			swfIds.put( symbolId, swfId );
			elements.add( symbolId );
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
			System.out.println( "SWF shape count is "+shapeIdCount );
			System.out.println( "SWF frame count is "+frameDefs.size() );
		    movie.encodeToFile(fileName);
		    System.out.println( "SWF movie written to file "+fileName );
		}
		catch (FSCoderException e) {
		    e.printStackTrace();
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
	}

}
