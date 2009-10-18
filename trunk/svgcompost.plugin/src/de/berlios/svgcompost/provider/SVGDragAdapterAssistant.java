package de.berlios.svgcompost.provider;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;
import org.w3c.dom.Element;

public class SVGDragAdapterAssistant extends CommonDragAdapterAssistant {

	Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
	
	public SVGDragAdapterAssistant() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Transfer[] getSupportedTransferTypes() {
		return transferTypes;
	}

	@Override
	public boolean setDragData(DragSourceEvent anEvent,
			IStructuredSelection selection) {
		Object firstElement = selection.getFirstElement();
		if( ! (firstElement instanceof Element) )
				return false;
		Element element = (Element) firstElement;
		if(! element.hasAttribute("id"))
			return false;
		if(element.getBaseURI() == null)
			return false;
		String uri = element.getBaseURI() + "#" + element.getAttribute("id");
		anEvent.data = uri;
		return true;
	}

}
