package org.inventivetalent.canvas;

import java.util.Arrays;

public class CanvasState {

    int w;
    int h;
    int s;
    String[] c;
    int n;
    int v;
    String u;

    @Override
    public String toString() {
        return "CanvasState{" +
                "w=" + w +
                ", h=" + h +
                ", s=" + s +
                ", c=" + Arrays.toString(c) +
                ", n=" + n +
                ", v=" + v +
                ", u='" + u + '\'' +
                '}';
    }

}
