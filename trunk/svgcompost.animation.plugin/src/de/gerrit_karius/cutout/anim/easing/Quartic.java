package de.gerrit_karius.cutout.anim.easing;

public class Quartic extends Easing {
	public static Quartic _in = new Quartic( Easing.EASE_IN );
	public static Quartic _out = new Quartic( Easing.EASE_OUT );
	public static Quartic _inOut = new Quartic( Easing.EASE_IN_OUT );
	
	public double easeIn(double percentage) {
		double p_2 = percentage * percentage;
		return p_2 * p_2;
	}

	public Quartic( int align ) {
		this.align = align;
	}
}
