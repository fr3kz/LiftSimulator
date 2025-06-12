package model;

import java.util.*;

public class Elevator {
    private int Max_Passengers = 5;
    private int currentFloor = 0;
    private boolean isMoving = false;
    private int direction = 0;
    private List<Integer> passengersInElevator = new ArrayList<>();
    private List<Integer> destinations = new ArrayList<>();

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int floor) {
        this.currentFloor = floor;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        this.isMoving = moving;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public List<Integer> getDestinations() {
        return destinations;
    }

    public void addDestination(int floor) {
        if (!destinations.contains(floor) && floor != currentFloor) {
            destinations.add(floor);
        }
    }

    public void removeDestination(int floor) {
        destinations.remove((Integer) floor);
    }

    public void addPassenger() {
        if (passengersInElevator.size() < Max_Passengers) {
            passengersInElevator.add(currentFloor);
        }
    }

    public void removePassenger() {
        if (!passengersInElevator.isEmpty()) {
            passengersInElevator.remove(0);
        }
    }

    public int getAvailableSpace() {
        return Max_Passengers - passengersInElevator.size();
    }

    public boolean isEmpty() {
        return passengersInElevator.isEmpty();
    }

    public void moveForOneFloor() {
        currentFloor += direction;
    }
}