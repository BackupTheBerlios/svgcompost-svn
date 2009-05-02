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

package de.berlios.svgcompost.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.eclipse.gef.GraphicalViewer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * A wrapper for an SVG element who´se children are currently being edited.
 * @author Gerrit Karius
 *
 */
public class BackgroundElement {

	public static final String INSERT = "Element.Insert";
	public static final String REMOVE = "Element.Remove";

	private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		changeSupport.addPropertyChangeListener(listener);
	}
	
	protected void firePropertyChange(String property, Object oldValue, Object newValue) {
		if (changeSupport.hasListeners(property)) {
			changeSupport.firePropertyChange(property, oldValue, newValue);
		}
	}
	
	private Element element;
	private List<EditableElement> editableElements = new ArrayList<EditableElement>();

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public BackgroundElement(Element element, BridgeContext ctx) {
		super();
		this.element = element;
		NodeList list = element.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if( list.item(i) instanceof Element && ctx.getGraphicsNode((Element)list.item(i)) != null )
				editableElements.add( new EditableElement( (Element) list.item(i), ctx, this ) );
		}
	}
	
	public void addChild(int index, EditableElement child) {
		if( index >= editableElements.size() )
			element.appendChild(child.getElement());
		else
			element.insertBefore(child.getElement(), editableElements.get(index).getElement());
		editableElements.add(index, child);
		child.setParent(this);
		firePropertyChange(INSERT, null, child);
	}

	public void addChild(EditableElement child) {
		editableElements.add(child);
		element.appendChild(child.getElement());
		child.setParent(this);
		firePropertyChange(INSERT, null, child);
	}

	public int removeChild(EditableElement child) {
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

	public List<EditableElement> getChildElements() {
		return editableElements;
	}
}
