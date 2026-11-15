import java.time.LocalDateTime;

public class EnergyReading {
    private String readingId;
    private String meterId;
    private double energyConsumed;
    private LocalDateTime timestamp;

    public EnergyReading(String readingId, String meterId,
                         double energyConsumed, LocalDateTime timestamp) {
        this.readingId = readingId;
        this.meterId = meterId;
        this.energyConsumed = energyConsumed;
        this.timestamp = timestamp;
    }

    public String getReadingId() {
        return readingId;
    }

    public String getMeterId() {
        return meterId;
    }

    public double getEnergyConsumed() {
        return energyConsumed;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void displayDetails() {
        System.out.println("Reading ID: " + readingId);
        System.out.println("Meter ID: " + meterId);
        System.out.println("Energy Consumed: " + energyConsumed + " kWh");
        System.out.println("Timestamp: " + timestamp);
    }
}