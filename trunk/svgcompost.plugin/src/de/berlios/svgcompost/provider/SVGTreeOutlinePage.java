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

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import de.berlios.svgcompost.editor.SVGEditor;
import de.berlios.svgcompost.model.SVGNode;
import de.berlios.svgcompost.part.BackgroundPart;
import de.berlios.svgcompost.part.EditablePart;


/**
 * Outline view for SVG documents which displays their elements as a tree.
 * @author Gerrit Karius
 *
 */
public class SVGTreeOutlinePage extends ContentOutlinePage implements ISelectionListener, EventListener {

	private Element root;
	
	private SVGEditor editor;
	
	public SVGTreeOutlinePage(SVGEditor editor) {
		super();
		this.editor = editor;
		editor.getSite().getPage().addPostSelectionListener(this);
	}
	
	public void setInput(Document doc) {
		if( doc == null )
			return;
		root = doc.getDocumentElement();
		if( root instanceof EventTarget ) {
			EventTarget target = (EventTarget) root;
			target.addEventListener("DOMAttrModified", this, false);
			target.addEventListener("DOMNodeInserted", this, false);
			target.addEventListener("DOMNodeRemoved", this, false);
			target.addEventListener("DOMCharacterDataModified", this, false);
		}
		initTreeViewer();
	}
	
	private void initTreeViewer() {
		TreeViewer treeViewer = getTreeViewer();
		
		if( treeViewer == null )
			return;
		
//		treeViewer.addPostSelectionChangedListener(editor);
		treeViewer.addDoubleClickListener(editor);

		if( treeViewer.getContentProvider() == null ) {
			treeViewer.setContentProvider(new SVGTreeContentProvider());
			treeViewer.setLabelProvider(new SVGLabelProvider());
		}
		
		if( root != null )
			treeViewer.setInput( root );
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		IActionBars bars = getSite().getActionBars();
		ActionRegistry ar = editor.getRegistry();
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), ar.getAction(ActionFactory.COPY.getId()));
		bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), ar.getAction(ActionFactory.PASTE.getId()));
		initTreeViewer();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if( selection instanceof StructuredSelection ) {
			StructuredSelection structured = (StructuredSelection) selection;
			Object selected = structured.getFirstElement();
			Element element = null;
			if( selected instanceof EditablePart )
				element = ((SVGNode)((EditablePart)selected).getModel()).getElement();
			else if( selected instanceof BackgroundPart )
				element = ((SVGNode)((BackgroundPart)selected).getEditRoot()).getElement();
			if( element != null )
				setSelection( new StructuredSelection(element) );
			else
				setSelection( new StructuredSelection() );
		}
	}

	@Override
	public void handleEvent(Event evt) {
		if( evt.getType().equals( "DOMNodeInserted" ) || evt.getType().equals( "DOMNodeRemoved" ) )
		getTreeViewer().refresh();
	}
	
	
	

}
