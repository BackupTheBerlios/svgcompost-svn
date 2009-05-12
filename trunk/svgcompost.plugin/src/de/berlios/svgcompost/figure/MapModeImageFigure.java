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

import java.awt.geom.Rectangle2D;

import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeTypes;
import org.eclipse.swt.graphics.Image;

/**
 * An ImageFIgure that implements IMapMode with straight identity mapping.
 * 
 */
public class MapModeImageFigure extends ImageFigure implements IMapMode {
	
	private IMapMode mm;
	protected Rectangle2D awtBounds;
	
	public MapModeImageFigure(Image image) {
		super(image);
		mm = MapModeTypes.IDENTITY_MM;
	}
	
	public void setAwtBounds(Rectangle2D awtBounds) {
		this.awtBounds = awtBounds;
 		setPreferredSize((int)awtBounds.getWidth(), (int)awtBounds.getHeight());
 		setLocation( new Point((int)awtBounds.getX(), (int)awtBounds.getY()) );
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