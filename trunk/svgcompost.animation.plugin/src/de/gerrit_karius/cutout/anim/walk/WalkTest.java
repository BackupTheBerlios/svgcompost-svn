package de.gerrit_karius.cutout.anim.walk;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;


import junit.framework.TestCase;

public class WalkTest extends TestCase {

	/*
	 * Test method for 'de.gerrit_karius.cutout.anim.chara.Walk.skewXbyYscaleY(Float, Float, Float)'
	 */
	public void testSkewXbyYscaleY_2() {
		Point2D.Float a_old = new Point2D.Float( randomNumber(), randomNumber() );
		Point2D.Float a_new = new Point2D.Float( randomNumber(), randomNumber() );
		Point2D.Float b_old = new Point2D.Float( randomNumber(), randomNumber() );
		Point2D.Float b_new = new Point2D.Float( randomNumber(), randomNumber() );
		
		AffineTransform trafo = Walk.skewXbyYscaleY( a_old, a_new, b_old, b_new );

		Point2D.Float a_test = new Point2D.Float();
		Point2D.Float b_test = new Point2D.Float();
		
		trafo.transform( a_old, a_test );
		trafo.transform( b_old, b_test );
		
		assertTrue( a_test.distance( a_new ) < 0.0001 );
		assertTrue( b_test.distance( b_new ) < 0.0001 );
	}
	
	public void testSkewXbyYscaleY() {
		
		Point2D.Float a_old = new Point2D.Float( randomNumber(), randomNumber() );
		Point2D.Float b_old = new Point2D.Float( randomNumber(), randomNumber() );
		Point2D.Float b_new = new Point2D.Float( randomNumber(), randomNumber() );
		
		AffineTransform trafo = Walk.skewXbyYscaleY( a_old, b_old, b_new );

//		AffineTransform trafo2 = new AffineTransform();
//		trafo2.setTransform(trafo.getScaleX(), trafo.getShearY(), trafo.getShearX(), trafo.getScaleY(), trafo.getTranslateX(), trafo.getTranslateY());
//		assertTrue( trafo2.equals( trafo ) );
		
		Point2D.Float c = new Point2D.Float( (a_old.x+b_old.x)*0.5f, a_old.y );
		Point2D.Float d = new Point2D.Float( a_old.x, (a_old.y+b_old.y)*0.5f );
		
		Point2D.Float a_test = new Point2D.Float();
		Point2D.Float b_test = new Point2D.Float();
		Point2D.Float c_test = new Point2D.Float();
		Point2D.Float d_test = new Point2D.Float();
		
		trafo.transform( a_old, a_test );
		trafo.transform( b_old, b_test );
		trafo.transform( c, c_test );
		trafo.transform( d, d_test );
		
		assertTrue( a_test.distance( a_old ) < 0.0001 );
		assertTrue( b_test.distance( b_new ) < 0.0001 );
		assertTrue( c_test.distance( c ) < 0.0001 );
		assertTrue( Math.abs(d_test.y - ( a_old.y + (d.y-a_old.y)*(b_new.y-a_old.y)/(b_old.y-a_old.y) ) ) < 0.0001 );
	}
	
	public static int randomNumber() {
		return (int)(Math.random()*1000-500);
	}

	/*
	 * Test method for 'de.gerrit_karius.cutout.anim.chara.Walk.skewYbyXscaleX(Float, Float, Float)'
	 */
	public void testSkewYbyXscaleX() {
		Point2D.Float a = new Point2D.Float( 100, 100 );
		Point2D.Float b_old = new Point2D.Float( 200, 200 );
		Point2D.Float b_new = new Point2D.Float( 300, 300 );
		
		AffineTransform trafo = Walk.skewYbyXscaleX( a, b_old, b_new );
		
		Point2D.Float c = new Point2D.Float( (a.x+b_old.x)*0.5f, a.y );
		Point2D.Float d = new Point2D.Float( a.x, (a.y+b_old.y)*0.5f );
		
		Point2D.Float a_test = new Point2D.Float();
		Point2D.Float b_test = new Point2D.Float();
		Point2D.Float c_test = new Point2D.Float();
		Point2D.Float d_test = new Point2D.Float();
		
		trafo.transform( a, a_test );
		trafo.transform( b_old, b_test );
		trafo.transform( c, c_test );
		trafo.transform( d, d_test );
		
		assertTrue( a_test.equals( a ) );
		assertTrue( b_test.equals( b_new ) );
		assertTrue( c_test.x == ( a.x + (c.x-a.x)*(b_new.x-a.x)/(b_old.x-a.x) ) );
		assertTrue( d_test.equals( d ) );
	}

}
