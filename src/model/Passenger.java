package model;

import java.awt.*;

public class Passenger {
    public int x, y;
    public Color color;
    public int entryFloor;

    public Passenger(int x, int y, Color color, int entryFloor) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.entryFloor = entryFloor;
    }
}