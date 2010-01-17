package de.berlios.svgcompost.swf;

import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;


/**
 * Converts an input ExtendedGeneralPath into an output ExtendedGeneralPath which doesn't use cubic curves any more.
 * Each cubic curve of the input is split into exactly 2 quadratic curves in the output.
 * @author Gerrit Karius
 *
 */
public class NonstaticPathConverter {
	
	public static final int X = 0;
	public static final int Y = 1;
	
	public ExtendedGeneralPath convertedPath;

	public ExtendedGeneralPath convertPath(ExtendedGeneralPath path) {
		ExtendedPathIterator iterator = path.getExtendedPathIterator();
		float[] points = new float[6];
		float[] current = new float[2];
//		float[] start = new float[2];
		
		convertedPath = new ExtendedGeneralPath();
		
		int i = 0;
		while(!iterator.isDone()) {
			int type = iterator.currentSegment(points);
			if( i == 0 ) {
				if( type == ExtendedPathIterator.SEG_MOVETO ) {
//					start[X] = points[0];
//					start[Y] = points[1];
				}
				else {
					swfMoveRecord( current, 0, 0 );
//					start[X] = 0;
//					start[Y] = 0;
				}
			}
			i++;
			convertEdge( type, current, points );

			iterator.next();
		}
		
//		swfMoveRecord( current, start[X], start[Y] );
		
		return convertedPath;
	}
	
	public void convertEdge( int type, float[] current, float[] points ) {
		switch (type) {
		case ExtendedPathIterator.SEG_CUBICTO:
			splitCubicIntoQuads( current, points );
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
	}
	
	public void swfMoveRecord( float[] current, float[] points ) {
		swfMoveRecord( current, points[0], points[1] );
	}
	
	public void swfMoveRecord( float[] current, float x, float y ) {
		convertedPath.moveTo( x, y );
		current[X] = x;
		current[Y] = y;
	}
	
	public void swfLineRecord( float[] current, float[] points ) {
		swfLineRecord( current, points[0], points[1] );
	}
	
	public void swfLineRecord( float[] current, float x, float y ) {
		convertedPath.lineTo( x, y );
		current[X] = x;
		current[Y] = y;
	}
	
	public void swfQuadRecord( float[] current, float[] points ) {
		swfQuadRecord( current, points[0], points[1], points[2], points[3] );
	}
	
	public void swfQuadRecord( float[] current, float cx, float cy, float ax, float ay ) {
//		if(0==0) {
//			swfLineRecord( current, ax, ay );
//			return;
//		}
		float daa = dist( current[0], current[1], ax, ay );
		float dca = dist( cx, cy, ax, ay );
		float dac = dist( current[0], current[1], cx, cy );
		final float threshold = 0.5f;
//		if( daa == 0 )
//			return;
		if( daa < threshold || dac < threshold || dca < threshold ) {
//			if( daa < threshold )
//				System.out.println( "daa: "+daa );
//			if( dac < threshold )
//				System.out.println( "dac: "+dac );
//			if( dca < threshold )
//				System.out.println( "dca: "+dca );
//			swfLineRecord( current, ax, ay );
//			return;
		}
		convertedPath.quadTo( cx, cy, ax, ay );
		current[X] = ax;
		current[Y] = ay;
	}
	
	public void splitCubicIntoQuads( float[] current, float[] points ) {
		// deconstruction of the cubic curve
		float[] p1 = current;
		float[] p2 = new float[] { points[0], points[1] };
		float[] p3 = new float[] { points[2], points[3] };
		float[] p4 = new float[] { points[4], points[5] };
		float[] q1 = center( p1, p2 );
		float[] q2 = center( p2, p3 );
		float[] q3 = center( p3, p4 );
		float[] r1 = center( q1, q2 );
		float[] r2 = center( q2, q3 );
		// new anchor point
		float[] s1 = center( r1, r2 );
		// catch nodes without control points
		if( p2[X] == p1[X] && p2[Y] == p1[Y] )
			p2 = p3;
		else if( p3[X] == p4[X] && p3[Y] == p4[Y] )
			p3 = p2;
		// new control points
		float[] c1 = isProblematic(p1, p2, r2, r1) ? center( p2, r1 ) : intersectionAbs( p1, p2, r1, r2 );
		float[] c2 = isProblematic(p4, p3, r1, r2) ? center( p3, r2 ) : intersectionAbs( p4, p3, r1, r2 );
		
		swfQuadRecord( current, c1[X], c1[Y], s1[X], s1[Y] );
		swfQuadRecord( current, c2[X], c2[Y], p4[X], p4[Y] );
	}

	public boolean isProblematic( float[] a1, float[] c1, float[] a2, float[] c2 ) {
		boolean controlsOnOneSide = triangleOrientation( a1, a2, c1 ) == triangleOrientation( a1, a2, c2 );
		// distance d = a2 - a1
		float[] d = new float[] {a2[X]-a1[X], a2[Y]-a1[Y]};
		// bound = a + rotateBy90(d)
		float[] bound1 = new float[] {a1[X]+d[Y], a1[Y]-d[X]};
		float[] bound2 = new float[] {a2[X]+d[Y], a2[Y]-d[X]};
		float a2OnA1 = triangleOrientation( a1, bound1, a2 );
		float a1OnA2 = triangleOrientation( a2, bound2, a1 );
		boolean controlsBoundedByA1 = triangleOrientation( a1, bound1, c1 ) == a2OnA1
			&& triangleOrientation( a1, bound1, c2 ) == a2OnA1;
		boolean controlsBoundedByA2 = triangleOrientation( a2, bound2, c1 ) == a1OnA2
			&& triangleOrientation( a2, bound2, c2 ) == a1OnA2;
		// The control handles should intersect nicely if they both can be boxed together
		// with the connection from a1 to a2 and two the two orthogonal bounding lines.
		// The lines could still intersect otherwise, but any other situation is not supposed
		// to occur in the bezier splitting context.
		return ! (controlsOnOneSide && controlsBoundedByA1 && controlsBoundedByA2);
	}
	
	public int triangleOrientation(float[] p, float[] q, float[] r) {
		return (int) Math.signum( (q[X]-p[X]) * (r[Y]-p[Y]) - (r[X]-p[X]) * (q[Y]-p[Y]) );
	}

	public float dist( float x1, float y1, float x2, float y2 ) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		return (float) Math.sqrt( dx*dx + dy*dy );
	}
	
	public float[] center( float[] p1, float[] p2 ) {
		float[] p3 = new float[2];
		p3[X] = (p1[X] + p2[X]) / 2;
		p3[Y] = (p1[Y] + p2[Y]) / 2;
		return p3;
	}

	public float[] intersectionAbs( float[] a, float[] b, float[] c, float[] d ) {
		// x1 = a + n*(b-a)
		// x2 = c + m*(d-c)
		return intersection( a, new float[] {b[X]-a[X], b[Y]-a[Y]}, c, new float[] {d[X]-c[X], d[Y]-c[Y]} );
	}
	
	public float[] intersection( float[] a, float[] b, float[] c, float[] d ) {
		// x1 = a + n*b
		// x2 = c + m*d
		float num = ( (c[Y]-a[Y])*b[X] - (c[X]-a[X])*b[Y] );
		float denom = ( d[X]*b[Y] - d[Y]*b[X] );
		float[] i = new float[] { c[X]+(num*d[X])/denom, c[1]+(num*d[Y])/denom };
		return i;
	}
	
}
