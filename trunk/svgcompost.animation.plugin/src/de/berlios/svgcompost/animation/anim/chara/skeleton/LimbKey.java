package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.awt.geom.Point2D;

/**
 * Represents a keyframe of a JointedLimb.
 * @author Gerrit Karius
 *
 */
public class LimbKey {

	protected Limb jointedLimb;
	protected SkeletonKey skeletonKey;
	
	protected Point2D.Float limbPoint;
	
	public LimbKey(Limb jointedLimb, SkeletonKey skeletonKey) {
		this.jointedLimb = jointedLimb;
		this.skeletonKey = skeletonKey;
	}

	public Point2D.Float getLimbPoint() {
		return limbPoint;
	}

	public void setLimbPoint(Point2D.Float limbPoint) {
		this.limbPoint = limbPoint;
	}

	public LimbKey nextKey() {
		SkeletonKey nextSkeletonKey = skeletonKey.nextKey();
		return nextSkeletonKey==null?null:nextSkeletonKey.getLimbKey(jointedLimb);
	}
	
	public LimbKey previousKey() {
		SkeletonKey previousSkeletonKey = skeletonKey.previousKey();
		return previousSkeletonKey==null?null:previousSkeletonKey.getLimbKey(jointedLimb);
	}

}
