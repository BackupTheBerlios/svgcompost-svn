package de.berlios.svgcompost.util;

import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.svg.SVGStylable;

public abstract class VisibilityHelper {
	
	public static final String VISIBILITY = "visibility";

	/**
	 * When editing keyframes, all but one are usually made invisible.
	 * In that case, however, no graphics nodes are constructed by the Batik framework.
	 * In order to access the keyframes, all must be made visible by setting the display attribute
	 * to inline mode. 
	 */
	public static boolean setDisplayToInline( Element element ) {
		boolean wasSetToNone = false;
		if( element instanceof SVGStylable ) {
			CSSStyleDeclaration style = ((SVGStylable)element).getStyle();
			if( style.getPropertyValue("display").equals("none") ) {
				style.setProperty("display", "inline", "");
				wasSetToNone = true;
			}
		}
		if( element.getAttribute("display").equals("none") ) {
			element.setAttribute("display", "inline");
			wasSetToNone = true;
		}
		return wasSetToNone;
	}

	public static boolean setDisplayValue( Element element, boolean display ) {
		String displayValue = display ? "inline" : "none";
		boolean oldValue = true;
		String oldProperty = "";
		if( element instanceof SVGStylable ) {
			CSSStyleDeclaration style = ((SVGStylable)element).getStyle();
			oldProperty = style.getPropertyValue("display");
			if( ! oldProperty.equals("") ) {
				oldValue = oldProperty.equals("none");
				style.setProperty("display", displayValue, "");
			}
		}
		String oldAttribute = element.getAttribute("display");
		if( ! oldAttribute.equals("") || (! display && oldProperty.equals("")) ) {
			oldValue = oldAttribute.equals("none");
			element.setAttribute("display", displayValue);
		}
		return oldValue;
	}

	public static void setVisibility( Element element, boolean visible ) {
		if( element.hasAttribute(VISIBILITY) )
			element.removeAttribute(VISIBILITY);
		if( element instanceof SVGStylable ) {
			CSSStyleDeclaration style = ((SVGStylable)element).getStyle();
			style.removeProperty(VISIBILITY);
			if( ! visible )
				style.setProperty(VISIBILITY, "hidden", "");
		}
		else {
			if( ! visible )
				element.setAttribute(VISIBILITY, "hidden");
		}
	}
	
	public static boolean getVisibility( Element element ) {
		if( element.hasAttribute(VISIBILITY) ) {
			String visibility = element.getAttribute(VISIBILITY);
			return ! (visibility.equals("hidden") || visibility.equals("collapse"));
		}
		if( element instanceof SVGStylable ) {
			CSSStyleDeclaration style = ((SVGStylable)element).getStyle();
			String visibility = style.getPropertyValue(VISIBILITY);
			return ! (visibility.equals("hidden") || visibility.equals("collapse"));
		}
		return true;
	}

}
