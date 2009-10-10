package de.berlios.svgcompost.animation;

import de.berlios.svgcompost.animation.anim.composite.Scene;
import de.berlios.svgcompost.animation.export.Export;


public class AnimControl {

//	private static Logger log = Logger.getLogger(AnimControl.class);

	public double timePerFrame = 83;
	public int millis = 0;
	protected Scene scene;
	protected Export capture;
	
	public AnimControl( Scene scene ) {
		this.scene = scene;
		scene.prepare();
	}

	public boolean nextFrame() {
		millis += timePerFrame;
//		log.info("millis: "+millis);
		if( millis > scene.getDurationinMillis() ) {
			return false;
		}
		if( scene.getDurationinMillis() != 0 )
			scene.animateAtTime( millis / scene.getDurationinMillis() );
		else
			scene.animateAtTime( 0 );
		if( capture != null )
			capture.captureFrame();
		return true;
	}

	public void setCapture(Export capture) {
		this.capture = capture;
	}
}
