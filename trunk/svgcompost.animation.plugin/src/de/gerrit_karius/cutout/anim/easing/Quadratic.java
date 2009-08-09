package de.gerrit_karius.cutout.anim.easing;

public class Quadratic extends Easing {

	public static Quadratic _in = new Quadratic( Easing.EASE_IN );
	public static Quadratic _out = new Quadratic( Easing.EASE_OUT );
	public static Quadratic _inOut = new Quadratic( Easing.EASE_IN_OUT );
	
	public double easeIn(double percentage) {
		return percentage * percentage;
	}

	public Quadratic( int align ) {
		this.align = align;
	}
}
