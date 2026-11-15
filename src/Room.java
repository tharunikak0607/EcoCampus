public class Room extends CampusEntity {

    private Building building;

    public Room(String roomName, String roomId, Building building) {
        super(roomId, roomName);
        this.building = building;
    }

    public String getRoomName() {
        return getName();
    }

    public String getRoomId() {
        return getId();
    }

    public Building getBuilding() {
        return building;
    }

    @Override
    public void displayDetails() {
        System.out.println("Room ID: " + getId());
        System.out.println("Room Name: " + getName());
        System.out.println("Building: " + building.getBuildingName());
    }
}