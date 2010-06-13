package de.berlios.svgcompost.animation.plugin;

import java.io.IOException;
import java.util.Iterator;
import java.util.zip.DataFormatException;

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

import de.berlios.svgcompost.swf.SWF2SVG;

public class SWF2SVGBuildAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchPage page;

	private ISelection selection;
	
	public SWF2SVGBuildAction() {
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
					if( ! extension.equals("swf") )
						continue;
					buildJob = new Job("SVG Build Job") {
						public IStatus run(IProgressMonitor monitor) {
							try {
								runSWFBuild(finalFile, monitor);
								if( monitor.isCanceled() )
									return Status.CANCEL_STATUS;
								return Status.OK_STATUS;
							} catch(Exception e) {
								return new Status(Status.ERROR, AnimPlugin.getDefault().getBundle().getSymbolicName(), 0,
										"An error ocurred during the SVG build: "+(e.getMessage()==null?e.getClass():e.getMessage()), e);
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
		String sourcePath = file.getLocation().toString();
//		String filenameWithoutExtension = file.getName().substring(0,file.getName().lastIndexOf(file.getFileExtension()));
//		String targetPath = file.getParent().getFile( new Path( filenameWithoutExtension+"svg" ) ).getLocation().toString();
		
		monitor.beginTask("Running SVG build.", 1);

//		BridgeContext ctx = GraphicsBuilder.readLibrary(sourcePath);
//		Canvas canvas = new Canvas( ctx );
//		canvas.setLibrary( ctx );
//		int foundKeyframes = 0;
//		Timeline timeline = canvas.getLibrary().createTimeline();
//		for( Layer layer : timeline.getLayers() )
//			foundKeyframes += layer.getKeyframes().size();
//		Export capture = new FlagstoneExport( ctx );
//		
//		
//		if( foundKeyframes == 0 ) {
//			// no keyframes, export as static SWF
//			canvas.renderDocument(canvas.getSourceDoc());
//			capture.captureFrame();
//		}
//		else {
//			Scene scene = canvas.getLibrary().createAnimsForTimeline(timeline);
//			scene.setDurationInSeconds(10);
//			AnimControl ctrl = new AnimControl( scene );
//			ctrl.setCapture( capture );
//			while( ctrl.nextFrame() ) {
//			}
//		}
//		capture.end();
		
		SWF2SVG swf2svg = new SWF2SVG();
		try {
			swf2svg.exportSWF2SVG(sourcePath);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (DataFormatException e1) {
			e1.printStackTrace();
		}
//		capture.writeOutput( targetPath );

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
