package de.berlios.svgcompost.animation.anim.easing;

public class Linear extends Easing {

	@Override
	protected double easeIn(double percentage) {
		return percentage;
	}

	@Override
	protected double easeOut(double percentage) {
		return percentage;
	}

}
