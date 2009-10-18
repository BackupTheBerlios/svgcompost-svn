package de.berlios.svgcompost.provider;

import java.util.List;

import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.util.SVGConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.berlios.svgcompost.model.SVGNode;
import de.berlios.svgcompost.part.BackgroundPart;


public class SVGDropTargetListener implements TransferDropTargetListener {

	private GraphicalViewer viewer;
	
	private static Transfer transfer = TextTransfer.getInstance();

	public SVGDropTargetListener(GraphicalViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public Transfer getTransfer() {
		return transfer;
	}

	@Override
	public boolean isEnabled(DropTargetEvent event) {
		return true;
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragLeave(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragOperationChanged(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragOver(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drop(DropTargetEvent event) {
		List<EditPart> children = viewer.getRootEditPart().getChildren();
		if( children.size() > 0 && children.get(0) instanceof BackgroundPart ) {
			BackgroundPart bg = (BackgroundPart) children.get(0);
			SVGNode editRoot = bg.getEditRoot();
			Document doc = editRoot.getElement().getOwnerDocument();
			Element newUseElement = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, "use");
			newUseElement.setAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "href", (String)event.data);
			SVGNode newUseNode = new SVGNode( newUseElement, editRoot );
			editRoot.addChild(newUseNode);
		}
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}


}
