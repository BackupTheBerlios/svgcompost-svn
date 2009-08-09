package de.berlios.svgcompost.animation.export;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.apache.batik.bridge.PaintServer;
import org.apache.batik.dom.GenericElement;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Element;

import de.berlios.svgcompost.animation.canvas.PathConverter;

public abstract class SwfmlShapeExport extends DomExport {
	
	public final int X = 0;
	public final int Y = 1;
	
	public Element edges;

//	public int colorCount = 0;
	
	// record x
	protected ArrayList<Integer> rx = new ArrayList<Integer>();
	// record y
	protected ArrayList<Integer> ry = new ArrayList<Integer>();
	// record type (ABS or REL)
	protected ArrayList<Integer> rt = new ArrayList<Integer>();
	
	public final int ABS = 0;
	public final int REL = 1;
	
	public void capturePath(Element pathElement, Element defineShape) {
		
		PathParser parser = new PathParser();
		AWTPathProducer producer = new AWTPathProducer();
		parser.setPathHandler( producer );
		parser.parse( pathElement.getAttribute( "d" ) );
		Shape shape = producer.getShape();

		Paint fill = PaintServer.convertFillPaint( pathElement, null, null );
		Paint strokeColor = PaintServer.convertStrokePaint( pathElement, null, null );
		Stroke stroke = PaintServer.convertStroke( pathElement );
				
		Element bounds = addElement( defineShape, "bounds" );		
		Element styles = addElement( defineShape, "styles" );
		Element styleList = addElement( styles, "StyleList" );
		styleList.appendChild( fillStyles( fill ) );
		styleList.appendChild( lineStyles( stroke, strokeColor ) );
		
		edges = addElement( addElement( addElement( defineShape, "shapes" ), "Shape" ), "edges" );
//		ExtendedGeneralPath path = new ExtendedGeneralPath( shape );
		ExtendedGeneralPath path = PathConverter.convertPath( new ExtendedGeneralPath( shape ) );
//		Rectangle bRect = path.getBounds();
		Rectangle2D bRect = path.getBounds2D();
		float strokeWidth = 1;//((BasicStroke)stroke).getLineWidth();
		bRect.setRect( bRect.getX()-strokeWidth, bRect.getY()-strokeWidth, bRect.getWidth()+2*strokeWidth, bRect.getHeight()+2*strokeWidth);
//		bRect.x -= strokeWidth;
//		bRect.y -= strokeWidth;
//		bRect.width += 2*strokeWidth;
//		bRect.height += 2*strokeWidth;
//		bRect = new Rectangle( -20, -20, 40, 40 );
		bounds.appendChild( rectangleTag( bRect ) );
		capturePath( path );
	}
	
	protected void capturePath( ExtendedGeneralPath path ) {

		ExtendedPathIterator iterator = path.getExtendedPathIterator();
		float[] points = new float[6];
		float[] current = new float[2];
		float[] start = new float[2];
		int[] coordStart = new int[2];
		
		rx = new ArrayList<Integer>();
		ry = new ArrayList<Integer>();
		rt = new ArrayList<Integer>();
		
		int i = 0;
		while(!iterator.isDone()) {
			int type = iterator.currentSegment(points);
			if( i == 0 ) {
				if( type == ExtendedPathIterator.SEG_MOVETO ) {
					start[X] = points[0];
					start[Y] = points[1];
					coordStart[X] = coord(points[0]);
					coordStart[Y] = coord(points[1]);
				}
				else {
					swfMoveRecord( current, 0, 0 );
					start[X] = 0;
					start[Y] = 0;
					coordStart[X] = 0;
					coordStart[Y] = 0;
				}
			}
			i++;
			addEdgeRecord( type, current, points );

			iterator.next();
		}
		// CHECK with ints
		int[] swf = new int[] {0,0};
		for( int j=0; j< rx.size(); j++ ) {
			int x = rx.get(j);
			int y = ry.get(j);
			if( rt.get(j) == ABS ) {
				swf[X] = x;
				swf[Y] = y;
			}
			else{
				swf[X] += x;
				swf[Y] += y;
			}
		}
//		System.out.println( "start:  "+coordStart[X]+","+coordStart[Y] );
//		System.out.println( "result: "+swf[X]+","+swf[Y] );
		if( coordStart[X] != swf[X] || coordStart[Y] != swf[Y] ) {
			System.err.println( "NOT identical!!!" );
		}
		
		if( current[X] != start[X] || current[Y] != start[Y] )
			swfLineRecord( current, start[X], start[Y] );		
//		swfMoveRecord( current, start[X], start[Y] );		
	}
	
	public Element addEdgeRecord( int type, float[] current, float[] points ) {
		Element node = null;
		switch (type) {
		case ExtendedPathIterator.SEG_CUBICTO:
			cubicError( current, points );
			break;
		case ExtendedPathIterator.SEG_QUADTO:
			swfQuadRecord( current, points );
			break;
		case ExtendedPathIterator.SEG_LINETO:
			swfLineRecord( current, points );
			break;
		case ExtendedPathIterator.SEG_MOVETO:
			swfMoveRecord( current, points );
			break;
		}
		if( node != null )
			edges.appendChild( node );
		return node;
	}
	
	public void swfMoveRecord( float[] current, float[] points ) {
		swfMoveRecord( current, points[0], points[1] );
	}
	
	public void swfMoveRecord( float[] current, float x, float y ) {
		Element node = (Element) edges.appendChild( new GenericElement( "ShapeSetup", document ) );
		node.setAttribute( "x", ""+coord(x) );
		node.setAttribute( "y", ""+coord(y) );
		node.setAttribute( "fillStyle0", "1" );
//		node.setAttribute( "fillStyle0", "0" );
		node.setAttribute( "fillStyle1", "0" );
		node.setAttribute( "lineStyle", "1" );
//		node.setAttribute( "lineStyle", colorCount%2 == 0 ? "1" : "2" );
//		colorCount++;
		current[X] = x;
		current[Y] = y;
		rx.add(coord(x));
		ry.add(coord(y));
		rt.add(ABS);
	}
	
//	public void switchColor( float[] current ) {
//		if(0==0)
//			return;
//		Element node = (Element) edges.appendChild( new GenericElement( "ShapeSetup", doc ) );
//		node.setAttribute( "x", ""+coord(current[X]) );
//		node.setAttribute( "y", ""+coord(current[Y]) );
//		node.setAttribute( "fillStyle0", "1" );
//		node.setAttribute( "fillStyle1", "0" );
//		node.setAttribute( "lineStyle", colorCount%2 == 0 ? "1" : "2" );
//		colorCount++;
//		rx.add(coord(current[X]));
//		ry.add(coord(current[Y]));
//		rt.add(ABS);
//	}
	
	public void swfLineRecord( float[] current, float[] points ) {
		swfLineRecord( current, points[0], points[1] );
	}
	
	public void swfLineRecord( float[] current, float x, float y ) {
		Element node = (Element) edges.appendChild( new GenericElement( "LineTo", document ) );
		node.setAttribute( "x", ""+( coord(x) - coord(current[X]) ) );
		node.setAttribute( "y", ""+( coord(y) - coord(current[Y]) ) );
		current[X] = x;
		current[Y] = y;
		rx.add(coord(x) - coord(current[X]));
		ry.add(coord(y) - coord(current[Y]));
		rt.add(REL);
	}
	
	public void swfQuadRecord( float[] current, float[] points ) {
		swfQuadRecord( current, points[0], points[1], points[2], points[3] );
	}
	
	public void swfQuadRecord( float[] current, float cx, float cy, float ax, float ay ) {
		Element node = (Element) edges.appendChild( new GenericElement( "CurveTo", document ) );
		node.setAttribute( "x1", ""+( coord(cx) - coord(current[X]) ) );
		node.setAttribute( "y1", ""+( coord(cy) - coord(current[Y]) ) );
		node.setAttribute( "x2", ""+( coord(ax) - coord(cx) ) );
		node.setAttribute( "y2", ""+( coord(ay) - coord(cy) ) );
		current[X] = ax;
		current[Y] = ay;
		rx.add(coord(cx) - coord(current[X]));
		ry.add(coord(cy) - coord(current[Y]));
		rt.add(REL);
		rx.add(coord(ax) - coord(cx));
		ry.add(coord(ay) - coord(cy));
		rt.add(REL);
	}
	
	public void cubicError( float[] current, float[] points ) {
		System.err.println( "cubic encountered!" );
		swfLineRecord( current, points[4], points[5] );
	}

	public float dist( float x1, float y1, float x2, float y2 ) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		return (float) Math.sqrt( dx*dx + dy*dy );
	}
		
	public Element fillStyles( Paint fill ) {
		if( fill == null )
			fill = Color.BLUE;
		Element fillStyles = new GenericElement( "fillStyles", document );
		Element solid = addElement( fillStyles, "Solid" );
		Element color = addElement( solid, "color" );
		color.appendChild( colorTag( Color.WHITE ) );
		return fillStyles;
	}
	
	public Element lineStyles( Stroke stroke, Paint strokeColor ) {
		if( stroke == null )
			stroke = new BasicStroke( 1 );
		if( strokeColor == null )
			strokeColor = Color.BLACK;
		Element lineStyles = new GenericElement( "lineStyles", document );
		Element lineStyle = addElement( lineStyles, "LineStyle" );		
		lineStyle.setAttribute( "width", ""+coord(((BasicStroke)stroke).getLineWidth()) );
		Element color = addElement( lineStyle, "color" );
		color.appendChild( colorTag( Color.YELLOW ) );
		
		Element lineStyle2 = addElement( lineStyles, "LineStyle" );		
		lineStyle2.setAttribute( "width", ""+coord(((BasicStroke)stroke).getLineWidth()) );
		Element color2 = addElement( lineStyle2, "color" );
		color2.appendChild( colorTag( Color.GREEN ) );
		return lineStyles;
	}
	
	public Element colorTag( Color color ) {
		int rgbColor = color == null ? 0 : color.getRGB();
		Element colorElement = new GenericElement( "Color", document );
		colorElement.setAttribute( "blue", ""+(rgbColor&255) );
		rgbColor >>= 8;
		colorElement.setAttribute( "green", ""+(rgbColor&255) );
		rgbColor >>= 8;
		colorElement.setAttribute( "red", ""+(rgbColor&255) );
		return colorElement;
	}
	
	public Element rectangleTag( Rectangle2D bRect ) {
		Element rect = new GenericElement( "Rectangle", document );
		rect.setAttribute( "left", ""+coord(bRect.getX()) );
		rect.setAttribute( "right", ""+coord(bRect.getX()+bRect.getWidth()) );
		rect.setAttribute( "top", ""+coord(bRect.getY()) );
		rect.setAttribute( "bottom", ""+coord(bRect.getY()+bRect.getHeight()) );
		return rect;
	}

//	public Element rectangleTag( Rectangle2D.Float bRect ) {
//		Element rect = new GenericElement( "Rectangle", document );
//		rect.setAttribute( "left", ""+coord(bRect.x) );
//		rect.setAttribute( "right", ""+coord(bRect.x+bRect.width) );
//		rect.setAttribute( "top", ""+coord(bRect.y) );
//		rect.setAttribute( "bottom", ""+coord(bRect.y+bRect.height) );
//		return rect;
//	}

}
