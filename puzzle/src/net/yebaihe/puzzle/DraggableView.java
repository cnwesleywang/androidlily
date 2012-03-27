package net.yebaihe.puzzle;

public interface DraggableView {
	public void beforeDrag();

    public DragView createDragView();
    public Object   getDraggedInfo();

    public void afterDrop(float x, float y);
}
