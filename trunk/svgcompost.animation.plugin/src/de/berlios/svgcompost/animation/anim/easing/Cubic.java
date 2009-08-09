package de.berlios.svgcompost.animation.anim.easing;

public class Cubic extends Easing {
	public static Cubic _in = new Cubic( Easing.EASE_IN );
	public static Cubic _out = new Cubic( Easing.EASE_OUT );
	public static Cubic _inOut = new Cubic( Easing.EASE_IN_OUT );
	
	public double easeIn(double percentage) {
		return percentage * percentage * percentage;
	}

	public Cubic( int align ) {
		this.align = align;
	}
}
