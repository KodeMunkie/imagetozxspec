/* Image to ZX Spec
 * Copyright (C) 2023 Silent Software (Benjamin Brown)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.silentsoftware.config;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to persist the options in the OptionsObject class.
 * Any field annotated with PreferencesField is stored in a platform
 * specific way as a key value pair in the user root.
 */
public class PreferencesService {

	private static Logger log = LoggerFactory.getLogger(PreferencesService.class);
	
	public final static String PREFS_FILE = "imagetozxspec";
	
	public final static Preferences preferences = Preferences.userRoot().node(PREFS_FILE);

	/**
	 * Loads previously persisted options if available using reflection
	 * Marked fields on the OptionsObject are updated using the standard property editor.
	 * 
	 * @return an OptionsObject if one existed in the user root
	 */
	public static Optional<OptionsObject> load() {
		OptionsObject oo = new OptionsObject();
		Field[] fields = FieldUtils.getFieldsWithAnnotation(OptionsObject.class, PreferencesField.class);
		for (Field field : fields) {
			field.setAccessible(true);
			Object value;
			try {
				value = field.get(oo);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
			if (value != null) {
				if (!field.getType().isPrimitive() && !field.getType().equals(String.class)) {
					log.debug("Non primitive field found {}", field.getName());
				}
				try {
					String valueAsString = preferences.get(field.getName(), value.toString());
					Object typedValue = convert(field.getType(), valueAsString);
					field.set(oo, typedValue);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}
		}
		return Optional.ofNullable(oo);
	}

	/**
	 * Converts a String value to the specified target type for use
	 * in setting a field by reflection.
	 * 
	 * @param targetType the field (property) type to convert the string value to
	 * @param text the field (property) content as text
	 * @return the specified target type field with the value set from the text
	 */
	private static Object convert(Class<?> targetType, String text) {
	    PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
	    editor.setAsText(text);
	    return editor.getValue();
	}
	
	/**
	 * Saves (persists) the options in the OptionsObject that are marked with the
	 * PreferencesField annotation.
	 */
	public static void save() {
		OptionsObject oo = OptionsObject.getInstance();
		Field[] fields = FieldUtils.getFieldsWithAnnotation(OptionsObject.class, PreferencesField.class);
		for (Field field : fields) {
			field.setAccessible(true);
			Object value;
			try {
				value = field.get(oo);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
			if (value != null) {
				if (!field.getType().isPrimitive()) {
					log.debug("Non primitive field found {}", field.getName());
				}
				preferences.put(field.getName(), value.toString());
			}
		}
	}
}
