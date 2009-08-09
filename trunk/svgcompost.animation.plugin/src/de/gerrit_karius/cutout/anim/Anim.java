package de.gerrit_karius.cutout.anim;

import de.gerrit_karius.cutout.anim.composite.Scene;
import de.gerrit_karius.cutout.anim.easing.Easing;

public abstract class Anim {
	
	protected String name;
	
	protected double duration = 0;
	protected Easing easing;
	protected Scene scene;
	

	public void setDurationinMillis( double duration ) {
		this.duration = duration;
	}
	public double getDurationinMillis() {
		return duration;
	}
	public void setDurationInSeconds( double duration ) {
		this.duration = duration * 1000;
	}
	public double getDurationInSeconds() {
		return duration / 1000.0;
	}

	public Anim setEasing( Easing easing ) {
		this.easing = easing;
		return this;
	}
	public void setScene( Scene scene ) {
		this.scene = scene;
	}

	public abstract void prepare();
	
	public void end() {
		animateAtTime( 1 );
	}
	
	public final void animateAtTime( double percentage ) {
		animate( Easing.applyEasing( easing, percentage ) );
	}
	
	protected abstract void animate( double percentage );

}
