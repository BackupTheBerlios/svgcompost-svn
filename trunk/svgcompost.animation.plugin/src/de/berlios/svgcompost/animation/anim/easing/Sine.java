package de.berlios.svgcompost.animation.anim.easing;

public class Sine extends Easing {

	public double easeIn( double percentage ) {
		return Math.sin(percentage);
	}

}
