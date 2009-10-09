package de.berlios.svgcompost.animation.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Layer {
	
	private Timeline timeline;
	
	private List<Keyframe> keyframes = new ArrayList<Keyframe>();

	public Timeline getTimeline() {
		return timeline;
	}
	
	public void setTimeline(Timeline timeline) {
		this.timeline = timeline;
	}
	
	public List<Keyframe> getKeyframes() {
		return keyframes;
	}
	
	public Keyframe getKeyframeAt( double time ) {
		int index = Collections.binarySearch(keyframes, new Keyframe(null,time));
		if( index == -1 )
			return null;
		if( index < - keyframes.size() )
			return null;
		if( index < 0 )
			index = - index - 1;
		return keyframes.get(index);
	}
	
	public int addKeyframe( Keyframe keyframe ) {
		int index = Collections.binarySearch(keyframes, keyframe);
		if( index >= 0 ) {
			while( index < keyframes.size() && keyframes.get(index).getTime() <= keyframe.getTime() )
				index ++;
		}
		else {
			index = - index - 1;
		}
		keyframes.add(index, keyframe);
		return index;
	}
	
	protected void insertKeyframe( int index, Keyframe keyframe ) {
		keyframes.add(index, keyframe);
		if( index > 0 ) {
			keyframe.setPreviousKey( keyframes.get(index-1) );
		}
		if( index < keyframes.size()-1 ) {
			keyframes.get(index+1).setPreviousKey( keyframe );
		}
	}
}
