package de.berlios.svgcompost.animation.anim.easing;

public class Quadratic extends Easing {

	@Override
	protected double easeIn(double percentage) {
		return percentage * percentage;
	}

	@Override
	protected double easeOut(double percentage) {
		return Math.sqrt( percentage );
	}

}
