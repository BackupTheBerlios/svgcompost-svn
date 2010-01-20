package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import junit.framework.TestCase;
import de.berlios.svgcompost.animation.canvas.Canvas;
import de.berlios.svgcompost.animation.canvas.CanvasNode;
import de.berlios.svgcompost.animation.util.Polar;

public class JointedConnectionTest extends TestCase {

	/*
	 * Test method for 'de.berlios.svgcompost.animation.anim.chara.JointedConnection.findRotationPoint(AffineTransform, AffineTransform)'
	 */
	public void testFindRotationPoint() {
		AffineTransform a = new AffineTransform( 6.1, -5.2, 4.3, -3.4, 2.5, -1.6 );
		AffineTransform b = new AffineTransform( 16, -25, 34, -43, 52, -61 );
//		AffineTransform a = new AffineTransform( 1, 0, 0, 1, 0, 0 );
//		AffineTransform b = new AffineTransform( 1, 0, 0, 1, 10, 10 );
		Point2D.Float p = JointedLimb.findRotationPoint( a, b );
		Point2D.Float pa = new Point2D.Float();
		Point2D.Float pb = new Point2D.Float();
		a.transform( p, pa );
		b.transform( p, pb );
		System.out.println( "p: "+p );
		System.out.println( "pa: "+pa );
		System.out.println( "pb: "+pb );
		assertTrue( pa.distance( pb ) < 0.00001 );
	}

	public void testAlignWithPoint() {
		System.out.println("testAlignWithPoint");
		Canvas canvas = new Canvas( null );
		CanvasNode node = canvas.getRoot().addEmptyChild( "node" );
		node.setXY( -55, 6 );
		Point2D.Float a = new Point2D.Float( -33, 10 );
		Point2D.Float b = new Point2D.Float( 10, -4 );
		JointedLimb.alignWithPoint( node, a, b );
		Point2D.Float center = node.projectCenterToLocal( node.getParent() );
		Point2D.Float aOnParent = node.projectPointToLocal( a, node.getParent() );
		Polar alignPointPolar = Polar.fromCartesianDiff( center, aOnParent );
		Polar alignWithPolar = Polar.fromCartesianDiff( center, b );
		float angle = alignWithPolar.a - alignPointPolar.a;
		System.out.println( "angle: "+angle );
		assertTrue( Math.abs( angle ) < 0.00001 );
	}
}
