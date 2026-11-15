public class MeterReadingTask implements Runnable {

    private EnergyReadingManager manager;
    private EnergyReading reading;

    public MeterReadingTask(EnergyReadingManager manager,
                            EnergyReading reading) {
        this.manager = manager;
        this.reading = reading;
    }

    @Override
    public void run() {
        try {
            manager.addReading(reading);

            System.out.println(
                "[" + Thread.currentThread().getName() + "] Successfully added reading: "
                + reading.getReadingId()
            );

        } catch (InvalidReadingException
                 | DuplicateReadingException e) {

            System.out.println(
                "[" + Thread.currentThread().getName() + "] Error: " + e.getMessage()
            );
        }
    }
}