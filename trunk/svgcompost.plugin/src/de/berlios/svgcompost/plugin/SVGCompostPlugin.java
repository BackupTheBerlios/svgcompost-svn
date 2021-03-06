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

package de.berlios.svgcompost.plugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The SVGCompost plugin.
 * @author Gerrit Karius
 *
 */
public class SVGCompostPlugin extends AbstractUIPlugin {

	private static SVGCompostPlugin singleton;
	
	public static SVGCompostPlugin getDefault() {
		return singleton;
	}
	
	public SVGCompostPlugin() {
		if (singleton == null) {
			singleton = this;
		}
	}

}