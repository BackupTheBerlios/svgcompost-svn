package de.berlios.svgcompost.popup;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.w3c.dom.Element;

import de.berlios.svgcompost.part.EditablePart;

public class AddAttributeAction implements IObjectActionDelegate {

	private Shell shell;
	private EditablePart part;
	
	/**
	 * Constructor for Action1.
	 */
	public AddAttributeAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
//		MessageDialog.openInformation(
//			shell,
//			"SVG Compost Editor",
//			"Add Attribute was executed.");
		if( part == null )
			return;
		InputDialog dialog = new InputDialog(
				shell,
				"Add Attribute",
				"Enter the name of the new attribute",
				null,
				null
		);
		if(dialog.open() == Window.OK) {
			String newName = dialog.getValue();
			Element element = (Element)part.getModel();
			if( ! element.hasAttribute(newName) )
				element.setAttribute(newName, "");
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if( ! (selection instanceof StructuredSelection) )
			return;
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		if( ! (structuredSelection.getFirstElement() instanceof EditablePart ) ) {
			return;
		}
		this.part = (EditablePart) structuredSelection.getFirstElement();
	}

}
