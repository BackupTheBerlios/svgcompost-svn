/**
 * Copyright 2009 Gerrit Karius
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.berlios.svgcompost.provider;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.UIJob;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * ContentProvider for the SVG outline view.
 * @author Gerrit Karius
 *
 */
public class SVGTreeContentProvider implements ITreeContentProvider,
IResourceChangeListener, IResourceDeltaVisitor {
	
	private StructuredViewer viewer;
	
	private static final Element[] NO_CHILDREN = new Element[0];
	private static final String SVG_EXT = "svg";
	
	public SVGTreeContentProvider() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	public Object[] getChildren(Object arg0) {
		if( arg0 instanceof Element ) {
			return getChildrenFromNode( (Element) arg0 );
		}
		else if( arg0 instanceof IFile ) {
			return updateModel( (IFile) arg0 );
		}
		return null;
	}
	
	protected Element[] getChildrenFromNode( Element element ) {
		NodeList list = element.getChildNodes();
		
		int elementCount = 0;
		for (int i = 0; i < list.getLength(); i++)
			if( list.item(i) instanceof Element )
				elementCount++;
		Element[] childNodes = new Element[elementCount];
		
		for (int i = 0; i < list.getLength(); i++)
			if( list.item(i) instanceof Element )
				childNodes[--elementCount] = (Element) list.item(i);

		return childNodes;
	}

	public Object getParent(Object arg0) {
		if( arg0 instanceof Element ) {
			return ((Element)arg0).getParentNode();
		}
		return null;
	}

	public boolean hasChildren(Object arg0) {
		if( arg0 instanceof IFile ) {
			IFile file = (IFile) arg0;
			if( file.getFileExtension().equals("svg") )
				return true;
			return false;
		}
		else if( arg0 instanceof Element ) {
			return ((Element) arg0).getChildNodes().getLength() > 0;
		}
		return false;
	}

	public Object[] getElements(Object arg0) {
		return getChildren(arg0);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		String arg1Name = oldInput == null ? "null" : oldInput.getClass().getSimpleName();
		String arg2Name = newInput == null ? "null" : newInput.getClass().getSimpleName();
		this.viewer = (StructuredViewer) viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		try {
			delta.accept(this);
		} catch (CoreException e) { 
			e.printStackTrace();
		} 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource source = delta.getResource();
		switch (source.getType()) {
		case IResource.ROOT:
		case IResource.PROJECT:
		case IResource.FOLDER:
			return true;
		case IResource.FILE:
			final IFile file = (IFile) source;
			if (SVG_EXT.equals(file.getFileExtension())) {
				updateModel(file);
				new UIJob("Update Properties Model in CommonViewer") {  //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (viewer != null && !viewer.getControl().isDisposed())
							viewer.refresh(file);
						return Status.OK_STATUS;						
					}
				}.schedule();
			}
			return false;
		}
		return false;
	}

	private Element[] updateModel(IFile file) {
		if( ! file.getFileExtension().equals("svg") )
			return NO_CHILDREN;
		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(file.getLocation().toOSString());
			return getChildrenFromNode( document.getDocumentElement() );
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return null;
	}

}
