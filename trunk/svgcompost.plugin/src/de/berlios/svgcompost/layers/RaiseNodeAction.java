package de.berlios.svgcompost.layers;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Element;

import de.berlios.svgcompost.part.EditablePart;
import de.berlios.svgcompost.plugin.SVGCompostPlugin;

public class RaiseNodeAction extends SelectionAction {
	
	public static String RAISE_NODE = "SVGCompost.RaiseNode";

	public RaiseNodeAction( IWorkbenchPart part ) {
		super(part);
		setLazyEnablementCalculation(true);
	}
	
	@Override
	protected void init() {
		super.init();
		setText("Raise Node");
		setId(RAISE_NODE);
		setHoverImageDescriptor(ImageDescriptor.createFromURL(SVGCompostPlugin.getDefault().getBundle().getResource("icons/up.gif")));
		setImageDescriptor(ImageDescriptor.createFromURL(SVGCompostPlugin.getDefault().getBundle().getResource("icons/up.gif")));
		setDisabledImageDescriptor(ImageDescriptor.createFromURL(SVGCompostPlugin.getDefault().getBundle().getResource("icons/up.gif")));
		setEnabled(false);
	}
	
	protected int getDirection() {
		return +1;
	}
	
	private Command createCommand( List<Object> selectedObjects ) {
		if( selectedObjects == null || selectedObjects.isEmpty() ) {
			return null;
		}
		if( ! (selectedObjects.get(0) instanceof EditablePart ) ) {
			return null;
		}
		ChangeNodeOrderCommand cmd = new ChangeNodeOrderCommand( (Element) ((EditablePart)selectedObjects.get(0)).getModel(), getDirection() );
		return cmd;
	}
	
	@Override
	protected boolean calculateEnabled() {
		Command cmd = createCommand( getSelectedObjects() );
		if( cmd == null )
			return false;
		return cmd.canExecute();
	}
	
	@Override
	public void run() {
		Command cmd = createCommand( getSelectedObjects() );
		if( cmd != null && cmd.canExecute() ) {
			execute(cmd);
		}
	}

}
