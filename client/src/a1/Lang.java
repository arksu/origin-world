/*
 *  This file is part of the Origin-World game client.
 *  Copyright (C) 2012 Arkadiy Fattakhov <ark@ark.su>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 3 of the License.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package a1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.newdawn.slick.UnicodeFont;

import a1.utils.INIFile;
import a1.utils.Resource;
import a1.Log;

public class Lang {
	static private HashMap<String, Integer> langs = new HashMap<String, Integer>();
	static public final int RUSSIAN = 1;
	static private INIFile lang_file; 
	static public int current = RUSSIAN;
	
	static public String getTranslate(String section, String text) {
		if (lang_file != null)
			return lang_file.getProperty(section, text, section+"_"+text);
		else
			return section+"_"+text;
	}
	
	static public char GetChar(char in) {
		int i = in;
		if (i > 127 && i < 256) {
			switch (current) {
			case RUSSIAN :
				i = i + 848;
			}
		}
		return (char)i;
	}
	
	static public void FontCharSets(UnicodeFont font) {
		font.addGlyphs(1040, 1103);
	}
	
	static public void LoadTranslate() {
		// ставим все возможные раскладки для ввода текста
		langs.put("en", RUSSIAN);
		langs.put("ru", RUSSIAN);
		
		if (Config.current_lang == null || Config.current_lang.length() < 1)
			current = RUSSIAN;
		// тут смотрим какой язык выбран в качестве перевода. и ставим текущую раскладку для ввода символов
		else {
			current = langs.get(Config.current_lang);
		
			try {
				Resource.ResBinary bin = Resource.binary.get("lang_"+Config.current_lang);
				if (bin != null) {
					ByteArrayInputStream in = new ByteArrayInputStream(bin.bin_data); 
					lang_file = new INIFile(in);
				}
			} catch (IOException e) {
				Log.info("failed load translate: "+Config.current_lang);
			}
		}
	}
}
