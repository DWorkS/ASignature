package dev.dworks.libs.asignature;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Bezier {
	private Point controlPointOne;
	private Point controlPointTwo;
	private Point endPoint;
	private Point startPoint;
	private int drawSteps;
	private int mColor;

	public Bezier() {
	}

	public Bezier(Point startPoint, Point controlPointOne, Point controlPointTwo, Point endPoint) {
		this.startPoint = startPoint;
		this.controlPointOne = controlPointOne;
		this.controlPointTwo = controlPointTwo;
		this.endPoint = endPoint;
		drawSteps = ((int) (startPoint.distanceTo(controlPointOne) + controlPointOne.distanceTo(controlPointTwo) + controlPointTwo.distanceTo(endPoint)));
	}

	public void draw(Canvas canvas, Paint paint, float startWidth, float endWidth) {
		float originalWidth = paint.getStrokeWidth();
		float widthDelta = endWidth - startWidth;

		for (int i = 0; i < drawSteps; i++) {
			float t = ((float) i) / drawSteps;
			float tt = t * t;
			float ttt = tt * t;
			float u = 1 - t;
			float uu = u * u;
			float uuu = uu * u;

			float x = uuu * startPoint.x;
			x += 3 * uu * t * getControlPointOne().x;
			x += 3 * u * tt * getControlPointTwo().x;
			x += ttt * endPoint.x;

			float y = uuu * startPoint.y;
			y += 3 * uu * t * getControlPointOne().y;
			y += 3 * u * tt * getControlPointTwo().y;
			y += ttt * endPoint.y;

			paint.setColor(getColor());
			paint.setStrokeWidth(startWidth + ttt * widthDelta);
			canvas.drawPoint(x, y, paint);
		}

		paint.setStrokeWidth(originalWidth);
	}

	public int getColor() {
		return mColor;
	}

	public Point getControlPointOne() {
		return controlPointOne;
	}

	public Point getControlPointTwo() {
		return controlPointTwo;
	}

	public int getDrawSteps() {
		return drawSteps;
	}

	public Point getEndPoint() {
		return endPoint;
	}

	public Point getStartPoint() {
		return startPoint;
	}

	public void setColor(int color) {
		mColor = Color.BLACK;
	}

	public void setControlPointOne(Point point) {
		controlPointOne = point;
	}

	public void setControlPointTwo(Point point) {
		controlPointTwo = point;
	}

	public void setDrawSteps(int steps) {
		drawSteps = steps;
	}

	public void setEndPoint(Point point) {
		endPoint = point;
	}

	public void setStartPoint(Point point) {
		startPoint = point;
	}
}