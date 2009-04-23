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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.handles.ResizableHandleKit;
import org.eclipse.gef.requests.ChangeBoundsRequest;

/**
 * An EditPolicy that implements free transformations in 2 modes:
 * scale / translate and rotate / skew, complete with bounds feedback. 
 * @author Gerrit Karius
 *
 */
public class FreeTransformEditPolicy extends ResizableEditPolicy {

	@Override
	protected IFigure createDragSourceFeedbackFigure() {
		Rectangle bounds = getInitialFeedbackBounds();
		int width = bounds.width / 2;
		int height = bounds.height / 2;

		TransformablePolygon p = new TransformablePolygon();

		PointList points = new PointList();
		points.addPoint(-width,-height);
		points.addPoint(width,-height);
		points.addPoint(width,height);
		points.addPoint(-width,height);
		p.setPoints(points);

		FigureUtilities.makeGhostShape(p);
		p.setLineStyle(Graphics.LINE_DOT);
		p.setForegroundColor(ColorConstants.white);

		addFeedback(p);
		return p;
	}
	
	@Override
	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
				
		// Test if figure accepts transforms.
		IFigure feedback = getDragSourceFeedbackFigure();
		if( feedback instanceof TransformablePolygon && request.getExtendedData().containsKey(FreeTransformHelper.FREE_TRANSFORM) ) {
			TransformablePolygon polygon = (TransformablePolygon) feedback;
			polygon.setTransform((AffineTransform)request.getExtendedData().get(FreeTransformHelper.FREE_TRANSFORM));
		}
		else {
			super.showChangeBoundsFeedback(request);
		}
	}

	protected boolean rotateSkewMode = false;
	
	public void setRotateSkewMode(boolean rotate) {
		rotateSkewMode = rotate;
		if( handles != null )
			for( Object handle : handles ) {
				if( handle instanceof FreeTransformHandle ) {
					FreeTransformHandle rotateSkewHandle = (FreeTransformHandle) handle;
					rotateSkewHandle.setRotateSkewMode(rotate);
					rotateSkewHandle.repaint();
				}
			}
	}
	
	public boolean getRotateSkewMode() {
		return rotateSkewMode;
	}

	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> list = new ArrayList<Handle>();	
		GraphicalEditPart part = (GraphicalEditPart) getHost();
		ResizableHandleKit.addMoveHandle(part, list);
		list.add(new FreeTransformHandle(part, PositionConstants.NORTH));
		list.add(new FreeTransformHandle(part, PositionConstants.NORTH_EAST));
		list.add(new FreeTransformHandle(part, PositionConstants.EAST));
		list.add(new FreeTransformHandle(part, PositionConstants.SOUTH_EAST));
		list.add(new FreeTransformHandle(part, PositionConstants.SOUTH));
		list.add(new FreeTransformHandle(part, PositionConstants.SOUTH_WEST));
		list.add(new FreeTransformHandle(part, PositionConstants.WEST));
		list.add(new FreeTransformHandle(part, PositionConstants.NORTH_WEST));
		return list;
	}
		
}
