package de.berlios.svgcompost.animation.timeline;

import de.berlios.svgcompost.animation.anim.chara.skeleton.SkeletonKey;
import de.berlios.svgcompost.animation.canvas.CanvasNode;

public class Keyframe implements Comparable<Keyframe> {
	
	protected CanvasNode node;
	protected double time;

	public Keyframe(CanvasNode node, double time) {
		this.time = time;
		this.node = node;
	}
	
	protected double durationToNextFrame;
	
	public double getDuration() {
		return durationToNextFrame;
	}

	public double getTime() {
		return time;
	}

	@Override
	public int compareTo(Keyframe keyframe) {
		if( keyframe.getTime() < time )
			return -1;
		if( keyframe.getTime() > time )
			return 1;
		return 0;
	}
	
	protected SkeletonKey previousKey;
	protected SkeletonKey nextKey;
	
	public SkeletonKey nextKey() {
		return nextKey;
	}
	public SkeletonKey previousKey() {
		return previousKey;
	}

}
