package com.svamp.planetwars.math;

import android.util.FloatMath;

public class Vector {
    public float x;
    public float y;

    public Vector(float x,float y) {
        this.x=x;
        this.y=y;
    }
    public Vector(Vector v) {
        this.x=v.x;
        this.y=v.y;
    }
    public void set(float x, float y) {
        this.x=x;
        this.y=y;
    }
    public void set(Vector v) {
        this.set(v.x,v.y);
    }
    public void add(float dx, float dy) {
        this.x+=dx;
        this.y+=dy;
    }
    public void scale(float sx, float sy) {
        this.x*=sx;
        this.y*=sy;
    }
    public void setLength(float length) {
        float scale = length/FloatMath.sqrt(lengthSq());
        x*=scale;
        y*=scale;
    }

    public void rotate(float angle) {
        float newX = x*FloatMath.cos(angle)-y*FloatMath.sin(angle);
        y = x*FloatMath.sin(angle)+y*FloatMath.cos(angle);
        x=newX;
    }

    public float lengthSq() {
        return x*x+y*y;
    }

    public float distanceTo(Vector v) {
        return FloatMath.sqrt(distanceToSq(v));
    }
    public float distanceToSq(Vector v) {
        return (x-v.x)*(x-v.x)+(y-v.y)*(y-v.y);
    }

    public String toString() {
        return x+";"+y;
    }

    public boolean equals(Object o) {
        //Hacky equals. Uses ulp to check if difference is significant
        float dist = distanceToSq((Vector) o);
        return o != null && dist<=5*Math.ulp(dist);
    }

    public int hashCode() {
        //Hacky hashcode. 0 significant decimals.
        return (int) (Math.floor(x)*1000+Math.floor(y));
    }

    /*
     * Takes a vector (radius,angle) and converts it to the (x,y) version
     */
    public static void toCartesian(Vector rad) {
        rad.set(rad.x*FloatMath.cos(rad.y),rad.x*FloatMath.sin(rad.y));
    }

    public static Vector sum(Vector ... args) {
        Vector res = new Vector(0,0);
        for(Vector arg : args) {
            res.add(arg.x,arg.y);
        }
        return res;
    }
}
