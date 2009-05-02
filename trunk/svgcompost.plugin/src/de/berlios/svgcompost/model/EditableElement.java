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

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.ext.awt.g2d.TransformStackElement;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGTransform;
import org.eclipse.draw2d.geometry.Dimension;
import org.w3c.dom.Element;

import de.berlios.svgcompost.freetransform.FreeTransformHelper;



/**
 * A wrapper for SVG elements that are edited as children of some parent node.
 * @author Gerrit Karius
 *
 */
public class EditableElement {

	public static final String TRANSFORM = "Element.Transform";

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
	private BridgeContext ctx;
	
	private BackgroundElement parent;

	public BackgroundElement getParent() {
		return parent;
	}
	public void setParent(BackgroundElement parent) {
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

	public EditableElement(Element element, BridgeContext ctx) {
		super();
		this.ctx = ctx;
		this.element = element;
	}
	
	public EditableElement(Element element, BridgeContext ctx, BackgroundElement parent) {
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

}
