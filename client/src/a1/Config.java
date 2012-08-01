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

import java.io.File;

import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.openal.SoundStore;

import a1.utils.AppSettings;
import a1.utils.TilesDebug;
import a1.Log;

public class Config {
	// имя конфиг файла в каталоге с клиентом
	public static final String config_file = "options.xml";
	// адрес логин сервера
	public static String login_server = "origin-world.com";
    // порт логин сервера
    public static int login_server_port = 777;
	// флаг отладки движка. пишет вывод в консоль
	public static boolean DebugEngine;
	// раземры экрана
	public static int ScreenWidth;
	public static int ScreenHeight;
	// позиция окна на рабочем столе
	public static int WindowPosX;
	public static int WindowPosY;
	// желаемое значение фпс
	public static int FrameFate;
	// запускать в полноэкранном режиме
	public static boolean StartFullscreen;
	// включены ли звуки
	public static boolean SoundEnabled;
	// уровень громкости музыки
	public static int MusicVolume;
	// уровень громкости звуков
	public static int SoundVolume;
	// размеры экрана для сохранения в конфиг файле (применятся при следующем запуске)
	public static int ScreenWidth_to_save;
	public static int ScreenHeight_to_save;
	// адрес сервера с реурсами
	public static String resource_remote_host = "upd.origin-world.com";
	// путь к каталогу ресурсов
	public static String resource_remote_path = "/res/";
	// путь к файлу с версиями
	public static String versions_file = "versions.xml";
	// локальный каталог для кеширования ресурсов
	public static String local_cache_dir = "/cache/";
	// путь до клиента для обновления
	public static String update_client = "/res/client.jar";
	// юзер агент для http запросов
	public static String user_agent = "origin_client";
	// текущий язык
	public static String current_lang;
	// логин и пароль под которым заходит юзер на сервер
	public static String user;
	public static transient String pass;
	// сохранять ли пароль
	public static boolean save_pass;
	// отображать полное количество объектов (растения в тайлах)
	public static boolean count_objs;
	// скрывать ли перекрывающие объекты
	public static boolean hide_overlapped;
    // постоянно двигаться с зажатой левой кнопкой мыши
    public static boolean move_inst_left_mouse;

	public static final int PROTO_VERSION = 2;
	public static final int CLIENT_VERSION = 59;

	public static final int ICON_SIZE = 32;

	// режим дебага
	public static boolean debug = false;
    // показывать детальную расшифровку пакетов
    public static boolean debug_packets = false;
	// режим локального старта. ищем versions.xml в каталоге кэша. версии ресурсов не проверяем.
	public static boolean local_start = false;
	// режим обновления клиентом самого себя
	public static boolean update_mode = false;
	// режим быстрого входа с последним вырбарным чаром
	public static boolean quick_login_mode = false;
	// дев режим. отладка тайлов
	public static boolean dev_tile_mode = false;
	// сетка тайлов
	public static boolean tile_grid = false;
	// список доступных разрешений экрана
	public static DisplayMode[] display_modes;

	public static void ParseCMD(String[] args) {
		try {
			int i = 0;
			while (i < args.length) {
				String arg = args[i];
				if (arg.equals("-d")) { // Debug mode. Format: "-d"
					debug = true;
				}
                if (arg.equals("-r")) { // Debug pkt mode. Format: "-r"
                    debug_packets = true;
                }
				if (arg.equals("-s")) { // Change server. Format: "-s servername"
					i++;
					login_server = args[i];
				}
                if (arg.equals("-p")) { // Change server. Format: "-p port"
                    i++;
                    login_server_port = Integer.parseInt(args[i]);
                }
				if (arg.equals("-u")) { // Update mode. Format: "-u"
					update_mode = true;
				}
				if (arg.equals("-l")) { // Local mode. Format: "-l"
					local_start = true;
				}
				if (arg.equals("-q")) { // Quick login mode. Format: "-q"
					quick_login_mode = true;
				}
				if (arg.equals("-dev_tile")) { // Format: -dev_tile <filename tiles.xml>
					dev_tile_mode = true;
					i++;
					TilesDebug.dev_tiles_xml = args[i];
				}
				i++;
			}
		} catch (Exception e) {
			Log.info("ParseCMD Error: " + e.getMessage());
		}
	}

	public static void PrintHelpCommads() {
		Log.info("Use commands:");
		Log.info("    Debug mode. Format: -d");
        Log.info("    Debug pkt mode. Format: -r");
		Log.info("    Change login server. Format: -s <servername>");
        Log.info("    Change login server port. Format: -p <port>");
		Log.info("    Update mode. Format: -u");
		Log.info("    Local mode. Format: -l");
		Log.info("    Quick login mode. Format: -q");
		Log.info("    Developer mode: tiles debug. Format: -dev_tile <filename tiles.xml>");
	}

	public static void Apply() {
		float val = SoundVolume;
		SoundStore.get().setSoundVolume(val / 100);
		val = MusicVolume;
		SoundStore.get().setMusicVolume(val / 100);
	}

	public static void load_options() {
		AppSettings.load(new File(config_file));

		ScreenWidth = AppSettings.getInt("screen_width", 1024);
		ScreenHeight = AppSettings.getInt("screen_height", 768);
		WindowPosX = AppSettings.getInt("window_pos_x", 0);
		WindowPosY = AppSettings.getInt("window_pos_y", 0);
		FrameFate = AppSettings.getInt("frame_rate", 60);
		SoundVolume = AppSettings.getInt("sound_vol", 50);
		MusicVolume = AppSettings.getInt("music_vol", 50);
		ScreenWidth_to_save = ScreenWidth;
		ScreenHeight_to_save = ScreenHeight;
		StartFullscreen = AppSettings.getBool("start_fullscreen", false);
		SoundEnabled = AppSettings.getBool("sound_enabled", true);
		DebugEngine = AppSettings.getBool("debug_engine", false);
		current_lang = AppSettings.get("language", "");
		count_objs = AppSettings.getBool("count_objs", true);
		hide_overlapped = AppSettings.getBool("hide_overlapped", true);
        move_inst_left_mouse= AppSettings.getBool("move_inst_left_mouse", true);
		user = AppSettings.get("user", "");
		pass = AppSettings.get("pass", "");

		save_pass = AppSettings.getBool("save_pass", false);
	}

	public static void save_options() {
		AppSettings.put("screen_width", ScreenWidth_to_save);
		AppSettings.put("screen_height", ScreenHeight_to_save);
		AppSettings.put("window_pos_x", WindowPosX);
		AppSettings.put("window_pos_y", WindowPosY);
		AppSettings.put("frame_rate", FrameFate);
		AppSettings.put("start_fullscreen",StartFullscreen);
		AppSettings.put("sound_enabled",SoundEnabled);
		AppSettings.put("sound_vol", SoundVolume);
		AppSettings.put("music_vol", MusicVolume);
		AppSettings.put("debug_engine",DebugEngine);
		AppSettings.put("language", current_lang);
		AppSettings.put("count_objs", count_objs);
		AppSettings.put("hide_overlapped", hide_overlapped);
        AppSettings.put("move_inst_left_mouse", move_inst_left_mouse);
		if (save_pass) {
			AppSettings.put("user", user);
			AppSettings.put("pass", pass);
		} else {
			AppSettings.put("user", "");
			AppSettings.put("pass", "");
		}
		AppSettings.put("save_pass", save_pass);

		AppSettings.save(new File(config_file));
	}
}
