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

package a1.net;

public class NetPacketsID {
	static final int GAMESERVER_HELO = 100;
	static final int GAMESERVER_COOKIE = 101;
	static final int GAMESERVER_PING = 102;
	static final int GAMESERVER_PONG = 103;
	static final int GAMESERVER_LOGGED = 104;
	static final int GAMESERVER_SET_KIN_INFO = 105;
	static final int GAMESERVER_OBJ_MOVE = 106;
	static final int GAMESERVER_OBJ_DELETE = 107;
	static final int GAMESERVER_MAP_DATA = 108;
	static final int GAMESERVER_MAP_CLICK = 109;
	static final int GAMESERVER_OBJ_CLICK = 110;
	static final int GAMESERVER_IACT_CLICK = 111;
	static final int GAMESERVER_OBJ_LINE_MOVE = 112;
	static final int GAMESERVER_OBJ_DELETE_PARAM = 113;
	static final int GAMESERVER_SET_DRAWABLE = 114;
	static final int GAMESERVER_CONTEXT_MENU = 115;
	static final int GAMESERVER_CONTEXT_ACTION = 116;
	static final int GAMESERVER_SETTINGS = 117;
	static final int GAMESERVER_PROGRESS = 118;
	static final int GAMESERVER_CLIENT_SAY = 119;
	static final int GAMESERVER_SERVER_SAY = 120;
	static final int GAMESERVER_ACTIONS_LIST = 121;
	static final int GAMESERVER_ACTION = 122;
	static final int GAMESERVER_SET_FOLLOW = 123;
	static final int GAMESERVER_SET_LIGHT = 124;
	static final int GAMESERVER_CURSOR = 125;
	static final int GAMESERVER_GUI_ADD = 126;
	static final int GAMESERVER_GUI_REMOVE = 127;
	static final int GAMESERVER_GUI_UPDATE = 128;
	static final int GAMESERVER_GUI_CLICK = 129;
	static final int GAMESERVER_GUI_DESTROY = 130;
	static final int GAMESERVER_PLACE_OBJECT = 131;
	static final int GAMESERVER_SET_OPENED = 132;
	static final int GAMESERVER_OBJ_CLEAR_PARAMS = 133;
	static final int GAMESERVER_SYSTEM_MSG = 134;
	static final int GAMESERVER_CRAFT_LIST = 135;
	static final int GAMESERVER_CRAFT_CLICK = 136;
	static final int GAMESERVER_GAIN_EXP = 137;
	static final int GAMESERVER_FLY_TEXT = 138;
	static final int GAMESERVER_BUFF_ADD = 139;
	static final int GAMESERVER_BUFF_DELETE = 140;
	static final int GAMESERVER_TARGET = 141;
	static final int GAMESERVER_TARGET_RESET = 142;
	static final int GAMESERVER_REUSE_TIME = 143;
	static final int GAMESERVER_SET_PARAM = 144;
	static final int GAMESERVER_CLIENT_SPEED = 145;
	static final int GAMESERVER_SET_SPEED = 146;
	static final int GAMESERVER_SET_PLAYER_PARAM = 147;
    static final int GAMESERVER_OBJ_TYPE = 148;
    static final int GAMESERVER_BUG_REPORT = 149;
    static final int GAMESERVER_KNOWLEDGE = 150;
    static final int GAMESERVER_KNOWLEDGE_INC = 151;
    static final int GAMESERVER_KNOWLEDGE_DEC = 152;
    static final int GAMESERVER_SKILL_BUY = 153;
    static final int GAMESERVER_DIALOG_OPEN = 154;
    static final int GAMESERVER_DIALOG_CLOSE = 155;
    static final int GAMESERVER_OBJECT_CLOSE = 156;
    static final int GAMESERVER_OBJECT_VISUAL_STATE = 157;
    static final int GAMESERVER_OBJECT_VISUAL_STATE_ACK = 158;
    static final int GAMESERVER_CLAIM_REMOVE = 159;
    static final int GAMESERVER_CLAIM_CHANGE = 160;
    static final int GAMESERVER_CLAIM_EXPAND = 161;
}
