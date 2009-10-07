package de.berlios.svgcompost.animation.timeline;

import java.util.ArrayList;
import java.util.List;

import de.berlios.svgcompost.animation.canvas.Canvas;

public class Timeline {
	
	private Canvas canvas;
	
	private List<Layer> layers = new ArrayList<Layer>();
	
	public List<Layer> getLayers() {
		return layers;
	}
	
	public void addLayer( Layer layer ) {
		layers.add(layer);
		layer.setTimeline(this);
	}
	
	public void setCanvas( Canvas canvas ) {
		this.canvas = canvas;
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
}
