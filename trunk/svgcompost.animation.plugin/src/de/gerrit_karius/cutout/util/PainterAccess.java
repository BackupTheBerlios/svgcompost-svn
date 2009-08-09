package de.gerrit_karius.cutout.util;

import java.awt.Paint;
import java.awt.Stroke;

import org.apache.batik.gvt.CompositeShapePainter;
import org.apache.batik.gvt.FillShapePainter;
import org.apache.batik.gvt.ShapePainter;
import org.apache.batik.gvt.StrokeShapePainter;

public class PainterAccess {

	public static Paint getPaint( ShapePainter painter ) {
		Paint paint = null;
		if( painter instanceof CompositeShapePainter ) {
			CompositeShapePainter comp = (CompositeShapePainter) painter;
			for(int i=0; i<comp.getShapePainterCount(); i++) {
				paint = getPaint( comp.getShapePainter(i) );
				if( paint != null )
					return paint;
			}
		}
		else if( painter instanceof FillShapePainter ) {
			// FIXME: getPaint() doesn't exist yet in Batik 1.6?
			return ((FillShapePainter)painter).getPaint();
		}
		return null;
	}

	public static Stroke getStroke( ShapePainter painter ) {
		Stroke stroke = null;
		if( painter instanceof CompositeShapePainter ) {
			CompositeShapePainter comp = (CompositeShapePainter) painter;
			for(int i=0; i<comp.getShapePainterCount(); i++) {
				stroke = getStroke( comp.getShapePainter(i) );
				if( stroke != null )
					return stroke;
			}
		}
		else if( painter instanceof StrokeShapePainter ) {
			// FIXME: getPaint() doesn't exist yet in Batik 1.6?
			return ((StrokeShapePainter)painter).getStroke();
		}
		return null;
	}


}
