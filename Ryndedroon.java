package com.example.droon;

public class Ryndedroon {
    public double x,y;
    private double sihtX, sihtY;
    private double kiirus=2;
    public Ryndedroon(double x, double y){
        this.x=x;
        this.y=y;
    }
    public void seaSiht(double x, double y){
        this.sihtX=x;
        this.sihtY=y;
    }
    public void liigu(){
        double dx = sihtX-x;
        double dy = sihtY-y;
        double nurk = Math.atan2(dy,dx);
        x+=Math.cos(nurk)*kiirus;
        y+=Math.sin(nurk)*kiirus;
    }
    public boolean onVaenlasegaKokkupuude(Vaenlane v){
        double dx = x - v.x;
        double dy = y - v.y;
        return Math.sqrt(dx*dx+dy*dy)<10; // kaugus väiksem kui 10
    }
}
