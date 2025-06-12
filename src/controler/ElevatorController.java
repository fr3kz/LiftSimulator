package controler;

import model.Building;
import model.Elevator;
import model.Passenger;
import view.ElevatorGUI;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElevatorController {
    private Elevator elevator;
    private Building building;
    private ElevatorGUI gui;

    private boolean simulationRunning = false;
    private boolean isExitPhase = false;

    private Timer mainTimer;

    // Centralized passenger data
    private final Set<Integer> activeCallFloors = new HashSet<>();
    private final List<Passenger> passengersInElevator = new ArrayList<>();
    private final List<List<Passenger>> passengersOnFloors = new ArrayList<>();

    public ElevatorController(Elevator elevator, Building building, ElevatorGUI gui) {
        this.elevator = elevator;
        this.building = building;
        this.gui = gui;
        initializePassengerLists();
    }

    private void initializePassengerLists() {
        for (int i = 0; i < building.getFloorsCount(); i++) {
            passengersOnFloors.add(new ArrayList<>());
        }
    }

    public void startSimulation() {
        simulationRunning = true;
        building.generateRandomPassengers();
        elevator.setCurrentFloor(0);
        updateFloorPassengerVisuals();
        gui.updateAfterStart();
        System.out.println("Symulacja rozpoczęta!");
    }

    public void callElevator(int floor) {
        building.addCall(floor);
        activeCallFloors.add(floor);
        System.out.println("Wezwanie windy na piętro " + floor);
        StartMovement();
    }

    public void selectDestination(int floor) {
        elevator.addDestination(floor);
        System.out.println("Wybrano cel: piętro " + floor);
        StartMovement();
    }

    public void exitPassenger() {
        if (canExitPassenger()) {
            elevator.removePassenger();
            removePassengerFromElevatorVisual();
            gui.updateButtonStates();
            System.out.println("Pasażer wysiadł na piętrze " + elevator.getCurrentFloor());
        }
    }

    private boolean canExitPassenger() {
        return simulationRunning && !elevator.isMoving() &&
                !gui.isElevatorAnimating() && !elevator.isEmpty() && isExitPhase;
    }

    private void StartMovement() {
        if (elevator.isMoving() || gui.isElevatorAnimating()) {
            return;
        }

        int nextFloor = findNextDestination();
        if (nextFloor != -1) {
            startMovingToFloor(nextFloor);
        } else {
            elevator.setDirection(0);
            gui.updateArrows();
            checkSimulationEnd();
        }
    }

    private int findNextDestination() {
        Set<Integer> allTargets = new HashSet<>(elevator.getDestinations());
        allTargets.addAll(building.getCalls());

        if (allTargets.isEmpty()) return -1;

        int currentFloor = elevator.getCurrentFloor();
        int direction = elevator.getDirection();
        int closest = -1;
        int minDistance = Integer.MAX_VALUE;

        // Najpierw szukaj w kierunku ruchu (jeśli winda się porusza)
        if (direction != 0) {
            for (int target : allTargets) {
                if ((direction > 0 && target > currentFloor) ||
                        (direction < 0 && target < currentFloor)) {
                    int distance = Math.abs(target - currentFloor);
                    if (distance < minDistance) {
                        closest = target;
                        minDistance = distance;
                    }
                }
            }
        }

        // Jeśli nie znaleziono w kierunku ruchu, znajdź najbliższy ogólnie
        if (closest == -1) {
            for (int target : allTargets) {
                int distance = Math.abs(target - currentFloor);
                if (distance > 0 && distance < minDistance) {
                    closest = target;
                    minDistance = distance;
                }
            }
        }

        return closest;
    }

    private void startMovingToFloor(int targetFloor) {
        elevator.setDirection(targetFloor > elevator.getCurrentFloor() ? 1 : -1);
        elevator.setMoving(true);
        gui.updateArrows();

        mainTimer = new Timer(1000, new MoveActionListener());
        mainTimer.setInitialDelay(500);
        mainTimer.start();
    }

    private class MoveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!gui.isElevatorAnimating()) {
                elevator.moveForOneFloor();
                gui.updateElevatorPosition();

                if (shouldStopAtCurrentFloor()) {
                    stopTimer();
                    waitForAnimationThenStop();
                }
            }
        }
    }

    private boolean shouldStopAtCurrentFloor() {
        int currentFloor = elevator.getCurrentFloor();
        return building.getCalls().contains(currentFloor) ||
                elevator.getDestinations().contains(currentFloor);
    }

    private void waitForAnimationThenStop() {
        Timer waitTimer = new Timer(50, e -> {
            if (!gui.isElevatorAnimating()) {
                ((Timer) e.getSource()).stop();
                handleFloorStop();
            }
        });
        waitTimer.start();
    }

    private void handleFloorStop() {
        elevator.setMoving(false);
        int currentFloor = elevator.getCurrentFloor();

        System.out.println("Winda zatrzymała się na piętrze " + currentFloor);

        // Wyczyść wezwania i cele
        building.removeCall(currentFloor);
        elevator.removeDestination(currentFloor);
        clearFloorCall(currentFloor);

        startPassengerExchange();
    }

    private void startPassengerExchange() {
        isExitPhase = true;
        System.out.println("Faza wysiadania - 4 sekundy");

        Timer exitTimer = new Timer(4000, e -> {
            ((Timer) e.getSource()).stop();
            isExitPhase = false;
            startEntryPhase();
        });
        exitTimer.setRepeats(false);
        exitTimer.start();
    }

    private void startEntryPhase() {
        System.out.println("Faza wsiadania - 1 sekunda");

        Timer entryTimer = new Timer(1000, e -> {
            ((Timer) e.getSource()).stop();
            processPassengerEntry();
            StartMovement();
        });
        entryTimer.setRepeats(false);
        entryTimer.start();
    }

    private void processPassengerEntry() {
        int currentFloor = elevator.getCurrentFloor();
        int entering = Math.min(
                building.getWaitingPassengers(currentFloor),
                elevator.getAvailableSpace()
        );

        for (int i = 0; i < entering; i++) {
            elevator.addPassenger();
            addPassengerToElevatorVisual();
        }

        building.removePassengers(currentFloor, entering);
        updateFloorPassengerVisual(currentFloor);
        gui.updateButtonStates();

        System.out.println("Wsiadło " + entering + " pasażerów na piętrze " + currentFloor);
    }

    private void checkSimulationEnd() {
        boolean shouldEnd = building.getCalls().isEmpty() &&
                elevator.getDestinations().isEmpty() &&
                elevator.isEmpty();

        if (shouldEnd) {
            Timer endTimer = new Timer(10000, e -> {
                ((Timer) e.getSource()).stop();
                if (isSimulationStillEmpty()) {
                    endSimulation();
                }
            });
            endTimer.setRepeats(false);
            endTimer.start();
        }
    }

    private boolean isSimulationStillEmpty() {
        return building.getCalls().isEmpty() &&
                elevator.getDestinations().isEmpty() &&
                elevator.isEmpty();
    }

    private void endSimulation() {
        simulationRunning = false;
        isExitPhase = false;
        stopTimer();

        // Reset visual data
        activeCallFloors.clear();
        passengersInElevator.clear();
        for (List<Passenger> floorPassengers : passengersOnFloors) {
            floorPassengers.clear();
        }

        gui.endSimulation();

        System.out.println("Symulacja zakończona!");
        JOptionPane.showMessageDialog(gui, "Symulacja zakończona!");
    }

    private void stopTimer() {
        if (mainTimer != null && mainTimer.isRunning()) {
            mainTimer.stop();
        }
    }

    // Visual management methods
    private void updateFloorPassengerVisuals() {
        for (int floor = 0; floor < building.getFloorsCount(); floor++) {
            updateFloorPassengerVisual(floor);
        }
    }

    private void updateFloorPassengerVisual(int floor) {
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

        gui.repaint();
    }

    private void addPassengerToElevatorVisual() {
        int dotIndex = passengersInElevator.size();
        int x = 5 + (dotIndex % 3) * 8;
        int y = 5 + (dotIndex / 3) * 10;

        Color passengerColor = Utils.Utils.getFloorColor(elevator.getCurrentFloor());
        Passenger newPassenger = new Passenger(x, y, passengerColor, elevator.getCurrentFloor());
        passengersInElevator.add(newPassenger);

        gui.repaint();
    }

    private void removePassengerFromElevatorVisual() {
        if (!passengersInElevator.isEmpty()) {
            passengersInElevator.remove(0);
            repositionPassengersInElevator();
            gui.repaint();
        }
    }

    private void repositionPassengersInElevator() {
        for (int i = 0; i < passengersInElevator.size(); i++) {
            Passenger passenger = passengersInElevator.get(i);
            passenger.x = 5 + (i % 3) * 8;
            passenger.y = 5 + (i / 3) * 10;
        }
    }

    private void clearFloorCall(int floor) {
        activeCallFloors.remove(floor);
        gui.updateArrows();
    }

    // Getters for GUI to access data
    public Set<Integer> getActiveCallFloors() {
        return activeCallFloors;
    }

    public List<Passenger> getPassengersInElevator() {
        return passengersInElevator;
    }

    public List<List<Passenger>> getPassengersOnFloors() {
        return passengersOnFloors;
    }

    public int determineDirectionForFloor(int floor) {
        int elevatorDirection = elevator.getDirection();
        int currentFloor = elevator.getCurrentFloor();

        if (elevatorDirection != 0) {
            return elevatorDirection;
        } else {
            return Integer.compare(floor, currentFloor);
        }
    }

    public boolean shouldEnableElevatorButton(int floor) {
        return !elevator.isEmpty() &&
                floor != elevator.getCurrentFloor() &&
                !elevator.getDestinations().contains(floor);
    }

    public boolean shouldEnableCallButton(int floor) {
        return building.hasWaitingPassengers(floor);
    }

}