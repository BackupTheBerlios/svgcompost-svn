package de.berlios.svgcompost.freetransform;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.util.SVGConstants;
import org.eclipse.gef.commands.Command;
import org.w3c.dom.Element;

import de.berlios.svgcompost.part.EditEvent;
import de.berlios.svgcompost.util.ElementTraversalHelper;

public class SetOriginCommand extends Command {

	protected Element element;
	protected List<Element> children;
	protected String elementName;
	protected Point2D.Float point;
	protected Point2D.Float inversePoint;
	private BridgeContext ctx;
	
	public SetOriginCommand(Element element, Point2D.Float point, BridgeContext ctx) {
		super();
		this.element = element;
		this.point = point;
		this.ctx = ctx;
		children = ElementTraversalHelper.getChildElements( element );
		inversePoint = new Point2D.Float( -point.x, -point.y );
		elementName = element.getLocalName();
	}

	@Override
	public boolean canExecute() {
		boolean canExecute = elementName.equals(SVGConstants.SVG_PATH_TAG) || elementName.equals(SVGConstants.SVG_G_TAG);
		return canExecute;
	}

	@Override
	public void execute() {
		if( canExecute() ) {
			if( elementName.equals(SVGConstants.SVG_PATH_TAG) )
				setPathOrigin(point);
			else if( elementName.equals(SVGConstants.SVG_G_TAG) )
				setGroupOrigin(point);
			((AbstractElement)element).dispatchEvent(new EditEvent(this, EditEvent.TRANSFORM, element, element));
		}
	}

	protected void setGroupOrigin(Point2D.Float point2) {
		// Shift the group to the new origin.
		AffineTransform transform;
		transform = ElementTraversalHelper.getTransform(element);
		transform.translate(point2.x, point2.y);
		ElementTraversalHelper.setTransform(element, transform, ctx);
		// Shift all children in the opposite direction. 
		for (Element child : children) {
			if( child instanceof SVGGraphicsElement ) {
				transform = ElementTraversalHelper.getTransform(child);
				transform.translate(-point2.x, -point2.y);
				ElementTraversalHelper.setTransform(child, transform, ctx);
			}
		}
	}
	
	protected void setPathOrigin(Point2D.Float point2) {
		// Shift the path to the new origin.
		AffineTransform transform;
		transform = ElementTraversalHelper.getTransform(element);
		transform.translate(point2.x, point2.y);
		ElementTraversalHelper.setTransform(element, transform, ctx);
		// Shift its shape the opposite direction. 
		ShapeNode gNode = (ShapeNode) ctx.getGraphicsNode(element);
		transform = AffineTransform.getTranslateInstance(-point2.x, -point2.y);
		Shape newPath = new ExtendedGeneralPath( gNode.getShape() ).createTransformedShape(transform);
		gNode.setShape(newPath);
	}

	@Override public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		if( elementName.equals(SVGConstants.SVG_PATH_TAG) )
			setPathOrigin(inversePoint);
		else if( elementName.equals(SVGConstants.SVG_G_TAG) )
			setGroupOrigin(inversePoint);
		((AbstractElement)element).dispatchEvent(new EditEvent(this, EditEvent.TRANSFORM, element, element));
	}
}
