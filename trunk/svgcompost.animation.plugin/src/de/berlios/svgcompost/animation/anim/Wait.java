package de.berlios.svgcompost.animation.anim;

public class Wait extends Anim {

	public Wait( double duration ) {
		setDurationInSeconds(duration);
	}

	@Override
	protected void animate(double percentage) {
	}

	@Override
	public void prepare() {
	}
	
}
