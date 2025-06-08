package Utils;

import java.awt.*;
public class Utils {
    public static Color getFloorColor(int floor) {
        Color[] floorColors = {
                new Color(255, 100, 100), // Czerwony - parter
                new Color(100, 255, 100), // Zielony - P1
                new Color(100, 100, 255), // Niebieski - P2
                new Color(255, 255, 100), // Żółty - P3
                new Color(255, 100, 255), // Magenta - P4
                new Color(100, 255, 255), // Cyan - P5
                new Color(255, 150, 100), // Pomarańczowy - P6
                new Color(150, 255, 150), // Jasnozielony - P7
                new Color(150, 150, 255), // Jasnoniebieski - P8
                new Color(255, 200, 200), // Różowy - P9
                new Color(200, 255, 200)  // Jasnozielony - P10
        };
        return floorColors[floor % floorColors.length];
    }

    public static Color getRandomPassengerColor() {
        Color[] passengerColors = {
                new Color(255, 80, 80),   // Czerwony
                new Color(80, 255, 80),   // Zielony
                new Color(80, 80, 255),   // Niebieski
                new Color(255, 255, 80),  // Żółty
                new Color(255, 80, 255),  // Magenta
                new Color(80, 255, 255),  // Cyan
                new Color(255, 165, 0),   // Pomarańczowy
                new Color(128, 0, 128),   // Fioletowy
                new Color(255, 192, 203), // Różowy
                new Color(0, 128, 0)      // Ciemnozielony
        };
        return passengerColors[(int) (Math.random() * passengerColors.length)];
    }
}
