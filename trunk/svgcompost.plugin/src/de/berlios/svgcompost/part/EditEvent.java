package de.berlios.svgcompost.part;

import org.apache.batik.dom.events.AbstractEvent;

public class EditEvent extends AbstractEvent {

	public static final String TRANSFORM = "Element.Transform";
	public static final String INSERT = "Element.Insert";
	public static final String REMOVE = "Element.Remove";
	public static final String CHANGE_ORDER = "Element.Move";
	public static final String XML_ATTRIBUTE = "Element.Attribute";

	private Object source;
	private Object oldValue;
	private Object newValue;

	public EditEvent( Object source, String type, Object oldValue, Object newValue ) {
		initEvent(type, true, false);
        this.source = source;
        this.oldValue = oldValue;
        this.newValue = newValue;
	}
	
    public Object getSource() {
        return source;
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
    public Object getOldValue() {
        return oldValue;
    }
}
