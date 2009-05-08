package de.berlios.svgcompost.copy;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import de.berlios.svgcompost.model.BackgroundElement;
import de.berlios.svgcompost.part.BackgroundElementPart;

public class PasteAction extends SelectionAction {

	public PasteAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}
	
	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setText("Paste");
		setId(ActionFactory.PASTE.getId());
		setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		setEnabled(false);
	}
	
	private Command createPasteCommand( List<Object> selectedObjects ) {
		if( selectedObjects == null || selectedObjects.isEmpty() ) {
			return null;
		}
		PasteCommand cmd = new PasteCommand();
		Iterator<Object> it = selectedObjects.iterator();
		while( it.hasNext() ) {
			Object nextSelected = it.next();
			if( ! (nextSelected instanceof BackgroundElementPart) )
				continue;
			BackgroundElementPart be = (BackgroundElementPart) nextSelected;
			if( !cmd.canBePastedInto(be.getModel()) )
				return null;
			cmd.setParentElement((BackgroundElement)be.getModel());
		}

		return cmd;
	}
	
	@Override
	protected boolean calculateEnabled() {
		Command command = createPasteCommand( getSelectedObjects() );
		return command != null && command.canExecute();
	}
	
	@Override
	public void run() {
		Command command = createPasteCommand( getSelectedObjects() );
		if( command != null && command.canExecute() )
			execute(command);
	}
}
