package de.berlios.svgcompost.animation.timeline;

import java.util.HashMap;
import java.util.Map;

import de.berlios.svgcompost.animation.anim.chara.skeleton.Skeleton;
import de.berlios.svgcompost.animation.anim.chara.skeleton.SkeletonKey;
import de.berlios.svgcompost.animation.canvas.CanvasNode;

public class Keyframe /*implements Comparable<Keyframe>*/ {
	
	protected CanvasNode node;
	protected double time;

	public Keyframe(CanvasNode node, double time) {
		this.time = time;
		this.node = node;
	}
	
	public Keyframe(CanvasNode node) {
		this.node = node;
	}
	
//	protected double durationToNextFrame;
//	
//	public double getDuration() {
//		return durationToNextFrame;
//	}
//
//	public double getTime() {
//		return time;
//	}
//
//	@Override
//	public int compareTo(Keyframe keyframe) {
//		if( keyframe.getTime() < time )
//			return -1;
//		if( keyframe.getTime() > time )
//			return 1;
//		return 0;
//	}
	
	public CanvasNode getNode() {
		return node;
	}

	protected Map<Skeleton,SkeletonKey> skeletonKeys = new HashMap<Skeleton,SkeletonKey>();
	
	public SkeletonKey getSkeletonKey( Skeleton forSkeleton ) {
		return skeletonKeys.get(forSkeleton);
	}
	
	public Map<Skeleton,SkeletonKey> getSkeletonKeys() {
		return skeletonKeys;
	}
	
	public SkeletonKey applySkeleton( Skeleton skeleton, Map<Skeleton, SkeletonKey> previousKeys ) {
		SkeletonKey skeletonKey = new SkeletonKey( skeleton, node );
		skeletonKeys.put( skeleton, skeletonKey );
		if( previousKeys != null && previousKeys.containsKey(skeleton) )
			skeletonKey.setPreviousKey( previousKeys.get(skeleton) );
		return skeletonKey;
	}
	
	protected Keyframe previousKey;
	protected Keyframe nextKey;
	
	public Keyframe nextKey() {
		return nextKey;
	}
	public Keyframe previousKey() {
		return previousKey;
	}
	public void setPreviousKey(Keyframe previousKey) {
		this.previousKey = previousKey;
		if( previousKey != null ) {
			previousKey.nextKey = this;
		}
	}

	public boolean hasNext() {
		return nextKey != null;
	}


}
