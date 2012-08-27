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

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class Lang {
	static private HashMap<String, Integer> langs = new HashMap<String, Integer>();
	static public final int RUSSIAN = 1;
    static public final int UKRAINIAN = 2;
    static public final int POLISH = 3;
    static public final int FRENCH = 4;
    static public final int GERMAN = 5;
    static public final int ITALIAN = 6;
    static public final int NETHERLANDISH = 7;
    static public final int LITHUANIAN = 8;
    static public final int ESTONIAN = 9;



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
                String path = Config.lang_path+Config.current_lang+".txt";
                URL lang_url = new URL(new URI("http", Config.lang_remote_host, path,"").toASCIIString());
                Log.info("load translate: "+lang_url.toString());
                URLConnection c;
                c = lang_url.openConnection();
                c.addRequestProperty("User-Agent", Config.user_agent);
                InputStream in = c.getInputStream();
                lang_file = new INIFile(in);

                // старый способ. переводы брали из файла ресурсов
//				Resource.ResBinary bin = Resource.binary.get("lang_"+Config.current_lang);
//				if (bin != null) {
//					ByteArrayInputStream in = new ByteArrayInputStream(bin.bin_data);
//					lang_file = new INIFile(in);
//				}
			} catch (Exception e) {
				Log.info("failed load translate: "+Config.current_lang);
                e.printStackTrace();
			}
		}
	}
}
