/*
 * This file is part of the Origin-World game client.
 * Copyright (C) 2012 Arkadiy Fattakhov <ark@ark.su>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package a1.utils;

import a1.Log;

import java.io.File;
import java.io.FileInputStream;

public class AppSettings {
    private static INIFile ini;
    private static String MAIN_SECTION = "main";

	public static String get(String key) {
		return ini.getProperty(MAIN_SECTION, key);
	}

	public static String get(String key, String def) {
		return ini.getProperty(MAIN_SECTION, key, def);
	}

	public static int getInt(String key, int def) {
		return ini.getProperty(MAIN_SECTION, key, def);
	}

	public static boolean getBool(String key, boolean def) {
		return ini.getProperty(MAIN_SECTION, key, def);
	}

	public static void put(String key, String data) {
		ini.putProperty(MAIN_SECTION, key, data);
	}

	public static void put(String key, int v) {
        ini.putProperty(MAIN_SECTION, key, v);
	}

	public static void put(String key, boolean v) {
		ini.putProperty(MAIN_SECTION, key, v);
	}

	public static void load(File file) {
        try {
			FileInputStream in = new FileInputStream(file);
            ini = new INIFile(in);
        } catch (Exception e) {
			Log.info("failed load app settings");
            ini = new INIFile();
		}
	}

	public static void save(File file) {
        ini.saveFile(file);
    }

}
