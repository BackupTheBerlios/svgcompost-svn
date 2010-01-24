package de.berlios.svgcompost.layers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.w3c.dom.Element;

import de.berlios.svgcompost.part.EditablePart;

public class GroupAction extends SelectionAction {

	public static String GROUP = "SVGCompost.Group";

	public GroupAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
		init();
	}

	@Override
	protected void init() {
		super.init();
		setText("Group");
		setId(GROUP);
		setEnabled(false);
	}
	
	private Command createCommand( List selectedObjects ) {
		if( selectedObjects == null || selectedObjects.isEmpty() ) {
			return null;
		}
		if( ! (selectedObjects.get(0) instanceof EditablePart ) ) {
			return null;
		}
		Element parent = (Element) ((Element) ((EditablePart)selectedObjects.get(0)).getModel()).getParentNode();
		List<Element> selectedElements = new ArrayList<Element>();
		for (Object object : selectedObjects) {
			if( ! (object instanceof EditablePart) )
				return null;
			selectedElements.add((Element)((EditablePart)object).getModel());
		}
		return new GroupCommand(parent, selectedElements);
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
