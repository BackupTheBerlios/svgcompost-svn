package de.berlios.svgcompost.animation.anim.chara.skeleton;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Represents a keyframe of a Limb.
 * @author Gerrit Karius
 *
 */
public class LimbKey {

	protected Limb limb;
	protected SkeletonKey skeletonKey;
	protected AffineTransform limbKeyMatrix;
	protected CatmullRomTweener limbTweener;
	
	/**
	 * {rotPointOnTarget, rotPointOnChild, rotPointOnParent}
	 * The rotation point on the target: With an arm, this would be
	 * the wrist, where the lower arm is jointed to the hand.
	 * This is not usually not the center of the target, and unless specified,
	 * it must be calculated dynamically.
	 */
	protected Point2D.Float[] limbPoint;
	
	public LimbKey(Limb limb, SkeletonKey skeletonKey) {
		this.limb = limb;
		this.skeletonKey = skeletonKey;
	}

	public CatmullRomTweener getLimbTweener() {
		return limbTweener;
	}

	public AffineTransform getLimbKeyMatrix() {
		return limbKeyMatrix;
	}

	public void setLimbKeyMatrix(AffineTransform limbKeyMatrix) {
		this.limbKeyMatrix = limbKeyMatrix;
	}

	public Point2D.Float[] getLimbPoint() {
		return limbPoint;
	}

	public void setLimbPoint(Point2D.Float[] limbPoint) {
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
