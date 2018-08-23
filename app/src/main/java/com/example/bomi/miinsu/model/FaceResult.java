package com.example.bomi.miinsu.model;

import android.graphics.PointF;


public class FaceResult extends Object {

    private PointF midEye;
    private float eyeDist;
    private float pose;
    private int id;
    private long time;

    public FaceResult() {
        id = 0;
        midEye = new PointF(0.0f, 0.0f);
        eyeDist = 0.0f;
        pose = 0.0f;
        time = System.currentTimeMillis();
    }


    public void setFace(int id, PointF midEye, float eyeDist, float pose, long time) {
        set(id, midEye, eyeDist, pose, time);
    }

    public void clear() {
        set(0, new PointF(0.0f, 0.0f), 0.0f, 0.0f, System.currentTimeMillis());
    }

    public synchronized void set(int id, PointF midEye, float eyeDist, float pose, long time) {
        this.id = id;
        this.midEye.set(midEye);
        this.eyeDist = eyeDist;
        this.pose = pose;
        this.time = time;
    }

    public float eyesDistance() {
        return eyeDist;
    }

    public void setEyeDist(float eyeDist) {
        this.eyeDist = eyeDist;
    }

    public void getMidPoint(PointF pt) {
        pt.set(midEye);
    }

    public PointF getMidEye() {
        return midEye;
    }

    public void setMidEye(PointF midEye) {
        this.midEye = midEye;
    }

    public float getPose() {
        return pose;
    }

    public void setPose(float pose) {
        this.pose = pose;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
