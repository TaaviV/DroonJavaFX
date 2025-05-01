package com.example.droon;

public class Vaenlane {
    public double x,y;
    public boolean lukustatud = false;
    public Vaenlane(double x, double y){
        this.x=x;
        this.y=y;
    }
    public void liigu(double nurk, double kiirus){
        x+=Math.cos(Math.toRadians(nurk))*kiirus;
        y+=Math.sin(Math.toRadians(nurk))*kiirus;

    }
}
