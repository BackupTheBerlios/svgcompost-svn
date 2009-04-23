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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;

import org.apache.batik.gvt.GraphicsNode;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.requests.ChangeBoundsRequest;


/**
 * Helper class which provides static functions to calculate transforms out of the editor's request data.
 * @author Gerrit Karius
 *
 */
public class FreeTransformHelper {
	
	public static final String FREE_TRANSFORM = "free transform";
	public static final String ROTATE_SKEW_MODE = "rotate skew mode";

	/**
	 * Calculates an AffineTransform which, if applied to the given Figure's
	 * current bounds rectangle, will map the point specified by the
	 * resize direction (e.g. NORTH_EAST means the upper right corner)
	 * to the current mouse position saved in the Request.
	 * The rotate skew mode specifies which kind of transformation will be created.
	 * @param request
	 * @param bounds
	 * @param rotateSkewMode
	 * @return
	 */
	public static AffineTransform createFreeTransform(ChangeBoundsRequest request, Rectangle2D bounds, boolean rotateSkewMode) {
		int direction = request.getResizeDirection();
		
		AffineTransform transform;
		
		// Test for simple drag.
		if( direction == PositionConstants.NONE ) {
			transform = getDragTransform(request, bounds);
			return transform;
		}
		
		// Calc delta from center of figure to current mouse location.
		Rectangle2D startRectangle = bounds;
		Point2D.Double center = getCenter(startRectangle);

		if( ! rotateSkewMode ) {
			Rectangle2D currentRectangle = getTransformedRectangle(request, bounds);
			
			transform = getResizeTransform(direction, startRectangle, currentRectangle);
		}
		else {
			Point2D.Double location = new Point2D.Double( request.getLocation().x, request.getLocation().y );
			Point2D.Double toCurrent = getDifference(location, center);
			switch (direction) {
			
			case PositionConstants.NORTH_EAST:
			case PositionConstants.NORTH_WEST:
			case PositionConstants.SOUTH_EAST:
			case PositionConstants.SOUTH_WEST:
				// Calc delta from center of figure to mouse starting location.
				Point2D.Double toStart = getDifference( getPosition(startRectangle, direction), center );
				// Calc rotation transform.
				transform = getRotationTransform(direction, toStart, toCurrent);
				break;
				
			case PositionConstants.NORTH:
			case PositionConstants.EAST:
			case PositionConstants.SOUTH:
			case PositionConstants.WEST:
				// Calc skew transform.
				transform = getSkewTransform(direction, toCurrent);
				break;
	
			case PositionConstants.NONE:
			default:
				transform = new AffineTransform();
				break;
			}
		}

		// Apply translation to transform.
		// (No need for matrix calculations, since rotate and skew translation is 0.)
		double[] m = new double[4];
		transform.getMatrix(m);
		transform.setTransform(m[0], m[1], m[2], m[3], center.x, center.y);
		// Apply transform to figure.
		return transform;
	}

	private static Double getDifference(Point2D.Double toPoint, Point2D.Double fromPoint) {
		return new Point2D.Double( toPoint.x - fromPoint.x, toPoint.y - fromPoint.y );
	}

	public static Point2D.Double getCenter(Rectangle2D rectangle) {
		return new Point2D.Double( rectangle.getCenterX(), rectangle.getCenterY() );
	}

	public static Rectangle2D getTransformedRectangle(ChangeBoundsRequest request, Rectangle2D rectangle) {
		Point moveDelta = request.getMoveDelta();
		Dimension sizeDelta = request.getSizeDelta();
		Rectangle2D transformedRectangle = new Rectangle2D.Double(
				rectangle.getX() + moveDelta.x,
				rectangle.getY() + moveDelta.y,
				rectangle.getWidth() + sizeDelta.width,
				rectangle.getHeight() + sizeDelta.height
				);
		return transformedRectangle;
	}

	protected static AffineTransform getDragTransform(ChangeBoundsRequest request, Rectangle2D bounds) {
		Point2D.Double currentCenter = getCenter(getTransformedRectangle( request, bounds ));
		System.out.println( "currentCenter = "+currentCenter );
		return AffineTransform.getTranslateInstance(currentCenter.x, currentCenter.y);
	}

	protected static AffineTransform getResizeTransform(int direction, Rectangle2D startRectangle, Rectangle2D currentRectangle) {
		AffineTransform transform = AffineTransform.getScaleInstance(
				(double)currentRectangle.getWidth()/(double)startRectangle.getWidth(),
				(double)currentRectangle.getHeight()/(double)startRectangle.getHeight() );
		return transform;
	}
	
	protected static AffineTransform getRotationTransform(int direction, Point2D.Double toStart, Point2D.Double toCurrent) {
		double angle;
		
		if(toStart.x==0&&toStart.y==0 || toCurrent.x==0&&toCurrent.y==0)
			angle = 0;
		else
			angle = Math.atan2(toStart.x, toStart.y) - Math.atan2(toCurrent.x, toCurrent.y);
		
		return AffineTransform.getRotateInstance(angle);
	}
	
	protected static AffineTransform getSkewTransform(int direction, Point2D.Double delta) {
		double dx = delta.x;
		double dy = delta.y;
		
		double skewX = 0;
		double skewY = 0;
		
		switch (direction) {
					
		case PositionConstants.NORTH:
			if( dy != 0 )
				skewX = dx / dy;
			break;
		case PositionConstants.SOUTH:
			if( dy != 0 )
				skewX = dx / dy;
			break;
		case PositionConstants.EAST:
			if( dx != 0 )
				skewY = dy / dx;
			break;
		case PositionConstants.WEST:
			if( dx != 0 )
				skewY = dy / dx;
			break;

		default:
			break;
		}
		
		return AffineTransform.getShearInstance(skewX, skewY);
	}
	
	public static Point2D.Double getPosition(Rectangle2D rect, int position) {
		switch (position) {
		
		case PositionConstants.NORTH_EAST:
			return new Point2D.Double( rect.getMaxX(), rect.getMinY() );
		case PositionConstants.NORTH_WEST:
			return new Point2D.Double( rect.getMinX(), rect.getMinY() );
		case PositionConstants.SOUTH_EAST:
			return new Point2D.Double( rect.getMaxX(), rect.getMaxY() );
		case PositionConstants.SOUTH_WEST:
			return new Point2D.Double( rect.getMinX(), rect.getMaxY() );
			
		case PositionConstants.NORTH:
			return new Point2D.Double( rect.getCenterX(), rect.getMinY() );
		case PositionConstants.EAST:
			return new Point2D.Double( rect.getMaxX(), rect.getCenterY() );
		case PositionConstants.SOUTH:
			return new Point2D.Double( rect.getCenterX(), rect.getMaxY() );
		case PositionConstants.WEST:
			return new Point2D.Double( rect.getMinX(), rect.getCenterY() );

		case PositionConstants.CENTER:
			return new Point2D.Double( rect.getCenterX(), rect.getCenterY() );

		default:
			return null;
		}
	}
	
	public static Rectangle2D getGlobalBounds(GraphicsNode gNode) {
		if( gNode.getParent() != null )
			return gNode.getTransformedBounds(gNode.getParent().getGlobalTransform());
		else
			return gNode.getBounds();
	}
	
}
