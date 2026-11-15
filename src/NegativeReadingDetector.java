public class NegativeReadingDetector implements AnomalyDetector {

    @Override
    public boolean isAnomaly(EnergyReading reading) {
        return reading.getEnergyConsumed() < 0;
    }

    @Override
    public String getStrategyName() {
        return "Negative Reading Detector";
    }
}