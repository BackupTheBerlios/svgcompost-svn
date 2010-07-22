package de.berlios.svgcompost.animation.anim.easing;

public class Cubic extends Easing {

	@Override
	protected double easeIn(double percentage) {
		return percentage * percentage * percentage;
	}

	@Override
	protected double easeOut(double percentage) {
		return Math.pow( percentage, 1.0/3.0 );
	}

}
