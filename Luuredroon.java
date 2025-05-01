package com.example.droon;

public class Luuredroon {
    private double suund;
    private double korgus;
    public Luuredroon(){
        this.suund=0;
        this.korgus=50;
    }
    public void pohjaSuundVasakule(){
        suund-=2;
        if (suund<0) suund+= 360;
    }
    public void pohjaSuundParemale(){
        suund+=2;
        if (suund>=360) suund -= 360;
    }
    public double getSuund(){
        return suund;
    }
    public double getKorgus(){
        return korgus;
    }
    public void muudaKorgust(double alfa){
        korgus+=alfa;
    }

}
