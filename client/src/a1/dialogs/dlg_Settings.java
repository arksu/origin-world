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
package a1.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.openal.SoundStore;

import a1.Config;
import a1.Coord;
import a1.DialogFactory;
import a1.Lang;
import a1.Main;
import a1.Sound;
import a1.gui.GUI;
import a1.gui.GUI_Button;
import a1.gui.GUI_Checkbox;
import a1.gui.GUI_ComboBox;
import a1.gui.GUI_Edit;
import a1.gui.GUI_Label;
import a1.gui.GUI_Panel;
import a1.gui.GUI_Scrollbar;
import a1.gui.GUI_Window;

public class dlg_Settings extends Dialog {
	public static dlg_Settings dlg = null;
	public GUI_Window wnd;
	public GUI_Edit fps_edit;
	public GUI_Button btn_ok;
	public GUI_Checkbox start_fullscreen, sound_enabled, debug_engine, count_objs, hide_overlapped,
    move_by_mouse;
	public GUI_Scrollbar music_vol, sound_vol;
	public GUI_Label lbl_resolution, lbl_fps, lbl1, lbl2, lbl_music, lbl_sound;
	public GUI_ComboBox resolution;
	public GUI_Button btn_ex_screen;
	
	public GUI_Panel main_panel, game_panel;
	public GUI_Button main_btn, game_btn;
	protected int panel_mode = 0;

	private boolean is_resolution_ex = false;

	public GUI_Edit width_edit, height_edit;

	static {
		Dialog.AddType("dlg_settings", new DialogFactory() {
			public Dialog create() {
				return new dlg_Settings();
			}
		});
	}

	public void DoShow() {
		dlg = this;
		wnd = new GUI_Window(GUI.getInstance().normal) {
			protected void DoClose() {
				dlg_Settings.dlg.wnd = null;
				Dialog.Hide("dlg_settings");
			};
		};
		wnd.caption = Lang.getTranslate("options", "wnd_caption");

		main_panel = new GUI_Panel(wnd);
		game_panel = new GUI_Panel(wnd);
		game_panel.visible = false;
		
		//-----------------------------------------------------------------------------
		main_btn = new GUI_Button(wnd) { 
			public void DoClick() {
				panel_mode = 0;
				main_panel.visible = true;
				game_panel.visible = false;
			};
		
			protected boolean getPressed() {
				return panel_mode == 0;
			};
		};	
		game_btn = new GUI_Button(wnd) {
			public void DoClick() {
				panel_mode = 1;
				main_panel.visible = false;
				game_panel.visible = true;
			};
		
			protected boolean getPressed() {
				return panel_mode == 1;
			};			
		};
		main_btn.caption = Lang.getTranslate("options", "main_tab");
		game_btn.caption = Lang.getTranslate("options", "game_tab");
		//-----------------------------------------------------------------------------
		lbl_resolution = new GUI_Label(main_panel);
		lbl_resolution.caption = Lang.getTranslate("options", "resolution");

		resolution = new GUI_ComboBox(main_panel);
		resolution.SetSize(200, 26);
		resolution.simple_hint = Lang.getTranslate("options", "apply_after");
		final List<Coord> modes = new ArrayList<Coord>();

		for (DisplayMode displayMode : Config.display_modes) {
			// ищем режим в доступных.
			boolean f = false;
			for (Coord m : modes) {
				if (m.x == displayMode.getWidth() && m.y == displayMode.getHeight()) {
					f = true;
					break;
				}
			}
			if (!f)
				modes.add(new Coord(displayMode.getWidth(), displayMode.getHeight()));
		}
		int i = 0;
		for (int j = 0; j < modes.size(); j++) {
			Coord m = modes.get(j);
			resolution.AddItem(Integer.toString(m.x) + "x" + Integer.toString(m.y));
			if (m.x == Config.ScreenWidth_to_save && m.y == Config.ScreenHeight_to_save)
				resolution.SetSelected(j);
			i++;
		}

		btn_ex_screen = new GUI_Button(main_panel) {
			public void DoClick() {
				if (!is_resolution_ex) {
                    btn_ex_screen.enabled = false;
					lbl1 = new GUI_Label(main_panel);
					lbl1.caption = Lang.getTranslate("options", "resolution_width");
					lbl1.SetPos(resolution.pos.x, resolution.pos.y + 35);

					width_edit = new GUI_Edit(main_panel);
					width_edit.SetPos(lbl1.pos.x, lbl1.pos.y + 20);
					width_edit.SetSize(100, 24);
					width_edit.SetText(Integer.toString(Config.ScreenWidth_to_save));
					width_edit.simple_hint = Lang.getTranslate("options", "apply_after");

					lbl2 = new GUI_Label(main_panel);
					lbl2.caption = Lang.getTranslate("options", "resolution_height");
					lbl2.SetPos(width_edit.pos.x, width_edit.pos.y + 35);

					height_edit = new GUI_Edit(main_panel);
					height_edit.SetPos(lbl2.pos.x, lbl2.pos.y + 20);
					height_edit.SetSize(100, 24);
					height_edit.SetText(Integer.toString(Config.ScreenHeight_to_save));
					height_edit.simple_hint = Lang.getTranslate("options", "apply_after");

					is_resolution_ex = true;
					resolution.SetSelected(-1);
					resolution.enabled = false;
					UpdateControlsPos();
				}
			};
		};
		btn_ex_screen.SetSize(70, 20);
		btn_ex_screen.caption = Lang.getTranslate("options", "ex_resolution");

		lbl_fps = new GUI_Label(main_panel);
		lbl_fps.caption = Lang.getTranslate("options", "fps_rate");

		fps_edit = new GUI_Edit(main_panel);
		fps_edit.SetSize(100, 24);
		fps_edit.SetText(Integer.toString(Config.FrameFate));
		fps_edit.simple_hint = Lang.getTranslate("options", "fps_rate_hint");

		start_fullscreen = new GUI_Checkbox(main_panel);
		start_fullscreen.SetSize(200, 21);
		start_fullscreen.caption = Lang.getTranslate("options", "start_fullscreen");
		start_fullscreen.checked = Config.StartFullscreen;
		start_fullscreen.simple_hint = Lang.getTranslate("options", "apply_after");

		debug_engine = new GUI_Checkbox(main_panel);
		debug_engine.SetSize(200, 21);
		debug_engine.caption = Lang.getTranslate("options", "debug_engine");
		debug_engine.checked = Config.DebugEngine;
		debug_engine.simple_hint = Lang.getTranslate("options", "apply_after");

		sound_enabled = new GUI_Checkbox(main_panel) {
			public void DoClick() {
				Config.SoundEnabled = sound_enabled.checked;
				if (!Config.SoundEnabled)
					Sound.StopAll();
				else
					Main.StartMusic();
			};
		};
		sound_enabled.SetSize(200, 21);
		sound_enabled.caption = Lang.getTranslate("options", "sound_enabled");
		sound_enabled.checked = Config.SoundEnabled;
		sound_enabled.simple_hint = Lang.getTranslate("options", "sounds_enabled_hint");

		lbl_music = new GUI_Label(main_panel);
		lbl_music.caption = Lang.getTranslate("options", "music_vol");

		music_vol = new GUI_Scrollbar(main_panel) {
			public void DoChange() {
				lbl_music.caption = Lang.getTranslate("options", "music_vol") + " (" + music_vol.getPos() + ")";
				float val = music_vol.getPos();
				SoundStore.get().setMusicVolume(val / 100);
			}
		};
		music_vol.SetVertical(false);
		music_vol.SetMax(110);
		music_vol.SetMin(0);
		music_vol.SetPageSize(10);
        music_vol.SetPos(Config.MusicVolume);

		lbl_sound = new GUI_Label(main_panel);
		lbl_sound.caption = Lang.getTranslate("options", "sound_vol");

		sound_vol = new GUI_Scrollbar(main_panel) {
			public void DoChange() {
				lbl_sound.caption = Lang.getTranslate("options", "sound_vol") + " (" + sound_vol.getPos() + ")";
				float val = sound_vol.getPos();
				SoundStore.get().setSoundVolume(val / 100);
			}
		};
		sound_vol.SetVertical(false);
		sound_vol.SetMax(110);
		sound_vol.SetMin(0);
		sound_vol.SetPageSize(10);
        sound_vol.SetPos(Config.SoundVolume);
		//-----------------------------------------------------------------------------
		count_objs = new GUI_Checkbox(game_panel) {
			public void DoClick() {
				Config.count_objs = count_objs.checked;
			};
		};
		count_objs.checked = Config.count_objs;
		count_objs.SetSize(200, 21);
		count_objs.caption = Lang.getTranslate("options", "many_plants");
		
		hide_overlapped = new GUI_Checkbox(game_panel) {
			public void DoClick() {
				Config.hide_overlapped = hide_overlapped.checked;
			}
		};
		hide_overlapped.checked = Config.hide_overlapped;
		hide_overlapped.SetSize(200, 21);
		hide_overlapped.caption = Lang.getTranslate("options", "hide_overlapped");

        move_by_mouse = new GUI_Checkbox(game_panel) {
            public void DoClick() {
                Config.move_inst_left_mouse = move_by_mouse.checked;
            }
        };
        move_by_mouse.checked = Config.move_inst_left_mouse;
        move_by_mouse.SetSize(200, 21);
        move_by_mouse.caption = Lang.getTranslate("options", "move_inst_left_mouse");
		
		//-----------------------------------------------------------------------------

		btn_ok = new GUI_Button(wnd) {
			public void DoClick() {
				if (is_resolution_ex) {
					try {
						Config.ScreenWidth_to_save = Integer.parseInt(width_edit.text);
					} catch (Exception e) {
						e.printStackTrace();
						Config.ScreenWidth_to_save = 1024;
					}
					try {
						Config.ScreenHeight_to_save = Integer.parseInt(height_edit.text);
					} catch (Exception e) {
						e.printStackTrace();
						Config.ScreenHeight_to_save = 768;
					}
				} else {
					if (resolution.GetSelected() >= 0 && resolution.GetSelected() < modes.size()) {
						Config.ScreenWidth_to_save = modes.get(resolution.GetSelected()).x;
						Config.ScreenHeight_to_save = modes.get(resolution.GetSelected()).y;
					} else {
						Config.ScreenWidth_to_save = Config.ScreenWidth;
						Config.ScreenHeight_to_save = Config.ScreenHeight;
					}
				}
				Config.StartFullscreen = start_fullscreen.checked;
				Config.SoundEnabled = sound_enabled.checked;
				Config.DebugEngine = debug_engine.checked;
				Config.MusicVolume = music_vol.getPos();
				Config.SoundVolume = sound_vol.getPos();
				Config.count_objs = count_objs.checked;
				Config.hide_overlapped = hide_overlapped.checked;
                Config.move_inst_left_mouse = move_by_mouse.checked;

				try {
					Config.FrameFate = Integer.parseInt(fps_edit.text);
				} catch (Exception e) {
					e.printStackTrace();
					Config.FrameFate = 0;
				}

				Config.save_options();
				Config.Apply();
				Dialog.Hide("dlg_settings");
			};
		};
		btn_ok.caption = Lang.getTranslate("generic", "ok");
		btn_ok.SetSize(100, 20);

		UpdateControlsPos();
	}

	public void UpdateControlsPos() {
		wnd.SetSize(500, 550);
		
		main_btn.SetSize(100, 30);
		main_btn.SetPos(10, 35);
		game_btn.SetSize(100, 30);
		game_btn.SetPos(120, 35);
		
		main_panel.SetSize(wnd.size.x, wnd.size.y-25);
		main_panel.SetPos(0, 55);
		game_panel.SetSize(wnd.size.x, wnd.size.y-25);
		game_panel.SetPos(0, 55);
		
		lbl_resolution.SetPos(15, 10);
		resolution.SetPos(lbl_resolution.pos.x, lbl_resolution.pos.y + 20);
		btn_ex_screen.SetPos(224, 59-25);

		if (is_resolution_ex)
			lbl_fps.SetPos(height_edit.pos.x, height_edit.pos.y + 35);
		else
			lbl_fps.SetPos(resolution.pos.x, resolution.pos.y + 35);

		fps_edit.SetPos(lbl_fps.pos.x, lbl_fps.pos.y + 20);
		start_fullscreen.SetPos(15, fps_edit.pos.y + 35);
		start_fullscreen.SetPos(15, fps_edit.pos.y + 35);
		debug_engine.SetPos(15, start_fullscreen.pos.y + 35);
		sound_enabled.SetPos(15, debug_engine.pos.y + 35);
		lbl_music.SetPos(resolution.pos.x, sound_enabled.pos.y + 35);
		music_vol.SetPos(15, lbl_music.pos.y + 20);
		lbl_sound.SetPos(resolution.pos.x, music_vol.pos.y + 35);
		sound_vol.SetPos(15, lbl_sound.pos.y + 20);
		sound_vol.SetWidth(wnd.Width() - 30);
		music_vol.SetWidth(wnd.Width() - 30);
		
		count_objs.SetPos(10, 10);
		hide_overlapped.SetPos(10, count_objs.pos.y+35);
        move_by_mouse.SetPos(10, hide_overlapped.pos.y+35);
		
		btn_ok.SetPos(wnd.size.x - btn_ok.size.x - 15, wnd.size.y - btn_ok.size.y - 15);
		wnd.Center();
	}

	public void DoHide() {
		dlg = null;
		if (wnd != null)
			wnd.Unlink();
		wnd = null;
	}

	public static boolean Exist() {
		return dlg != null;
	}


}
