package de.berlios.svgcompost.model;

import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.apache.batik.ext.awt.g2d.TransformStackElement;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.TransformListParser;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGTransform;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGRect;

import de.berlios.svgcompost.util.VisibilityHelper;

public class SVGNode  implements IPropertySource {

	public static final String TRANSFORM = "Element.Transform";
	public static final String INSERT = "Element.Insert";
	public static final String REMOVE = "Element.Remove";
	public static final String CHANGE_ORDER = "Element.Move";
	public static final String XML_ATTRIBUTE = "Element.Attribute";

	private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	private TransformListParser parser = new TransformListParser();
	private AWTTransformProducer tp = new AWTTransformProducer();
	
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
				Element childElement = (Element) list.item(i);
				// FIXME: Elements without a GraphicsNode cause the editor to crash in some places.
				// Thus, they can never be edited and remade visible.
				// Therefore, as a workaround, make all elements visible by setting display to inline.
				boolean wasDisplayed = VisibilityHelper.setDisplayValue(childElement,true);
				if( ! wasDisplayed ) {
					VisibilityHelper.setVisibility(childElement, false);
				}
				GraphicsNode gNode = ctx.getGraphicsNode(childElement);
				// We don't want to edit empty elements, they cause lots of crashes.
				if( gNode != null && gNode.getBounds() != null ) {
					editableElements.add( new SVGNode( childElement, this ) );
				}
			}
		}
	}
		
	public SVGNode(Element element, SVGNode parent) {
		this(element, parent.getBridgeContext());
		setParent(parent);
	}
	
	public Dimension getSize() {
		if( element instanceof SVGGraphicsElement ) {
			SVGRect bounds = ((SVGGraphicsElement)element).getBBox();
			if( bounds != null )
				return new Dimension( (int)bounds.getWidth(), (int)bounds.getHeight() );
		}
		return new Dimension();
	}

	public void setTransform(AffineTransform transform) {
		AffineTransform oldTransform = getTransform();
		SVGGeneratorContext genCtx = SVGGeneratorContext.createDefault(ctx.getDocument());
		SVGTransform converter = new SVGTransform(genCtx);
		String transformAttributeValue = converter.toSVGTransform(new TransformStackElement[]{TransformStackElement.createGeneralTransformElement(transform)});

		element.setAttribute("transform", transformAttributeValue);
		
		// FIXME: bounds don't change.
		// Solution: only the global bounds change.

		firePropertyChange(TRANSFORM, oldTransform, transform);
	}

	public AffineTransform getTransform() {
		String value = element.getAttribute("transform");
		if( value == null || value.equals("") )
			return new AffineTransform();
        parser.setTransformListHandler(tp);
        parser.parse(value);
        return tp.getAffineTransform();
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
		if( editableElements.indexOf(child) == -1 )
			editableElements.add(child);
		if( ! child.getElement().getParentNode().equals( element ) )
			element.appendChild(child.getElement());
		if( ! child.getParent().equals( this )  )
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

	public void moveChild(SVGNode child, int oldIndex, int newIndex) {
		// Careful: there might be more (non-visible) XML elements than model nodes.
		editableElements.remove(oldIndex);
		editableElements.add(newIndex, child);
		Element movedElement = child.getElement();
		element.removeChild(movedElement);
		if( newIndex == editableElements.size() -1 )
			element.appendChild(movedElement);
		else
			element.insertBefore(movedElement, editableElements.get(newIndex+1).getElement());
		firePropertyChange(CHANGE_ORDER, oldIndex, newIndex);
	}

	public Object getEditableValue() {
		return this;
	}
	public IPropertyDescriptor[] getPropertyDescriptors() {
		IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[element.getAttributes().getLength()];
		for (int i=0;i<element.getAttributes().getLength();i++) {				
			Attr attribute = (Attr) element.getAttributes().item(i);
			PropertyDescriptor descriptor = new TextPropertyDescriptor(attribute,attribute.getName());
			int delimIndex = attribute.getName().indexOf(':');
			descriptor.setCategory( delimIndex == -1 ? "no namespace" :  attribute.getName().substring(0, delimIndex));
			propertyDescriptors[i] = descriptor;
		}
		return propertyDescriptors;

	}
	public Object getPropertyValue(Object object) {
		Attr attribute = (Attr) object;
		return attribute.getValue();
	}
	public boolean isPropertySet(Object object) {
		Attr attribute = (Attr) object;
		if( attribute.getNamespaceURI() == null )
			return element.hasAttribute(attribute.getName());
		else
			return element.hasAttributeNS(attribute.getNamespaceURI(),attribute.getName());
	}
	public void resetPropertyValue(Object id) {
	}
	public void setPropertyValue(Object object, Object value) {
		Attr attribute = (Attr) object;
		String oldValue = attribute.getValue();
		attribute.setValue( (String) value);
		String newValue = attribute.getValue();
		if( ! oldValue.equals(newValue) )
			firePropertyChange(XML_ATTRIBUTE, oldValue, newValue);
	}
}
