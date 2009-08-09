package de.berlios.svgcompost.animation.anim.composite;

import de.berlios.svgcompost.animation.anim.Anim;

/**
 * Loops a wrapped anim several times.
 * @author Gerrit
 *
 */
public class Loop extends Anim {
	
	int count = 0;
	int noOfTimes = 0;
	Anim anim;
	
	public Loop( Anim anim, int noOfTimes ) {
		this.noOfTimes = noOfTimes;
		this.anim = anim;
	}

	public void prepare() {
		anim.prepare();
		count = 0;
		duration = anim.getDurationinMillis() * noOfTimes;
	}
	
	protected void animate(double percentage) {
		percentage *= noOfTimes;
		anim.animateAtTime( percentage );
	}
	
}
