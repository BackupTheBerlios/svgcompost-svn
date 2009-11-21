package de.berlios.svgcompost.provider;

import org.apache.batik.dom.AbstractElement;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import de.berlios.svgcompost.part.EditEvent;

public class ElementPropertySource implements IPropertySource {
	
	protected Element element;
	
	public ElementPropertySource(Element element) {
		this.element = element;
	}

	@Override
	public Object getEditableValue() {
		return element;
	}

	@Override
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

	@Override
	public Object getPropertyValue(Object object) {
		Attr attribute = (Attr) object;
		return attribute.getValue();
	}

	@Override
	public boolean isPropertySet(Object object) {
		Attr attribute = (Attr) object;
		if( attribute.getNamespaceURI() == null )
			return element.hasAttribute(attribute.getName());
		else
			return element.hasAttributeNS(attribute.getNamespaceURI(),attribute.getName());
	}

	@Override
	public void resetPropertyValue(Object id) {
	}

	@Override
	public void setPropertyValue(Object object, Object value) {
		Attr attribute = (Attr) object;
		String oldValue = attribute.getValue();
		attribute.setValue( (String) value);
		String newValue = attribute.getValue();
		// TODO: use Command
		if( ! oldValue.equals(newValue) )
			((AbstractElement)element).dispatchEvent(new EditEvent(this, EditEvent.XML_ATTRIBUTE, oldValue, newValue));
	}
}
