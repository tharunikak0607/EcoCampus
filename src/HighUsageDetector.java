public class HighUsageDetector implements AnomalyDetector {

    private double threshold;

    public HighUsageDetector(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean isAnomaly(EnergyReading reading) {
        return reading.getEnergyConsumed() > threshold;
    }

    @Override
    public String getStrategyName() {
        return "High Usage Detector";
    }
}