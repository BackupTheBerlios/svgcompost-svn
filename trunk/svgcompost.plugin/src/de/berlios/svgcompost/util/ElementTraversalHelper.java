package de.berlios.svgcompost.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.ext.awt.g2d.TransformStackElement;
import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.TransformListParser;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGTransform;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ElementTraversalHelper {
	
	public static int indexOfNode( Node parent, Node child ) {
		NodeList list = parent.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if( list.item(i).equals( child ) )
				return i;
		}
		return -1;
	}
	
	public static void insertNodeAt( Node parent, Node child, int index ) {
		NodeList list = parent.getChildNodes();
		if( list.getLength() > index )
			throw new IndexOutOfBoundsException("Cannot insert XML node at position "+index+" into parent with "+list.getLength()+" child nodes.");
		else if( list.getLength() == index )
			parent.appendChild(child);
		else
			parent.insertBefore(child, list.item(index));
	}
	
	public static void moveChild(Element movedElement, int newIndex) {
		// Careful: there might be more (non-visible) XML elements than model nodes.
		Node parent = movedElement.getParentNode();
		parent.removeChild(movedElement);
		if( newIndex >= parent.getChildNodes().getLength() -1 )
			parent.appendChild(movedElement);
		else
			parent.insertBefore(movedElement, parent.getChildNodes().item(newIndex));
//		firePropertyChange(CHANGE_ORDER, oldIndex, newIndex);
	}

	public static List<Element> getChildElements( Element parent ) {
		List<Element> elements = new ArrayList<Element>();
		NodeList list = parent.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if( list.item(i) instanceof Element )
				elements.add((Element)list.item(i));
		}
		return elements;
	}
	
	public static void setTransform( Element element, AffineTransform transform, BridgeContext ctx ) {
//		AffineTransform oldTransform = getTransform(element);
		SVGGeneratorContext genCtx = SVGGeneratorContext.createDefault(ctx.getDocument());
		SVGTransform converter = new SVGTransform(genCtx);
		String transformAttributeValue;
		if( transform != null )
			transformAttributeValue = converter.toSVGTransform(new TransformStackElement[]{TransformStackElement.createGeneralTransformElement(transform)});
		else
			transformAttributeValue = "";

		element.setAttribute("transform", transformAttributeValue);
		
		// FIXME: bounds don't change.
		// Solution: only the global bounds change.

//		firePropertyChange(TRANSFORM, oldTransform, transform);
	}
	
	public static void setGlobalTransform( Element element, AffineTransform transform, BridgeContext ctx ) {
		if( element.getParentNode() != null ) {
			transform = (AffineTransform) transform.clone();
			try {
				transform.preConcatenate( ctx.getGraphicsNode((Element) element.getParentNode() ).getGlobalTransform().createInverse() );
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}
		setTransform( element, transform, ctx );
	}

	
	private static TransformListParser parser = new TransformListParser();
	private static AWTTransformProducer tp = new AWTTransformProducer();
	
	public static AffineTransform getTransform(Element element) {
		String value = element.getAttribute("transform");
		if( value == null || value.equals("") )
			return new AffineTransform();
        parser.setTransformListHandler(tp);
        parser.parse(value);
        return tp.getAffineTransform();
	}

}
