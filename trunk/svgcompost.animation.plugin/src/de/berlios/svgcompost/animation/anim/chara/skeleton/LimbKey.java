package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.awt.geom.Point2D;

/**
 * Represents a keyframe of a Limb.
 * @author Gerrit Karius
 *
 */
public class LimbKey {

	protected Limb limb;
	protected SkeletonKey skeletonKey;
	
	protected Point2D.Float limbPoint;
	
	public LimbKey(Limb limb, SkeletonKey skeletonKey) {
		this.limb = limb;
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
		return nextSkeletonKey==null?null:nextSkeletonKey.getLimbKey(limb);
	}
	
	public LimbKey previousKey() {
		SkeletonKey previousSkeletonKey = skeletonKey.previousKey();
		return previousSkeletonKey==null?null:previousSkeletonKey.getLimbKey(limb);
	}

}
