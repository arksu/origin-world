package a1.gui.utils;

import a1.Coord;
import a1.gui.GUI_Control;
import a1.gui.GUI_DragControl;

public class DragInfo {
	public static final int DRAG_STATE_NONE = 0;
	// over empty space or controls that don't have drag'n'drop
	public static final int DRAG_STATE_MISS = 1;
	// over another Object that accept dropping on it
	public static final int DRAG_STATE_ACCEPT = 2;
	// over another Object that refuse dropping on it
	public static final int DRAG_STATE_REFUSE = 3;
	//--------------------------------------------------------------------	
	
	// состояние перетаскивания 
	public int state = DRAG_STATE_NONE;
	// перетаскиваемый контрол
	public GUI_DragControl drag_control = null;
	// смещение перетаскиваемого объекта относительно мыши
	public Coord hotspot = new Coord(0, 0);
	
	public void Reset() {
		state = DRAG_STATE_NONE;
		
		if (drag_control != null) {
			GUI_Control c = drag_control;
			drag_control = null;
			c.Unlink();
		}
		
		hotspot = new Coord(0, 0);
	}
}
