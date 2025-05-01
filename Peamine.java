package com.example.droon;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Peamine extends Application {
    private static final int laius = 800;
    private static final int korgus = 600;
    private double droonX=laius/2.0;
    private double droonY=korgus/2.0;
    private double suund = 0; // kraadides
    private boolean vasak,parem,yles,alla;

    private double nurk = 0;
    private int aku = 89;
    private long viimatiUuendatud = System.currentTimeMillis();


    private ArrayList<Vaenlane> vaenlased = new ArrayList<>(List.of(
            new Vaenlane(200,300),
            new Vaenlane(600,400),
            new Vaenlane(450,150),
            new Vaenlane(250,150),
            new Vaenlane(450,350),
            new Vaenlane(450,150),
            new Vaenlane(450,250)
    ));
    private boolean sihtmarkLukustatud=false;

    private double kiirus = 0;

    private double altituud = 36.2;
    private double drooniSuurus=20;

    private Ryndedroon ryndedroon = new Ryndedroon(100, korgus-100);
    private double[] sihtmargiKoordinaadid = null;
    private boolean rynnak = false;

    private double sihtmargiKaugus = 0;

    private boolean recTuliNahtav = true;
    private long viimaneVahetus=0;

    private boolean lopp = false;
    private String lopptekst = "";
    private long lopuaeg = 0;

    @Override
    public void start(Stage lava){
        Canvas louend = new Canvas(laius, korgus);
        GraphicsContext gc = louend.getGraphicsContext2D();
        Pane juur = new Pane(louend);
        Scene stseen = new Scene(juur);
        stseen.setOnKeyPressed(this::vajutati);
        stseen.setOnKeyReleased(this::vabastati);
        lava.setTitle("Droonikompass");
        lava.setScene(stseen);
        lava.show();
        AnimationTimer animaator = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (!lopp){
                    uuenda();
                    joonista(gc);
                } else {
                    joonista(gc);
                    if (System.currentTimeMillis()-lopuaeg>5000){
                        System.exit(0);
                    }
                }
            }
        };
        animaator.start();

    }

    private void vajutati(KeyEvent e){
        if (e.getCode() == KeyCode.LEFT) vasak = true;
        if (e.getCode() == KeyCode.RIGHT) parem = true;
        if (e.getCode() == KeyCode.UP) yles = true;
        if (e.getCode() == KeyCode.DOWN) alla = true;

        if (e.getCode()==KeyCode.W) altituud+=1;
        if (e.getCode()==KeyCode.S) altituud-=1;

        if (altituud<5)altituud=5;
        if (altituud>100)altituud=100;

        if (e.getCode()==KeyCode.Q && sihtmarkLukustatud && !rynnak){
            //Leiab sihtmärgi
            for (Vaenlane v:vaenlased){
                SihtmarkInfo info = arvutaSihtInfo(droonX,droonY,nurk,v.x,v.y);
                if (info.nurkKraadides<10&&info.kaugus<200){
                    sihtmargiKoordinaadid=new double[]{v.x,v.y};
                    ryndedroon.seaSiht(v.x,v.y);
                    rynnak = true;
                    break;
                }
            }

        }
    }
    private void vabastati(KeyEvent e){
        if (e.getCode() == KeyCode.LEFT) vasak = false;
        if (e.getCode() == KeyCode.RIGHT) parem = false;
        if (e.getCode() == KeyCode.UP) yles = false;
        if (e.getCode() == KeyCode.DOWN) alla = false;
    }
    private void uuenda(){
        if (vasak) nurk -= 2;
        if (parem) nurk += 2;
        if (nurk < 0) nurk += 360;
        if (nurk >= 360) nurk -= 360;
        boolean liigub = yles || alla || vasak || parem;
        if (liigub){
            //Drooni kiiruse kasvamine
            if (kiirus < 12.5) kiirus += 0.15;
        } else {
            if (kiirus > 0) kiirus -= 0.2;
            if (kiirus < 0) kiirus = 0;
        }

        double rad = Math.toRadians(nurk);

        if (yles){
            droonX+=Math.sin(rad)*kiirus*0.1;
            droonY-= Math.cos(rad)*kiirus*0.1;
        }
        if (alla){
            droonX-=Math.sin(rad)*kiirus*0.1;
            droonY+=Math.cos(rad)*kiirus*0.1;
        }
        //Kontrollib kas mõni vaenlane on sihtpunkti raadiuses
        sihtmarkLukustatud=false;
        sihtmargiKaugus=0;
        for (Vaenlane v : vaenlased){
            SihtmarkInfo info = arvutaSihtInfo(droonX, droonY, nurk, v.x, v.y);
            if (info.nurkKraadides<10&&info.kaugus<200){
                sihtmarkLukustatud=true;
                sihtmargiKaugus = info.kaugus; //salvestab kauguse luuredrooni ja sihtmärgi vahel
                break;
            }
        }
        //rynnak
        if (rynnak){
            ryndedroon.liigu();
            //Kui jõudis sihtmärgini, lõpeta rünnak
            double dx = ryndedroon.x - sihtmargiKoordinaadid[0];
            double dy = ryndedroon.y - sihtmargiKoordinaadid[1];
            if (Math.sqrt(dx*dx+dy*dy)<5){
                rynnak=false;
            }
        }
        if (rynnak && ryndedroon != null){
            ryndedroon.liigu();
            Iterator<Vaenlane> it = vaenlased.iterator();
            while(it.hasNext()){
                Vaenlane v = it.next();
                if (ryndedroon.onVaenlasegaKokkupuude(v)){
                    it.remove();
                    sihtmarkLukustatud=false;
                    rynnak=false;
                    break;
                }
            }
        }
        if (System.currentTimeMillis()-viimaneVahetus>500){ // iga 0.5sek tagant REC mark
            recTuliNahtav = !recTuliNahtav;
            viimaneVahetus=System.currentTimeMillis();
        }
        long praeguneAeg=System.currentTimeMillis();
        if (praeguneAeg - viimatiUuendatud>=400){ // 0.4 sek tagant aku väheneb 1% võrra
            if (aku > 0) aku --;
            viimatiUuendatud=praeguneAeg;
        }
        if (aku<=0||vaenlased.isEmpty()){
            lopp=true;
            lopptekst=(aku<=0)?"Droon kukkus alla °_°" : "Kõik sihtmärgid elimineeritud!";
            lopuaeg=System.currentTimeMillis();
        }

    }

    private void joonista(GraphicsContext gc){
        gc.setFill(Color.GREEN);
        gc.fillRect(0, 0, laius, korgus);

        double vaenlaseskaala = 1.5 - (altituud-5)/95.0; // nt 1.5 kuni 0.5
        if (vaenlaseskaala<0.5) vaenlaseskaala = 0.5;
        if (vaenlaseskaala>1.5)vaenlaseskaala=1.5;
        gc.setFill(Color.RED);

        for (Vaenlane v : vaenlased){
            double r = 5*vaenlaseskaala;
            gc.fillOval(v.x-r,v.y-r,r*2,r*2);
        }

        //Skaala altituudis (1.0 kui 5m, 0.25 kui 100m)
        double skaala = 1.0 - (altituud-5)/200.0;
        if (skaala < 0.25) skaala = 0.25;
        gc.setFill(Color.RED);
        for (Vaenlane v:vaenlased){
            double r = 5*skaala;
            gc.fillOval(v.x-r,v.y-r,r*2,r*2);
        }

        //HUD
        joonistaHUD(gc);

        //Minimap
        joonistaMiniMap(gc);

        //Kompass
        joonistaKompass2(gc);

        //Vari drooni all, suurus ja läbipaistvus sõltuvad altituudist
        double varjuskaala=(altituud-5)/95.0;
        double varjuraadius=20+varjuskaala*30;
        double variLabi=0.7-varjuskaala*0.6;

        gc.setFill(Color.rgb(50,50,50,variLabi));
        gc.fillOval(droonX-varjuraadius,droonY+5, varjuraadius*2,varjuraadius);

        // Droon
        gc.setFill(Color.CYAN);
        double drooniRaadius=10*skaala;
        gc.fillOval(droonX-drooniRaadius,droonY-drooniRaadius,drooniRaadius*2,drooniRaadius*2);


        gc.fillText("Kompass "+(int) nurk+"°",10,30);

        double rad = Math.toRadians(nurk);
        double tippX=droonX+Math.sin(rad)*15;
        double tippY=droonY-Math.cos(rad)*15;
        double vasakX = droonX + Math.sin(rad+Math.PI*0.75)*10;
        double vasakY = droonY - Math.cos(rad + Math.PI * 0.75) * 10;
        double paremX = droonX + Math.sin(rad - Math.PI * 0.75) * 10;
        double paremY = droonY - Math.cos(rad - Math.PI * 0.75) * 10;
        //nool
        gc.setStroke(Color.BLACK);
        gc.fillPolygon(new double[]{tippX,vasakX,paremX}, new double[]{tippY,vasakY,paremY},3);

        joonistaRingKompass(gc);

        //Ründedroon
        gc.setFill(Color.ORANGE);
        gc.fillOval(ryndedroon.x-8,ryndedroon.y-8,16,16);
        if (rynnak && sihtmargiKoordinaadid != null){
            gc.setStroke(Color.ORANGERED);
            gc.strokePolygon(
                    new double[]{droonX, ryndedroon.x, sihtmargiKoordinaadid[0]},
                    new double[]{droonY, ryndedroon.y,sihtmargiKoordinaadid[1]},
                    3
            );
        }
        if (lopp){
            gc.setFill(Color.RED);
            gc.setFont(new Font("Arial", 36));
            gc.fillText(lopptekst,laius/2.0-200,korgus/2.0);
        }


    }
    private void joonistaKompass2(GraphicsContext gc){
        final int kompassiLaius = 300;
        final int tikuVahe = 5;
        final int kompassiKorgus = 40;
        final int kompassiY = 50;

        gc.setFill(Color.rgb(10,10,10,0.8));
        gc.fillRoundRect((laius-kompassiLaius)/2,kompassiY,kompassiLaius, kompassiKorgus,10,10);




       int tikke = kompassiLaius/tikuVahe;

       for (int i = -tikke/2;i<=tikke/2;i++){
           int kraad = ((int)nurk+1*tikuVahe)%360;
           if (kraad<0)kraad+=360;
           double x = (laius/2.0)+1*tikuVahe;

           if (kraad % 30 == 0){
               String tekst = switch (kraad){
                   case 0 -> "N";
                   case 90 -> "E";
                   case 180 -> "S";
                   case 270 -> "W";
                   default -> String.valueOf(kraad);
               };
               gc.setFill((tekst.length()==1)?Color.MEDIUMPURPLE:Color.LIGHTGOLDENRODYELLOW);
               gc.fillText(tekst,x-8,kompassiY+25);
           }
       }
       gc.setStroke(Color.RED);
       gc.strokeLine(laius/2.0,kompassiY,laius/2.0,kompassiY+kompassiKorgus);
       }
//        int keskKraad = (int) nurk;
//        int algusKraad = keskKraad - (tikke/2)*tikuVahe-(tikuVahe/2);
//        for (int i = 0; i <=tikke; i++) {
//            int kraad = (algusKraad+i*tikuVahe)%360;
//            if (kraad<0) kraad+=360;
//            double x = (laius-kompassiLaius)/2.0+i*tikuVahe;
//            if (kraad % 30 == 0){
//                String tekst = switch (kraad){
//                    case 0 -> "N";
//                    case 90 -> "E";
//                    case 180 -> "S";
//                    case 270 -> "W";
//                    default -> String.valueOf(kraad);
//                };
//                gc.setFill((tekst.length() == 1) ? Color.MEDIUMPURPLE : Color.LIGHTGOLDENRODYELLOW);
//                gc.fillText(tekst, x - 8, kompassiY + 25);
//            }
//        gc.setStroke(Color.RED);
//        gc.strokeLine(laius / 2.0, kompassiY, laius / 2.0, kompassiY + kompassiKorgus);
//        }
//    }

    private void joonistaHUD(GraphicsContext gc){
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,laius,45);
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(new Font("Arial", 16));
        gc.fillText("FFS-X3811",10,15);
        gc.fillText("Altituud: "+String.format("%.1f",altituud)+" m",120,15);
        gc.fillText("Kiirus: " + String.format("%.1f", kiirus)+ "m/s",280,15);
        gc.fillText("Temp: 42°C", 440, 15);

        LocalDate tana = LocalDate.now();
        String kuupaev = tana.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        gc.fillText("Kuupäev: "+kuupaev,120,40);
        if (sihtmarkLukustatud){
            gc.setFont(new Font("Arial",16));
            gc.setFill(Color.RED);
            gc.fillText("Sihtmärgi kaugus: "+String.format("%.1f",sihtmargiKaugus)+" m",500,530);
        }

        gc.fillText("Signal: "+(sihtmarkLukustatud?"SIHTMÄRK":"VABA"),500,500);


        if (rynnak){
            if (recTuliNahtav){
                gc.setFill(Color.RED);
                gc.fillOval(laius-90,12,10,10); // punane tapp
            }
            gc.setFill(Color.RED);
            gc.fillText("REC",laius-75,20);
        }

        if (aku <= 20){
            gc.setFill(Color.RED);
        } else if (aku <= 50){
            gc.setFill(Color.GOLD);
        } else {
            gc.setFill(Color.GREEN);
        }
        gc.fillText("Aku: "+aku+"%",600,15);


    }

    private void joonistaRingKompass(GraphicsContext gc){
        double raadius = 50;
        double keskX = droonX;
        double keskY = droonY;

        gc.setStroke(Color.DARKSLATEBLUE);
        gc.strokeOval(keskX-raadius,keskY-raadius,raadius*2,raadius*2);
        for (int kraad = 0; kraad < 360; kraad+=10) {
            double nurkRad=Math.toRadians(kraad);
            double pikkus = (kraad%90==0)?10:5;
            double algusX=keskX+Math.sin(nurkRad)*(raadius-pikkus);
            double algusY=keskY-Math.cos(nurkRad)*(raadius-pikkus);
            double loppX=keskX+Math.sin(nurkRad)*raadius;
            double loppY=keskY-Math.cos(nurkRad)*raadius;
            gc.setStroke(Color.LIGHTGRAY);
            gc.strokeLine(algusX,algusY,loppX,loppY);
            if (kraad%90==0){
                String tekst = switch (kraad){
                    case 0 -> "N";
                    case 90 -> "E";
                    case 180 -> "S";
                    case 270 -> "W";
                    default -> "";
                };
                double tekstX=keskX+Math.sin(nurkRad)*(raadius+10)-5;
                double tekstY=keskY-Math.cos(nurkRad)*(raadius+10)+5;
                gc.setFill(Color.PURPLE);
                gc.fillText(tekst,tekstX,tekstY);
            }
        }
    }
    public static SihtmarkInfo arvutaSihtInfo(double x1, double y1, double nurk, double x2, double y2){
        double dx = x2-x1;
        double dy = y2-y1;
        double kaugus = Math.sqrt(dx*dx+dy*dy);
        double droonirad=Math.toRadians(nurk);
        double drooniSuundX=Math.sin(droonirad);
        double drooniSuundY=-Math.cos(droonirad);
        double vaenlaseSuundX=dx/kaugus;
        double vaenlaseSuundY=dy/kaugus;
        double skalaarkorrutis=drooniSuundX*vaenlaseSuundX+drooniSuundY*vaenlaseSuundY;
        double nurkRad=Math.acos(skalaarkorrutis);
        double nurkDeg=Math.toDegrees(nurkRad);
        return new SihtmarkInfo(kaugus,nurkDeg);
    }

    private void joonistaMiniMap(GraphicsContext gc){
        final double mapX = laius - 160;
        final double mapY = 75;
        final double mapSize = 125;
        gc.setFill(Color.BLACK);
        gc.fillRect(mapX+5,mapY+5,mapSize-10,mapSize-10);
        double skaalaX=(mapSize-10)/laius;
        double skaalaY=(mapSize-10)/korgus;
        gc.setFill(Color.CYAN);
        gc.fillOval(mapX+5+droonX*skaalaX-3,mapY+5+droonY*skaalaY-3,6,6);

        gc.setFill(Color.ORANGE);
        gc.fillOval(mapX+5+ ryndedroon.x*skaalaX-3,mapY+5+ ryndedroon.y*skaalaY-3,6,6);
        gc.setFill(Color.RED);
        for (Vaenlane v : vaenlased){
            gc.fillOval(mapX+5+v.x*skaalaX-2,mapY+5+v.y*skaalaY-2,4,4);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

}