package de.berlios.svgcompost.animation.anim.chara.skeleton;

public interface Limb {

	/**
	 * Reads the point where the center of the jointedLimb target appears on
	 * the 2nd jointedLimb part in the specified keyframe.
	 * The position is destroyed once the regular tweening begins, so it has to be read in advance.
	 * @param keyframeLink
	 */
	public abstract void readRotationPoint(SkeletonKey keyframeLink);

	/**
	 * 
	 * @param tweeningKeyLink The first of the two keys that are tweened.
	 * @param activeKeyLink The key (first or second) that is changed to create the tweens.
	 * @param percentage The percentage of tweening.
	 */
	public abstract void tween(SkeletonKey tweeningKeyLink,
			SkeletonKey activeKeyLink, double percentage);

	public abstract Bone getTarget();

	public abstract void setSkeleton(Skeleton skeleton);

}