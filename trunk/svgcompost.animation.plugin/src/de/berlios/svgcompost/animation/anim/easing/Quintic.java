package de.berlios.svgcompost.animation.anim.easing;

public class Quintic extends Easing {
	public static Quintic _in = new Quintic( Easing.EASE_IN );
	public static Quintic _out = new Quintic( Easing.EASE_OUT );
	public static Quintic _inOut = new Quintic( Easing.EASE_IN_OUT );
	
	public double easeIn(double percentage) {
		double p_2 = percentage * percentage;
		return percentage * p_2 * p_2;
	}

	public Quintic( int align ) {
		this.align = align;
	}
}
