package view;

import controler.ElevatorController;
import model.Building;
import model.Elevator;
import model.Passenger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ElevatorGUI extends JFrame {
    private Elevator elevator;
    private Building building;
    private ElevatorController controller;

    // Komponenty UI
    private JPanel mainPanel;
    private JButton startButton;
    private JButton[] callButtons;
    private JButton[] elevatorButtons;
    private JLabel[] directionArrows;
    private JPanel[] floorPanels;

    //Stałe animacji
    private double elevatorY;
    private int targetFloor;
    private Timer animationTimer;
    private boolean isAnimating = false;
    private double pulleyRotation = 0;
    private static final double animation_speed = 1.0;
    private static final double circle_speed = 11.0;
    private static final int floor_height = 50;
    private static final int elevator_height = 40;

    public ElevatorGUI() {
        initializeModels();
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
        floorPanels[floor].setBounds(450, 590 - floor * floor_height, 120, 40);
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

        double targetY = 600 - targetFloor * floor_height;
        double distance = targetY - elevatorY;

        if (Math.abs(distance) < 1.0) {
            elevatorY = targetY;
            isAnimating = false;
            animationTimer.stop();
        } else {
            double step = Math.signum(distance) * animation_speed;
            elevatorY += step;
            updatePulleyRotation(distance);
        }

        mainPanel.repaint();
    }

    private void updatePulleyRotation(double distance) {
        pulleyRotation += circle_speed * Math.signum(distance);
        if (pulleyRotation >= 360) pulleyRotation -= 360;
        if (pulleyRotation < 0) pulleyRotation += 360;
    }

    private void drawBuilding(Graphics g) {
        drawElevatorBuilding(g);
        drawPulleySystem(g);
    }

    private void drawElevatorBuilding(Graphics g) {
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
            int y = 600 - i * floor_height;
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
        g2d.drawRoundRect(elevatorX, elevatorY, elevator_height, elevator_height, 8, 8);

        // Punkt mocowania
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillOval(elevatorX + elevator_height /2 - 3, elevatorY - 5, 6, 6);

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

        List<Passenger> passengers = controller.getPassengersInElevator();
        for (Passenger passenger : passengers) {
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
            int floorY = 600 - floor * floor_height;
            int startX = 160;

            int waitingPassengers = building.getWaitingPassengers(floor);

            for (int i = 0; i < waitingPassengers; i++) {
                Color passengerColor = Utils.Utils.getFloorColor(floor);
                int x = startX + i * 12;
                int y = floorY + 10;

                g2d.setColor(passengerColor);
                g2d.fillOval(x, y, 6, 6);
                g2d.setColor(passengerColor.darker());
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
            floorLabel.setBounds(280, 590 - i * floor_height, 30, 40);
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
        return x >= elevatorX && x <= elevatorX + elevator_height &&
                y >= elevatorY && y <= elevatorY + elevator_height;
    }

    public void updateAfterStart() {
        startButton.setEnabled(false);
        updateButtonStates();
    }

    public void updateElevatorPosition() {
        targetFloor = elevator.getCurrentFloor();
        if (!isAnimating) {
            isAnimating = true;
            animationTimer.start();
        }

    }

    public boolean isElevatorAnimating() {
        return isAnimating;
    }

    public void updateArrows() {
        for (JLabel arrow : directionArrows) {
            arrow.setVisible(false);
        }

        for (int floor : controller.getActiveCallFloors()) {
            int direction = controller.determineDirectionForFloor(floor);

            if (direction > 0) {
                directionArrows[floor].setText("↑");
                directionArrows[floor].setForeground(Color.GREEN);
            } else if (direction < 0) {
                directionArrows[floor].setText("↓");
                directionArrows[floor].setForeground(Color.RED);
            }

            directionArrows[floor].setVisible(true);
        }
    }

    public void updateButtonStates() {

        for (int i = 0; i < building.getFloorsCount(); i++) {
            callButtons[i].setEnabled(controller.shouldEnableCallButton(i));
        }

        for (int i = 0; i < building.getFloorsCount(); i++) {
            elevatorButtons[i].setEnabled(controller.shouldEnableElevatorButton(i));
        }
    }

    public void endSimulation() {
        resetUI();
        stopAnimation();
        mainPanel.repaint();
    }

    private void resetUI() {
        startButton.setEnabled(true);

        for (int i = 0; i < building.getFloorsCount(); i++) {
            callButtons[i].setEnabled(false);
            elevatorButtons[i].setEnabled(false);
        }

        updateArrows();
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        isAnimating = false;
        elevatorY = 600;
        pulleyRotation = 0;
    }

    public void repaint() {
        if (mainPanel != null) {
            mainPanel.repaint();
        }
    }
}