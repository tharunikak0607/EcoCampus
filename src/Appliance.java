public class Appliance extends CampusEntity {

    private Room room;

    public Appliance(String applianceName, String applianceId, Room room) {
        super(applianceId, applianceName);
        this.room = room;
    }

    public String getApplianceName() {
        return getName();
    }

    public String getApplianceId() {
        return getId();
    }

    public Room getRoom() {
        return room;
    }

    @Override
    public void displayDetails() {
        System.out.println("Appliance ID: " + getId());
        System.out.println("Appliance Name: " + getName());
        System.out.println("Room: " + room.getRoomName());
    }
}
