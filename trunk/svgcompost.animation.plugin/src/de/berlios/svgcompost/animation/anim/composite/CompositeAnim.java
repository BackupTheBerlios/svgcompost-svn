package de.berlios.svgcompost.animation.anim.composite;

import java.util.ArrayList;

import de.berlios.svgcompost.animation.anim.Anim;

public abstract class CompositeAnim extends Anim {

	protected ArrayList<Anim> anims;
	
	/**
	 * Overrides the child anim's durations, stretching them to the length of the parent CompositeAnim.
	 */
	protected boolean overrideDurations = true;
	
	protected void setOverrideInnerDurations( boolean overrideDurations ) {
		this.overrideDurations = overrideDurations;
	}
	
	/**
	 * The real duration which is calculated from the durations of all child anims.
	 * The CompositeAnim may be given a different duration, which overrides the inner duration.
	 */
	protected double innerDuration = 0;
	protected boolean calcedInnerDuration = false;
	
	protected boolean inferDuration = true;
	
	public CompositeAnim addAnim( Anim anim ) {
		if( anims == null )
			anims = new ArrayList<Anim>();
		anims.add( anim );
		return this;
	}
	
//	public CompositeAnim runMulti( Anim[] animArray ) {
//		if( anims == null )
//			anims = new ArrayList<Anim>();
//		for( int i=0; i<animArray.length; i++ )
//			anims.add( animArray[i] );
//		return this;
//	}
	
	public int getSize() {
		if( anims == null )
			return 0;
		return anims.size();
	}
	
	protected abstract void calcInnerDuration();
	
	@Override
	public void setScene( Scene scene ) {
		super.setScene( scene );
		if( anims == null )
			return;
		for( int i=0; i<anims.size(); i++ )
			((Anim)anims.get(i)).setScene( scene );
	}
		
}
