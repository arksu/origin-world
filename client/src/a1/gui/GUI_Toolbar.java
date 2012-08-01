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
package a1.gui;

import static a1.gui.Skin.StateHighlight;
import static a1.gui.Skin.StateNormal;

import java.util.ArrayList;

import a1.Coord;
import a1.Input;
import a1.utils.AppSettings;

public class GUI_Toolbar extends GUI_Control {
	/**
	 * Размер одного слота (в пикселях)
	 */
	public static final int SLOT_SIZE = 32;
	/**
	 * Размер расстояния между слотами и между слотами и окна (в пикселях)
	 */
	public static final int BORDER_SIZE = 2;
	/**
	 * Размер области для технических нужд (перетаскивание)
	 */
	public static final int ACTION_REGION_SIZE = 16;
	
	public static final int MINIMUM_SLOTS = 1;
	public static final int MAXIMUM_SLOTS = 12;
	public static final int DEFAULT_SLOTS = 12;

	/**
	 * Время (в миллисекундах), через которое покажутся элементы управления
	 */
	public static final int ACTION_REACTION_TIME = 1000 * 3;
	/**
	 * Размер кнопок действий (поворот, лок)
	 */
	public static final int ACTION_BUTTON_SIZE = 16;
	
	
	/**
	 * Массив слотов данного тулбара
	 */
	protected ArrayList<GUI_ToolbarSlot> toolbarSlots;
	/**
	 * Имя тулбара (для сейв/лоада состояния)
	 */
	protected String toolbarName;
	
	/**
	 * Контрол для технического обслуживания (перемещение, вызов опций)
	 */
	protected GUI_Control actionControl;
	/**
	 * Контрол для ресайза
	 */
	protected GUI_Control resizeControl;
	/**
	 * Время когда мышь зашла на контрол
	 */
	protected long mouseHoverStarted = 0;
	
	/**
	 * Размер области для расширения (изменяется динамически)
	 */
	protected int extendedRegionWidth = 0;
	
	/**
	 * Минимальное количество слотов
	 */
	protected int minSlotsCount = 4;
	
	/**
	 * Координата последнего сдвига во время ресайза
	 */
	protected Coord resizeLastBite = Coord.z;
	
	/* Toolbar state */
	protected boolean isHorizontal = true;		// В горизонтальном ли тулбар режиме
	protected boolean isLocked = false;			// Заблокирован ли тулбар
	protected boolean isExtended = false;		// Показаны ли кнопки лока и ротейта
	
	/**
	 * Создает новый тулбар
	 * @param parent Родительский элемент
	 * @param name Уникальное название тулбара (нужно для сейва/загрузки состояния в конфиг)
	 * @param defaultPosition Дефолтная позиция относительно родительского элемента (используется если не удалось загрузить предустановки)
	 */
	public GUI_Toolbar(GUI_Control parent, String name, Coord defaultPosition) {
		super(parent);
		toolbarName = name;
		toolbarSlots = new ArrayList<GUI_ToolbarSlot>();
		SetPos(defaultPosition);
		actionControl = new GUI_Control(this) {
			public boolean DoMouseBtn(int btn, boolean down) {
				if (((GUI_Toolbar)parent).isLocked()) return false;
				if (btn == Input.MB_LEFT) {
					if (down) {
						if (MouseInMe()) {
							parent.BeginDragMove();
							return true;
						}				
					} else
						parent.EndDragMove();
				}
				return false;
			}
		};
		resizeControl = new GUI_Control(this) {
			private boolean isResized = false;
			
			public boolean DoMouseBtn(int btn, boolean down) {
				if (((GUI_Toolbar)parent).isLocked()) return false;
				if (btn == Input.MB_LEFT) {
					if (down) {
						if (MouseInMe())
							isResized = true;		
					} else {
						isResized = false;
						((GUI_Toolbar)parent).resizeEnded();
					}
				}
				return false;
			}
			
			@Override
			public void DoUpdate() {
				if (isResized) {
					((GUI_Toolbar)parent).sizeChanged();
				}
			}
			
			@Override
			public void DoRender() {
				getSkin().Draw("window_resize_right", abs_pos.x, abs_pos.y, size.x, size.y, ((isResized || MouseInMe()) ? StateHighlight : StateNormal));
			}
		};
		resizeControl.SetSize(getSkin().GetElementSize("window_resize_right"));
		
		loadToolbarState();
		loadSlots(computeSlotsCountBasedOnSize());
		recalcSize();
	}
	
	protected void saveToolbarState() {
		// Сохранить позицию
		{
			AppSettings.putCharacterValue("DEFAULT_PLAYER", toolbarName + "_position_x", pos.x);
			AppSettings.putCharacterValue("DEFAULT_PLAYER", toolbarName + "_position_y", pos.y);
		}
		// Сохранить размер
		{
			AppSettings.putCharacterValue("DEFAULT_PLAYER", toolbarName + "_size_x", size.x);
			AppSettings.putCharacterValue("DEFAULT_PLAYER", toolbarName + "_size_y", size.y);
		}
		// Настройки
		{
			AppSettings.putCharacterValue("DEFAULT_PLAYER", toolbarName + "_horizontal", isHorizontal);
			AppSettings.putCharacterValue("DEFAULT_PLAYER", toolbarName + "_locked", isLocked);
		}
	}
	
	/**
	 * Загрузка состояния тулбара из конфига
	 */
	protected void loadToolbarState() {
		// Восстановить позицию
		{
			int px = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", toolbarName + "_position_x", Integer.class);
			int py = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", toolbarName + "_position_y", Integer.class);
			if (px != 0 && py != 0) SetPos(px, py);
		}
		// Восстановить размер
		{
			int sx = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", toolbarName + "_size_x", Integer.class);
			int sy = (Integer)AppSettings.getCharacterValue("DEFAULT_PLAYER", toolbarName + "_size_y", Integer.class);
			if (sx == 0 && sy == 0) {
                sx = 92;
                sy = 39;
            }
            if (sx != 0 && sy != 0) SetSize(sx, sy);
		}
		// Настройки
		{
			isHorizontal = (Boolean)AppSettings.getCharacterValue("DEFAULT_PLAYER", toolbarName + "_horizontal", true, Boolean.class);
			isLocked = (Boolean)AppSettings.getCharacterValue("DEFAULT_PLAYER", toolbarName + "_locked", false, Boolean.class);
		}
	}

	

	
	/**
	 * Пересчитать размеры и положение элементов в зависимости от состояния тулбара
	 */
	public void recalcSize() {
		// Recalc actionControl size and position
		int translateExtended = BORDER_SIZE + (extendedRegionWidth + BORDER_SIZE) * (extendedRegionWidth == 0 ? 0 : 1);
		if (isHorizontal) {
			actionControl.SetPos(translateExtended, BORDER_SIZE);
			actionControl.SetSize(ACTION_REGION_SIZE, SLOT_SIZE);
		} else {
			actionControl.SetPos(BORDER_SIZE, translateExtended);
			actionControl.SetSize(SLOT_SIZE, ACTION_REGION_SIZE);
		}
		// Recalc slots
		for (int i = 0; i < slotsCount(); i++) {
			int position = translateExtended + ACTION_REGION_SIZE + (BORDER_SIZE + SLOT_SIZE) * i;
			toolbarSlots.get(i).SetPos(isHorizontal ? position : BORDER_SIZE, isHorizontal ? BORDER_SIZE : position);
		}
		// Recalc toolbar size
		int size = translateExtended + BORDER_SIZE + ACTION_REGION_SIZE + (BORDER_SIZE + SLOT_SIZE) * slotsCount();
		if (isHorizontal) {
			SetSize(size, BORDER_SIZE * 2 + SLOT_SIZE + 1);
		} else {
			SetSize(BORDER_SIZE * 2 + SLOT_SIZE + 1, size);
		}
		// Recalc resizeControl
		{
			int px = this.size.x - resizeControl.size.x - 1;
			int py = this.size.y - resizeControl.size.y - 1;
			resizeControl.SetPos(px, py);
		}
		// Recalc action buttons
		if (btnRotate != null) {
			btnRotate.SetPos(isHorizontal ? BORDER_SIZE : ACTION_BUTTON_SIZE + BORDER_SIZE, BORDER_SIZE);
		}
		if (btnLock != null) {
			btnLock.SetPos(BORDER_SIZE, isHorizontal ? ACTION_BUTTON_SIZE + BORDER_SIZE : BORDER_SIZE);
		}
	}
	
	@Override
	public void DoRender() {
		getSkin().Draw("window", abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
	}
	
	@Override
	public void DoDestroy() {
		for (GUI_ToolbarSlot slot : toolbarSlots) {
			slot.Unlink();
		}
		saveToolbarState();
	}

	@Override
	public void DoUpdate() {
		if (MouseInMe() || MouseInChilds()) {
			if (mouseHoverStarted == 0) {
				mouseHoverStarted = System.currentTimeMillis();
			} else {
				if (System.currentTimeMillis() - mouseHoverStarted >= ACTION_REACTION_TIME) {
					updateSettings(true);
				}
			}
		} else {
			mouseHoverStarted = 0;
			updateSettings(false);
		}
	}
	
	/**
	 * Загрузка состояния слотов из конфига
	 */
	protected void loadSlots(int slotsCount) {
		if (toolbarSlots != null && toolbarSlots.size() > slotsCount) {
			while (toolbarSlots.size() > slotsCount) {
				toolbarSlots.get(toolbarSlots.size() - 1).Unlink();
				toolbarSlots.remove(toolbarSlots.size() - 1);
			}
		}
		if (slotsCount > toolbarSlots.size()) {
			for (int i = toolbarSlots.size(); i < slotsCount; i++) {
				System.out.println(i);
				toolbarSlots.add(new GUI_ToolbarSlot(this, i));
			}
		}
	}
	
	/**
	 * Вычисляет количество слотов которое умещается в заданный размер, 
	 * также устанавливает размер в минимально допустимый для данного количества слотов
	 * @return Допустимое количество слотов для данного размера
	 */
	protected int computeSlotsCountBasedOnSize() {
		int size = isHorizontal ? this.size.x : this.size.y;
		int slots = (int)((size - (BORDER_SIZE * 2 + ACTION_REGION_SIZE)) / (BORDER_SIZE + SLOT_SIZE));
		int ret = slots >= MINIMUM_SLOTS ? slots : MINIMUM_SLOTS;
		ret = ret <= MAXIMUM_SLOTS ? slots : MAXIMUM_SLOTS;
		return ret;
	}

	
	/**
	 * Возвращает текущее количество слотов в данном тулбаре
	 * @return Количество слотов
	 */
	public int slotsCount() {
		return toolbarSlots.size();
	}
	
	/**
	 * Изменяет направление тулбара (вертикальный/горизонтальный)
	 */
	public void rotate() {
		if (isLocked()) return;
		isHorizontal = !isHorizontal;
		recalcSize();
	}
	
	/**
	 * Блокирует тулбар
	 */
	public void lock() {
		isLocked = !isLocked;
	}
	
	/**
	 * Заблокирован ли тулбар
	 * @return true если заблокирован
	 */
	public boolean isLocked() {
		return isLocked;
	}
	
	/**
	 * Обновить контролы поворота и лока
	 * @param show показать контролы если true
	 */
	protected GUI_IconButton btnRotate, btnLock;
	protected void updateSettings(boolean show) {
		if ((!isExtended && !show) || (show && isExtended)) return;
		if (show) {
			isExtended = true;
			extendedRegionWidth = 16;
			btnRotate = new GUI_IconButton(this) {
				@Override
				public void DoClick() {
					((GUI_Toolbar)parent).rotate();
				}
			};
			btnRotate.SetIcon("btn_rotate");
			btnRotate.SetSize(ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE);
			btnLock = new GUI_IconButton(this) {
				@Override
				public void DoClick() {
					((GUI_Toolbar)parent).lock();
				}
			};
			btnLock.SetIcon("btn_rotate");
			btnLock.SetSize(ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE);
		} else {
			isExtended = false;
			extendedRegionWidth = 0;
			if (btnRotate != null) btnRotate.Unlink();
			if (btnLock != null) btnLock.Unlink();
		}
		recalcSize();
	}
	
	
	/**
	 * Обработчик изменени размера при ресайзе тулбара
	 */
	protected void sizeChanged() {
		if (resizeLastBite == Coord.z) resizeLastBite = new Coord(gui.mouse_pos);
		Coord current = gui.mouse_pos;
		Coord delta = resizeLastBite.sub(current);
		int dt = isHorizontal ? delta.x : delta.y;
		int slots = (int)Math.round((double)dt / (SLOT_SIZE + BORDER_SIZE));
		if (slots != 0) {
			if (slotsCount() - slots > MINIMUM_SLOTS) {
				resizeLastBite = new Coord(current);
				loadSlots(slotsCount() - slots);
				recalcSize();
			}
		}
	}
	
	/**
	 * Остановка ресайза
	 */
	protected void resizeEnded() {
		resizeLastBite = Coord.z;
	}
}
