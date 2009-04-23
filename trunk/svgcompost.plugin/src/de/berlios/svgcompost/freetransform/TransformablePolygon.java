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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;


/**
 * A shape which can be used together with affine transforms.
 * @author Gerrit Karius
 *
 */
public class TransformablePolygon extends Shape {
	
	protected PointList points = new PointList();
	public PointList getPoints() {
		return points;
	}
	
	protected AffineTransform transform = AffineTransform.getRotateInstance(Math.PI/4.0);
	
	protected PointList transformedPoints = new PointList();
	protected Rectangle transformedBounds = new Rectangle();
	
	private Point min = new Point();
	private Point max = new Point();
	private Point2D.Double awtPoint = new Point2D.Double();
	private Point draw2dPoint = new Point();
	
	protected void setTransform(AffineTransform transform) {
		this.transform = transform;
		calcTransformedPoints();
		setBounds(transformedBounds);
	}
	
	protected void calcTransformedPoints() {
		PointList points = getPoints();
		transformedPoints.setSize(points.size());
		for (int i=0; i<points.size(); i++) {
			awtPoint.x = points.getPoint(i).x;
			awtPoint.y = points.getPoint(i).y;
			transform.transform(awtPoint, awtPoint);
			draw2dPoint.x = (int) awtPoint.getX();
			draw2dPoint.y = (int) awtPoint.getY();
			transformedPoints.setPoint(draw2dPoint,i);
		}
		calcBounds(transformedBounds, transformedPoints);
	}
	
	private void calcBounds(Rectangle transformedBounds, PointList points) {
		if( points.size() == 0 ) {
			transformedBounds.setLocation(0, 0);
			transformedBounds.setSize(0, 0);
			return;
		}
		min.setLocation( points.getFirstPoint() );
		max.setLocation( points.getFirstPoint() );
		for (int i=1; i<points.size(); i++) {
			Point point = points.getPoint(i);
			if( point.x < min.x )
				min.x = point.x;
			else if( point.x > max.x )
				max.x = point.x;
			if( point.y < min.y )
				min.y = point.y;
			else if( point.y > max.y )
				max.y = point.y;
		}
		transformedBounds.setLocation(min.x, min.y);
		transformedBounds.setSize(max.x-min.x, max.y-min.y);
		int lineWidth = (getLineWidth()+1) / 2;
		transformedBounds.expand(lineWidth, lineWidth);
	}
	
	public TransformablePolygon() {
	}
	
	public void setPoints(PointList points) {
		this.points = points;
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		graphics.fillPolygon(transformedPoints);
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.drawPolygon(transformedPoints);
	}

}
