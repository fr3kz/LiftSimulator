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
    private JLabel elevatorLabel;
    private JLabel elevatorPassengersLabel;

    private List<PassengerDot> passengersInElevatorDots = new ArrayList<>();


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

        setTitle("Symulacja Windy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPassengersInElevator(g);
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

        elevatorLabel = new JLabel();
        elevatorLabel.setBounds(320, 600, 40, 40);
        elevatorLabel.setBackground(Color.BLUE);
        elevatorLabel.setOpaque(true);
        elevatorLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        elevatorPassengersLabel = new JLabel("0");
        elevatorPassengersLabel.setBounds(325, 605, 30, 30);
        elevatorPassengersLabel.setHorizontalAlignment(SwingConstants.CENTER);
        elevatorPassengersLabel.setForeground(Color.WHITE);
        elevatorPassengersLabel.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private void drawPassengersInElevator(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int elevatorX = 320;
        int elevatorY = 600 - elevator.getCurrentFloor() * 50;

        for (PassengerDot dot : passengersInElevatorDots) {
            g2d.setColor(dot.color);
            g2d.fillOval(elevatorX + dot.x, elevatorY + dot.y, 8, 8);
            g2d.setColor(dot.color.darker());
            g2d.drawOval(elevatorX + dot.x, elevatorY + dot.y, 8, 8);
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

    private void setupLayout() {
        mainPanel.add(startButton);

        for (int i = 0; i < building.getFloorsCount(); i++) {
            mainPanel.add(callButtons[i]);
            mainPanel.add(upArrows[i]);
            mainPanel.add(downArrows[i]);
            mainPanel.add(floorButtons[i]);
            mainPanel.add(passengerLabels[i]);
        }

        mainPanel.add(elevatorLabel);
        mainPanel.add(elevatorPassengersLabel);

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

        elevatorLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.exitPassenger();
            }
        });
    }

    public void updateAfterStart() {
        startButton.setEnabled(false);

        for (int i = 0; i < building.getFloorsCount(); i++) {
            updatePassengerDisplay(i);
            callButtons[i].setEnabled(building.hasWaitingPassengers(i));
        }

        updateElevatorPosition();
        updateArrows();
    }

    public void updateElevatorPosition() {
        elevatorLabel.setBounds(320, 600 - elevator.getCurrentFloor() * 50, 40, 40);
        elevatorPassengersLabel.setBounds(325, 605 - elevator.getCurrentFloor() * 50, 30, 30);
        mainPanel.repaint();
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
    }

    public void updateElevatorPassengersDisplay() {
        elevatorPassengersLabel.setText(String.valueOf(elevator.getPassengersInElevator().size()));
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
        updateArrows();
        mainPanel.repaint();
    }
}