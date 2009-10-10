package de.berlios.svgcompost.animation.util;

import java.awt.geom.Point2D;

public class Polar {
	
	public static float rad2deg = 180 / (float) Math.PI;
	
	public float r;
	public float a;
	
	public Polar( float r, float a ) {
		this.r = r;
		this.a = a;
	}

	public Polar( double r, double a ) {
		this.r = (float) r;
		this.a = (float) a;
	}

	public static Polar fromCartesian( float dx, float dy ) {
		float radius = (float) Math.sqrt( dx*dx + dy*dy );
		// ?? 1st param must usually be y
		// usually measured cc-wise from the x-axis.
		// IMPORTANT: 1st param is y, 2nd param is x!!!
		float angle = (float) Math.atan2( dy, dx );
		return new Polar( radius, angle );
	}
	
	public static Polar fromCartesian( Point2D.Float p ) {
		return fromCartesian( p.x, p.y );
	}
	
	public static Polar fromCartesianDiff( Point2D.Float from, Point2D.Float to ) {
		return fromCartesian( to.x - from.x, to.y - from.y );
	}
//	public static Polar fromCartesian9( Point p ) {
//		float dx = p.x;
//		float dy = p.y;
//		float radius = intrinsic.Math.sqrt( dx*dx + dy*dy );
//		System.out.println( "radius: "+radius );
//		float angle = - intrinsic.Math.atan2( dx, dy );
//		return new Polar( radius, angle );
//	}
	
	public static Point2D.Float toCartesian( Polar p ) {
		float x = (float) Math.cos( p.a ) * p.r;
		float y = (float) Math.sin( p.a ) * p.r;
		return new Point2D.Float( x, y );
	}
	public static Point2D.Float toCartesian( float r, float a ) {
		float x = (float) Math.cos( a ) * r;
		float y = (float) Math.sin( a ) * r;
		return new Point2D.Float( x, y );
	}
	
	public static void copyToCartesian( float pa, float pr, Point2D.Float toCart ) {
		toCart.x = (float) Math.cos( pa ) * pr;
		toCart.y = (float) Math.sin( pa ) * pr;
	}
	
//	public static Point toCartesian9( Polar p ) {
//		float x = Math.cos( p.a + (Math.PI * 0.5) ) * p.r;
//		float y = Math.cos( p.a ) * p.r;
//		return new Point( x, y );
//	}
	
	public String toString() {
		return "(r="+r+", a="+(a*rad2deg)+")";
	}
	
	public Object clone() {
		return new Polar( r, a );
	}
	
}
