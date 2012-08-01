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

import org.lwjgl.input.Keyboard;

import a1.Config;
import a1.Connection;
import a1.DialogFactory;
import a1.Input;
import a1.Lang;
import a1.Main;
import a1.Render2D;
import a1.gui.GUI;
import a1.gui.GUI_Button;
import a1.gui.GUI_Checkbox;
import a1.gui.GUI_Edit;
import a1.gui.GUI_Label;
import a1.gui.GUI_Texture;
import a1.gui.GUI_Window;
import a1.net.NetLogin;
import a1.utils.Resource;
import static a1.Main.*;

public class dlg_Login extends Dialog {
	public static dlg_Login dlg = null;
	
	GUI_Window wnd;
	GUI_Edit user;
	GUI_Edit pass;	
	GUI_Button btn;
	GUI_Label status;
	GUI_Button btn_lang;
	GUI_Checkbox save_pass;
	GUI_Texture logo;

	static {
		Dialog.AddType("dlg_login", new DialogFactory() {		
			public Dialog create() {
				return new dlg_Login();
			}
		});
	}
	
	static public void ShowLogin() {
		if (Resource.srv_versions.get("client") != Config.CLIENT_VERSION) {
			Dialog.Show("dlg_version");
		} else {
			Dialog.Show("dlg_login");
		}
	}
	
	public void DoShow() {
		dlg = this;
		Main.StartMusic();
		
		wnd = new GUI_Window(GUI.getInstance().normal);
		wnd.SetSize(300, 200);
		wnd.caption = Lang.getTranslate("login", "login");
		wnd.set_close_button(false);
		wnd.resizeable = false;
		
		logo = new GUI_Texture(GUI.getInstance().normal);
		logo.setTexture(Resource.getTexture("origin_logo"));
		logo.SetSize(512, 128);
		
		
		user = new GUI_Edit(wnd){
			public void DoEnter() {
				Login();
			};
		};
		user.SetPos(10, 50);
		user.SetSize(200,24);
		user.SetText(Config.user);
		user.CenterX();
		
		pass = new GUI_Edit(wnd) {
			public void DoEnter() {
				Login();
			};
		};
		pass.is_pass = true;
		pass.SetPos(10, user.pos.y + 30);
		pass.SetSize(200, 24);
		pass.SetText(Config.pass);
		pass.CenterX();
		save_pass = new GUI_Checkbox(wnd);
		save_pass.SetPos(pass.pos.x, pass.pos.y+30);
		save_pass.SetSize(200, 21);
		save_pass.caption = Lang.getTranslate("login", "save_pass");
		save_pass.checked = Config.save_pass;
		
		btn = new GUI_Button(wnd) {
			public void DoClick() {
				Login();
			}
		};
		btn.SetPos(10,save_pass.pos.y + 30);
		btn.SetSize(200, 20);
		btn.caption = Lang.getTranslate("login", "login");
		btn.CenterX();
		
		status = new GUI_Label(wnd);
		status.SetPos(user.pos.x, btn.pos.y + 30);
		status.SetSize(200, 50);
		status.align = Render2D.Align_HCenter + Render2D.Align_Top;

		btn_lang = new GUI_Button(GUI.getInstance().normal){
			public void DoClick() {
				Dialog.HideAll();
				Dialog.Show("dlg_language");
			};
		};
		btn_lang.caption = Config.current_lang;
		btn_lang.SetSize(80, 20);
		
		if (Config.quick_login_mode) Login();
	}
	
	@Override
	public void DoResolutionChanged() {
		wnd.Center();
		logo.SetPos(100, 100);
		logo.CenterX();
		btn_lang.SetPos(Config.ScreenWidth - btn_lang.size.x - 10, Config.ScreenHeight - btn_lang.size.y - 10);
	}

	public void DoHide() {
		dlg = null;
		wnd.Unlink();
		wnd = null;
		btn_lang.Unlink();
		btn_lang = null;
		logo.Unlink();
		logo = null;
	}
	
	public void DoUpdate() {
		if (Input.KeyHit(Keyboard.KEY_TAB)) {
			Input.RemoveHit(Keyboard.KEY_TAB);
//			if (!user.isFocused() && !pass.isFocused())
//				GUI.getInstance().SetFocus(user);
			if (!user.isFocused())
				GUI.getInstance().SetFocus(user);
			else if (!pass.isFocused()) 
					GUI.getInstance().SetFocus(pass);
		}
		
		if (Input.KeyHit(Keyboard.KEY_RETURN) 
				&& !Input.isAltPressed() 
				&& !Input.isCtrlPressed() 
				&& !Input.isShiftPressed()) {
			Login();
		}
		
		if (NetLogin.error_text.length() > 0)
			status.caption = Lang.getTranslate("net_error", NetLogin.error_text);
		else
			if (LoginConnect != null)
				if (LoginConnect.Alive())
					status.caption = Lang.getTranslate("net_error",LoginConnect.GetStateDesc());
				else {
					status.caption = Lang.getTranslate("net_error",
							Connection.GetErrorReason(LoginConnect.network_error));
				}
			else
				status.caption = Lang.getTranslate("net_error","disconnected");
	}

	public void Login() {
		if (user.text.isEmpty() || pass.text.isEmpty()) {
			Config.quick_login_mode = false;
			return;
		}
		
		//Config.quick_login_mode = false;
		Config.user = user.text;
		Config.pass = pass.text;
		Config.save_pass = save_pass.checked;
		Config.save_options();
		btn.enabled = false;
		if (Main.LoginConnect != null) {
			Main.LoginConnect.Close();
			Main.LoginConnect = null;
		}
		NetLogin.login = user.text;
		NetLogin.pwd = pass.text;
		NetLogin.error_text = "";
		NetLogin.cookie = null;
		NetLogin.game_server = "";
		Main.LoginConnect = new Connection(Config.login_server, Config.login_server_port);
	}

	public static boolean Exist() {
		return dlg != null;
	}
	
}
