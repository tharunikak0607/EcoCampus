import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        EnergyReadingManager manager = new EnergyReadingManager();

        int choice = -1;

        do {
            System.out.println("\n===== EcoCampus =====");
            System.out.println("1. Add Energy Reading");
            System.out.println("2. View All Readings");
            System.out.println("3. Calculate Total Energy");
            System.out.println("4. Detect Anomalies");
            System.out.println("5. Simulate Concurrent Meter Readings");
            System.out.println("6. Energy Consumption Comparison (Daily & Weekly)");
            System.out.println("7. Repeated High-Usage Meter Alerts");
            System.out.println("8. Calculate Estimated Electricity Bill (Rs. / ₹)");
            System.out.println("9. Launch Web Dashboard Server (http://localhost:8080)");
            System.out.println("0. Exit");

            choice = readInt(sc, "Enter your choice: ");

            switch (choice) {

                case 1:
                    String readingId = readNonEmptyString(sc, "Enter Reading ID: ");
                    double energyValue = readDouble(sc, "Enter Energy Value (kWh): ");
                    LocalDateTime timestamp = readTimestamp(
                        sc, "Enter Timestamp (yyyy-MM-ddTHH:mm) or press [Enter] for now: "
                    );
                    String meterId = readNonEmptyString(sc, "Enter Meter ID: ");

                    EnergyReading reading = new EnergyReading(
                        readingId, meterId, energyValue, timestamp
                    );

                    // Execute addition through a background worker thread
                    MeterReadingTask task = new MeterReadingTask(manager, reading);
                    Thread thread = new Thread(task);
                    thread.start();

                    // Wait for thread to finish before re-displaying menu to avoid console clashes
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Reading processing was interrupted.");
                    }
                    break;

                case 2:
                    System.out.println("\n--- All Energy Readings ---");
                    manager.displayAllReadings();
                    break;

                case 3:
                    System.out.println(
                        "\nTotal Energy: "
                        + manager.calculateTotalEnergy()
                        + " kWh"
                    );
                    break;

                case 4:
                    System.out.println("\n--- Anomaly Detection ---");
                    List<EnergyReading> readings = manager.getReadings();
                    if (readings.isEmpty()) {
                        System.out.println("No energy readings available to analyze.");
                        break;
                    }

                    System.out.print(
                        "Enter threshold for High Usage Detector in kWh (Press [Enter] for default 100.0): "
                    );
                    String threshInput = sc.nextLine().trim();
                    double threshold = 100.0;
                    if (!threshInput.isEmpty()) {
                        try {
                            double parsedThreshold = Double.parseDouble(threshInput);
                            if (Double.isFinite(parsedThreshold) && parsedThreshold >= 0) {
                                threshold = parsedThreshold;
                            } else {
                                System.out.println("Invalid threshold! Threshold must be a finite, non-negative number. Using default 100.0 kWh.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid threshold! Using default 100.0 kWh.");
                        }
                    }

                    // Polymorphic strategy list using the AnomalyDetector interface
                    List<AnomalyDetector> detectors = new ArrayList<>();
                    detectors.add(new NegativeReadingDetector());
                    detectors.add(new HighUsageDetector(threshold));

                    for (AnomalyDetector detector : detectors) {
                        System.out.println("\nRunning Strategy: " + detector.getStrategyName());
                        boolean foundAnomaly = false;

                        for (EnergyReading r : readings) {
                            if (detector.isAnomaly(r)) {
                                System.out.println(
                                    "  [!] Anomaly found in Reading: " + r.getReadingId()
                                    + " | Meter: " + r.getMeterId()
                                    + " | Consumed: " + r.getEnergyConsumed() + " kWh"
                                );
                                foundAnomaly = true;
                            }
                        }

                        if (!foundAnomaly) {
                            System.out.println("  No anomalies detected with this strategy.");
                        }
                    }

                    System.out.println("\nAnomaly check completed.");
                    break;

                case 5:
                    System.out.println("\n--- Simulating Concurrent Meter Readings ---");
                    EnergyReading r1 = new EnergyReading("R-SIM-01", "MTR-101", 45.5, LocalDateTime.now());
                    EnergyReading r2 = new EnergyReading("R-SIM-02", "MTR-102", 72.3, LocalDateTime.now());

                    MeterReadingTask task1 = new MeterReadingTask(manager, r1);
                    MeterReadingTask task2 = new MeterReadingTask(manager, r2);

                    Thread t1 = new Thread(task1, "MeterThread-1");
                    Thread t2 = new Thread(task2, "MeterThread-2");

                    t1.start();
                    t2.start();

                    try {
                        t1.join();
                        t2.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Concurrent reading simulation was interrupted.");
                    }

                    System.out.println("Concurrent execution finished.");
                    break;

                case 6:
                    System.out.println("\n--- Energy Consumption Comparison (Daily & Weekly) ---");
                    List<EnergyReading> compReadings = manager.getReadings();
                    if (compReadings.isEmpty()) {
                        System.out.println("No energy readings available to analyze.");
                        System.out.print("Would you like to load simulated historical test readings? (y/N): ");
                        String loadChoice = sc.nextLine().trim();
                        if (loadChoice.equalsIgnoreCase("y")) {
                            loadSimulatedHistoricalReadings(manager);
                            compReadings = manager.getReadings();
                        } else {
                            break;
                        }
                    } else {
                        LocalDate today = LocalDate.now();
                        boolean hasHistorical = false;
                        for (EnergyReading r : compReadings) {
                            if (r.getTimestamp() != null && !r.getTimestamp().toLocalDate().equals(today)) {
                                hasHistorical = true;
                                break;
                            }
                        }
                        if (!hasHistorical) {
                            System.out.println("Notice: All recorded readings belong to today (" + today + ").");
                            System.out.println("Previous-day and previous-week totals will be 0.00 kWh without historical data.");
                            System.out.print("Would you like to load simulated historical test readings? (y/N): ");
                            String loadChoice = sc.nextLine().trim();
                            if (loadChoice.equalsIgnoreCase("y")) {
                                loadSimulatedHistoricalReadings(manager);
                            }
                        }
                    }

                    LocalDate refDate = readOptionalDate(
                        sc, "Enter reference date for comparison (yyyy-MM-dd) or press [Enter] for today: ", LocalDate.now()
                    );

                    // Daily Comparison: Reference Day vs Previous Day
                    LocalDate prevDay = refDate.minusDays(1);
                    double currentDayKwh = manager.getEnergyForDate(refDate);
                    double prevDayKwh = manager.getEnergyForDate(prevDay);
                    double dayDiff = currentDayKwh - prevDayKwh;

                    System.out.println("\n------------------------------------------------------------");
                    System.out.println("                DAILY ENERGY COMPARISON");
                    System.out.println("------------------------------------------------------------");
                    System.out.printf("Reference Day (%s) : %8.2f kWh%n", refDate, currentDayKwh);
                    System.out.printf("Previous Day  (%s) : %8.2f kWh%n", prevDay, prevDayKwh);
                    System.out.printf("Net Difference          : %+8.2f kWh%n", dayDiff);
                    System.out.println("Percentage Change       : " + formatPercentageChange(currentDayKwh, prevDayKwh));

                    // Weekly Comparison: Current Week (Mon-Sun) vs Previous Week (Mon-Sun)
                    LocalDate currWeekStart = refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    LocalDate currWeekEnd = currWeekStart.plusDays(6);
                    LocalDate prevWeekStart = currWeekStart.minusWeeks(1);
                    LocalDate prevWeekEnd = currWeekStart.minusDays(1);

                    double currentWeekKwh = manager.getEnergyForDateRange(currWeekStart, currWeekEnd);
                    double prevWeekKwh = manager.getEnergyForDateRange(prevWeekStart, prevWeekEnd);
                    double weekDiff = currentWeekKwh - prevWeekKwh;

                    System.out.println("\n------------------------------------------------------------");
                    System.out.println("                WEEKLY ENERGY COMPARISON");
                    System.out.println("------------------------------------------------------------");
                    System.out.printf("Current Week  (%s to %s) : %8.2f kWh%n", currWeekStart, currWeekEnd, currentWeekKwh);
                    System.out.printf("Previous Week (%s to %s) : %8.2f kWh%n", prevWeekStart, prevWeekEnd, prevWeekKwh);
                    System.out.printf("Net Difference                       : %+8.2f kWh%n", weekDiff);
                    System.out.println("Percentage Change                    : " + formatPercentageChange(currentWeekKwh, prevWeekKwh));
                    System.out.println("------------------------------------------------------------");
                    break;

                case 7:
                    System.out.println("\n--- Repeated High-Usage Meter Alerts ---");
                    if (manager.getReadings().isEmpty()) {
                        System.out.println("No energy readings available to analyze.");
                        break;
                    }

                    double repeatThresh = readOptionalDouble(
                        sc, "Enter High-Usage threshold in kWh (Press [Enter] for default 100.0): ", 100.0
                    );
                    int minViolations = readOptionalInt(
                        sc, "Enter minimum violation count to trigger alert (Press [Enter] for default 2): ", 2
                    );

                    Map<String, List<EnergyReading>> repeatedAlerts =
                        manager.getRepeatedHighUsageMeters(repeatThresh, minViolations);

                    if (repeatedAlerts.isEmpty()) {
                        System.out.println(
                            "\n[OK] No meters exceeded " + repeatThresh
                            + " kWh at least " + minViolations + " time(s)."
                        );
                    } else {
                        System.out.println(
                            "\n[!] ATTENTION: Found " + repeatedAlerts.size()
                            + " meter(s) with repeated high-usage violations:"
                        );
                        for (Map.Entry<String, List<EnergyReading>> entry : repeatedAlerts.entrySet()) {
                            String meter = entry.getKey();
                            List<EnergyReading> violations = entry.getValue();
                            System.out.println("============================================================");
                            System.out.println(" [ALERT: REPEATED HIGH USAGE]");
                            System.out.println(" Meter ID        : " + meter);
                            System.out.println(" Violations Count: " + violations.size() + " breaches (Threshold: > " + repeatThresh + " kWh)");
                            System.out.println(" Violation Readings:");
                            for (EnergyReading vr : violations) {
                                System.out.printf("   - Reading: %-20s | Timestamp: %s | Consumed: %.2f kWh%n",
                                    vr.getReadingId(), vr.getTimestamp(), vr.getEnergyConsumed());
                            }
                            System.out.println("============================================================");
                        }
                    }
                    break;

                case 8:
                    System.out.println("\n--- Estimated Electricity Bill Calculation ---");
                    if (manager.getReadings().isEmpty()) {
                        System.out.println("No energy readings available to calculate electricity bill.");
                        break;
                    }

                    double rate = readOptionalDouble(
                        sc, "Enter electricity tariff in Rs. (INR) per kWh (Press [Enter] for default Rs. 8.50): ", 8.50
                    );

                    double totalKwh = manager.calculateTotalEnergy();
                    double estimatedBill = manager.calculateEstimatedBill(rate);
                    Map<String, Double> meterSummary = manager.getEnergyConsumedByMeter();

                    System.out.println("\n======================================================================");
                    System.out.println("               [ESTIMATE] CAMPUS ELECTRICITY BILL");
                    System.out.println("======================================================================");
                    System.out.println("DISCLAIMER:");
                    System.out.println("  This calculation is strictly an ESTIMATE based on total recorded kWh");
                    System.out.println("  and a flat unit rate. Actual utility electricity tariffs may differ");
                    System.out.println("  significantly due to slab-wise pricing, peak/off-peak Time-of-Day");
                    System.out.println("  (ToD) tariffs, fixed sanction load charges, power factor surcharges,");
                    System.out.println("  and applicable state electricity duties / taxes.");
                    System.out.println("----------------------------------------------------------------------");
                    System.out.printf("Total Energy Consumed : %10.2f kWh%n", totalKwh);
                    System.out.printf("Electricity Tariff    :      Rs. %6.2f per kWh%n", rate);
                    System.out.printf("ESTIMATED TOTAL BILL  :      Rs. %10.2f%n", estimatedBill);
                    System.out.println("----------------------------------------------------------------------");
                    System.out.println("Per-Meter Estimated Cost Breakdown:");
                    for (Map.Entry<String, Double> entry : meterSummary.entrySet()) {
                        double mKwh = entry.getValue();
                        double mCost = mKwh * rate;
                        System.out.printf("  - Meter %-10s : %8.2f kWh  =>  ESTIMATED Rs. %8.2f%n",
                            entry.getKey(), mKwh, mCost);
                    }
                    System.out.println("======================================================================");
                    break;

                case 9:
                    System.out.println("\n--- Launching EcoCampus Web Dashboard Server ---");
                    WebServer.ensureInitialized();
                    for (EnergyReading r : manager.getReadings()) {
                        try {
                            WebServer.getSharedManager().addReading(r);
                        } catch (Exception ignored) {}
                    }
                    WebServer.startServer(8080);
                    break;

                case 0:
                    System.out.println("Exiting EcoCampus. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice! Please choose a number between 0 and 9.");
            }

        } while (choice != 0);

        sc.close();
    }

    // Helper methods for robust, crash-proof user input and formatted analytics

    private static String formatPercentageChange(double current, double previous) {
        if (previous == 0.0) {
            if (current == 0.0) {
                return "0.00% (No energy consumed in either period)";
            } else {
                return "N/A (Baseline was 0.00 kWh; new consumption of "
                    + String.format("%.2f", current) + " kWh recorded)";
            }
        }
        double diff = current - previous;
        double pct = (diff / previous) * 100.0;
        if (pct > 0) {
            return String.format("[+] +%.2f%% increase", pct);
        } else if (pct < 0) {
            return String.format("[-] %.2f%% decrease", pct);
        } else {
            return "0.00% (No change in consumption)";
        }
    }

    private static void loadSimulatedHistoricalReadings(EnergyReadingManager manager) {
        System.out.println("\n--- Loading Simulated Historical Readings ---");
        LocalDate today = LocalDate.now();
        List<EnergyReading> simulated = new ArrayList<>();

        // Clearly marked with SIM-HIST- prefix
        simulated.add(new EnergyReading(
            "SIM-HIST-YEST-01", "MTR-101", 55.0, today.minusDays(1).atTime(10, 0)
        ));
        simulated.add(new EnergyReading(
            "SIM-HIST-YEST-02", "MTR-102", 120.0, today.minusDays(1).atTime(14, 30)
        ));
        simulated.add(new EnergyReading(
            "SIM-HIST-PREVWK-01", "MTR-101", 115.0, today.minusWeeks(1).atTime(9, 15)
        ));
        simulated.add(new EnergyReading(
            "SIM-HIST-PREVWK-02", "MTR-102", 130.0, today.minusWeeks(1).atTime(16, 45)
        ));

        int addedCount = 0;
        for (EnergyReading r : simulated) {
            try {
                manager.addReading(r);
                addedCount++;
            } catch (InvalidReadingException | DuplicateReadingException e) {
                // Ignore if duplicate
            }
        }

        System.out.println(
            "[SIMULATED DATA] Added " + addedCount
            + " demo historical readings (IDs prefixed with 'SIM-HIST-')."
        );
        System.out.println("These readings are strictly for historical comparison demonstration.");
    }

    private static double readOptionalDouble(Scanner sc, String prompt, double defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                double val = Double.parseDouble(input);
                if (!Double.isFinite(val) || val < 0) {
                    System.out.println("Invalid value! Must be a non-negative finite number.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Please enter a valid decimal number.");
            }
        }
    }

    private static int readOptionalInt(Scanner sc, String prompt, int defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                int val = Integer.parseInt(input);
                if (val <= 0) {
                    System.out.println("Invalid count! Must be a positive integer (>= 1).");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Please enter a valid integer.");
            }
        }
    }

    private static LocalDate readOptionalDate(Scanner sc, String prompt, LocalDate defaultDate) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                return defaultDate;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println(
                    "Invalid date format! Format must be 'yyyy-MM-dd' (e.g., 2026-10-24) or press [Enter] for today."
                );
            }
        }
    }

    private static String readNonEmptyString(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty. Please enter a value.");
        }
    }

    private static double readDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (!Double.isFinite(val)) {
                    System.out.println("Invalid number! Value must be a finite number (NaN and Infinity are not allowed).");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Please enter a valid decimal number (e.g., 25.5).");
            }
        }
    }

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Please enter a valid integer option.");
            }
        }
    }

    private static LocalDateTime readTimestamp(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
                System.out.println("Using current timestamp: " + now);
                return now;
            }
            try {
                return LocalDateTime.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println(
                    "Invalid date format! Format must be 'yyyy-MM-ddTHH:mm' (e.g., 2026-10-24T14:30) or press [Enter] for current time."
                );
            }
        }
    }
}