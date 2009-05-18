package de.berlios.svgcompost.model;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.ext.awt.g2d.TransformStackElement;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGTransform;
import org.eclipse.draw2d.geometry.Dimension;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.berlios.svgcompost.freetransform.FreeTransformHelper;

public class SVGNode {

	public static final String TRANSFORM = "Element.Transform";
	public static final String INSERT = "Element.Insert";
	public static final String REMOVE = "Element.Remove";

	private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		changeSupport.addPropertyChangeListener(listener);
	}
	
	public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		changeSupport.removePropertyChangeListener(listener);
	}
	
	protected void firePropertyChange(String property, Object oldValue, Object newValue) {
		if (changeSupport.hasListeners(property)) {
			changeSupport.firePropertyChange(property, oldValue, newValue);
		}
	}
	
	private List<SVGNode> editableElements = new ArrayList<SVGNode>();
	private Element element;
	private BridgeContext ctx;
	
	private SVGNode parent;

	public SVGNode getParent() {
		return parent;
	}
	public void setParent(SVGNode parent) {
		this.parent = parent;
	}

	public Element getElement() {
		return element;
	}
	public void setElement(Element element) {
		this.element = element;
	}

	public BridgeContext getBridgeContext() {
		return ctx;
	}

	public GraphicsNode getGraphicsNode() {
		return ctx.getGraphicsNode(element);
	}

	public SVGNode(Element element, BridgeContext ctx) {
		super();
		this.element = element;
		this.ctx = ctx;
		NodeList list = element.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if( list.item(i) instanceof Element ) {
				GraphicsNode gNode = ctx.getGraphicsNode((Element)list.item(i));
				// We don't want to edit empty elements, they cause lots of crashes.
				if( gNode != null && gNode.getBounds() != null ) {
					editableElements.add( new SVGNode( (Element) list.item(i), ctx, this ) );
				}
			}
		}
	}
		
	public SVGNode(Element element, BridgeContext ctx, SVGNode parent) {
		this(element, ctx);
		setParent(parent);
	}
	
	public void applySvgValues() {
		GraphicsNode gNode = ctx.getGraphicsNode(element);
		if( gNode == null )
			return;
	}

	public Dimension getSize() {
		Rectangle2D bounds = FreeTransformHelper.getGlobalBounds(getGraphicsNode());
		return new Dimension( (int)bounds.getWidth(), (int)bounds.getHeight() );
	}

	public void setTransform(AffineTransform transform) {
		AffineTransform oldTransform = getTransform();
		SVGGeneratorContext genCtx = SVGGeneratorContext.createDefault(ctx.getDocument());
		SVGTransform converter = new SVGTransform(genCtx);
		String transformAttributeValue = converter.toSVGTransform(new TransformStackElement[]{TransformStackElement.createGeneralTransformElement(transform)});
		System.out.println( "transformAttributeValue = "+transformAttributeValue );
		
		System.out.println( element.getAttribute("id")+".transform = "+element.getAttribute("transform") );

		element.setAttribute("transform", transformAttributeValue);
		
		System.out.println( element.getAttribute("id")+".transform = "+element.getAttribute("transform") );
		
		// FIXME: bounds don't change.
		// Solution: only the global bounds change.

		firePropertyChange(TRANSFORM, oldTransform, transform);
	}

	public AffineTransform getTransform() {
		return getGraphicsNode().getTransform();
	}

	public void addChild(int index, SVGNode child) {
		if( index >= editableElements.size() )
			element.appendChild(child.getElement());
		else
			element.insertBefore(child.getElement(), editableElements.get(index).getElement());
		editableElements.add(index, child);
		child.setParent(this);
		firePropertyChange(INSERT, null, child);
	}

	public void addChild(SVGNode child) {
		editableElements.add(child);
		element.appendChild(child.getElement());
		child.setParent(this);
		firePropertyChange(INSERT, null, child);
	}

	public int removeChild(SVGNode child) {
		int index = editableElements.indexOf(child);
		if(index != -1) {
			editableElements.remove(index);
			element.removeChild(child.getElement());
			// TODO: set parent to null once a target for pasting clone nodes can be determined from context
//			editableElements.get(index).setParent(null);
			firePropertyChange(REMOVE, child, null);
		}
		return index;
	}

	public List<SVGNode> getChildElements() {
		return editableElements;
	}
}
