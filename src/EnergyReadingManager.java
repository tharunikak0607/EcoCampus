import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnergyReadingManager {

    private List<EnergyReading> readings = new ArrayList<>();
    private Set<String> readingIds = new HashSet<>();
    private Map<String, EnergyReading> readingMap = new HashMap<>();

    public synchronized void addReading(EnergyReading reading)
            throws InvalidReadingException, DuplicateReadingException {

        if (reading == null) {
            throw new InvalidReadingException(
                "Energy reading cannot be null!"
            );
        }

        if (!Double.isFinite(reading.getEnergyConsumed())) {
            throw new InvalidReadingException(
                "Energy reading must be a finite number!"
            );
        }

        if (reading.getEnergyConsumed() < 0) {
            throw new InvalidReadingException(
                "Energy reading cannot be negative!"
            );
        }

        if (readingIds.contains(reading.getReadingId())) {
            throw new DuplicateReadingException(
                "Duplicate reading ID: " + reading.getReadingId()
            );
        }

        readings.add(reading);
        readingIds.add(reading.getReadingId());
        readingMap.put(reading.getReadingId(), reading);
    }

    public synchronized List<EnergyReading> getReadings() {
        return new ArrayList<>(readings);
    }

    public synchronized EnergyReading getReadingById(String readingId) {
        return readingMap.get(readingId);
    }

    public synchronized Map<String, EnergyReading> getReadingMap() {
        return new HashMap<>(readingMap);
    }

    public synchronized void displayAllReadings() {
        if (readings.isEmpty()) {
            System.out.println("No energy readings available.");
            return;
        }

        for (EnergyReading reading : readings) {
            reading.displayDetails();
            System.out.println("--------------------");
        }
    }

    public synchronized double calculateTotalEnergy() {
        double total = 0;

        for (EnergyReading reading : readings) {
            total += reading.getEnergyConsumed();
        }

        return total;
    }

    public synchronized double getEnergyForDate(LocalDate date) {
        if (date == null) {
            return 0.0;
        }
        double total = 0.0;
        for (EnergyReading r : readings) {
            if (r.getTimestamp() != null && r.getTimestamp().toLocalDate().equals(date)) {
                total += r.getEnergyConsumed();
            }
        }
        return total;
    }

    public synchronized double getEnergyForDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0.0;
        }
        double total = 0.0;
        for (EnergyReading r : readings) {
            if (r.getTimestamp() != null) {
                LocalDate d = r.getTimestamp().toLocalDate();
                if (!d.isBefore(startDate) && !d.isAfter(endDate)) {
                    total += r.getEnergyConsumed();
                }
            }
        }
        return total;
    }

    public synchronized Map<String, List<EnergyReading>> getRepeatedHighUsageMeters(
            double threshold, int minOccurrences) {

        Map<String, List<EnergyReading>> violationsByMeter = new HashMap<>();

        for (EnergyReading r : readings) {
            if (r.getEnergyConsumed() > threshold) {
                violationsByMeter
                    .computeIfAbsent(r.getMeterId(), k -> new ArrayList<>())
                    .add(r);
            }
        }

        Map<String, List<EnergyReading>> repeatedAlerts = new HashMap<>();
        for (Map.Entry<String, List<EnergyReading>> entry : violationsByMeter.entrySet()) {
            if (entry.getValue().size() >= minOccurrences) {
                repeatedAlerts.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }

        return repeatedAlerts;
    }

    public synchronized Map<String, Double> getEnergyConsumedByMeter() {
        Map<String, Double> summary = new HashMap<>();
        for (EnergyReading r : readings) {
            summary.put(
                r.getMeterId(),
                summary.getOrDefault(r.getMeterId(), 0.0) + r.getEnergyConsumed()
            );
        }
        return summary;
    }

    public synchronized double calculateEstimatedBill(double ratePerUnit) {
        if (ratePerUnit < 0 || !Double.isFinite(ratePerUnit)) {
            return 0.0;
        }
        return calculateTotalEnergy() * ratePerUnit;
    }
}