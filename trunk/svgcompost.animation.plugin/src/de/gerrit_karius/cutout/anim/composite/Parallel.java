package de.gerrit_karius.cutout.anim.composite;

import de.gerrit_karius.cutout.anim.Anim;

public class Parallel extends CompositeAnim {
	
	
	@Override
	public void prepare() {
		if( anims == null )
			return;
		// is now explicitely set
		//overrideDurations = durationInput != null;
		for( int i = 0; i < anims.size(); i++ )
			( (Anim) anims.get(i) ).prepare();
		calcInnerDuration();
	}
	
	@Override
	protected void animate(double percentage) {
		if( anims == null )
			return;
		for( int i = 0; i < anims.size(); i++ ) {
			Anim anim = (Anim) anims.get(i);
			if( overrideDurations ) {
				// make child anims last for the duration of the parallel
				anim.animateAtTime( percentage );
			}
			else {
				// make all child anims run for their own duration
				anim.animateAtTime( anim.getDurationinMillis() == 0 ? 1 : Math.min( 1, percentage * innerDuration / anim.getDurationinMillis() ) );
			}
		}
	}
	
	public void calcInnerDuration() {
		innerDuration = 0;
		for( int i = 0; i < anims.size(); i++ )
			innerDuration = Math.max( innerDuration, ( (Anim) anims.get(i) ).getDurationinMillis() );
		if( inferDuration )
			duration = innerDuration;
		calcedInnerDuration = true;
	}

	@Override
	public void end() {
		for( Anim anim : anims )
			anim.end();
		super.end();
	}
}
