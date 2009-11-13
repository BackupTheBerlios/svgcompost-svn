package de.berlios.svgcompost.copy;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.w3c.dom.Element;

import de.berlios.svgcompost.part.EditablePart;

public class CopyAction extends SelectionAction {

	public CopyAction( IWorkbenchPart part ) {
		super(part);
		setLazyEnablementCalculation(true);
	}
	
	@Override
	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setText("Copy");
		setId(ActionFactory.COPY.getId());
		setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		setEnabled(false);
	}
	
	private Command createCopyCommand( List<Object> selectedObjects ) {
		if( selectedObjects == null || selectedObjects.isEmpty() ) {
			return null;
		}
		CopyCommand cmd = new CopyCommand();
		Iterator<Object> it = selectedObjects.iterator();
		while( it.hasNext() ) {
			Object nextSelected = it.next();
			if( ! (nextSelected instanceof EditablePart) )
				continue;
			EditablePart ep = (EditablePart) nextSelected;
			if( !cmd.isCopyable(ep.getModel()) )
				return null;
			cmd.addElement((Element)ep.getModel());
		}
		return cmd;
	}
	
	@Override
	protected boolean calculateEnabled() {
		Command cmd = createCopyCommand( getSelectedObjects() );
		if( cmd == null )
			return false;
		return cmd.canExecute();
	}
	
	@Override
	public void run() {
		Command cmd = createCopyCommand( getSelectedObjects() );
		if( cmd != null && cmd.canExecute() ) {
			cmd.execute();
		}
	}

}
