public class Building extends CampusEntity {

    public Building(String buildingName, String buildingId) {
        super(buildingId, buildingName);
    }

    public String getBuildingName() {
        return getName();
    }

    public String getBuildingId() {
        return getId();
    }

    @Override
    public void displayDetails() {
        System.out.println("Building ID: " + getId());
        System.out.println("Building Name: " + getName());
    }
}