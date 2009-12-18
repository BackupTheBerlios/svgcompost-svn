package de.berlios.svgcompost.provider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.svg.SVGOMDocument;
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

import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.part.EditEvent;
import de.berlios.svgcompost.util.LinkHelper;


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
			Element editRoot = bg.getEditRoot();
			Document doc = editRoot.getOwnerDocument();
			Element newUseElement = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, "use");
			URI referencedUri;
			try {
				referencedUri = new URI( (String)event.data );
//				URI referencingUri = new URI( ((Document)editRoot.getElement().getOwnerDocument()).getBaseURI() );
				URI referencingUri = new URI( "file://"+((SVGOMDocument)editRoot.getOwnerDocument()).getURLObject().getPath() );
//				URI relativeUri = referencedUri.relativize(referencingUri);
				String relativeUri = LinkHelper.createRelativePath( referencingUri.toString(), referencedUri.toString() );
				System.out.println("referencedUri = "+referencedUri);
				System.out.println("referencingUri = "+referencingUri);
				System.out.println("relativeUri = "+relativeUri);
				newUseElement.setAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, "xlink:href", relativeUri.toString());
				editRoot.appendChild(newUseElement);
				// TODO: use a Command
				((AbstractElement)editRoot).dispatchEvent(new EditEvent(this, EditEvent.CHANGE_ORDER, null, newUseElement));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}


}
