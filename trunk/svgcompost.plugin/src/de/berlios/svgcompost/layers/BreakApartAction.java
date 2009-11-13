package de.berlios.svgcompost.layers;

import java.util.List;

import org.apache.batik.bridge.BridgeContext;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.w3c.dom.Element;

import de.berlios.svgcompost.part.EditablePart;

public class BreakApartAction extends SelectionAction {

	public static String BREAK_APART = "Action.BreakApart";

	public BreakApartAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		setText("Break Apart");
		setId(BREAK_APART);
		setEnabled(false);
	}
	
	private Command createCommand( List<Object> selectedObjects ) {
		if( selectedObjects == null || selectedObjects.isEmpty() ) {
			return null;
		}
		if( ! (selectedObjects.get(0) instanceof EditablePart ) ) {
			return null;
		}
		Element element = (Element) ((EditablePart)selectedObjects.get(0)).getModel();
		BridgeContext ctx = (BridgeContext) ((EditablePart)selectedObjects.get(0)).getBridgeContext();
		if( element.getNodeName().equals("g") )
			return new BreakApartGElementCommand( element, ctx );
		if( element.getNodeName().equals("use") )
			return new BreakApartUseElementCommand( element, ctx );
		return null;
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
