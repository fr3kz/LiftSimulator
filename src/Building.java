// ==================== Building.java ====================
import java.util.*;

public class Building {
    private static final int FLOORS = 11; // 0-10

    private int[] waitingPassengers = new int[FLOORS];
    private Set<Integer> calls = new HashSet<>();

    public int getFloorsCount() {
        return FLOORS;
    }

    public int[] getWaitingPassengers() {
        return waitingPassengers;
    }

    public int getWaitingPassengers(int floor) {
        return waitingPassengers[floor];
    }

    public void setWaitingPassengers(int floor, int count) {
        waitingPassengers[floor] = count;
    }

    public Set<Integer> getCalls() {
        return calls;
    }

    public void addCall(int floor) {
        calls.add(floor);
    }

    public void removeCall(int floor) {
        calls.remove(floor);
    }

    public void removePassengers(int floor, int count) {
        waitingPassengers[floor] = Math.max(0, waitingPassengers[floor] - count);
    }

    public void generateRandomPassengers() {
        Random random = new Random();
        for (int i = 0; i < FLOORS; i++) {
            waitingPassengers[i] = random.nextInt(6); // 0-5 pasażerów
        }
    }

    public boolean hasWaitingPassengers(int floor) {
        return waitingPassengers[floor] > 0;
    }
}