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

import a1.dialogs.Dialog;
import a1.dialogs.dlg_Loading;
import a1.dialogs.dlg_Login;
import a1.dialogs.dlg_SysMsg;
import a1.gui.GUI;
import a1.gui.GUI_Debug;
import a1.gui.Skin;
import a1.gui.Skin_MyGUI;
import a1.net.Connection;
import a1.net.NetGame;
import a1.net.NetLogin;
import a1.obj.ObjectVisual;
import a1.utils.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.newdawn.slick.Color;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Main {
	private int fps_counter;
	private long last_fps_tick, last_tick;
	private int debug_row_pos;
	public static long DataRecieved;
	public static long DataSended;
	public static Main instance;

	// промежуток времени между апдейтами, мсек
	static public long dt;
	static public int FPS = 0;
	static public int GameState = 0; // 0 - login, 1 - game
	static public Connection LoginConnect = null;
	static public Connection GameConnect = null;
	
	static public Skin skin;
	public Input input;
	private boolean active = true;
		
	public static void main(String[] args) throws LWJGLException {
		ErrorHandler.initialize();
		RotateLog();
		Log.init();
		Log.info("build: "+Game.buildVersion());
        Log.info("lwjgl: "+Sys.getVersion());
        Log.info("is 64 bit: " + (Sys.is64Bit()? "true" : "false"));
		Log.info("Start a1 client...");
		Config.PrintHelpCommads();
		 
		instance = new Main(); 
		// грузим опции из файла
		Config.load_options();
        // загрузим перевод
        Lang.LoadTranslate();
        // ставим если нужен дебаг движка в консоли
		if (Config.DebugEngine)	System.setProperty("org.lwjgl.util.Debug","true");
		// парсим параметры запуска
		Config.ParseCMD(args); 
		// загрузим файл версий с сервера
		Resource.load_versions();  
		// если запустились в режиме апдейта клиента
		if (Config.update_mode) { 
			// если версия клиента не совпала
			if (Resource.srv_versions.get("client") != Config.CLIENT_VERSION) {
				Log.info("my ver="+Config.CLIENT_VERSION+" new ver="+Resource.srv_versions.get("client"));
				// обновляемся
				Resource.update_client();
			} else
				// скажем что все ок
				Log.info("client is current version. dont need updatig.");
			System.exit(0);
		} else {
			// запуск в обычном режиме
			if (instance.initialize()) {
				instance.execute();  
				instance.destroy();
			}
			Config.save_options();
			AL.destroy();
			System.exit(0);
		}
	}

	public boolean initialize() {

		GameState = 0;

		Keyboard.enableRepeatEvents(true);
		input = new Input(GUI.getInstance());
		Dialog.Init();

		if (!initDisplay())
			return false;

		last_fps_tick = last_tick = System.currentTimeMillis();
		fps_counter = 0;
		FPS = 0;

		Render2D.LoadSystemFont();
		SetIcon();

		Log.info("adapter: " + Display.getAdapter());
		Log.info("version: " + Display.getVersion());

		Log.info("gl vendor: " + GL11.glGetString(GL11.GL_VENDOR));
		if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object)
			Log.info("gl: GL_ARB_vertex_buffer_object supported!");
		else
			Log.info("gl: GL_ARB_vertex_buffer_object NOT supported!");

		initGL();
		setView(Config.getScreenWidth(), Config.getScreenHeight());

		ResSources.Init();
		skin = new Skin_MyGUI();
		skin.Init();

		return true;
	}

	private void initGL() {
		// enable textures since we're going to use these for our sprites
		GL11.glPointSize(1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);

		glClearColor(0.0f, 0.0f, 0.0f, 0);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// disable the OpenGL depth test since we're rendering 2D graphics
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	private boolean initDisplay() {
		try {
			// задаем начальный размер рабочего экрана
			Config.display_modes = Display.getAvailableDisplayModes();

			if (Config.isFullscreen) {
				Display.setDisplayMode(Display.getDesktopDisplayMode());
				Display.setFullscreen(true);
			} else {
				Display.setDisplayMode(new DisplayMode(Config.WindowWidth,
						Config.WindowHeight));
			}

			// позиция окна на экране
			Display.setLocation(Config.WindowPosX, Config.WindowPosY);
			// заголовок окна
			// также не забудем про dlg_Version - там тоже надо поправить вывод
			// версии
			Display.setTitle("Codename: Origin ver: 0."
					+ Integer.toString(Config.CLIENT_VERSION) + " (prealpha)");

			Display.setVSyncEnabled(Config.VSync);
			Display.setResizable(true);
			Display.setInitialBackground(0, 0, 0);
			Display.create();

			return true;
		} catch (LWJGLException e) {
			Log.info(e.getMessage());
		}
		return false;
	}

	private void setView(int screenWidth, int screenHeight) {
        GL11.glViewport(0, 0, screenWidth, screenHeight);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, screenWidth, screenHeight, 0, -1, 1);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    public void destroy() {
		// уничтожаем все гуи контролы
		GUI.getInstance().root.Unlink();
		// уничтожаем дисплей - освобождаем все что взяли
		Display.destroy();
	}	
	
	private void execute() {	
		Dialog.Show("dlg_loading");
		dlg_Loading.FillLoading();
		
		// wait for user to close window
		while (active) {
			
			if (Display.isActive()) {
				// The window is in the foreground, so we should play the game
				input.Update();
				update();
				render();

				DisplaySync();
			} else {

				update();
				if (Display.isVisible() || Display.isDirty()) {
					// Only bother rendering if the window is visible or dirty
					render();
				}

				if (Config.ReduceInBackground) {
					Display.sync(10);
				} else {
					DisplaySync();
				}
			}

			if (Display.wasResized())
				WindowResized();

            if (Display.getX() != Config.WindowPosX || Display.getY() != Config.WindowPosY) {
                Config.WindowPosX = Display.getX();
                Config.WindowPosY = Display.getY();
                Config.save_options();
            }

			if (Display.isCloseRequested()) {
				active = !WindowClose();
				break;
			}
			
			Display.update();
		}
	}
	
	private boolean WindowClose() {

		Config.save_options();

		return true;
	}

	private void DisplaySync() {
		if (Config.FrameFate > 0 && Config.FrameFate < 10000)
			Display.sync(Config.FrameFate);
	}
		
	private void update() {
		dt = (System.currentTimeMillis() - last_tick);
		last_tick = System.currentTimeMillis();
		
		if (GameState == 1) {
			if (GameConnect != null)
				if (!GameConnect.Alive()) {
					Config.quick_login_mode = false;
					NetLogin.error_text = Connection.GetErrorReason(GameConnect.network_error);
					GameState = 0;
					Dialog.HideAll();
					dlg_Login.ShowLogin();
					GameConnect.Close();
					GameConnect = null;
					ReleaseAll();
				}
		} else {
			if (LoginConnect != null)
				if (!LoginConnect.Alive()) {
					Config.quick_login_mode = false;
					NetLogin.error_text = Connection.GetErrorReason(LoginConnect.network_error);
					GameState = 0;
					Dialog.HideAll();
					dlg_Login.ShowLogin();
					LoginConnect.Close();
					LoginConnect = null;
					ReleaseAll();
				}
		}
		
		TilesDebug.Update();
		
		// отложенная загрузка ресурсов
		Resource.update_load();
		// обработка пакетов логина
		NetLogin.ProcessPackets();
		// обработка гейм пакетов
		NetGame.ProcessPackets();
		 
		ObjCache.update();
		Dialog.Update(); 
		dlg_SysMsg.Update();
        Hotbar.UpdateAll();
		GUI.getInstance().Update();
		if (GUI_Debug.active) GUI_Debug.Update();
		
		// handle ALT+ENTER
		if (Input.KeyHit(Keyboard.KEY_RETURN) && Input.isAltPressed() && Config.fullscreen_alt_enter)
			SwitchFullscreenMode();

		Hotkeys.ProcessKey();
	}

	private void SwitchFullscreenMode() {
		try {
			Config.isFullscreen = !Config.isFullscreen;
			if (Config.isFullscreen) {
				// Сохраняем размер окна
				Config.WindowWidth = Display.getWidth();
				Config.WindowHeight = Display.getHeight();

				// Меняем разрешение
				Display.setDisplayMode(Display.getDesktopDisplayMode());
                Config.ScreenWidth = Display.getWidth();
                Config.ScreenHeight = Display.getHeight();

                // "Хеллоу фулскрин!"
                Display.setFullscreen(true);

                // Ресайз
                WindowResized();
			} else {
				// Выходим из фулскрна
				Display.setFullscreen(false);

				// Восстанавливаем разрешение окна
				Display.setDisplayMode(new DisplayMode(Config.WindowWidth,
						Config.WindowHeight));

				// Ресайз
				WindowResized();
			}

		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
	}

	private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		GL11.glLoadIdentity();

        GL11.glPushMatrix();
		GUI.getInstance().Render();
        GL11.glPopMatrix();
		
		// render debug info
		if (Config.debug)
			DrawDebug();
		
		fps_counter++;
		if (System.currentTimeMillis() - last_fps_tick > 1000) {
			last_fps_tick = System.currentTimeMillis();
			FPS = fps_counter;
			fps_counter = 0;
		}
		
	}	
	
	private void DrawDebug() {
		debug_row_pos = 10;
		RenderDebugRow("FPS: " + Integer.toString(FPS));
		RenderDebugRow("Data recv: " + Utils.data2string(DataRecieved));
		RenderDebugRow("Data sent: " + Utils.data2string(DataSended));
		// RenderDebugRow("Memory: "+getMemoryString());
		if (GUI.map != null) {
			RenderDebugRow("map coord: " + Integer.toString(GUI.map.mc.x)
					+ ", " + Integer.toString(GUI.map.mc.y));
			RenderDebugRow("rendered objects=" + GUI.map.render_parts.size());
			RenderDebugRow("tiles rendered: "
					+ Integer.toString(GUI.map.RenderedTiles));
			RenderDebugRow("parts rendered: "
					+ Integer.toString(GUI.map.RenderedObjects));
		}
		RenderDebugRow("Ping=" + NetGame.Ping);
		RenderDebugRow("Mouse: " + Integer.toString(Input.MouseX) + ", "
				+ Integer.toString(Input.MouseY));
		String s = "";
		for (int i = 0; i < 255; i++) {
			if (Keyboard.isKeyDown(i))
				s += " " + Integer.toString(i);
		}
		RenderDebugRow("Keys: " + s);

		if (GUI.getInstance().mouse_in_control != null)
			RenderDebugRow("mouse in control: "
					+ GUI.getInstance().mouse_in_control.toString());
		else
			RenderDebugRow("<< NULL >> ");

		if (GameState == 1) {
			if (GameConnect != null)
				if (GameConnect.Alive())
					RenderDebugRow(GameConnect.GetStateDesc());
				else
					RenderDebugRow(Connection
							.GetErrorReason(GameConnect.network_error));
			else
				RenderDebugRow("disconnected");
		} else if (NetLogin.error_text.length() > 0)
			RenderDebugRow(NetLogin.error_text);
		else if (LoginConnect != null)
			if (LoginConnect.Alive())
				RenderDebugRow(LoginConnect.GetStateDesc());
			else
				RenderDebugRow(Connection
						.GetErrorReason(LoginConnect.network_error));
		else
			RenderDebugRow("disconnected");

		if (GUI_Debug.active)
			GUI_Debug.Render();
	}

	private void RenderDebugRow(String text) {
		Render2D.Text("system", 10, debug_row_pos, text, Color.green);
		debug_row_pos += 15;
	} 
		
	// реакция на изменение размера окна
	private void WindowResized() {
        if (Config.isFullscreen) {
            Config.ScreenHeight_to_save = Config.ScreenHeight;
            Config.ScreenWidth_to_save = Config.ScreenWidth;
        } else {
            Config.WindowWidth = Display.getWidth();
            Config.WindowHeight = Display.getHeight();
        }
        Config.save_options();

        setView(Display.getWidth(), Display.getHeight());

		GUI.getInstance().ResolutionChanged();
		Dialog.ResolutionChanged();
	}
	
	// запустить музыку (после полной остановки надо начать с начала)
	public static void StartMusic() {
		if (Dialog.IsActive("dlg_game"))
			Sound.PlayMusic("theme1");
		else
			Sound.PlayMusic("menu");
	}
	
	// все ресурсы загружены. надо подготовить к нормальному старту игру
	public static void ResLoaded() {
		Render2D.InitFonts();
		
		Config.Apply();
		// если режим отладки тайлов - загрузимся из внешней хмл
		if (Config.dev_tile_mode)
			TilesDebug.ParseTilesXML();
		else
			TilesDebug.ParseTilesXML(null);
		
		Cursor.setCursor("");
		skin.ParseIcons();
	}
	
	// установить иконку приложения
	private static void SetIcon() {
		ByteBuffer[] bb = new ByteBuffer[3];
		try {
			bb[0] = IconLoader.LoadIconFromPNG(Main.class.getResourceAsStream("/etc/a1_16.png"));
			bb[1] = IconLoader.LoadIconFromPNG(Main.class.getResourceAsStream("/etc/a1_32.png"));
			bb[2] = IconLoader.LoadIconFromPNG(Main.class.getResourceAsStream("/etc/a1_128.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Display.setIcon(bb);
	}
	
	// подготовить и удалить лишние лог файлы
	public static void RotateLog() {
		int maxLogs = 2;

		File last = new File("client-" + maxLogs + ".log");
		if (last.exists()) {
			if (!last.delete()) {
				System.err.println("Error removing old log file:" + last);
			}
		}
		for (int i = maxLogs - 1; i > 0; i--) {
			File current = new File("client-" + i + ".log");
			if (current.exists()) {
				if (!current.renameTo(last))
					System.err.println("Error renaming:" + current + " to:" + last);
			}
			last = current;
		}

		File current = new File("client.log");
		if (current.exists()) {
			if (!current.renameTo(last))
				System.err.println("Error renaming:" + current + " to:" + last);
		}
	}
	
	public static String getMemoryString() {
		Runtime rt = Runtime.getRuntime();
		long free = rt.freeMemory();
		long total = rt.totalMemory();
		long max = rt.maxMemory();

		long used = total - free;

		long percent1 = used * 100L / total;
		long percent2 = used * 100L / max;

		return "Working memory: " + percent1 + "% (" + used + "/" + total + ")" + "  VM Max: " + percent2 + "% (" + used
				+ "/" + max + ")";
	}

	static public void GlobalError(String msg) {
		Log.info(msg);
		System.exit(-1);
	}
	
	// освободить все занятые ресурсы и очистить
	static public void ReleaseAll() {
		ObjCache.ClearAll();
		FlyText.Clear();
		MapCache.grids.clear();
		Player.Clear();
		
		Config.save_options();
		Dialog.HideAll();
        ObjectVisual.CloseAll();
        Hotbar.ClearAll();
		GUI.getInstance().FreeRemoteControls();
		Cursor.setCursor("");
		
		if (GameConnect != null)
			GameConnect.Close();	
		GameConnect = null;
		
		if (LoginConnect != null)
			LoginConnect.Close();
		LoginConnect = null;
		
		NetLogin.LastPingTime = 0;
		NetLogin.login_state = "";
		
		dlg_Login.ShowLogin();
	}
}
