package de.berlios.svgcompost.layers;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class LowerNodeAction extends RaiseNodeAction {

	public static String LOWER_NODE = "Action.LowerNode";

	public LowerNodeAction(IWorkbenchPart part) {
		super(part);
	}
	
	@Override
	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setText("Lower Node");
		setId(LOWER_NODE);
		setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));
		setEnabled(false);
	}
	
	@Override
	protected int getDirection() {
		return -1;
	}

}
