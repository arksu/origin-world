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
package a1.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import a1.Log;

public class AppSettings {
	private static HashMap<String, String> fHashMap = new HashMap<String, String>();
	
	// Чортовы настройки персов
	private static HashMap<String, String> fCHashMap = new HashMap<String, String>();
	
	// Извлечение объекта из коллекции
	public static String get(String key) {
		String s = fHashMap.get(key);
		if (s == null) 
			return "";
		else
			return s;
	}

	// Извлечение объекта из коллекции
	// при отсутствии данных возвращается значение по умолчанию
	public static String get(String key, String deflt) {
		String s = fHashMap.get(key);
		if (s == null) {
			return deflt;
		} else {
			return s;
		}
	}

	// Для упрощения извлечения данных типа int
	public static int getInt(String key, int deflt) {
		String s = fHashMap.get(key);
		if (s == null) {
			return deflt;
		} else {
			return new Integer(s).intValue();
		}
	}

	public static boolean getBool(String key, boolean deflt) {
		String s = fHashMap.get(key);
		if (s == null) {
			return deflt;
		} else {
			if (s.equals("1") || s.equals("true") || s.equals("TRUE"))
				return true;
			else
				return false;
		}
	}

	// Добавление объекта в коллекцию
	public static void put(String key, String data) {
		// prevent null values. Hasmap allow them
		if (data == null) {
			throw new IllegalArgumentException();
		} else {
			fHashMap.put(key, data);
		}
	}

	public static void put(String key, int v) {
		fHashMap.put(key, Integer.toString(v));
	}

	public static void put(String key, boolean v) {
		if (v)
			fHashMap.put(key, "1");
		else
			fHashMap.put(key, "0");
	}

	public static void load(File file) {
		try {
			FileInputStream in = new FileInputStream(file);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(in);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("property");
			String key, value;
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					key = eElement.getAttributeNode("key").getNodeValue();
					value = eElement.getAttributeNode("value").getNodeValue();
					fHashMap.put(key, value);
				}
			}
			
			/* Character begin load */ 
			NodeList buf = doc.getElementsByTagName("characters");
			if (buf.getLength() > 0) {
				NodeList characters = buf.item(0).getChildNodes();
				for (int chars = 0; chars < characters.getLength(); chars++) {
					String k, v;
					NodeList saves = characters.item(chars).getChildNodes();
					String charname = characters.item(chars).getNodeName();
					for (int temp = 0; temp < saves.getLength(); temp++) {
						Node nNode = saves.item(temp);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							k = nNode.getNodeName();
							v = nNode.getTextContent();
							fCHashMap.put(charname + ">" + k, v);
						}
					}
				}
			}
			/* Character end load */ 
		} catch (Exception e) {
			Log.info("ERROR: failed load app settings");
		}
	}

	public static void save(File file) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document document = dBuilder.newDocument();
			Node rootElement = document.createElement("list");
			document.appendChild(rootElement);
			Set<String> set = fHashMap.keySet();
			if (set != null) {
				for (Iterator<String> iterator = set.iterator(); iterator
						.hasNext();) {
					String key = iterator.next().toString();
					String value = fHashMap.get(key);
					Element em = document.createElement("property");
					em.setAttribute("key", key);
					em.setAttribute("value", value);
					rootElement.appendChild(em);
				}
			}
			
			/* Characters begin save */
			Node chars = document.createElement("characters");
			rootElement.appendChild(chars);
			Set<String> kset = fCHashMap.keySet();
			if (kset != null) {
				for (Iterator<String> iterator = kset.iterator(); iterator.hasNext();) {
					String key = iterator.next().toString();
					String charname = key.split(">")[0];
					String nodename = key.split(">")[1];
					String value = fCHashMap.get(key);
					
					NodeList search = document.getElementsByTagName(charname);
					Node chr;
					if (search.getLength() > 0) chr = search.item(0);
					else {
						chr = document.createElement(charname);
						chars.appendChild(chr);
					}

					Element e = document.createElement(nodename);
					e.setTextContent(value);
					chr.appendChild(e);
				}
			}
			/* Characters end save */
			
			DOMSerializer serializer = new DOMSerializer();
			serializer.serialize(document, file);
		} catch (Exception e) {
			Log.info("ERROR: failed save app settings");
		}
	}
	
	// Ох уж это ООП ^^ 
	
	// Добавление элемента
	@SuppressWarnings("rawtypes")
	public static Object getCharacterValue(String charname, String key, Class type) {
		String normal_key = charname + ">" + key;
		boolean contains = fCHashMap.containsKey(normal_key);
		if (type == Integer.class) {
			if (contains) return Integer.parseInt(fCHashMap.get(normal_key));
			else return 0;
		} else if (type == Boolean.class) {
			if (contains) return Boolean.parseBoolean(fCHashMap.get(normal_key));
			else return false;
		} else if (type == String.class) {
			if (contains) return fCHashMap.get(normal_key);
			else return "";
		} else return null;
	}
	
	@SuppressWarnings("rawtypes")
	public static Object getCharacterValue(String charname, String key, Object def, Class type) {
		String normal_key = charname + ">" + key;
		boolean contains = fCHashMap.containsKey(normal_key);
		if (type == Integer.class) {
			if (contains) return Integer.parseInt(fCHashMap.get(normal_key));
			else return def;
		} else if (type == Boolean.class) {
			if (contains) return Boolean.parseBoolean(fCHashMap.get(normal_key));
			else return def;
		} else if (type == String.class) {
			if (contains) return fCHashMap.get(normal_key);
			else return def;
		} else return def;
	}

	
	// Получение элемента (+ удаление в случае типа аргумента отличного от описанных)
	public static void putCharacterValue(String charname, String key, Object value) {
		String normal_key = charname + ">" + key;
		boolean contains = fCHashMap.containsKey(normal_key);
		if (contains) fCHashMap.remove(normal_key);
		if (value instanceof Integer) {
			fCHashMap.put(normal_key, Integer.toString((Integer)value));
		} else if (value instanceof Boolean) {
			fCHashMap.put(normal_key, Boolean.toString((Boolean)value));
		} else if (value instanceof String) {
			fCHashMap.put(normal_key, (String)value);
		}
	}
}
