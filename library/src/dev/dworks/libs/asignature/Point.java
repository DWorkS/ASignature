package dev.dworks.libs.asignature;

import android.util.FloatMath;

public class Point {
    public long time;
    public float x;
    public float y;

    public Point(float x, float y) {
    	this.x = x;
    	this.y = y;
    }

    public Point(float x, float y, long time) {
    	this.x = x;
    	this.y = y;
    	this.time = time;
    }

    protected float distanceTo(Point paramPoint) {
        float f1 = x - paramPoint.x;
        float f2 = y - paramPoint.y;
        return FloatMath.sqrt(f1 * f1 + f2 * f2);
    }

    public void setX(float paramFloat) {
        x = paramFloat;
    }

    public void setY(float paramFloat) {
        y = paramFloat;
    }

    public float velocityFrom(Point paramPoint) {
            return distanceTo(paramPoint) / (time - paramPoint.time);
    }
}