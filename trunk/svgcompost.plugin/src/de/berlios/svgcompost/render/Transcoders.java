package de.berlios.svgcompost.render;

import org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.SVGImageConverter;

public class Transcoders {

	private static GVTRenderer gvtRenderer = null;
	private static SVGImageConverter svgImageConverter = null;
	
	public static GVTRenderer getGVTRenderer() {
		if(gvtRenderer == null)
			gvtRenderer = new GVTRenderer();
		return gvtRenderer;
	}
	
	public static SVGImageConverter getSVGImageConverter() {
		if(svgImageConverter == null)
			svgImageConverter = new SVGImageConverter();
		return svgImageConverter;
	}
	
}
