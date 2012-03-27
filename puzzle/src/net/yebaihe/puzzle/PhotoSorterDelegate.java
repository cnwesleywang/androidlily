package net.yebaihe.puzzle;

import net.yebaihe.puzzle.MultiTouchController.PointInfo;
import net.yebaihe.puzzle.PhotoSorterView.Img;

public interface PhotoSorterDelegate {

	void imagePositionChange(Img img, float x, float y, PointInfo touchPoint);
	void sizeChanged();

}
