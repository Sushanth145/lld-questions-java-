package questions;

import java.util.*;

interface VehicleType {
    void type();
}

class Car implements VehicleType {
    public void type() {
        System.out.println("Car");
    }
}

class MotorCycle implements VehicleType {
    public void type() {
        System.out.println("MotorCycle");
    }
}

class VehicleFactory {
    public static VehicleType getVehicle(String type) {
        if (type.equalsIgnoreCase("car")) return new Car();
        if (type.equalsIgnoreCase("motorcycle")) return new MotorCycle();
        return null;
    }
}

interface ParkingLotObserver {
    void update(int available);
}

class DisplayBoard implements ParkingLotObserver {
    public void update(int available) {
        System.out.println("Display Updated. Free spots: " + available);
    }
}

class ParkingSpot {
    VehicleType vehicle;
    int spotNumber;
    ParkingSpot(VehicleType v, int spotNumber) {
        this.vehicle = v;
        this.spotNumber = spotNumber;
    }
}

class Level {
    ArrayList<ParkingSpot> spots = new ArrayList<>();
    int capacity = 5;

    public int addSpot(VehicleType v) {
        ParkingSpot ps = new ParkingSpot(v, spots.size());
        spots.add(ps);
        capacity--;
        return ps.spotNumber;
    }

    public void removeSpot(int spotNumber) {
        spots.removeIf(s -> s.spotNumber == spotNumber);
        capacity++;
    }

    public boolean hasSpace() {
        return capacity > 0;
    }

    public int getAvailable() {
        return capacity;
    }
}

interface SpotAssignmentStrategy {
    Level assignLevel(ArrayList<Level> levels);
}

class FirstAvailableStrategy implements SpotAssignmentStrategy {
    public Level assignLevel(ArrayList<Level> levels) {
        for (Level l : levels)
            if (l.hasSpace()) return l;
        return null;
    }
}

class ParkingLot {

    private static ParkingLot instance;
    ArrayList<Level> levels = new ArrayList<>();
    SpotAssignmentStrategy strategy = new FirstAvailableStrategy();
    ArrayList<ParkingLotObserver> observers = new ArrayList<>();
    HashMap<Integer, ParkingSpot> tickets = new HashMap<>();

    int nextTicket = 1;
    int maxLevels = 3;

    private ParkingLot() {}

    public static ParkingLot getInstance() {
        if (instance == null) instance = new ParkingLot();
        return instance;
    }

    public void addObserver(ParkingLotObserver obs) {
        observers.add(obs);
    }

    private void notifyAllObservers() {
        int total = 0;
        for (Level l : levels) total += l.getAvailable();
        for (ParkingLotObserver o : observers) o.update(total);
    }

    private Level createLevelIfNeeded() {
        if (levels.isEmpty() || !levels.get(levels.size() - 1).hasSpace()) {
            if (levels.size() == maxLevels) return null;
            levels.add(new Level());
        }
        return levels.get(levels.size() - 1);
    }

    public int park(VehicleType v) {
        Level assigned = strategy.assignLevel(levels);
        if (assigned == null) assigned = createLevelIfNeeded();
        if (assigned == null) {
            System.out.println("Parking Full");
            return -1;
        }
        int spot = assigned.addSpot(v);
        ParkingSpot ps = new ParkingSpot(v, spot);
        tickets.put(nextTicket, ps);
        notifyAllObservers();
        return nextTicket++;
    }

    public void exit(int ticketId) {
        ParkingSpot ps = tickets.get(ticketId);
        if (ps == null) return;
        for (Level l : levels) l.removeSpot(ps.spotNumber);
        tickets.remove(ticketId);
        notifyAllObservers();
    }
}

interface ParkingCommand {
    void execute();
}

class ParkCommand implements ParkingCommand {
    ParkingLot lot;
    VehicleType v;
    ParkCommand(ParkingLot lot, VehicleType v) {
        this.lot = lot;
        this.v = v;
    }
    public void execute() {
        int ticket = lot.park(v);
        System.out.println("Parked with ticket: " + ticket);
    }
}

class ExitCommand implements ParkingCommand {
    ParkingLot lot;
    int ticket;
    ExitCommand(ParkingLot lot, int ticket) {
        this.lot = lot;
        this.ticket = ticket;
    }
    public void execute() {
        lot.exit(ticket);
        System.out.println("Exited ticket: " + ticket);
    }
}

public class ParkingLotQuestion {
    public static void main(String[] args) {

        ParkingLot lot = ParkingLot.getInstance();
        lot.addObserver(new DisplayBoard());

        VehicleType car = VehicleFactory.getVehicle("car");

        ParkingCommand park1 = new ParkCommand(lot, car);
        park1.execute();

        ParkingCommand park2 = new ParkCommand(lot, car);
        park2.execute();

        ParkingCommand exit1 = new ExitCommand(lot, 1);
        exit1.execute();
    }
}
