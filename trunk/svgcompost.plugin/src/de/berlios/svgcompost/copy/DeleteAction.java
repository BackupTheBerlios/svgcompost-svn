package de.berlios.svgcompost.copy;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import de.berlios.svgcompost.model.SVGNode;
import de.berlios.svgcompost.part.EditablePart;

public class DeleteAction extends SelectionAction {

	public DeleteAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}
	
	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setText("Delete");
		setId(ActionFactory.DELETE.getId());
		setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		setEnabled(false);
	}
	
	private Command createDeleteCommand( List<Object> selectedObjects ) {
		if( selectedObjects == null || selectedObjects.isEmpty() ) {
			return null;
		}
		DeleteCommand cmd = new DeleteCommand();
		Iterator<Object> it = selectedObjects.iterator();
		while( it.hasNext() ) {
			Object nextSelected = it.next();
			if( nextSelected instanceof EditablePart ) {
				EditablePart part = (EditablePart) nextSelected;
				SVGNode node = (SVGNode) part.getModel();
				cmd.addNode( node );
			}
		}

		return cmd;
	}
	
	@Override
	protected boolean calculateEnabled() {
		Command command = createDeleteCommand( getSelectedObjects() );
		return command != null && command.canExecute();
	}
	
	@Override
	public void run() {
		Command command = createDeleteCommand( getSelectedObjects() );
		if( command != null && command.canExecute() )
			execute(command);
	}
}
