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

// класс для отладки тайлов. отслеживает изменения хмл конфига тайлов. и перегружает все тайлы из него.

import a1.Config;
import a1.Log;
import a1.Main;
import a1.MapCache;
import a1.utils.Resource.ResTile;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class TilesDebug {
	public static final int UPDATE_TIME = 1000;
	
	// время изменения файла с хмл конфигом
	public static long last_xml_time = 0;
	
	public static long update_timer = 0;
	// имя файла с хмл конфигом
	public static String dev_tiles_xml = "";
	
	public static void ParseTilesXML() {
		if (!Config.dev_tile_mode) return;
		File f = new File(dev_tiles_xml);
		try {
			last_xml_time = f.lastModified();
			InputStream in = new FileInputStream(f);
			ParseTilesXML(in);
		} catch (Exception e) {
			e.printStackTrace();
			// в случае ошибки - вырубаем режим отладки тайлов
			Config.dev_tile_mode = false;
			Log.info("ERROR: load tiles config");
		}
	}
	
	public static void ParseTilesXML(InputStream in) {
		boolean is_ext = true;
		
		// если на входе ничего - читаем из рес пака
		if (in == null) {
			is_ext = false;
			
			in = Resource.binary.get("tiles").get_stream();
		}
		
		// удалим все существующие тайлсеты
		for (int i = 0; i < Resource.TILES_MAX; i++) {
			Resource.tile_sets[i] = null;
		}
		
		// очистим кэш
		MapCache.Reset();
		
		// парсим хмл
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(in);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("tile");
			int type;
			String texture_name;
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					type = Integer.parseInt(eElement.getAttributeNode("type").getNodeValue());
					texture_name = eElement.getAttributeNode("texture").getNodeValue();
					
					// если читаем данные из внешнего источника
					if (is_ext) {
						// перечитаем текстуру если надо
						File tf = new File(texture_name+".png");
						InputStream fin = new FileInputStream(tf);
						Texture tex = TextureLoader.getTexture("PNG", fin, GL11.GL_NEAREST);
						Resource.textures.put(texture_name, tex);
					}
					
					Resource.tile_sets[type] = new ResTile(type, texture_name, eElement);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.info("ERROR: load tiles.xml");
			// в случае ошибки - вырубаем режим отладки тайлов
			Config.dev_tile_mode = false;
		}
	}
	
	public static void Update() {
		if (!Config.dev_tile_mode) return;
		
		update_timer += Main.dt;
		// раз в указанное время проверяем хмл конфиг на изменение
		if (update_timer > UPDATE_TIME) {
			update_timer = 0;
			
			File f = new File(dev_tiles_xml);
			try {
				// проверяем дату изменения файла конфига
				long time = f.lastModified();
				// если даты не совпали - перечитываем конфиг
				if (time != last_xml_time) {
					Log.info(dev_tiles_xml+" changed! rebuild tiles...");
					last_xml_time = time;
					ParseTilesXML();
				}
			} catch (Exception e) {
				e.printStackTrace();
				// в случае ошибки - вырубаем режим отладки тайлов
				Config.dev_tile_mode = false;
			}
		}
	}

}
