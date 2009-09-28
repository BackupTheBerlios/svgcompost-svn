package de.berlios.svgcompost.animation.anim.skeleton;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;

import de.berlios.svgcompost.animation.anim.Tweener;
import de.berlios.svgcompost.animation.util.CatmullRomSpline;

public class CatmullRomTweener extends Tweener {
	
	Point2D.Float p0;
	Point2D.Float p1;
	Point2D.Float p2;
	Point2D.Float p3;

	public CatmullRomTweener(Float p0, Float p1, Float p2, Float p3) {
		load(p0,p1,p2,p3);
	}

	public void load(Float p0, Float p1, Float p2, Float p3) {
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}

	public Object tween( double percentage ) {
		return CatmullRomSpline.tween(percentage,p0==null?p1:p0,p1,p2,p3==null?p2:p3);
	}

}
