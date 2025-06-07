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
        if (simulationRunning && !elevator.isMoving() && !gui.isElevatorAnimating() && !elevator.isEmpty()) {
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
                    // Sprawdź czy winda już nie jest animowana, zanim przejdziesz do kolejnego piętra
                    if (!gui.isElevatorAnimating()) {
                        elevator.moveOneFloor();
                        gui.updateElevatorPosition(); // To rozpocznie animację

                        // Sprawdź czy winda ma zatrzymać się na tym piętrze
                        if (building.getCalls().contains(elevator.getCurrentFloor()) ||
                                elevator.getDestinations().contains(elevator.getCurrentFloor())) {
                            movementTimer.cancel();
                            // Poczekaj aż animacja się skończy, potem zatrzymaj się
                            waitForAnimationAndStop();
                        }
                    }
                });
            }
        }, 500, 800); // Zwolniony timer, żeby animacja była płynniejsza
    }

    private void waitForAnimationAndStop() {
        Timer waitTimer = new Timer();
        waitTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (!gui.isElevatorAnimating()) {
                        waitTimer.cancel();
                        stopAtFloor();
                    }
                });
            }
        }, 50, 50);
    }

    private void stopAtFloor() {
        elevator.setMoving(false);

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
        }, 3000); // Skrócony czas postoju
    }

    private void enterPassengers() {
        int entering = Math.min(building.getWaitingPassengers(elevator.getCurrentFloor()),
                elevator.getAvailableSpace());

        for (int i = 0; i < entering; i++) {
            elevator.addPassenger();
            gui.addPassengerToElevator();
        }

        building.removePassengers(elevator.getCurrentFloor(), entering);
        gui.updatePassengerDisplay(elevator.getCurrentFloor());

        System.out.println("Wsiadło " + entering + " pasażerów na piętrze " + elevator.getCurrentFloor());
    }

    // Ta metoda jest wywoływana przez GUI gdy animacja się kończy
    public void onElevatorArrivedAtFloor() {
        // Możesz tutaj dodać dodatkową logikę gdy winda dotrze na piętro
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