public class Meter {
    private String meterId;
    private Appliance appliance;

    public Meter(String meterId, Appliance appliance) {
        this.meterId = meterId;
        this.appliance = appliance;
    }

    public String getMeterId() {
        return meterId;
    }

    public Appliance getAppliance() {
        return appliance;
    }

    public void displayDetails() {
        System.out.println("Meter ID: " + meterId);
        System.out.println("Appliance: " + appliance.getApplianceName());
        System.out.println("Room: " + appliance.getRoom().getRoomName());
    }
}