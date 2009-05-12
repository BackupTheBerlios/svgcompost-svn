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

package de.berlios.svgcompost.freetransform;

import java.awt.geom.AffineTransform;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.handles.ResizeHandle;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.ResizeTracker;
import org.eclipse.swt.graphics.Cursor;

import de.berlios.svgcompost.figure.MapModeImageFigure;


/**
 * A Handle that implements free transformations in 2 modes.
 * In scale / translate mode the handle is painted as a square,
 * in rotate / skew mode it is painted as a circle.
 * @author Gerrit Karius
 *
 */
public class FreeTransformHandle extends ResizeHandle {
	
	protected boolean rotateSkewMode = false;
	
	public void setRotateSkewMode(boolean rotate) {
		rotateSkewMode = rotate;
		setCursor(Cursors.getDirectionalCursor(getCursorDirection(), getOwner().getFigure().isMirrored()));
	}

	private int direction = 0;

	public FreeTransformHandle(GraphicalEditPart owner, int direction) {
		super(owner, direction);
		this.direction = direction;
	}
	
	@Override
	protected DragTracker createDragTracker() {
		return new ResizeTracker(getOwner(), direction) {
			@Override
			protected Cursor getDefaultCursor() {
				return SharedCursors.getDirectionalCursor(
						getCursorDirection(), getTargetEditPart().getFigure().isMirrored());
			}
			@Override
			protected void updateSourceRequest() {
				super.updateSourceRequest();
				ChangeBoundsRequest request = (ChangeBoundsRequest) getSourceRequest();
				MapModeImageFigure figure = (MapModeImageFigure) getOwner().getFigure();
				AffineTransform transform = FreeTransformHelper.createFreeTransform( request, figure.getAwtBounds(), rotateSkewMode );
//				System.out.println( "requested transform = "+transform );
				request.getExtendedData().put(FreeTransformHelper.FREE_TRANSFORM, transform);
				request.getExtendedData().put(FreeTransformHelper.ROTATE_SKEW_MODE, rotateSkewMode);
			}
		};
	}

	protected int getCursorDirection() {
		return rotateSkewMode ? getSkewedDirection(direction) : direction;
	}

	public static int getSkewedDirection(int direction) {
		switch (direction) {
			case PositionConstants.NORTH :
				return PositionConstants.EAST;
			case PositionConstants.SOUTH:
				return PositionConstants.WEST;
			case PositionConstants.EAST :
				return PositionConstants.SOUTH;
			case PositionConstants.WEST:
				return PositionConstants.NORTH;
			case PositionConstants.SOUTH_EAST:
				return PositionConstants.SOUTH_WEST;
			case PositionConstants.SOUTH_WEST:
				return PositionConstants.NORTH_WEST;
			case PositionConstants.NORTH_EAST:
				return PositionConstants.SOUTH_EAST;
			case PositionConstants.NORTH_WEST:
				return PositionConstants.NORTH_EAST;
			default:
			 	break;
		}
		return 0;
	}

	@Override
	public void paintFigure(Graphics g) {
		if( ! rotateSkewMode ) {
			super.paintFigure(g);
			return;
		}
		Rectangle r = getBounds();
		r.shrink(1, 1);
		try {
			g.setBackgroundColor(getFillColor());
			g.fillOval(r.x, r.y, r.width, r.height);
			g.setForegroundColor(getBorderColor()); 
			g.drawOval(r.x, r.y, r.width, r.height);
		} finally {
			r.expand(1, 1);
		}
	}
	
}
