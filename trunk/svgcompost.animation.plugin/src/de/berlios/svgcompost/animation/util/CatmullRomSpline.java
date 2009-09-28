package de.berlios.svgcompost.animation.util;

import java.awt.geom.Point2D;

public class CatmullRomSpline {

	
	public static Point2D.Float tween(double t, Point2D.Float p0, Point2D.Float p1, Point2D.Float p2, Point2D.Float p3) {
		double t2 = t*t;
		double t3 = t2*t;
		double tx = 0.5 * ( (2 * p1.x) +
				(-p0.x + p2.x) * t +
				(2*p0.x - 5*p1.x + 4*p2.x - p3.x) * t2 +
				(-p0.x + 3*p1.x- 3*p2.x + p3.x) * t3);
		double ty = 0.5 * ( (2 * p1.y) +
				(-p0.y + p2.y) * t +
				(2*p0.y - 5*p1.y + 4*p2.y - p3.y) * t2 +
				(-p0.y + 3*p1.y- 3*p2.y + p3.y) * t3);
		return new Point2D.Float((float)tx, (float)ty);
	}

}
