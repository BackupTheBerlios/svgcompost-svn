package de.berlios.svgcompost.animation.anim.chara.skeleton;

import org.apache.log4j.Logger;

import de.berlios.svgcompost.animation.anim.Anim;
import de.berlios.svgcompost.animation.anim.easing.Easing;
import de.berlios.svgcompost.animation.canvas.CanvasNode;

/**
 * The KeyframeAnim class is responsible for switching keyframes at the right time.
 * This is done by making the active keyframe visible and the others invisible.
 * It also calls the Skeleton instance to do the actual tweening.
 * @author gerrit
 *
 */
public class KeyframeAnim extends Anim {

	private static Logger log = Logger.getLogger(KeyframeAnim.class);

//	protected int tweeningPairIndex;
	protected Skeleton model;
	protected CanvasNode frame1;
	protected CanvasNode frame2;
	protected boolean switchActiveFrame = false;
//	protected List<CanvasNode> frames;
	
//	protected int activeKey;
//	protected int inactiveKey;
	protected CanvasNode activeKey;
	protected CanvasNode inactiveKey;
	
	public KeyframeAnim(Skeleton model, /*List<CanvasNode> frames, int tweeningPairIndex,*/ CanvasNode frame1, CanvasNode frame2) {
		this.model = model;
//		this.tweeningPairIndex = tweeningPairIndex;
		this.frame1 = frame1;
		this.frame2 = frame2;
//		this.frames = frames;
	}

	@Override
	public void prepare() {
		switch( Easing.getAlign(easing) ) {
		case Easing.EASE_IN:
			setActiveFrame( true );
			break;
		case Easing.EASE_OUT:
			setActiveFrame( false );
			break;
		case Easing.EASE_IN_OUT:
			setActiveFrame( true );
			switchActiveFrame = true;
			break;
		}
	}

	@Override
	protected void animate( double percentage ) {
		log.debug( "KeyTweening.animate: "+percentage );

		if( switchActiveFrame ) {
			setActiveFrame( percentage < 0.5 );
		}
		
//		SkeletonKey.tween(frames.get(tweeningPairIndex), frames.get(activeKey), percentage);
		SkeletonKey.tween(frame1, activeKey, percentage);
	}
	
	protected void setActiveFrame( boolean firstFrameActive ) {
		if( firstFrameActive ) {
			activeKey = frame1; //tweeningPairIndex;
			inactiveKey = frame2; //tweeningPairIndex+1;
		}
		else {
			activeKey = frame2; //tweeningPairIndex+1;
			inactiveKey = frame1; //tweeningPairIndex;
		}
//		frames.get(activeKey).setVisible(true);
//		frames.get(inactiveKey).setVisible(false);
		activeKey.setVisible(true);
		inactiveKey.setVisible(false);
	}
	
	@Override
	public void end() {
		super.end();
		setActiveFrame( false );
	}


}
