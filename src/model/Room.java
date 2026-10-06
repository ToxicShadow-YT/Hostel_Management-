package model;

/**
 * Model representing a Hostel Room.
 */
public class Room {
    private int roomId;
    private String roomNumber;
    private String block;
    private int floor;
    private String roomType;
    private int capacity;
    private int occupied;
    private String status; // 'Available', 'Full', 'Maintenance'

    public Room() {
    }

    public Room(int roomId, String roomNumber, String block, int floor, 
                String roomType, int capacity, int occupied, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.block = block;
        this.floor = floor;
        this.roomType = roomType;
        this.capacity = capacity;
        this.occupied = occupied;
        this.status = status;
    }

    public Room(String roomNumber, String block, int floor, 
                String roomType, int capacity, int occupied, String status) {
        this(0, roomNumber, block, floor, roomType, capacity, occupied, status);
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOccupied() {
        return occupied;
    }

    public void setOccupied(int occupied) {
        this.occupied = occupied;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAvailableCapacity() {
        return Math.max(0, capacity - occupied);
    }

    @Override
    public String toString() {
        return roomNumber + " (" + roomType + " | " + occupied + "/" + capacity + " occupied)";
    }
}
