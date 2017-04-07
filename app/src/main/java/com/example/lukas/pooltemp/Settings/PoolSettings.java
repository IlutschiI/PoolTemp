package com.example.lukas.pooltemp.Settings;

/**
 * Created by wicki on 24.09.2016.
 */
public class PoolSettings {

    public static String CUBIC_CURVE="cubicCurve";
    public static String XAXIS_ENABLED="xAxisEnabled";
    public static String YAXIS_ENABLED="yAxisEnabled";
    public static String ZOOMING_MULTIPLIER="zoomingMultiplier";
    public static String NUMBER_OF_POINTS="numberOfPoints";
    public static String ANIMATION_DURATION="animationDuration";


    private boolean cubicCurves=true;
    private float zoomingMultiplier;
    private boolean xAxisEnabled=true;
    private boolean yAxisEnabled=true;
    private int numberOfPoints;
    private int animationDuration;

    public PoolSettings() {
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public boolean isCubicCurves() {
        return cubicCurves;
    }

    public void setCubicCurves(boolean cubicCurves) {
        this.cubicCurves = cubicCurves;
    }

    public float getZoomingMultiplier() {
        return zoomingMultiplier;
    }

    public void setZoomingMultiplier(float zoomingMultiplier) {
        this.zoomingMultiplier = zoomingMultiplier;
    }

    public boolean isxAxisEnabled() {
        return xAxisEnabled;
    }

    public void setxAxisEnabled(boolean xAxisEnabled) {
        this.xAxisEnabled = xAxisEnabled;
    }

    public boolean isyAxisEnabled() {
        return yAxisEnabled;
    }

    public void setyAxisEnabled(boolean yAxisEnabled) {
        this.yAxisEnabled = yAxisEnabled;
    }

    public int getNumberOfPoints() {
        return numberOfPoints;
    }

    public void setNumberOfPoints(int numberOfPoints) {
        this.numberOfPoints = numberOfPoints;
    }
}
