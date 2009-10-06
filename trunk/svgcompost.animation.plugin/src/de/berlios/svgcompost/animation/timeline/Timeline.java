package de.berlios.svgcompost.animation.timeline;

import java.util.ArrayList;
import java.util.List;

public class Timeline {
	
	private List<Layer> layers = new ArrayList<Layer>();
	
	public List<Layer> getLayers() {
		return layers;
	}
	
	public void addLayer( Layer layer ) {
		layers.add(layer);
	}
	
}
