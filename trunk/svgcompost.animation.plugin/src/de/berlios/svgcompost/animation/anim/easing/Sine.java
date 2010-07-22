package de.berlios.svgcompost.animation.anim.easing;

public class Sine extends Easing {

	@Override
	protected double easeIn( double percentage ) {
		return Math.sin(percentage*(Math.PI/2.0));
	}

	@Override
	protected double easeOut(double percentage) {
		return Math.asin(percentage)/(Math.PI/2.0);
	}

}
