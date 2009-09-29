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


}
