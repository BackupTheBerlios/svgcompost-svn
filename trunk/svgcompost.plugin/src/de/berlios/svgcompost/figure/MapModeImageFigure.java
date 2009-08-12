/**
 * Copyright 2009 Gerrit Karius
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.berlios.svgcompost.figure;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeTypes;
import org.eclipse.swt.graphics.Image;

import de.berlios.svgcompost.plugin.SVGCompostConstants;
import de.berlios.svgcompost.plugin.SVGCompostPlugin;

/**
 * An ImageFIgure that implements IMapMode with straight identity mapping.
 * 
 */
public class MapModeImageFigure extends ImageFigure implements IMapMode {
	
	private IMapMode mm;
	protected Rectangle2D awtBounds;
	protected Point2D awtOrigin;
	
	public MapModeImageFigure(Image image) {
		super(image);
		mm = MapModeTypes.IDENTITY_MM;
	}
	
	public void setAwtBounds(Rectangle2D awtBounds) {
		this.awtBounds = awtBounds;
 		setPreferredSize((int)awtBounds.getWidth()+1, (int)awtBounds.getHeight()+1);
 		setLocation( new Point((int)awtBounds.getX(), (int)awtBounds.getY()) );
	}
	
	public void setAwtOrigin(Point2D awtOrigin) {
		this.awtOrigin = awtOrigin;
	}
	
	public Point2D getAwtOrigin() {
		if( awtOrigin == null && bounds != null) {
			Point c = bounds.getCenter();
			return new Point2D.Float( c.x, c.y );
		}
		return awtOrigin;
	}
	
	public Point getDraw2dOrigin() {
		if( awtOrigin == null && bounds != null) {
			return bounds.getCenter();
		}
		return new Point( awtOrigin.getX(), awtOrigin.getY() );
	}
	
	public Dimension getPreferredSize(int wHint, int hHint) {
		if( getImage() != null )
			return super.getPreferredSize(wHint, hHint);

		if (prefSize != null)
			return prefSize;
		if (getLayoutManager() != null) {
			Dimension d = getLayoutManager().getPreferredSize(this, wHint, hHint);
			if (d != null)
				return d;
		}
		return getSize();
	}
	
	public Rectangle2D getAwtBounds() {
		if( awtBounds == null && bounds != null) {
			return new Rectangle2D.Float( bounds.x, bounds.y, bounds.width, bounds.height );
		}
		return awtBounds;
	}
	
	private static int crosshairRadius = 5;
	
	protected void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
		
//		Rectangle originalBounds = getBounds();
//		Rectangle drawingBounds = new Rectangle( originalBounds );
		
		boolean transformCenter = SVGCompostPlugin.getDefault().getPluginPreferences().getBoolean(SVGCompostConstants.FREETRANSFORM_CENTER);
		Point o = getDraw2dOrigin();
		Point c = transformCenter ? bounds.getCenter() : o;
		
//		drawingBounds.union( new Rectangle(o.x-crosshairRadius,o.y-crosshairRadius,crosshairRadius,crosshairRadius) );
//		setBounds( drawingBounds );
		
		graphics.fillOval(c.x-crosshairRadius, c.y-crosshairRadius, crosshairRadius*2, crosshairRadius*2);
		graphics.drawOval(c.x-crosshairRadius, c.y-crosshairRadius, crosshairRadius*2, crosshairRadius*2);
		
		graphics.drawLine(o.x, o.y-crosshairRadius, o.x, o.y+crosshairRadius);
		graphics.drawLine(o.x-crosshairRadius, o.y, o.x+crosshairRadius, o.y);
		
//		setBounds(originalBounds);
	}
	
	public int DPtoLP(int deviceUnit) {
		return mm.DPtoLP(deviceUnit);
	}
	
	public Translatable DPtoLP(Translatable t) {
		return mm.DPtoLP(t);
	}
	
	public int LPtoDP(int logicalUnit) {
		return mm.LPtoDP(logicalUnit);
	}
	
	public Translatable LPtoDP(Translatable t) {
		return mm.LPtoDP(t);
	}
}