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

import java.io.*;
import java.util.*;
import a1.Log;

public class INIFile {
	HashMap<String, String> map = new HashMap<String, String>();

	public INIFile(String fname) throws IOException {
		FileInputStream fs = new FileInputStream(fname);
		try {
			loadFile(fs);
		} finally {
			fs.close();
		}	
	}

	public INIFile(InputStream in) throws IOException {
		try {
			loadFile(in);
		} catch (Exception e) {
			Log.info("Error read lang data!");
		}	
	}	
	private void loadFile(InputStream in) throws IOException {
		String section = "";
		String line;
		boolean ended = false;

		int c;
		List<Integer> buf = new ArrayList<Integer>();
		while (!ended) {
			buf.clear();
			while (true) {
				c = in.read();

				if (c == -1) {
					ended = true;
					break;
				}
				if (c == 13 || c == 10)
					break;
				else
					buf.add(c);
			}
			if (buf.size() < 1)
				continue;

			byte[] arr = new byte[buf.size()];
			for (int i = 0; i < buf.size(); i++)
				arr[i] = buf.get(i).byteValue();
			line = new String(arr, "utf-8");

			if (line.startsWith(";"))
				continue;
			if (line.startsWith("[")) {
				section = line.substring(1, line.lastIndexOf("]")).trim();
				continue;
			}
			if (line.length() < 1)
				continue;
			if (section.length() > 0)
				addProperty(section, line);
		}

	}
	
//	public void saveFile(String filename) {
//		try {
//			List<String> sections_list = new ArrayList<String>();
//			for (String key : map.keySet()) {
//				String section = key.split("\\.")[0];
//			    if (!sections_list.contains(section)) sections_list.add(section);
//			}
//			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
//			for (String sect : sections_list) {
//				writer.write("[" + sect + "]\n");
//				for (String key : map.keySet()) {
//					String[] keys = key.split("\\.");
//					if (sect.equals(keys[0])) {
//						writer.write(keys[1] + "=" + map.get(key) + '\n');
//					}
//				}
//			}
//			writer.close();
//		} catch (Exception e) {
//			Log.info("Error while saving ini file: " + filename);
//		}
//	}
	
	public void addProperty(String section, String line) {
		int equalIndex = line.indexOf("=");

		if (equalIndex > 0) {
			String name = section + '.' + line.substring(0, equalIndex).trim();
			String value = line.substring(equalIndex + 1).trim();
			if (map.containsKey(name)) map.remove(name);
			map.put(name, value);
		}
	}

	public String getProperty(String section, String var, String def) {
		String s = map.get(section + '.' + var);
		if (s == null)
			return def;
		else
			return s;
	}
	
	public void deleteProperty(String section, String var) {
		if (map.containsKey(section + '.' + var)) map.remove(section + '.' + var);
	}

	public int getProperty(String section, String var, int def) {
		String sval = getProperty(section, var, Integer.toString(def));

		return Integer.decode(sval).intValue();
	}

	public boolean getProperty(String section, String var, boolean def) {
		String sval = getProperty(section, var, def ? "True" : "False");

		return sval.equalsIgnoreCase("Yes") || sval.equalsIgnoreCase("True");
	}
}