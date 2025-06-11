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
    private  Elevator elevator;
    private  Building building;
    private  ElevatorController controller;

    // Komponenty UI
    private JPanel mainPanel;
    private JButton startButton;
    private JButton[] callButtons;
    private JButton[] elevatorButtons;
    private JLabel[] directionArrows;
    private JPanel[] floorPanels;

    // Stany pasażerów
    private final Set<Integer> activeCallFloors = new HashSet<>();
    private final List<Passenger> passengersInElevator = new ArrayList<>();
    private final List<List<Passenger>> passengersOnFloors = new ArrayList<>();

    // Animacja
    private double elevatorY = 600;
    private int targetFloor = 0;
    private Timer animationTimer;
    private boolean isAnimating = false;
    private double pulleyRotation = 0;

    // Stałe animacji
    private static final double ANIMATION_SPEED = 1.0;
    private static final double PULLEY_SPEED = 11.0;
    private static final int FLOOR_HEIGHT = 50;
    private static final int ELEVATOR_SIZE = 40;

    public ElevatorGUI() {
        initializeModels();
        initializePassengerLists();
        setupWindow();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupAnimationTimer();
    }

    private void initializeModels() {
        elevator = new Elevator();
        building = new Building();
        controller = new ElevatorController(elevator, building, this);
        elevatorY = 600;
        targetFloor = 0;
    }

    private void initializePassengerLists() {
        for (int i = 0; i < building.getFloorsCount(); i++) {
            passengersOnFloors.add(new ArrayList<>());
        }
    }

    private void setupWindow() {
        setTitle("Symulacja Windy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        createMainPanel();
        createStartButton();
        createFloorComponents();
        createElevatorButtons();
    }

    private void createMainPanel() {
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBuilding(g);
                drawElevator(g);
                drawPassengers(g);
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.LIGHT_GRAY);
    }

    private void createStartButton() {
        startButton = new JButton("START");
        startButton.setBounds(50, 50, 100, 30);
    }

    private void createFloorComponents() {
        int floorCount = building.getFloorsCount();
        callButtons = new JButton[floorCount];
        directionArrows = new JLabel[floorCount];
        floorPanels = new JPanel[floorCount];

        for (int i = 0; i < floorCount; i++) {
            createFloorPanel(i);
            createCallButton(i);
            createDirectionArrow(i);
        }
    }

    private void createFloorPanel(int floor) {
        floorPanels[floor] = new JPanel();
        floorPanels[floor].setLayout(null);
        floorPanels[floor].setBounds(450, 590 - floor * FLOOR_HEIGHT, 120, 40);
        floorPanels[floor].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        floorPanels[floor].setBackground(new Color(240, 240, 240));
    }

    private void createCallButton(int floor) {
        callButtons[floor] = new JButton("Wezwij");
        callButtons[floor].setBounds(5, 8, 70, 25);
        callButtons[floor].setEnabled(false);
        callButtons[floor].setFont(new Font("Arial", Font.PLAIN, 10));
        floorPanels[floor].add(callButtons[floor]);
    }

    private void createDirectionArrow(int floor) {
        directionArrows[floor] = new JLabel("", SwingConstants.CENTER);
        directionArrows[floor].setBounds(80, 5, 30, 30);
        directionArrows[floor].setFont(new Font("Arial", Font.BOLD, 20));
        directionArrows[floor].setVisible(false);
        floorPanels[floor].add(directionArrows[floor]);
    }

    private void createElevatorButtons() {
        int floorCount = building.getFloorsCount();
        elevatorButtons = new JButton[floorCount];

        for (int i = 0; i < floorCount; i++) {
            elevatorButtons[i] = new JButton(String.valueOf(i));
            elevatorButtons[i].setBounds(15 + (i % 3) * 40, 500 + (i / 3) * 40, 35, 35);
            elevatorButtons[i].setEnabled(false);
            elevatorButtons[i].setFocusable(false);
        }
    }

    private void setupAnimationTimer() {
        animationTimer = new Timer(0, e -> updateAnimation());
    }

    private void updateAnimation() {
        if (!isAnimating) return;

        double targetY = 600 - targetFloor * FLOOR_HEIGHT;
        double distance = targetY - elevatorY;

        if (Math.abs(distance) < 1.0) {
            elevatorY = targetY;
            isAnimating = false;
            animationTimer.stop();
        } else {
            double step = Math.signum(distance) * ANIMATION_SPEED;
            elevatorY += step;
            updatePulleyRotation(distance);
        }

        mainPanel.repaint();
    }

    private void updatePulleyRotation(double distance) {
        pulleyRotation += PULLEY_SPEED * Math.signum(distance);
        if (pulleyRotation >= 360) pulleyRotation -= 360;
        if (pulleyRotation < 0) pulleyRotation += 360;
    }

    private void drawBuilding(Graphics g) {
        drawElevatorShaft(g);
        drawPulleySystem(g);
    }

    private void drawElevatorShaft(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Ściany szybu
        g2d.setColor(new Color(80, 80, 80));
        g2d.setStroke(new BasicStroke(5));
        g2d.drawLine(315, 100, 315, 650); // lewa
        g2d.drawLine(365, 100, 365, 650); // prawa

        // Linie pięter
        g2d.setColor(new Color(120, 120, 120));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= building.getFloorsCount(); i++) {
            int y = 600 - i * FLOOR_HEIGHT;
            g2d.drawLine(315, y + 40, 365, y + 40);
        }

        // Dach
        g2d.setColor(new Color(60, 60, 60));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine(300, 95, 380, 95);

        g2d.dispose();
    }

    private void drawPulleySystem(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = 340, centerY = 80, radius = 15;

        // Rysuj koło z rotacją
        g2d.translate(centerX, centerY);
        g2d.rotate(Math.toRadians(pulleyRotation));

        g2d.setColor(new Color(150, 150, 150));
        g2d.fillOval(-radius, -radius, radius * 2, radius * 2);
        g2d.setColor(new Color(80, 80, 80));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(-radius, -radius, radius * 2, radius * 2);
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillOval(-4, -4, 8, 8);

        g2d.rotate(-Math.toRadians(pulleyRotation));
        g2d.translate(-centerX, -centerY);

        // Liny
        g2d.setColor(new Color(139, 69, 19));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, centerY + radius, 340, (int) elevatorY);

        // Przeciwwaga
        int counterY = (int)(400 - elevatorY + 300);
        g2d.drawLine(centerX + radius, centerY, centerX + 50, counterY);
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRect(centerX + 42, counterY, 16, 30);

        g2d.dispose();
    }

    private void drawElevator(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int elevatorX = 320;
        int elevatorY = (int) this.elevatorY;

        // Ramka windy
        g2d.setColor(new Color(30, 60, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(elevatorX, elevatorY, ELEVATOR_SIZE, ELEVATOR_SIZE, 8, 8);

        // Punkt mocowania
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillOval(elevatorX + ELEVATOR_SIZE/2 - 3, elevatorY - 5, 6, 6);

        g2d.dispose();
    }

    private void drawPassengers(Graphics g) {
        drawPassengersInElevator(g);
        drawPassengersOnFloors(g);
    }

    private void drawPassengersInElevator(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int elevatorX = 320;
        int elevatorY = (int) this.elevatorY;

        for (Passenger passenger : passengersInElevator) {
            // Cień
            g2d.setColor(new Color(passenger.color.getRed(), passenger.color.getGreen(),
                    passenger.color.getBlue(), 100));
            g2d.fillOval(elevatorX + passenger.x - 1, elevatorY + passenger.y - 1, 10, 10);

            // Pasażer
            g2d.setColor(passenger.color);
            g2d.fillOval(elevatorX + passenger.x, elevatorY + passenger.y, 8, 8);
            g2d.setColor(passenger.color.darker());
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawOval(elevatorX + passenger.x, elevatorY + passenger.y, 8, 8);
        }

        g2d.dispose();
    }

    private void drawPassengersOnFloors(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int floor = 0; floor < building.getFloorsCount(); floor++) {
            int floorY = 600 - floor * FLOOR_HEIGHT;
            int startX = 160;

            List<Passenger> floorPassengers = passengersOnFloors.get(floor);
            for (int i = 0; i < floorPassengers.size(); i++) {
                Passenger passenger = floorPassengers.get(i);
                int x = startX + i * 12;
                int y = floorY + 10;

                g2d.setColor(passenger.color);
                g2d.fillOval(x, y, 6, 6);
                g2d.setColor(passenger.color.darker());
                g2d.drawOval(x, y, 6, 6);
            }
        }

        g2d.dispose();
    }

    private void setupLayout() {
        mainPanel.add(startButton);

        for (int i = 0; i < building.getFloorsCount(); i++) {
            mainPanel.add(floorPanels[i]);
            mainPanel.add(elevatorButtons[i]);

            // Etykiety pięter
            JLabel floorLabel = new JLabel("P" + i);
            floorLabel.setBounds(280, 590 - i * FLOOR_HEIGHT, 30, 40);
            floorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            floorLabel.setFont(new Font("Arial", Font.BOLD, 12));
            mainPanel.add(floorLabel);
        }

        add(mainPanel);
    }

    private void setupEventHandlers() {
        startButton.addActionListener(e -> controller.startSimulation());

        for (int i = 0; i < building.getFloorsCount(); i++) {
            setupFloorButton(i);
            setupElevatorButton(i);
        }

        setupElevatorClickHandler();
    }

    private void setupFloorButton(int floor) {
        callButtons[floor].addActionListener(e -> {
            controller.callElevator(floor);
            callButtons[floor].setEnabled(false);
            activeCallFloors.add(floor);
            updateArrows();
        });
    }

    private void setupElevatorButton(int floor) {
        elevatorButtons[floor].addActionListener(e -> {
            controller.selectDestination(floor);
            elevatorButtons[floor].setEnabled(false);
        });
    }

    private void setupElevatorClickHandler() {
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isClickOnElevator(e.getX(), e.getY())) {
                    controller.exitPassenger();
                }
            }
        });
    }

    private boolean isClickOnElevator(int x, int y) {
        int elevatorX = 320;
        int elevatorY = (int) this.elevatorY;
        return x >= elevatorX && x <= elevatorX + ELEVATOR_SIZE &&
                y >= elevatorY && y <= elevatorY + ELEVATOR_SIZE;
    }

    // Metody publiczne dla kontrolera
    public void updateAfterStart() {
        startButton.setEnabled(false);

        for (int i = 0; i < building.getFloorsCount(); i++) {
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

    public void updateFloorPassengerDots(int floor) {
        List<Passenger> floorDots = passengersOnFloors.get(floor);
        int waitingPassengers = building.getWaitingPassengers(floor);

        // Dodaj brakujących pasażerów
        while (floorDots.size() < waitingPassengers) {
            Color passengerColor = Utils.Utils.getRandomPassengerColor();
            Passenger newPassenger = new Passenger(0, 0, passengerColor, floor);
            floorDots.add(newPassenger);
        }

        // Usuń nadmiarowych pasażerów
        while (floorDots.size() > waitingPassengers) {
            floorDots.remove(floorDots.size() - 1);
        }

        mainPanel.repaint();
    }

    public void addPassengerToElevator() {
        int dotIndex = passengersInElevator.size();
        int x = 5 + (dotIndex % 3) * 8;
        int y = 5 + (dotIndex / 3) * 10;

        Color passengerColor = Utils.Utils.getFloorColor(elevator.getCurrentFloor());
        Passenger newPassenger = new Passenger(x, y, passengerColor, elevator.getCurrentFloor());
        passengersInElevator.add(newPassenger);

        mainPanel.repaint();
    }

    public void removePassengerFromElevator() {
        if (!passengersInElevator.isEmpty()) {
            passengersInElevator.remove(0);
            repositionPassengersInElevator();
            mainPanel.repaint();
        }
    }

    private void repositionPassengersInElevator() {
        for (int i = 0; i < passengersInElevator.size(); i++) {
            Passenger passenger = passengersInElevator.get(i);
            passenger.x = 5 + (i % 3) * 8;
            passenger.y = 5 + (i / 3) * 10;
        }
    }

    public void updateArrows() {
        for (JLabel arrow : directionArrows) {
            arrow.setVisible(false);
        }


        for (int floor : activeCallFloors) {
            updateArrowForFloor(floor);
        }
    }

    private void updateArrowForFloor(int floor) {
        int direction = determineDirectionForFloor(floor);

        if (direction > 0) {
            directionArrows[floor].setText("↑");
            directionArrows[floor].setForeground(Color.GREEN);
        } else if (direction < 0) {
            directionArrows[floor].setText("↓");
            directionArrows[floor].setForeground(Color.RED);
        }

        directionArrows[floor].setVisible(true);
    }

    private int determineDirectionForFloor(int floor) {
        int elevatorDirection = elevator.getDirection();
        int currentFloor = elevator.getCurrentFloor();

        if (elevatorDirection != 0) {
            return elevatorDirection;
        } else {
            return Integer.compare(floor, currentFloor);
        }
    }

    public void updateFloorButtonsAvailability() {
        for (int i = 0; i < building.getFloorsCount(); i++) {
            boolean shouldEnable =
                    !elevator.isEmpty() &&
                    i != elevator.getCurrentFloor() &&
                    !elevator.getDestinations().contains(i);

            elevatorButtons[i].setEnabled(shouldEnable);
        }
    }

    public void clearFloorCall(int floor) {
        activeCallFloors.remove(floor);
        updateArrows();
    }

    public void endSimulation() {
        resetUI();
        clearPassengers();
        stopAnimation();
        mainPanel.repaint();
    }

    private void resetUI() {
        startButton.setEnabled(true);

        for (int i = 0; i < building.getFloorsCount(); i++) {
            callButtons[i].setEnabled(false);
            elevatorButtons[i].setEnabled(false);
        }

        activeCallFloors.clear();
        updateArrows();
    }

    private void clearPassengers() {
        passengersInElevator.clear();
        for (List<Passenger> floorPassengers : passengersOnFloors) {
            floorPassengers.clear();
        }
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        isAnimating = false;
        elevatorY = 600;
        pulleyRotation = 0;
    }
}