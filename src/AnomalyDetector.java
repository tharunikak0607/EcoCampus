public interface AnomalyDetector {
    boolean isAnomaly(EnergyReading reading);
    String getStrategyName();
}