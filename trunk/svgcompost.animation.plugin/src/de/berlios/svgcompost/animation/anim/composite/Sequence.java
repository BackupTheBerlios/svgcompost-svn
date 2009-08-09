package de.berlios.svgcompost.animation.anim.composite;

import de.berlios.svgcompost.animation.anim.Anim;

public class Sequence extends CompositeAnim {

//	private static Logger log = Logger.getLogger(Sequence.class);

	protected double[] durationAtStart;
	protected double[] durationAtEnd;
	protected int count;
	protected double currentTime;
	protected int currentIndex;
	
	@Override
	public void prepare() {
		count = 0;
		if( anims == null )
			return;
		( (Anim) anims.get(0) ).prepare();
		if( ! calcedInnerDuration )
			calcInnerDuration();
	}
	
	@Override
	protected void animate(double percentage) {
		if( anims == null )
			return;
		int prevIndex = currentIndex;
		calcCurrentIndex( percentage );
		
		Anim anim = (Anim) anims.get( currentIndex );
		initTowardsAnim( prevIndex, currentIndex );
		anim.animateAtTime( (currentTime - durationAtStart[currentIndex]) / anim.getDurationinMillis() );
	}
	
	protected void calcCurrentIndex( double percentage ) {
		if( anims == null )
			return;
		currentTime = percentage * innerDuration;
		currentIndex = -1;
		for( int i=0; i<anims.size(); i++ ) {
			if( currentTime >= durationAtStart[i] && currentTime <= durationAtEnd[i] ) {
				currentIndex = i;
				break;
			}
		}
	}
	
	protected void initTowardsAnim( int fromIndex, int toIndex ) {
		if( fromIndex >= toIndex )
			return;
		for (int i = fromIndex; i < toIndex; i++) {
			anims.get(i).end();
			anims.get(i+1).prepare();
		}
	}
	
	@Override
	public void calcInnerDuration() {
		innerDuration = 0;
		durationAtStart = new double[anims.size()];
		durationAtEnd = new double[anims.size()];
		for( int i = 0; i < anims.size(); i++ ) {
			Anim anim = (Anim) anims.get(i);
			durationAtStart[i] = innerDuration;
			innerDuration += anim.getDurationinMillis();
			durationAtEnd[i] = innerDuration;
		}
//		if( log.isDebugEnabled() )
//			log.debug( "calculated inner duration: "+innerDuration );
		if( inferDuration )
			duration = innerDuration;
		calcedInnerDuration = true;
	}
	
	@Override
	public void end() {
		initTowardsAnim( currentIndex, anims.size()-1 );
		anims.get( anims.size()-1 ).end();
		super.end();
	}
}
