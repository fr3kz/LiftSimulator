import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ElevatorGUI extends JFrame {
    private Elevator elevator;
    private Building building;
    private ElevatorController controller;

    private JPanel mainPanel;
    private JButton startButton;
    private JButton[] callButtons;
    private JButton[] floorButtons;
    private JLabel[] upArrows;
    private JLabel[] downArrows;
    private JLabel[] passengerLabels;

    private List<PassengerDot> passengersInElevatorDots = new ArrayList<>();
    private List<List<PassengerDot>> passengersOnFloorsDots = new ArrayList<>();

    // Animacja windy
    private double elevatorAnimationY = 0; // Aktualna pozycja Y windy (z animacją)
    private int targetFloor = 0; // Docelowe piętro
    private Timer animationTimer;
    private boolean isAnimating = false;
    private final double ANIMATION_SPEED = 2.0; // Piksele na klatke animacji

    // Animacja koła mechanicznego
    private double pulleyRotation = 0; // Rotacja koła (w stopniach)
    private final double PULLEY_SPEED = 8.0; // Prędkość obracania koła

    private static class PassengerDot {
        int x, y;
        Color color;
        int entryFloor;

        PassengerDot(int x, int y, Color color, int entryFloor) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.entryFloor = entryFloor;
        }
    }

    public ElevatorGUI() {
        elevator = new Elevator();
        building = new Building();
        controller = new ElevatorController(elevator, building, this);

        // Inicjalizacja pozycji animacji windy
        elevatorAnimationY = 600; // Startowa pozycja (parter)
        targetFloor = 0;

        // Inicjalizacja list kropek dla każdego piętra
        for (int i = 0; i < building.getFloorsCount(); i++) {
            passengersOnFloorsDots.add(new ArrayList<>());
        }

        setTitle("Symulacja Windy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
        setupEventHandlers();

        // Inicjalizuj timer animacji
        setupAnimationTimer();
    }

    private void initializeComponents() {
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBuildingStructure(g);
                drawPulleySystem(g);
                drawElevator(g);
                drawPassengersInElevator(g);
                drawPassengersOnFloors(g);
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.LIGHT_GRAY);

        startButton = new JButton("START");
        startButton.setBounds(50, 50, 100, 30);

        callButtons = new JButton[building.getFloorsCount()];
        upArrows = new JLabel[building.getFloorsCount()];
        downArrows = new JLabel[building.getFloorsCount()];
        floorButtons = new JButton[building.getFloorsCount()];
        passengerLabels = new JLabel[building.getFloorsCount()];

        for (int i = 0; i < building.getFloorsCount(); i++) {
            callButtons[i] = new JButton("Wezwij");
            callButtons[i].setBounds(200, 600 - i * 50, 80, 25);
            callButtons[i].setEnabled(false);

            upArrows[i] = new JLabel("↑");
            upArrows[i].setBounds(285, 595 - i * 50, 20, 15);
            upArrows[i].setVisible(false);

            downArrows[i] = new JLabel("↓");
            downArrows[i].setBounds(285, 610 - i * 50, 20, 15);
            downArrows[i].setVisible(false);

            floorButtons[i] = new JButton(String.valueOf(i));
            floorButtons[i].setBounds(500 + (i % 3) * 40, 500 + (i / 3) * 40, 35, 35);
            floorButtons[i].setEnabled(false);

            passengerLabels[i] = new JLabel("0");
            passengerLabels[i].setBounds(150, 600 - i * 50, 40, 25);
            passengerLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            passengerLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
    }

    private void setupAnimationTimer() {
        animationTimer = new Timer(16, e -> { // ~60 FPS
            if (isAnimating) {
                double targetY = 600 - targetFloor * 50;
                double distance = targetY - elevatorAnimationY;

                if (Math.abs(distance) < 1.0) {
                    // Animacja zakończona
                    elevatorAnimationY = targetY;
                    isAnimating = false;
                    animationTimer.stop();

                    // Powiadom controller że winda dotarła
                    if (controller != null) {
                        controller.onElevatorArrivedAtFloor();
                    }
                } else {
                    // Kontynuuj animację
                    double step = Math.signum(distance) * ANIMATION_SPEED;
                    elevatorAnimationY += step;

                    // Obracaj koło podczas ruchu
                    pulleyRotation += PULLEY_SPEED * Math.signum(distance);
                    if (pulleyRotation >= 360) pulleyRotation -= 360;
                    if (pulleyRotation < 0) pulleyRotation += 360;
                }

                mainPanel.repaint();
            }
        });
    }

    private void drawBuildingStructure(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Szyb windy - pionowe prowadnice
        g2d.setColor(new Color(80, 80, 80));
        g2d.setStroke(new BasicStroke(3));
        // Lewa prowadnica
        g2d.drawLine(315, 100, 315, 650);
        // Prawa prowadnica
        g2d.drawLine(365, 100, 365, 650);

        // Poziome linie pięter
        g2d.setColor(new Color(120, 120, 120));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= building.getFloorsCount(); i++) {
            int y = 600 - i * 50;
            g2d.drawLine(315, y + 40, 365, y + 40);
        }

        // Górna konstrukcja budynku
        g2d.setColor(new Color(60, 60, 60));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine(300, 100, 380, 100); // Górna belka

        g2d.dispose();
    }

    private void drawPulleySystem(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int pulleyX = 340; // Środek koła
        int pulleyY = 80;  // Pozycja Y koła
        int pulleyRadius = 15;

        // Wspornik koła
        g2d.setColor(new Color(40, 40, 40));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(pulleyX, 100, pulleyX, pulleyY - pulleyRadius - 5);

        // Koło mechaniczne z obracaniem
        g2d.translate(pulleyX, pulleyY);
        g2d.rotate(Math.toRadians(pulleyRotation));

        // Zewnętrzna część koła (metalowa)
        g2d.setColor(new Color(150, 150, 150));
        g2d.fillOval(-pulleyRadius, -pulleyRadius, pulleyRadius * 2, pulleyRadius * 2);
        g2d.setColor(new Color(80, 80, 80));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(-pulleyRadius, -pulleyRadius, pulleyRadius * 2, pulleyRadius * 2);

        // Szprychy koła
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int x1 = (int)(Math.cos(angle) * 4);
            int y1 = (int)(Math.sin(angle) * 4);
            int x2 = (int)(Math.cos(angle) * (pulleyRadius - 2));
            int y2 = (int)(Math.sin(angle) * (pulleyRadius - 2));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Środkowa oś
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillOval(-4, -4, 8, 8);

        // Powrót do oryginalnej transformacji
        g2d.rotate(-Math.toRadians(pulleyRotation));
        g2d.translate(-pulleyX, -pulleyY);

        // Lina/kabel od koła do windy
        g2d.setColor(new Color(139, 69, 19)); // Brązowy kolor liny
        g2d.setStroke(new BasicStroke(3));

        int elevatorCenterX = 340;
        int elevatorTopY = (int) elevatorAnimationY;

        // Lina od koła do windy
        g2d.drawLine(pulleyX, pulleyY + pulleyRadius, elevatorCenterX, elevatorTopY);

        // Przeciwwaga (opcjonalnie po drugiej stronie)
        g2d.setColor(new Color(100, 100, 100));
        int counterweightX = pulleyX + 50;
        int counterweightY = (int)(600 - elevatorAnimationY + 300); // Przeciwwaga idzie w przeciwną stronę
        if (counterweightY > 650) counterweightY = 650;
        if (counterweightY < 200) counterweightY = 200;

        // Lina do przeciwwagi
        g2d.setColor(new Color(139, 69, 19));
        g2d.drawLine(pulleyX + pulleyRadius, pulleyY, counterweightX, counterweightY);

        // Przeciwwaga
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRect(counterweightX - 8, counterweightY, 16, 30);
        g2d.setColor(new Color(60, 60, 60));
        g2d.drawRect(counterweightX - 8, counterweightY, 16, 30);

        g2d.dispose();
    }

    private void drawElevator(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int elevatorX = 320;
        int elevatorY = (int) elevatorAnimationY; // Użyj animowanej pozycji
        int elevatorWidth = 40;
        int elevatorHeight = 40;

        // Efekt cienia pod windą
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(elevatorX - 2, elevatorY + elevatorHeight, elevatorWidth + 4, 8);

        // Gradient dla windy
        GradientPaint gradient = new GradientPaint(
                elevatorX, elevatorY, new Color(100, 150, 255),
                elevatorX, elevatorY + elevatorHeight, new Color(50, 100, 200)
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(elevatorX, elevatorY, elevatorWidth, elevatorHeight, 8, 8);

        // Ramka windy
        g2d.setColor(new Color(30, 60, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(elevatorX, elevatorY, elevatorWidth, elevatorHeight, 8, 8);

        // Efekt świetlny na górze windy
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.fillRoundRect(elevatorX + 3, elevatorY + 3, elevatorWidth - 6, 8, 4, 4);

        // Punkt mocowania liny na górze windy
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillOval(elevatorX + elevatorWidth/2 - 3, elevatorY - 5, 6, 6);
        g2d.setColor(new Color(40, 40, 40));
        g2d.drawOval(elevatorX + elevatorWidth/2 - 3, elevatorY - 5, 6, 6);

        // Rysuj liczbę pasażerów w windzie
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String passengerCount = String.valueOf(elevator.getPassengersInElevator().size());
        FontMetrics fm = g2d.getFontMetrics();
        int textX = elevatorX + (elevatorWidth - fm.stringWidth(passengerCount)) / 2;
        int textY = elevatorY + elevatorHeight - 8;

        // Cień tekstu
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString(passengerCount, textX + 1, textY + 1);
        g2d.setColor(Color.WHITE);
        g2d.drawString(passengerCount, textX, textY);

        // Wskaźnik kierunku ruchu
        if (isAnimating && elevator.getDirection() != 0) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String arrow = elevator.getDirection() > 0 ? "▲" : "▼";
            int arrowX = elevatorX + elevatorWidth + 5;
            int arrowY = elevatorY + elevatorHeight / 2 + 5;
            g2d.drawString(arrow, arrowX, arrowY);
        }

        g2d.dispose();
    }

    private void drawPassengersInElevator(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int elevatorX = 320;
        int elevatorY = (int) elevatorAnimationY; // Użyj animowanej pozycji

        for (PassengerDot dot : passengersInElevatorDots) {
            // Efekt świetlny dla kropek
            g2d.setColor(new Color(dot.color.getRed(), dot.color.getGreen(), dot.color.getBlue(), 100));
            g2d.fillOval(elevatorX + dot.x - 1, elevatorY + dot.y - 1, 10, 10);

            g2d.setColor(dot.color);
            g2d.fillOval(elevatorX + dot.x, elevatorY + dot.y, 8, 8);
            g2d.setColor(dot.color.darker());
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawOval(elevatorX + dot.x, elevatorY + dot.y, 8, 8);
        }

        g2d.dispose();
    }

    private void drawPassengersOnFloors(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int floor = 0; floor < building.getFloorsCount(); floor++) {
            int floorY = 600 - floor * 50;
            int startX = 370; // Pozycja X gdzie zaczynają się kropki pasażerów

            List<PassengerDot> floorPassengers = passengersOnFloorsDots.get(floor);
            for (int i = 0; i < floorPassengers.size(); i++) {
                PassengerDot dot = floorPassengers.get(i);
                int x = startX + (i % 8) * 12; // 8 kropek w rzędzie
                int y = floorY + 10 + (i / 8) * 12; // Nowy rząd co 8 kropek

                // Efekt pulsowania dla czekających pasażerów
                long time = System.currentTimeMillis();
                double pulse = 0.8 + 0.2 * Math.sin(time * 0.005 + i * 0.5);
                int size = (int)(10 * pulse);

                // Efekt świetlny
                g2d.setColor(new Color(dot.color.getRed(), dot.color.getGreen(), dot.color.getBlue(), 80));
                g2d.fillOval(x - 2, y - 2, size + 4, size + 4);

                g2d.setColor(dot.color);
                g2d.fillOval(x, y, size, size);
                g2d.setColor(dot.color.darker());
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(x, y, size, size);
            }
        }

        g2d.dispose();
    }

    private Color getFloorColor(int floor) {
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

    private Color getRandomPassengerColor() {
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

    private void setupLayout() {
        mainPanel.add(startButton);

        for (int i = 0; i < building.getFloorsCount(); i++) {
            mainPanel.add(callButtons[i]);
            mainPanel.add(upArrows[i]);
            mainPanel.add(downArrows[i]);
            mainPanel.add(floorButtons[i]);
            mainPanel.add(passengerLabels[i]);
        }

        for (int i = 0; i < building.getFloorsCount(); i++) {
            JLabel floorLabel = new JLabel("P" + i);
            floorLabel.setBounds(100, 600 - i * 50, 30, 25);
            mainPanel.add(floorLabel);
        }

        add(mainPanel);
    }

    private void setupEventHandlers() {
        startButton.addActionListener(e -> controller.startSimulation());

        for (int i = 0; i < building.getFloorsCount(); i++) {
            final int floor = i;
            callButtons[i].addActionListener(e -> {
                controller.callElevator(floor);
                callButtons[floor].setEnabled(false);
            });

            floorButtons[i].addActionListener(e -> {
                controller.selectDestination(floor);
                floorButtons[floor].setEnabled(false);
            });
        }

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Sprawdź czy kliknięto w obszar windy (używaj animowanej pozycji)
                int elevatorX = 320;
                int elevatorY = (int) elevatorAnimationY;
                int elevatorWidth = 40;
                int elevatorHeight = 40;

                if (e.getX() >= elevatorX && e.getX() <= elevatorX + elevatorWidth &&
                        e.getY() >= elevatorY && e.getY() <= elevatorY + elevatorHeight) {
                    controller.exitPassenger();
                }
            }
        });
    }

    public void updateAfterStart() {
        startButton.setEnabled(false);

        for (int i = 0; i < building.getFloorsCount(); i++) {
            updatePassengerDisplay(i);
            updateFloorPassengerDots(i);
            callButtons[i].setEnabled(building.hasWaitingPassengers(i));
        }

        updateElevatorPosition();
        updateArrows();
    }

    public void updateElevatorPosition() {
        // Rozpocznij płynną animację do nowego piętra
        startElevatorAnimation(elevator.getCurrentFloor());
    }

    public void startElevatorAnimation(int floor) {
        targetFloor = floor;
        if (!isAnimating) {
            isAnimating = true;
            animationTimer.start();
        }
    }

    public boolean isElevatorAnimating() {
        return isAnimating;
    }

    public void updatePassengerDisplay(int floor) {
        passengerLabels[floor].setText(String.valueOf(building.getWaitingPassengers(floor)));
        if (building.getWaitingPassengers(floor) > 0) {
            passengerLabels[floor].setBackground(Color.YELLOW);
            passengerLabels[floor].setOpaque(true);
        } else {
            passengerLabels[floor].setBackground(null);
            passengerLabels[floor].setOpaque(false);
            callButtons[floor].setEnabled(false);
        }

        // Aktualizuj kropki pasażerów na piętrze
        updateFloorPassengerDots(floor);
    }

    public void updateFloorPassengerDots(int floor) {
        List<PassengerDot> floorDots = passengersOnFloorsDots.get(floor);
        int currentPassengers = building.getWaitingPassengers(floor);

        // Jeśli jest więcej pasażerów niż kropek - dodaj kropki
        while (floorDots.size() < currentPassengers) {
            Color passengerColor = getRandomPassengerColor();
            PassengerDot newDot = new PassengerDot(0, 0, passengerColor, floor);
            floorDots.add(newDot);
        }

        // Jeśli jest mniej pasażerów niż kropek - usuń kropki
        while (floorDots.size() > currentPassengers) {
            floorDots.remove(floorDots.size() - 1);
        }

        mainPanel.repaint();
    }

    public void updateElevatorPassengersDisplay() {
        // Tylko odśwież panel - liczba pasażerów jest teraz rysowana w drawElevator()
        mainPanel.repaint();
    }

    public void addPassengerToElevator() {
        int dotIndex = passengersInElevatorDots.size();
        int x = 5 + (dotIndex % 5) * 8;
        int y = 5 + (dotIndex / 5) * 10;

        Color passengerColor = getFloorColor(elevator.getCurrentFloor());
        PassengerDot newDot = new PassengerDot(x, y, passengerColor, elevator.getCurrentFloor());
        passengersInElevatorDots.add(newDot);

        updateElevatorPassengersDisplay();
    }

    public void removePassengerFromElevator() {
        if (!passengersInElevatorDots.isEmpty()) {
            passengersInElevatorDots.remove(0);

            for (int i = 0; i < passengersInElevatorDots.size(); i++) {
                PassengerDot dot = passengersInElevatorDots.get(i);
                dot.x = 5 + (i % 5) * 8;
                dot.y = 5 + (i / 5) * 10;
            }

            updateElevatorPassengersDisplay();
        }
    }

    public void updateArrows() {
        for (int i = 0; i < building.getFloorsCount(); i++) {
            upArrows[i].setVisible(false);
            downArrows[i].setVisible(false);

            if (building.getCalls().contains(i)) {
                if (elevator.getDirection() > 0) {
                    upArrows[i].setVisible(true);
                } else if (elevator.getDirection() < 0) {
                    downArrows[i].setVisible(true);
                }
            }
        }
    }

    public void updateFloorButtonsAvailability() {
        for (int i = 0; i < building.getFloorsCount(); i++) {
            floorButtons[i].setEnabled(
                    controller.isSimulationRunning() &&
                            !elevator.isEmpty() &&
                            i != elevator.getCurrentFloor() &&
                            !elevator.getDestinations().contains(i)
            );
        }
    }

    public void endSimulation() {
        startButton.setEnabled(true);

        for (int i = 0; i < building.getFloorsCount(); i++) {
            callButtons[i].setEnabled(false);
            floorButtons[i].setEnabled(false);
        }

        passengersInElevatorDots.clear();

        // Wyczyść wszystkie kropki pasażerów na piętrach
        for (List<PassengerDot> floorDots : passengersOnFloorsDots) {
            floorDots.clear();
        }

        // Zatrzymaj animację
        if (animationTimer != null) {
            animationTimer.stop();
        }
        isAnimating = false;
        elevatorAnimationY = 600; // Reset do parteru
        pulleyRotation = 0; // Reset rotacji koła

        updateArrows();
        mainPanel.repaint();
    }
}