package de.berlios.svgcompost.animation.anim.easing;

public class Linear extends Easing {

	public Linear( int align ) {
		this.align = align;
	}

	public double easeIn(double percentage) {
		return percentage;
	}

}
