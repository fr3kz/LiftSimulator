import java.util.*;
import java.util.Timer;

public class ElevatorController {
    private Elevator elevator;
    private Building building;
    private ElevatorGUI gui;
    private boolean simulationRunning = false;

    private Timer movementTimer;
    private Timer stopTimer;

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

        if (!elevator.isMoving()) {
            planNextMove();
        }

        System.out.println("Wezwanie windy na piętro " + floor);
    }

    public void selectDestination(int floor) {
        elevator.addDestination(floor);

        if (!elevator.isMoving()) {
            planNextMove();
        }

        System.out.println("Wybrano cel: piętro " + floor);
    }

    public void exitPassenger() {
        if (simulationRunning && !elevator.isMoving() && !elevator.isEmpty()) {
            elevator.removePassenger();
            gui.updateElevatorPassengersDisplay();
            gui.updateFloorButtonsAvailability();

            System.out.println("Pasażer wysiadł na piętrze " + elevator.getCurrentFloor());
        }
    }

    private void planNextMove() {
        if (elevator.isMoving()) return;

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

        int closest = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int target : allTargets) {
            int distance = Math.abs(target - elevator.getCurrentFloor());
            if (distance < minDistance && distance > 0) {
                if (elevator.getDirection() != 0) {
                    if ((elevator.getDirection() > 0 && target > elevator.getCurrentFloor()) ||
                            (elevator.getDirection() < 0 && target < elevator.getCurrentFloor())) {
                        closest = target;
                        minDistance = distance;
                    }
                } else {
                    closest = target;
                    minDistance = distance;
                }
            }
        }

        if (closest == -1) {
            for (int target : allTargets) {
                int distance = Math.abs(target - elevator.getCurrentFloor());
                if (distance < minDistance && distance > 0) {
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

        movementTimer = new Timer();
        movementTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    elevator.moveOneFloor();
                    gui.updateElevatorPosition();

                    if (building.getCalls().contains(elevator.getCurrentFloor()) ||
                            elevator.getDestinations().contains(elevator.getCurrentFloor())) {
                        stopAtFloor();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void stopAtFloor() {
        elevator.setMoving(false);
        movementTimer.cancel();

        System.out.println("Winda zatrzymała się na piętrze " + elevator.getCurrentFloor());

        building.removeCall(elevator.getCurrentFloor());
        elevator.removeDestination(elevator.getCurrentFloor());

        Timer entryTimer = new Timer();
        entryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    enterPassengers();
                    gui.updateFloorButtonsAvailability();
                });
            }
        }, 1000);

        stopTimer = new Timer();
        stopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    planNextMove();
                });
            }
        }, 5000);
    }

    private void enterPassengers() {
        int entering = Math.min(building.getWaitingPassengers(elevator.getCurrentFloor()),
                elevator.getAvailableSpace());

        for (int i = 0; i < entering; i++) {
            elevator.addPassenger();
        }

        building.removePassengers(elevator.getCurrentFloor(), entering);
        gui.updatePassengerDisplay(elevator.getCurrentFloor());
        gui.updateElevatorPassengersDisplay();

        System.out.println("Wsiadło " + entering + " pasażerów na piętrze " + elevator.getCurrentFloor());
    }

    private void checkSimulationEnd() {
        if (building.getCalls().isEmpty() && elevator.getDestinations().isEmpty() && elevator.isEmpty()) {
            Timer endTimer = new Timer();
            endTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        if (building.getCalls().isEmpty() && elevator.getDestinations().isEmpty() && elevator.isEmpty()) {
                            endSimulation();
                        }
                    });
                }
            }, 10000);
        }
    }

    private void endSimulation() {
        simulationRunning = false;
        gui.endSimulation();

        System.out.println("Symulacja zakończona!");
        javax.swing.JOptionPane.showMessageDialog(gui, "Symulacja zakończona!");
    }

    public boolean isSimulationRunning() {
        return simulationRunning;
    }
}