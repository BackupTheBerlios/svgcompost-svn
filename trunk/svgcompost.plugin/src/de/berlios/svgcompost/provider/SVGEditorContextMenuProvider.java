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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;

import de.berlios.svgcompost.layers.BreakApartAction;
import de.berlios.svgcompost.layers.GroupAction;
import de.berlios.svgcompost.layers.LowerNodeAction;
import de.berlios.svgcompost.layers.RaiseNodeAction;


/**
 * Provides a context menu for the editor.
 */
public class SVGEditorContextMenuProvider extends ContextMenuProvider {

	private ActionRegistry actionRegistry;

	public SVGEditorContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
		super(viewer);
		if (registry == null) {
			throw new IllegalArgumentException();
		}
		actionRegistry = registry;
	}

	public void buildContextMenu(IMenuManager menu) {
		GEFActionConstants.addStandardActionGroups(menu);
		menu.add(new Separator(GEFActionConstants.GROUP_REORGANIZE));

		menu.appendToGroup( GEFActionConstants.GROUP_UNDO, getAction(ActionFactory.UNDO.getId()));
		menu.appendToGroup( GEFActionConstants.GROUP_UNDO, getAction(ActionFactory.REDO.getId()));
		menu.appendToGroup( GEFActionConstants.GROUP_EDIT, getAction(ActionFactory.DELETE.getId()));
		menu.appendToGroup( GEFActionConstants.GROUP_REORGANIZE, getAction(GroupAction.GROUP));
		menu.appendToGroup( GEFActionConstants.GROUP_REORGANIZE, getAction(GroupAction.GROUP));
		menu.appendToGroup( GEFActionConstants.GROUP_REORGANIZE, getAction(RaiseNodeAction.RAISE_NODE));
		menu.appendToGroup( GEFActionConstants.GROUP_REORGANIZE, getAction(LowerNodeAction.LOWER_NODE));
	}

	private IAction getAction(String actionId) {
		return actionRegistry.getAction(actionId);
	}

}
