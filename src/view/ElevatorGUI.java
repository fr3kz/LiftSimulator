package view;

import controler.ElevatorController;
import model.Building;
import model.Elevator;
import model.Passenger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ElevatorGUI extends JFrame {
    private Elevator elevator;
    private Building building;
    private ElevatorController controller;

    private JPanel mainPanel;
    private JButton startButton;
    private JButton[] callButtons;
    private JButton[] floorButtons;
    private JLabel[] directionArrows;
    private JLabel[] passengerLabels;

    private JPanel[] floorPanels;

    private Set<Integer> activeCallFloors = new HashSet<>();

    private List<Passenger> passengersInElevatorDots = new ArrayList<>();
    private List<List<Passenger>> passengersOnFloorsDots = new ArrayList<>();


    private double elevatorAnimationY = 0;
    private int targetFloor = 0;
    private Timer animationTimer;
    private boolean isAnimating = false;
    private final double ANIMATION_SPEED = 2.0;


    private double pulleyRotation = 0;
    private final double PULLEY_SPEED = 8.0;

    public ElevatorGUI() {
        elevator = new Elevator();
        building = new Building();
        controller = new ElevatorController(elevator, building, this);

        elevatorAnimationY = 600;
        targetFloor = 0;

        for (int i = 0; i < building.getFloorsCount(); i++) {
            passengersOnFloorsDots.add(new ArrayList<>());
        }

        setTitle("Symulacja Windy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
        setupEventHandlers();

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
        directionArrows = new JLabel[building.getFloorsCount()];
        floorButtons = new JButton[building.getFloorsCount()];
        passengerLabels = new JLabel[building.getFloorsCount()];
        floorPanels = new JPanel[building.getFloorsCount()];

        for (int i = 0; i < building.getFloorsCount(); i++) {
            floorPanels[i] = new JPanel();
            floorPanels[i].setLayout(null);
            floorPanels[i].setBounds(180, 590 - i * 50, 120, 40);
            floorPanels[i].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
            floorPanels[i].setBackground(new Color(240, 240, 240));

            callButtons[i] = new JButton("Wezwij");
            callButtons[i].setBounds(5, 8, 70, 25);
            callButtons[i].setEnabled(false);
            callButtons[i].setFont(new Font("Arial", Font.PLAIN, 10));

            directionArrows[i] = new JLabel("", SwingConstants.CENTER);
            directionArrows[i].setBounds(80, 5, 30, 30);
            directionArrows[i].setFont(new Font("Arial", Font.BOLD, 20));
            directionArrows[i].setVisible(false);

            floorPanels[i].add(callButtons[i]);
            floorPanels[i].add(directionArrows[i]);

            floorButtons[i] = new JButton(String.valueOf(i));
            floorButtons[i].setBounds(500 + (i % 3) * 40, 500 + (i / 3) * 40, 35, 35);
            floorButtons[i].setEnabled(false);

            passengerLabels[i] = new JLabel("0");
            passengerLabels[i].setBounds(130, 590 - i * 50, 40, 40);
            passengerLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            passengerLabels[i].setVerticalAlignment(SwingConstants.CENTER);
            passengerLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            passengerLabels[i].setOpaque(true);
            passengerLabels[i].setBackground(Color.WHITE);
        }
    }

    private void setupAnimationTimer() {
        animationTimer = new Timer(16, e -> { // ~60 FPS
            if (isAnimating) {
                double targetY = 600 - targetFloor * 50;
                double distance = targetY - elevatorAnimationY;

                if (Math.abs(distance) < 1.0) {
                    elevatorAnimationY = targetY;
                    isAnimating = false;
                    animationTimer.stop();


                } else {
                    double step = Math.signum(distance) * ANIMATION_SPEED;
                    elevatorAnimationY += step;

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
        g2d.drawLine(300, 95, 380, 95); // Górna belka

        g2d.dispose();
    }

    private void drawPulleySystem(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int pulleyX = 340; // Środek koła
        int pulleyY = 80;  // Pozycja Y koła
        int pulleyRadius = 15;

        g2d.translate(pulleyX, pulleyY);
        g2d.rotate(Math.toRadians(pulleyRotation));

        // Zewnętrzna część koła
        g2d.setColor(new Color(150, 150, 150));
        g2d.fillOval(-pulleyRadius, -pulleyRadius, pulleyRadius * 2, pulleyRadius * 2);
        g2d.setColor(new Color(80, 80, 80));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(-pulleyRadius, -pulleyRadius, pulleyRadius * 2, pulleyRadius * 2);



        g2d.setColor(new Color(60, 60, 60));
        g2d.fillOval(-4, -4, 8, 8);

        g2d.rotate(-Math.toRadians(pulleyRotation));
        g2d.translate(-pulleyX, -pulleyY);

        // Lina/kabel od koła do windy
        g2d.setColor(new Color(139, 69, 19));
        g2d.setStroke(new BasicStroke(3));

        int elevatorCenterX = 340;
        int elevatorTopY = (int) elevatorAnimationY;

        // Lina od koła do windy
        g2d.drawLine(pulleyX, pulleyY + pulleyRadius, elevatorCenterX, elevatorTopY);

        // Przeciwwaga (opcjonalnie po drugiej stronie)
        g2d.setColor(new Color(100, 100, 100));
        int counterweightX = pulleyX + 50;
        int counterweightY = (int)(400 - elevatorAnimationY + 300);


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
        int elevatorY = (int) elevatorAnimationY;
        int elevatorWidth = 40;
        int elevatorHeight = 40;


        // Ramka windy
        g2d.setColor(new Color(30, 60, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(elevatorX, elevatorY, elevatorWidth, elevatorHeight, 8, 8);

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

        g2d.dispose();
    }

    private void drawPassengersInElevator(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int elevatorX = 320;
        int elevatorY = (int) elevatorAnimationY; // Użyj animowanej pozycji

        for (Passenger dot : passengersInElevatorDots) {
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
            int startX = 370;

            List<Passenger> floorPassengers = passengersOnFloorsDots.get(floor);
            for (int i = 0; i < floorPassengers.size(); i++) {
                Passenger dot = floorPassengers.get(i);
                int x = startX + (i % 8) * 12;
                int y = floorY + 10 + (i / 8) * 12;

                g2d.setColor(dot.color);
                g2d.fillOval(x, y, 6, 6);
                g2d.setColor(dot.color.darker());
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(x, y, 6, 6);
            }
        }

        g2d.dispose();
    }

    private void setupLayout() {
        mainPanel.add(startButton);

        for (int i = 0; i < building.getFloorsCount(); i++) {
            mainPanel.add(floorPanels[i]);
            mainPanel.add(floorButtons[i]);
            mainPanel.add(passengerLabels[i]);
        }

        // Etykiety pięter
        for (int i = 0; i < building.getFloorsCount(); i++) {
            JLabel floorLabel = new JLabel("P" + i);
            floorLabel.setBounds(80, 590 - i * 50, 30, 40);
            floorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            floorLabel.setVerticalAlignment(SwingConstants.CENTER);
            floorLabel.setFont(new Font("Arial", Font.BOLD, 12));
            mainPanel.add(floorLabel);
        }

        add(mainPanel);
    }

    private void setupEventHandlers() {
        startButton.addActionListener(e -> controller.startSimulation());

        for (int i = 0; i < building.getFloorsCount(); i++) {
            int floor = i;
            callButtons[i].addActionListener(e -> {
                controller.callElevator(floor);
                callButtons[floor].setEnabled(false);

                activeCallFloors.add(floor);
                updateArrows();
            });

            floorButtons[i].addActionListener(e -> {
                controller.selectDestination(floor);
                floorButtons[floor].setEnabled(false);
            });
        }

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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
            passengerLabels[floor].setBackground(Color.WHITE);
            passengerLabels[floor].setOpaque(true);
            callButtons[floor].setEnabled(false);
        }
        updateFloorPassengerDots(floor);
    }

    public void updateFloorPassengerDots(int floor) {
        List<Passenger> floorDots = passengersOnFloorsDots.get(floor);
        int currentPassengers = building.getWaitingPassengers(floor);

        while (floorDots.size() < currentPassengers) {
            Color passengerColor = Utils.Utils.getRandomPassengerColor();
            Passenger newDot = new Passenger(0, 0, passengerColor, floor);
            floorDots.add(newDot);
        }

        // Jeśli jest mniej pasażerów niż kropek - usuń kropki
        while (floorDots.size() > currentPassengers) {
            floorDots.remove(floorDots.size() - 1);
        }

        mainPanel.repaint();
    }

    public void updateElevatorPassengersDisplay() {
        mainPanel.repaint();
    }

    public void addPassengerToElevator() {
        int dotIndex = passengersInElevatorDots.size();
        int x = 5 + (dotIndex % 5) * 8;
        int y = 5 + (dotIndex / 5) * 10;

        Color passengerColor = Utils.Utils.getFloorColor(elevator.getCurrentFloor());
        Passenger newDot = new Passenger(x, y, passengerColor, elevator.getCurrentFloor());
        passengersInElevatorDots.add(newDot);

        updateElevatorPassengersDisplay();
    }

    public void removePassengerFromElevator() {
        if (!passengersInElevatorDots.isEmpty()) {
            passengersInElevatorDots.remove(0);

            for (int i = 0; i < passengersInElevatorDots.size(); i++) {
                Passenger dot = passengersInElevatorDots.get(i);
                dot.x = 5 + (i % 5) * 8;
                dot.y = 5 + (i / 5) * 10;
            }

            updateElevatorPassengersDisplay();
        }
    }

    public void updateArrows() {
        for (int i = 0; i < building.getFloorsCount(); i++) {
            directionArrows[i].setVisible(false);
        }

        for (int floor : activeCallFloors) {
            if (elevator.getDirection() > 0) {
                directionArrows[floor].setText("↑");
                directionArrows[floor].setForeground(Color.GREEN);
            } else if (elevator.getDirection() < 0) {
                directionArrows[floor].setText("↓");
                directionArrows[floor].setForeground(Color.RED);
            } else {
                // Gdy winda stoi, pokaż kierunek na podstawie względnej pozycji
                if (floor > elevator.getCurrentFloor()) {
                    directionArrows[floor].setText("↑");
                    directionArrows[floor].setForeground(Color.GREEN);
                } else if (floor < elevator.getCurrentFloor()) {
                    directionArrows[floor].setText("↓");
                    directionArrows[floor].setForeground(Color.RED);
                }
            }
            directionArrows[floor].setVisible(true);
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

    public void clearFloorCall(int floor) {
        activeCallFloors.remove(floor);
        updateArrows();
    }

    public void endSimulation() {
        startButton.setEnabled(true);

        for (int i = 0; i < building.getFloorsCount(); i++) {
            callButtons[i].setEnabled(false);
            floorButtons[i].setEnabled(false);
        }

        passengersInElevatorDots.clear();

        for (List<Passenger> floorDots : passengersOnFloorsDots) {
            floorDots.clear();
        }

        activeCallFloors.clear();

        if (animationTimer != null) {
            animationTimer.stop();
        }
        isAnimating = false;
        elevatorAnimationY = 600;
        pulleyRotation = 0;

        updateArrows();
        mainPanel.repaint();
    }
}