package de.berlios.svgcompost.layers;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import de.berlios.svgcompost.plugin.SVGCompostPlugin;

public class LowerNodeAction extends RaiseNodeAction {

	public static String LOWER_NODE = "SVGCompost.LowerNode";

	public LowerNodeAction(IWorkbenchPart part) {
		super(part);
	}
	
	@Override
	protected void init() {
		super.init();
		setText("Lower Node");
		setId(LOWER_NODE);
		setHoverImageDescriptor(ImageDescriptor.createFromURL(SVGCompostPlugin.getDefault().getBundle().getResource("icons/down.gif")));
		setImageDescriptor(ImageDescriptor.createFromURL(SVGCompostPlugin.getDefault().getBundle().getResource("icons/down.gif")));
		setDisabledImageDescriptor(ImageDescriptor.createFromURL(SVGCompostPlugin.getDefault().getBundle().getResource("icons/down.gif")));
		setEnabled(false);
	}
	
	@Override
	protected int getDirection() {
		return -1;
	}

}
