package de.berlios.svgcompost.animation.timeline;

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

	public CanvasNode getNode() {
		return node;
	}

	@Override
	public int compareTo(Keyframe keyframe) {
		if( keyframe.getTime() < time )
			return -1;
		if( keyframe.getTime() > time )
			return 1;
		return 0;
	}
	
	protected Keyframe previousKey;
	protected Keyframe nextKey;
	
	public Keyframe nextKey() {
		return nextKey;
	}
	public Keyframe previousKey() {
		return previousKey;
	}

}
