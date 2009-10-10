package de.berlios.svgcompost.animation.plugin;

import java.util.Iterator;

import org.apache.batik.bridge.BridgeContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import de.berlios.svgcompost.animation.AnimControl;
import de.berlios.svgcompost.animation.GraphicsBuilder;
import de.berlios.svgcompost.animation.anim.composite.Scene;
import de.berlios.svgcompost.animation.canvas.Canvas;
import de.berlios.svgcompost.animation.export.Export;
import de.berlios.svgcompost.animation.export.binary.FlagstoneExport;
import de.berlios.svgcompost.animation.timeline.Timeline;

public class SWFBuildAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchPage page;

	private ISelection selection;
	
	public SWFBuildAction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.page = window.getActivePage();
		
	}

	@Override
	public void run(IAction action) {
		Job buildJob = null;
		if (selection instanceof IStructuredSelection) {
			for (Iterator it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				if (element instanceof IFile) {
					final IFile finalFile = (IFile) element;
					String extension = finalFile.getFileExtension();
					if( ! extension.equals("svg") )
						continue;
					buildJob = new Job("SWF Build Job") {
						public IStatus run(IProgressMonitor monitor) {
							try {
								runSWFBuild(finalFile, monitor);
								if( monitor.isCanceled() )
									return Status.CANCEL_STATUS;
								return Status.OK_STATUS;
							} catch(Exception e) {
								return new Status(Status.ERROR, AnimPlugin.getDefault().getBundle().getSymbolicName(), 0, "An error ocurred during the SWF build.", e);
							}
						}
					};
					break;
				}
			}
		}
		if( buildJob != null ) {
			buildJob.setPriority(Job.LONG);
			buildJob.schedule();
		}
	}
	
	private void runSWFBuild(IFile file, IProgressMonitor monitor) {
		String svgPath = file.getLocation().toString();
		String filenameWithoutExtension = file.getName().substring(0,file.getName().lastIndexOf(file.getFileExtension()));
		String swfPath = file.getParent().getFile( new Path( filenameWithoutExtension+"swf" ) ).getLocation().toString();
		
		monitor.beginTask("Running SWF build.", 1);

		BridgeContext ctx = GraphicsBuilder.readLibrary(svgPath);
		Canvas canvas = new Canvas( ctx );
		canvas.setLibrary( ctx );
		Export capture = new FlagstoneExport( canvas );
		Timeline timeline = canvas.getLibrary().createTimeline();
		Scene scene = canvas.getLibrary().createAnimsForTimeline(timeline);
		scene.setDurationInSeconds(10);
		AnimControl ctrl = new AnimControl( scene );
		ctrl.setCapture( capture );
		while( ctrl.nextFrame() ) {
		}
		capture.end();
		capture.writeOutput( swfPath );

		monitor.done();
			
		try {
			file.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}


}
