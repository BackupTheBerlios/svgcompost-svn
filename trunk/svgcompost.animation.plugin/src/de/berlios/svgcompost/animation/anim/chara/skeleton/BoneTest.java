package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.awt.geom.AffineTransform;

import junit.framework.TestCase;

public class BoneTest extends TestCase {

	/*
	 * Test method for 'de.berlios.svgcompost.animation.anim.cluster.Bone.subtractFromMatrix(AffineTransform, AffineTransform)'
	 */
	public void testSubtractFromMatrix() {
		AffineTransform a = new AffineTransform( 6.1, -5.2, 4.3, -3.4, 2.5, -1.6 );
		AffineTransform aClone = (AffineTransform) a.clone();
		AffineTransform b = new AffineTransform( 16, -25, 34, -43, 52, -61 );
		Bone.subtractFromMatrix( b, a );
		a.preConcatenate( b );
		System.out.println( a );
		System.out.println( aClone );
		double maxDiff = 0.00001;
		assertTrue( Math.abs( a.getTranslateX() - aClone.getTranslateX() ) < maxDiff );
		assertTrue( Math.abs( a.getTranslateY() - aClone.getTranslateY() ) < maxDiff );
		assertTrue( Math.abs( a.getShearX() - aClone.getShearX() ) < maxDiff );
		assertTrue( Math.abs( a.getShearY() - aClone.getShearY() ) < maxDiff );
		assertTrue( Math.abs( a.getScaleX() - aClone.getScaleX() ) < maxDiff );
		assertTrue( Math.abs( a.getScaleY() - aClone.getScaleY() ) < maxDiff );
	}

}
