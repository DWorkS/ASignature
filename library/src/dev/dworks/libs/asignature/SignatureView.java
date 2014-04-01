package dev.dworks.libs.asignature;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SignatureView extends View {
	private static final float STROKE_WIDTH = 5f;
	private static final float MAX_STROKE_WIDTH_FACTOR = 1.2F;
	private static final float MIN_STROKE_WIDTH_FACTOR = 0.4F;
	
	private static final float MAX_VELOCITY = 6.0F;
	private static final float MIDDLE_VELOCITY = 3.0F;
	private static final float MIN_VELOCITY = 0.0F;
	private static final float VELOCITY_FILTER_WEIGHT = 0.5F;
	private static final float VELOCITY_RANGE = 6.0F;

	private int stateToSave;
	private int color = Color.BLACK;
	private Bitmap bitmap;
	private final Paint borderPaint;
	private Paint penPaint;
	private Canvas canvas;
	private Point cropBotRight;
	private Point cropTopLeft;
	private int pointIndex = 0;
	private ArrayList<Point> points = new ArrayList<Point>();
	private final float strokeWidth;
	private final float desiredDash;
	private float currentX;
	private float currentY;
	private float lastWidth = strokeWidth(3.0F);
	private float lastVelocity = MIDDLE_VELOCITY;
	private final float maxStrokeWidth;
	private final float minStrokeWidth;
	boolean empty;
	  
	public SignatureView(Context paramContext) {
		this(paramContext, null);
	}

	public SignatureView(Context paramContext, AttributeSet paramAttributeSet) {
		this(paramContext, paramAttributeSet, 0);
	}

	public SignatureView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		setFocusable(true);
		penPaint = new Paint();
		penPaint.setAntiAlias(true);
		penPaint.setColor(Color.BLACK);
		penPaint.setStrokeWidth(5.0F);
		penPaint.setStrokeJoin(Paint.Join.ROUND);
		penPaint.setStrokeCap(Paint.Cap.ROUND);
	    penPaint.setStyle(Paint.Style.STROKE);
	      
		currentY = (0.0F / 0.0F);
		currentX = (0.0F / 0.0F);
		strokeWidth = STROKE_WIDTH;
		desiredDash = 10.0F;
		borderPaint = new Paint();
		borderPaint.setColor(Color.BLACK);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(strokeWidth);
		
		maxStrokeWidth = (MIN_STROKE_WIDTH_FACTOR * strokeWidth);
		minStrokeWidth = (MAX_STROKE_WIDTH_FACTOR * strokeWidth);
	}

	public void addBezier(Bezier curve, float lastWidth, float newWidth) {
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
			canvas = new Canvas(bitmap);
		}
		curve.draw(canvas, penPaint, lastWidth, newWidth);
	}

	public void addPoint(Point newPoint) {
/*		if ((newPoint.getX() < cropTopLeft.getX()) && (newPoint.getX() >= 0.0F))
			cropTopLeft.setX(newPoint.getX());
		if ((newPoint.getY() < cropTopLeft.getY()) && (newPoint.getY() >= 0.0F))
			cropTopLeft.setY(newPoint.getY());
		if ((newPoint.getX() > cropBotRight.getX()) && (newPoint.getX() <= canvas.getWidth()))
			cropBotRight.setX(newPoint.getX());
		if ((newPoint.getY() > cropBotRight.getY()) && (newPoint.getY() <= canvas.getHeight()))
			cropBotRight.setY(newPoint.getY());*/
		//points.add(newPoint);
		
		Point endPoint = null;
		if (points.size() > 0) {
			endPoint = (Point) points.get(points.size() - 1);
			if ((endPoint.x != newPoint.x) || (endPoint.y != newPoint.y)){
				if (endPoint.time != newPoint.time) {
					points.add(newPoint);
				}
			}
		}
		else{
			points.add(newPoint);
		}
		drawPoints();
	}

	public void clear() {
		if (canvas == null)
			return;
		//if (!empty) {
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			empty = true;
			invalidate();
		//}
	}

	public void drawBitmap(Bitmap paramBitmap) {
		clear();
		if ((paramBitmap != null) && (canvas != null) && (canvas.getWidth() != 0) && (canvas.getHeight() != 0)) {
			Matrix localMatrix = new Matrix();
			localMatrix.setRectToRect(new RectF(0.0F, 0.0F, paramBitmap.getWidth(), paramBitmap.getHeight()),
					new RectF(0.0F, 0.0F, canvas.getWidth(), canvas.getHeight()), Matrix.ScaleToFit.CENTER);
			canvas.drawBitmap(paramBitmap, localMatrix, null);
			empty = false;
		}
		invalidate();
	}

	public void drawPoints() {
		if ((points.size() >= 4) && (4 + pointIndex <= points.size())) {
			Point startPoint = (Point) points.get(pointIndex);
			Point controlPoint1 = (Point) points.get(1 + pointIndex);
			Point controlPoint2 = (Point) points.get(2 + pointIndex);
			Point endPoint = (Point) points.get(3 + pointIndex);
			Bezier bezier = new Bezier(startPoint, controlPoint1, controlPoint2, endPoint);
			bezier.setColor(Color.GREEN);
			//float width = strokeWidth(8.0F / endPoint.velocityFrom(startPoint));
			float velocity = 0.5F * endPoint.velocityFrom(startPoint) + 0.5F * lastVelocity;
          	float width = strokeWidth(8.0F/velocity);
			addBezier(bezier, lastWidth, width);
			invalidate();
			lastWidth = width;
			lastVelocity = velocity;
			pointIndex = (3 + pointIndex);
			empty = false;
		}
		else {
			//TODO draw dot
			Point startPoint = (Point) points.get(0);
			Point endPoint = (Point) points.get(0);
			Point controlPoint1 = startPoint;
			Point controlPoint2 = endPoint;
			Bezier bezier = new Bezier(startPoint, controlPoint1, controlPoint2, endPoint);
			float width = strokeWidth(8.0F / endPoint.velocityFrom(startPoint));
			addBezier(bezier, lastWidth, width);
			invalidate();
		}
	}

	public boolean isEmpty() {
		return empty;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public int getColor() {
		return color;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (bitmap != null)
			canvas.drawBitmap(bitmap, 0.0F, 0.0F, null);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = View.MeasureSpec.getSize(widthMeasureSpec);
		int height = View.MeasureSpec.getSize(heightMeasureSpec);
		cropTopLeft = new Point(width, height);
		cropBotRight = new Point(0.0F, 0.0F);
		setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Bitmap currentBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(currentBitmap);
		float interalON = 2.0F * (canvas.getWidth() + canvas.getHeight() - 2.0F * strokeWidth);
		float fintervalOFF = interalON * desiredDash / (Math.round(interalON / (4.0F * desiredDash)) * (4.0F * desiredDash));
		Paint paint = borderPaint;
		float[] intervals = new float[]{interalON, fintervalOFF};
		paint.setPathEffect(new DashPathEffect(intervals, fintervalOFF / 2.0F));
		clear();
		if (bitmap != null) {
			Rect rect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
			canvas.drawBitmap(bitmap, null, rect, null);
			empty = false;
		}
		bitmap = currentBitmap;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
/*		int i = 0xFF & event.getAction();
		if (i == 0) {
			currentX = event.getX();
			currentY = event.getY();
			addPoint(new Point(currentX, currentY, event.getEventTime()));
			getParent().requestDisallowInterceptTouchEvent(true);
		}
		// while (m_Empty) {
		if ((i == 1) || (i == 3)) {
			currentY = (0.0F / 0.0F);
			currentX = (0.0F / 0.0F);
			points.clear();
			pointIndex = 0;
			getParent().requestDisallowInterceptTouchEvent(false);
		}
		// if ((m_Points.size() < 4) || (4 + m_PointIndex >
		// m_Points.size()))
		// while (1 + m_PointIndex <= m_Points.size())
		drawPoints();
		if ((i == 2) || (i == 1)) {
			for (int j = 0; j < event.getHistorySize(); j++)
				addPoint(new Point(event.getHistoricalX(j), event.getHistoricalY(j), event.getHistoricalEventTime(j)));
			addPoint(new Point(event.getX(), event.getY(), event.getEventTime()));

		}
		// }
*/		
		int action = event.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			currentX = event.getX();
			currentY = event.getY();
			addPoint(new Point(currentX, currentY, event.getEventTime()));
			getParent().requestDisallowInterceptTouchEvent(true);
			return true;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if(!empty){
				currentY = 0;
				currentX = 0;
				points.clear();
				pointIndex = 0;
				getParent().requestDisallowInterceptTouchEvent(false);
			}
			break;
			
		case MotionEvent.ACTION_MOVE:
			drawPoints();
			for (int j = 0; j < event.getHistorySize(); j++)
				addPoint(new Point(event.getHistoricalX(j), event.getHistoricalY(j), event.getHistoricalEventTime(j)));
			addPoint(new Point(event.getX(), event.getY(), event.getEventTime()));
			break;

		default:
			Log.d("default", "Ignored touch event: " + event.toString());
			return false;
		}
		return true;
	}

	public void setColor(int paramInt) {
		color = Color.BLACK;
	}

	public Point getCropBotRight() {
		return cropBotRight;
	}

	public Point getCropTopLeft() {
		return cropTopLeft;
	}

	// public float strokeWidth(float paramFloat) {
	// if (paramFloat > 11.0F)
	// paramFloat = 10.0F;
	// if (paramFloat < 5.0F)
	// paramFloat = 6.0F;
	// return paramFloat;
	// }

	public float strokeWidth(float paramFloat) {
		if (paramFloat > 11.0F)
			paramFloat = 10.0F;
		
		if (paramFloat < 5.0F)
			paramFloat = 6.0F;
		return paramFloat;
	}
	
	/*private float strokeWidth(float velocity) {
		float strokeWidth = maxStrokeWidth - maxStrokeWidth * (velocity / MAX_VELOCITY);
		if (strokeWidth < minStrokeWidth)
			strokeWidth = minStrokeWidth;
		return Math.max(strokeWidth, minStrokeWidth);
		//return Math.max(maxStrokeWidth / velocity, minStrokeWidth);
	}*/

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("instanceState", super.onSaveInstanceState());
		bundle.putInt("stateToSave", stateToSave);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			stateToSave = bundle.getInt("stateToSave");
			state = bundle.getParcelable("instanceState");
		}
		super.onRestoreInstanceState(state);
	}
}