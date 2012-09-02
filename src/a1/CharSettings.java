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

package a1;


import a1.utils.INIFile;

import java.io.File;
import java.io.FileInputStream;

/**
 * все настройки игрока - хотбары, положение различных контролов и прочее
 */
public class CharSettings {
    public static INIFile ini;
    public static String MAIN_SECTION = "main";

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

    public static void load() {
        try {
            FileInputStream in = new FileInputStream(get_file());
            ini = new INIFile(in);
        } catch (Exception e) {
            Log.info("failed load <"+Player.CharName+"> settings");
            ini = new INIFile();
        }

        // загрузим настройки всех хотбаров
        Hotbar.LoadAll();
    }

    public static void save() {
        ini.saveFile(get_file());
    }

    private static File get_file() {
        return new File("opt_"+Player.CharName+".ini");
    }

}
