package de.berlios.svgcompost.animation.canvas;

import java.awt.RenderingHints;

public class LabelKey extends RenderingHints.Key {
	public LabelKey( int key ) {
		super(key);
	}
	public boolean isCompatibleValue( Object object ) {
		return true; //object instanceof String;
	}
}
