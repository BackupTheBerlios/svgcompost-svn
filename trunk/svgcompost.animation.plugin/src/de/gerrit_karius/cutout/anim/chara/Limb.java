package de.gerrit_karius.cutout.anim.chara;

import de.gerrit_karius.cutout.anim.skeleton.Bone;

/**
 * Represents a limb, e.g. an arm or a leg.
 * The limb can optionally have a joint in the middle, and an appendage at the end.
 * The term "appendage" here refers to a part appended to the limb, e.g. a hand or foot,
 * as opposed to the entire limb.
 * @author gerrit
 *
 */
public abstract class Limb {
	public Bone full;
	// TODO: move to subclasses
	public Bone upper;
	public Bone lower;
	public Bone appendage;

	public String name;
	public Bone parent;
	
//	public void attachAppendage( String symbolId, String name ) {
		// TODO: move specific code to subclasses
//		appendage = PartInfo.createAnchoredPart( full, name, symbolId, lower == null ? full : lower, name );
//	}


//	public MovieClip floatingAnchor;
	
//	public boolean snapToFloatingAnchor = false;
//	public boolean alignToFloatingAnchor = true;
	
//	public boolean hasAppendage;
//	public boolean appendageBehindLimb;
//	public boolean hasJoints;
//	public boolean stretches;
//	public boolean skews;
//	public boolean bendsClockwise = true;

	public double fullLength() {
//		if( hasJoints )
//			return upperLength() + lowerLength();
//		else
//			return appendage.getAnchor()._y;
		return 0;
	}
//	public double upperLength() {
//		return lower.getAnchor()._y;
//	}
//	public double lowerLength() {
//		return appendage.getAnchor()._y;
//	}
//	public MovieClip getMc() {
//		return full.mc;
//	}
	
//	/**
//	 * Creates a floating anchor on a specified MovieClip.
//	 * On subsequent calls to snapToAnchors(), the limb will snap its appendage to this anchor.
//	 * @param parent parent MovieClip for the anchor.
//	 */
//	public void createAnchor( MovieClip parent ) {
//		int depth = parent.getNextHighestDepth();
//		floatingAnchor = parent.createEmptyMovieClip( "anchor"+depth, depth );
//		snapToFloatingAnchor = true;
//	}
//	
//	public void destroyAnchor() {
//		if( floatingAnchor != null ) {
//			floatingAnchor.removeMovieClip();
//			floatingAnchor = null;
//		}
//	}
	
	public abstract void instantiate();
	
	/**
	 * Snaps all limb parts to their respective anchors.
	 * If the floating anchor is used, the limb snaps so that the appendage is fitted to the anchor.
	 */
	public abstract void snapToAnchors();
	
	/**
	 * Rotates the limb parts so that the appendage is placed at a specific point.
	 * @param anchor The MovieClip anchor that the limb snaps to.
	 */
//	public void snapToTarget( MovieClip target ) {
//		snapToPoint( new Point( target._x, target._y ), target._parent );
//	}
	
	/**
	 * Rotates the limb parts so that the appendage is placed at a specific point.
	 * @param p The point that the limb snaps to. 
	 * @param system The coordinate system mc of that point.
	 */
//	public abstract void snapToPoint( Point p, MovieClip system );

}
