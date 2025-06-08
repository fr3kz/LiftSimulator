package controler;

import model.Building;
import model.Elevator;
import view.ElevatorGUI;

import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ElevatorController {
    private Elevator elevator;
    private Building building;
    private ElevatorGUI gui;
    private boolean simulationRunning = false;

    private Timer movementTimer;
    private Timer waitTimer;
    private Timer exitTimer;
    private Timer entryTimer;
    private Timer continueTimer;
    private Timer endTimer;

    // Stan procesu wymiany pasażerów
    private boolean isExitPhase = false;
    private boolean isEntryPhase = false;

    public ElevatorController(Elevator elevator, Building building, ElevatorGUI gui) {
        this.elevator = elevator;
        this.building = building;
        this.gui = gui;
    }

    public void startSimulation() {
        simulationRunning = true;
        building.generateRandomPassengers();
        elevator.setCurrentFloor(0);
        gui.updateAfterStart();
        System.out.println("Symulacja rozpoczęta!");
    }

    public void callElevator(int floor) {
        building.addCall(floor);
        if (!elevator.isMoving() && !gui.isElevatorAnimating()) {
            planNextMove();
        }
        System.out.println("Wezwanie windy na piętro " + floor);
    }

    public void selectDestination(int floor) {
        elevator.addDestination(floor);
        if (!elevator.isMoving() && !gui.isElevatorAnimating()) {
            planNextMove();
        }
        System.out.println("Wybrano cel: piętro " + floor);
    }

    public void exitPassenger() {
        // Pasażerowie mogą wysiadać tylko podczas fazy wysiadania
        if (simulationRunning && !elevator.isMoving() && !gui.isElevatorAnimating() &&
                !elevator.isEmpty() && isExitPhase) {

            elevator.removePassenger();
            gui.removePassengerFromElevator();
            gui.updateFloorButtonsAvailability();
            System.out.println("Pasażer wysiadł na piętrze " + elevator.getCurrentFloor());
        }
    }

    private void planNextMove() {
        if (elevator.isMoving() || gui.isElevatorAnimating()) return;

        int nextFloor = findNextDestination();
        if (nextFloor != -1) {
            elevator.setDirection((nextFloor > elevator.getCurrentFloor()) ? 1 : -1);
            startMoving();
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

    private void startMoving() {
        elevator.setMoving(true);
        gui.updateArrows();

        movementTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gui.isElevatorAnimating()) {
                    elevator.moveForOneFloor();
                    gui.updateElevatorPosition();

                    // Sprawdź czy zatrzymać się na tym piętrze
                    if (building.getCalls().contains(elevator.getCurrentFloor()) ||
                            elevator.getDestinations().contains(elevator.getCurrentFloor())) {
                        movementTimer.stop();
                        waitForAnimationAndStop();
                    }
                }
            }
        });
        movementTimer.setInitialDelay(500);
        movementTimer.start();
    }

    private void waitForAnimationAndStop() {
        waitTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gui.isElevatorAnimating()) {
                    waitTimer.stop();
                    stopAtFloor();
                }
            }
        });
        waitTimer.start();
    }

    private void stopAtFloor() {
        elevator.setMoving(false);
        int currentFloor = elevator.getCurrentFloor();

        System.out.println("Winda zatrzymała się na piętrze " + currentFloor);

        building.removeCall(currentFloor);
        elevator.removeDestination(currentFloor);
        gui.clearFloorCall(currentFloor);
        startPassengerExchange();
    }

    private void startPassengerExchange() {
        isExitPhase = true;
        isEntryPhase = false;

        System.out.println("Faza wysiadania rozpoczęta - pasażerowie mają 3 sekundy na wysiadanie");

        exitTimer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitTimer.stop();
                isExitPhase = false;
                startEntryPhase();
            }
        });
        exitTimer.setRepeats(false);
        exitTimer.start();
    }

    private void startEntryPhase() {
        isEntryPhase = true;

        System.out.println("Faza wsiadania rozpoczęta");

        entryTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                entryTimer.stop();
                enterPassengers();
                isEntryPhase = false;
                gui.updateFloorButtonsAvailability();

                // Po zakończeniu wymiany pasażerów, kontynuuj ruch po 2 sekundach
                continueTimer = new Timer(2000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e2) {
                        continueTimer.stop();
                        planNextMove();
                    }
                });
                continueTimer.setRepeats(false);
                continueTimer.start();
            }
        });
        entryTimer.setRepeats(false);
        entryTimer.start();
    }

    private void enterPassengers() {
        int currentFloor = elevator.getCurrentFloor();
        int entering = Math.min(
                building.getWaitingPassengers(currentFloor),
                elevator.getAvailableSpace()
        );

        for (int i = 0; i < entering; i++) {
            elevator.addPassenger();
            gui.addPassengerToElevator();
        }

        building.removePassengers(currentFloor, entering);
        gui.updatePassengerDisplay(currentFloor);

        System.out.println("Wsiadło " + entering + " pasażerów na piętrze " + currentFloor);
    }

    private void checkSimulationEnd() {
        if (building.getCalls().isEmpty() &&
                elevator.getDestinations().isEmpty() &&
                elevator.isEmpty()) {

            endTimer = new Timer(10000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    endTimer.stop();
                    if (building.getCalls().isEmpty() &&
                            elevator.getDestinations().isEmpty() &&
                            elevator.isEmpty()) {
                        endSimulation();
                    }
                }
            });
            endTimer.setRepeats(false);
            endTimer.start();
        }
    }

    private void endSimulation() {
        simulationRunning = false;
        stopAllTimers();

        isExitPhase = false;
        isEntryPhase = false;

        gui.endSimulation();

        System.out.println("Symulacja zakończona!");
        JOptionPane.showMessageDialog(gui, "Symulacja zakończona!");
    }

    private void stopAllTimers() {
        if (movementTimer != null && movementTimer.isRunning()) {
            movementTimer.stop();
        }
        if (waitTimer != null && waitTimer.isRunning()) {
            waitTimer.stop();
        }
        if (exitTimer != null && exitTimer.isRunning()) {
            exitTimer.stop();
        }
        if (entryTimer != null && entryTimer.isRunning()) {
            entryTimer.stop();
        }
        if (continueTimer != null && continueTimer.isRunning()) {
            continueTimer.stop();
        }
        if (endTimer != null && endTimer.isRunning()) {
            endTimer.stop();
        }
    }

    public boolean isSimulationRunning() {
        return simulationRunning;
    }

    public boolean isExitPhase() {
        return isExitPhase;
    }

    public boolean isEntryPhase() {
        return isEntryPhase;
    }
}