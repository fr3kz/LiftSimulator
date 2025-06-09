package model;

import java.util.*;

public class Building {
    private static final int Floors = 11;

    private int[] waitingPassengers = new int[Floors];
    private Set<Integer> calls = new HashSet<>();

    public int getFloorsCount() {
        return Floors;
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
        for (int i = 1; i < Floors; i++) {

            waitingPassengers[i] = random.nextInt(6);
        }
    }

    public boolean hasWaitingPassengers(int floor) {
        return waitingPassengers[floor] > 0;
    }
}