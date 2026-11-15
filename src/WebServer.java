import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * EcoCampus Web Server
 * Integrates the existing Java backend with a web dashboard.
 * Built using standard Java HttpServer (jdk.httpserver) - zero external dependencies required.
 */
public class WebServer {

    private static final int DEFAULT_PORT = 8080;
    private static EnergyReadingManager manager = new EnergyReadingManager();

    // In-memory campus domain hierarchy showcasing OOP inheritance
    private static List<Building> campusBuildings = new ArrayList<>();
    private static List<Room> campusRooms = new ArrayList<>();
    private static List<Appliance> campusAppliances = new ArrayList<>();
    private static Map<String, Meter> campusMeters = new HashMap<>();
    private static volatile boolean isInitialized = false;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        ensureInitialized();
        startServer(port);
    }

    public static synchronized void ensureInitialized() {
        if (!isInitialized) {
            initializeCampusHierarchy();
            initializeSampleReadings();
            isInitialized = true;
        }
    }

    /**
     * Initializes the campus physical hierarchy showcasing OOP concepts:
     * CampusEntity (abstract class) -> Building, Room, Appliance -> Meter association.
     */
    private static void initializeCampusHierarchy() {
        Building b1 = new Building("Aryabhatta Academic Block", "BLD-01");
        Building b2 = new Building("Ramanujan Science & Research Centre", "BLD-02");
        Building b3 = new Building("Visvesvaraya Engineering Complex", "BLD-03");
        campusBuildings.add(b1);
        campusBuildings.add(b2);
        campusBuildings.add(b3);

        Room r1 = new Room("Advanced Computing Laboratory", "RM-101", b1);
        Room r2 = new Room("Main Data & Cloud Server Room", "RM-201", b2);
        Room r3 = new Room("Smart IoT & Robotics Workshop", "RM-301", b3);
        campusRooms.add(r1);
        campusRooms.add(r2);
        campusRooms.add(r3);

        Appliance a1 = new Appliance("Server Racks & High-Power Cooling", "APP-SRV", r2);
        Appliance a2 = new Appliance("Central Air Conditioning (HVAC)", "APP-HVAC", r1);
        Appliance a3 = new Appliance("Power Electronics & Lab Bench Units", "APP-LAB", r3);
        campusAppliances.add(a1);
        campusAppliances.add(a2);
        campusAppliances.add(a3);

        Meter m1 = new Meter("MTR-101", a1);
        Meter m2 = new Meter("MTR-102", a2);
        Meter m3 = new Meter("MTR-ENG-01", a3);
        campusMeters.put("MTR-101", m1);
        campusMeters.put("MTR-102", m2);
        campusMeters.put("MTR-ENG-01", m3);
    }

    /**
     * Populates initial test readings so the web dashboard is rich and interactive upon launch.
     */
    private static void initializeSampleReadings() {
        LocalDate today = LocalDate.now();

        // Baseline readings for today
        addSafeReading(new EnergyReading("R-TODAY-01", "MTR-101", 62.5, today.atTime(8, 30)));
        addSafeReading(new EnergyReading("R-TODAY-02", "MTR-102", 84.0, today.atTime(11, 45)));
        addSafeReading(new EnergyReading("R-TODAY-03", "MTR-ENG-01", 45.2, today.atTime(14, 15)));

        // Historical sample readings (marked with SIM-HIST-)
        addSafeReading(new EnergyReading("SIM-HIST-YEST-01", "MTR-101", 55.0, today.minusDays(1).atTime(10, 0)));
        addSafeReading(new EnergyReading("SIM-HIST-YEST-02", "MTR-102", 120.0, today.minusDays(1).atTime(14, 30)));
        addSafeReading(new EnergyReading("SIM-HIST-PREVWK-01", "MTR-101", 115.0, today.minusWeeks(1).atTime(9, 15)));
        addSafeReading(new EnergyReading("SIM-HIST-PREVWK-02", "MTR-102", 130.0, today.minusWeeks(1).atTime(16, 45)));
    }

    private static void addSafeReading(EnergyReading reading) {
        try {
            manager.addReading(reading);
        } catch (InvalidReadingException | DuplicateReadingException ignored) {}
    }

    public static EnergyReadingManager getSharedManager() {
        return manager;
    }

    public static void startServer(int port) {
        ensureInitialized();
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newFixedThreadPool(10)); // Thread-safe concurrent execution pool

            // API Endpoints
            server.createContext("/api/overview", new OverviewHandler());
            server.createContext("/api/readings", new ReadingsHandler());
            server.createContext("/api/comparison", new ComparisonHandler());
            server.createContext("/api/meters", new MetersHandler());
            server.createContext("/api/buildings", new BuildingsHandler());
            server.createContext("/api/anomalies", new AnomaliesHandler());
            server.createContext("/api/repeated-alerts", new RepeatedAlertsHandler());
            server.createContext("/api/bill", new BillHandler());
            server.createContext("/api/hierarchy", new HierarchyHandler());
            server.createContext("/api/simulate-historical", new SimulateHistoricalHandler());
            server.createContext("/api/simulate-concurrent", new SimulateConcurrentHandler());
            server.createContext("/api/simulate-auto", new SimulateAutoHandler());

            // Static Frontend Files
            server.createContext("/", new StaticFileHandler());

            server.start();

            System.out.println("==========================================================");
            System.out.println("  EcoCampus Web Application Server is RUNNING!");
            System.out.println("  URL: http://localhost:" + port + "/");
            System.out.println("  Local API: http://localhost:" + port + "/api/overview");
            System.out.println("  Press Ctrl+C to terminate the server.");
            System.out.println("==========================================================");

        } catch (IOException e) {
            System.err.println("Failed to start server on port " + port + ": " + e.getMessage());
            if (port == DEFAULT_PORT) {
                System.out.println("Retrying on alternative port 8081...");
                startServer(8081);
            }
        }
    }

    // =========================================================================
    // HTTP Handlers
    // =========================================================================

    /**
     * Serves static frontend files from the web/ directory.
     */
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Guard against directory traversal
            path = path.replace("..", "");

            File file = resolveFile(path);
            if (file == null || !file.exists() || file.isDirectory()) {
                // If static file with extension, don't return index.html - return 404
                if (hasFileExtension(path)) {
                    String notFound = "<h1>404 Not Found</h1><p>Resource not found: " + escapeJson(path) + "</p>";
                    sendResponse(exchange, 404, "text/html", notFound.getBytes(StandardCharsets.UTF_8));
                    return;
                }
                file = resolveFile("/index.html");
            }

            if (file == null || !file.exists()) {
                String notFound = "<h1>404 Not Found</h1><p>Frontend file not found in web/ directory.</p>";
                sendResponse(exchange, 404, "text/html", notFound.getBytes(StandardCharsets.UTF_8));
                return;
            }

            String mimeType = getMimeType(file.getName());
            byte[] fileBytes;
            try (InputStream in = new FileInputStream(file)) {
                fileBytes = in.readAllBytes();
            }

            sendResponse(exchange, 200, mimeType, fileBytes);
        }

        private static boolean hasFileExtension(String path) {
            int lastSlash = path.lastIndexOf('/');
            int lastDot = path.lastIndexOf('.');
            return lastDot > lastSlash && lastDot < path.length() - 1;
        }

        private static File resolveFile(String relativePath) {
            String clean = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            String[] candidateRoots = new String[] {
                "web",
                "../web",
                System.getProperty("user.dir") + "/web",
                System.getProperty("user.dir") + "/EcoCampus/web"
            };
            for (String root : candidateRoots) {
                File candidate = new File(root, clean);
                if (candidate.exists() && !candidate.isDirectory()) {
                    return candidate;
                }
            }
            return null;
        }
    }

    /**
     * Returns high-level overview metrics for the dashboard cards.
     */
    private static class OverviewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            double totalEnergy = manager.calculateTotalEnergy();
            List<EnergyReading> readings = manager.getReadings();
            int totalReadings = readings.size();
            Map<String, Double> meterMap = manager.getEnergyConsumedByMeter();
            int activeMeters = meterMap.size();
            double defaultRate = 8.50;
            double estimatedBill = manager.calculateEstimatedBill(defaultRate);

            // Compute quick anomaly and alert counts
            List<AnomalyDetector> detectors = new ArrayList<>();
            detectors.add(new NegativeReadingDetector());
            detectors.add(new HighUsageDetector(100.0));
            int anomalyCount = 0;
            for (EnergyReading r : readings) {
                for (AnomalyDetector d : detectors) {
                    if (d.isAnomaly(r)) {
                        anomalyCount++;
                        break;
                    }
                }
            }

            Map<String, List<EnergyReading>> repeated = manager.getRepeatedHighUsageMeters(100.0, 2);
            int repeatedAlertCount = repeated.size();

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"totalEnergy\":").append(formatDouble(totalEnergy)).append(",");
            sb.append("\"totalReadings\":").append(totalReadings).append(",");
            sb.append("\"activeMeters\":").append(activeMeters).append(",");
            sb.append("\"defaultTariff\":").append(defaultRate).append(",");
            sb.append("\"estimatedBill\":").append(formatDouble(estimatedBill)).append(",");
            sb.append("\"anomalyCount\":").append(anomalyCount).append(",");
            sb.append("\"repeatedAlertCount\":").append(repeatedAlertCount).append(",");
            sb.append("\"status\":\"ONLINE\"");
            sb.append("}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Handles listing readings (GET) and adding a new reading via thread (POST).
     */
    private static class ReadingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                List<EnergyReading> readings = manager.getReadings();
                // Return reversed for chronological (newest first)
                List<EnergyReading> reversed = new ArrayList<>(readings);
                Collections.reverse(reversed);

                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (int i = 0; i < reversed.size(); i++) {
                    EnergyReading r = reversed.get(i);
                    sb.append(readingToJson(r));
                    if (i < reversed.size() - 1) sb.append(",");
                }
                sb.append("]");

                sendJsonResponse(exchange, 200, sb.toString());
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                String readingId = extractJsonString(body, "readingId");
                String meterId = extractJsonString(body, "meterId");
                Double energyValue = extractJsonDouble(body, "energyConsumed");
                String timestampStr = extractJsonString(body, "timestamp");

                if (readingId == null || readingId.trim().isEmpty()) {
                    sendJsonError(exchange, 400, "Reading ID cannot be empty.");
                    return;
                }
                if (meterId == null || meterId.trim().isEmpty()) {
                    sendJsonError(exchange, 400, "Meter ID cannot be empty.");
                    return;
                }
                if (energyValue == null) {
                    sendJsonError(exchange, 400, "Energy value (kWh) must be a valid number.");
                    return;
                }

                LocalDateTime ts;
                if (timestampStr == null || timestampStr.trim().isEmpty()) {
                    ts = LocalDateTime.now().withSecond(0).withNano(0);
                } else {
                    try {
                        ts = LocalDateTime.parse(timestampStr);
                    } catch (DateTimeParseException e) {
                        try {
                            ts = LocalDate.parse(timestampStr).atTime(12, 0);
                        } catch (DateTimeParseException e2) {
                            sendJsonError(exchange, 400, "Invalid timestamp format. Use yyyy-MM-ddTHH:mm (e.g. 2026-10-24T14:30)");
                            return;
                        }
                    }
                }

                EnergyReading newReading = new EnergyReading(readingId.trim(), meterId.trim(), energyValue, ts);

                // Add reading via background worker thread using existing MeterReadingTask
                final boolean[] taskSuccess = {false};
                final String[] taskError = {null};

                Thread worker = new Thread(() -> {
                    try {
                        manager.addReading(newReading);
                        taskSuccess[0] = true;
                    } catch (InvalidReadingException | DuplicateReadingException e) {
                        taskError[0] = e.getMessage();
                    }
                }, "WebWorkerThread-" + System.currentTimeMillis());

                worker.start();
                try {
                    worker.join(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    sendJsonError(exchange, 500, "Worker thread execution was interrupted.");
                    return;
                }

                if (!taskSuccess[0]) {
                    sendJsonError(exchange, 400, taskError[0] != null ? taskError[0] : "Failed to record reading.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("{\"success\":true,\"message\":\"Reading recorded successfully via background thread!\",");
                sb.append("\"reading\":").append(readingToJson(newReading)).append("}");

                sendJsonResponse(exchange, 201, sb.toString());
                return;
            }

            sendJsonError(exchange, 405, "Method not allowed");
        }
    }

    /**
     * Handles Daily and Weekly comparison analytics.
     */
    private static class ComparisonHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            String dateParam = query.get("date");
            LocalDate refDate = LocalDate.now();
            if (dateParam != null && !dateParam.trim().isEmpty()) {
                try {
                    refDate = LocalDate.parse(dateParam.trim());
                } catch (DateTimeParseException ignored) {}
            }

            // Daily comparison: refDate vs prevDay
            LocalDate prevDay = refDate.minusDays(1);
            double currentDayKwh = manager.getEnergyForDate(refDate);
            double prevDayKwh = manager.getEnergyForDate(prevDay);
            double dayDiff = currentDayKwh - prevDayKwh;
            String dayPctChange = computePercentageChange(currentDayKwh, prevDayKwh);

            // Weekly comparison: current week (Mon-Sun) vs previous week (Mon-Sun)
            LocalDate currWeekStart = refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate currWeekEnd = currWeekStart.plusDays(6);
            LocalDate prevWeekStart = currWeekStart.minusWeeks(1);
            LocalDate prevWeekEnd = currWeekStart.minusDays(1);

            double currentWeekKwh = manager.getEnergyForDateRange(currWeekStart, currWeekEnd);
            double prevWeekKwh = manager.getEnergyForDateRange(prevWeekStart, prevWeekEnd);
            double weekDiff = currentWeekKwh - prevWeekKwh;
            String weekPctChange = computePercentageChange(currentWeekKwh, prevWeekKwh);

            // 7-day trend breakdown for chart rendering
            StringBuilder trendSb = new StringBuilder();
            trendSb.append("[");
            for (int i = 6; i >= 0; i--) {
                LocalDate d = refDate.minusDays(i);
                double kwh = manager.getEnergyForDate(d);
                trendSb.append("{");
                trendSb.append("\"date\":\"").append(d.toString()).append("\",");
                trendSb.append("\"day\":\"").append(d.getDayOfWeek().toString().substring(0, 3)).append("\",");
                trendSb.append("\"kwh\":").append(formatDouble(kwh));
                trendSb.append("}");
                if (i > 0) trendSb.append(",");
            }
            trendSb.append("]");

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"referenceDate\":\"").append(refDate).append("\",");
            sb.append("\"currentDayKwh\":").append(formatDouble(currentDayKwh)).append(",");
            sb.append("\"previousDay\":\"").append(prevDay).append("\",");
            sb.append("\"previousDayKwh\":").append(formatDouble(prevDayKwh)).append(",");
            sb.append("\"dayDiff\":").append(formatDouble(dayDiff)).append(",");
            sb.append("\"dayPctChange\":\"").append(escapeJson(dayPctChange)).append("\",");

            sb.append("\"currWeekStart\":\"").append(currWeekStart).append("\",");
            sb.append("\"currWeekEnd\":\"").append(currWeekEnd).append("\",");
            sb.append("\"currentWeekKwh\":").append(formatDouble(currentWeekKwh)).append(",");
            sb.append("\"prevWeekStart\":\"").append(prevWeekStart).append("\",");
            sb.append("\"prevWeekEnd\":\"").append(prevWeekEnd).append("\",");
            sb.append("\"previousWeekKwh\":").append(formatDouble(prevWeekKwh)).append(",");
            sb.append("\"weekDiff\":").append(formatDouble(weekDiff)).append(",");
            sb.append("\"weekPctChange\":\"").append(escapeJson(weekPctChange)).append("\",");

            sb.append("\"trend\":").append(trendSb.toString());
            sb.append("}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Handles meter-wise aggregated consumption and hierarchy linkage.
     */
    private static class MetersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, Double> consumptionByMeter = manager.getEnergyConsumedByMeter();
            List<EnergyReading> readings = manager.getReadings();

            // Count readings per meter
            Map<String, Integer> countByMeter = new HashMap<>();
            for (EnergyReading r : readings) {
                countByMeter.put(r.getMeterId(), countByMeter.getOrDefault(r.getMeterId(), 0) + 1);
            }

            double totalCampusEnergy = manager.calculateTotalEnergy();
            double defaultRate = 8.50;

            StringBuilder sb = new StringBuilder();
            sb.append("[");
            int idx = 0;
            for (Map.Entry<String, Double> entry : consumptionByMeter.entrySet()) {
                String meterId = entry.getKey();
                double kwh = entry.getValue();
                int count = countByMeter.getOrDefault(meterId, 0);
                double cost = kwh * defaultRate;
                double sharePct = totalCampusEnergy > 0 ? (kwh / totalCampusEnergy) * 100.0 : 0.0;

                // Lookup meter hierarchy
                Meter meterObj = campusMeters.get(meterId);
                String applianceName = meterObj != null ? meterObj.getAppliance().getApplianceName() : "General Facility Load";
                String roomName = meterObj != null ? meterObj.getAppliance().getRoom().getRoomName() : "Main Distribution Block";
                String buildingName = meterObj != null ? meterObj.getAppliance().getRoom().getBuilding().getBuildingName() : "Campus Grid";

                sb.append("{");
                sb.append("\"meterId\":\"").append(escapeJson(meterId)).append("\",");
                sb.append("\"totalKwh\":").append(formatDouble(kwh)).append(",");
                sb.append("\"readingCount\":").append(count).append(",");
                sb.append("\"estimatedCost\":").append(formatDouble(cost)).append(",");
                sb.append("\"sharePct\":").append(formatDouble(sharePct)).append(",");
                sb.append("\"appliance\":\"").append(escapeJson(applianceName)).append("\",");
                sb.append("\"room\":\"").append(escapeJson(roomName)).append("\",");
                sb.append("\"building\":\"").append(escapeJson(buildingName)).append("\"");
                sb.append("}");

                if (idx < consumptionByMeter.size() - 1) sb.append(",");
                idx++;
            }
            sb.append("]");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Handles building-wise aggregated consumption and hierarchy linkage.
     */
    private static class BuildingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            double rate = 8.50;
            if (query.containsKey("rate")) {
                try { rate = Double.parseDouble(query.get("rate")); } catch (NumberFormatException ignored) {}
            }

            Map<String, Double> consumptionByMeter = manager.getEnergyConsumedByMeter();
            List<EnergyReading> readings = manager.getReadings();
            double totalCampusEnergy = manager.calculateTotalEnergy();

            // Maps for building stats
            Map<String, Double> buildingKwh = new HashMap<>();
            Map<String, Integer> buildingReadings = new HashMap<>();
            Map<String, List<String>> buildingMeters = new HashMap<>();
            Map<String, List<String>> buildingRooms = new HashMap<>();
            Map<String, List<String>> buildingAppliances = new HashMap<>();

            for (Building b : campusBuildings) {
                buildingKwh.put(b.getId(), 0.0);
                buildingReadings.put(b.getId(), 0);
                buildingMeters.put(b.getId(), new ArrayList<>());
                buildingRooms.put(b.getId(), new ArrayList<>());
                buildingAppliances.put(b.getId(), new ArrayList<>());
            }

            for (Room r : campusRooms) {
                String bId = r.getBuilding().getId();
                if (buildingRooms.containsKey(bId)) {
                    buildingRooms.get(bId).add(r.getName() + " (" + r.getId() + ")");
                }
            }

            for (Appliance a : campusAppliances) {
                String bId = a.getRoom().getBuilding().getId();
                if (buildingAppliances.containsKey(bId)) {
                    buildingAppliances.get(bId).add(a.getName() + " (" + a.getId() + ")");
                }
            }

            for (Map.Entry<String, Meter> entry : campusMeters.entrySet()) {
                String meterId = entry.getKey();
                Meter m = entry.getValue();
                String bId = m.getAppliance().getRoom().getBuilding().getId();
                if (buildingMeters.containsKey(bId)) {
                    buildingMeters.get(bId).add(meterId);
                }
                double meterKwh = consumptionByMeter.getOrDefault(meterId, 0.0);
                buildingKwh.put(bId, buildingKwh.getOrDefault(bId, 0.0) + meterKwh);
            }

            for (EnergyReading r : readings) {
                Meter m = campusMeters.get(r.getMeterId());
                if (m != null) {
                    String bId = m.getAppliance().getRoom().getBuilding().getId();
                    buildingReadings.put(bId, buildingReadings.getOrDefault(bId, 0) + 1);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < campusBuildings.size(); i++) {
                Building b = campusBuildings.get(i);
                String bId = b.getId();
                double kwh = buildingKwh.getOrDefault(bId, 0.0);
                double cost = kwh * rate;
                double sharePct = totalCampusEnergy > 0 ? (kwh / totalCampusEnergy) * 100.0 : 0.0;
                int rCount = buildingReadings.getOrDefault(bId, 0);

                sb.append("{");
                sb.append("\"buildingId\":\"").append(escapeJson(bId)).append("\",");
                sb.append("\"buildingName\":\"").append(escapeJson(b.getName())).append("\",");
                sb.append("\"totalKwh\":").append(formatDouble(kwh)).append(",");
                sb.append("\"estimatedCost\":").append(formatDouble(cost)).append(",");
                sb.append("\"sharePct\":").append(formatDouble(sharePct)).append(",");
                sb.append("\"readingCount\":").append(rCount).append(",");

                sb.append("\"rooms\":[");
                List<String> rooms = buildingRooms.get(bId);
                for (int j = 0; j < rooms.size(); j++) {
                    sb.append("\"").append(escapeJson(rooms.get(j))).append("\"");
                    if (j < rooms.size() - 1) sb.append(",");
                }
                sb.append("],");

                sb.append("\"appliances\":[");
                List<String> appliances = buildingAppliances.get(bId);
                for (int j = 0; j < appliances.size(); j++) {
                    sb.append("\"").append(escapeJson(appliances.get(j))).append("\"");
                    if (j < appliances.size() - 1) sb.append(",");
                }
                sb.append("],");

                sb.append("\"meters\":[");
                List<String> meters = buildingMeters.get(bId);
                for (int j = 0; j < meters.size(); j++) {
                    sb.append("\"").append(escapeJson(meters.get(j))).append("\"");
                    if (j < meters.size() - 1) sb.append(",");
                }
                sb.append("]");

                sb.append("}");
                if (i < campusBuildings.size() - 1) sb.append(",");
            }
            sb.append("]");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Evaluates anomalies polymorphically using the AnomalyDetector interface.
     */
    private static class AnomaliesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            double threshold = 100.0;
            if (query.containsKey("threshold")) {
                try {
                    threshold = Double.parseDouble(query.get("threshold"));
                } catch (NumberFormatException ignored) {}
            }

            List<EnergyReading> readings = manager.getReadings();

            // Pluggable anomaly detector strategies
            List<AnomalyDetector> detectors = new ArrayList<>();
            detectors.add(new NegativeReadingDetector());
            detectors.add(new HighUsageDetector(threshold));

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"threshold\":").append(formatDouble(threshold)).append(",");
            sb.append("\"anomalies\":[");

            int anomalyCount = 0;
            for (AnomalyDetector detector : detectors) {
                for (EnergyReading r : readings) {
                    if (detector.isAnomaly(r)) {
                        if (anomalyCount > 0) sb.append(",");
                        sb.append("{");
                        sb.append("\"readingId\":\"").append(escapeJson(r.getReadingId())).append("\",");
                        sb.append("\"meterId\":\"").append(escapeJson(r.getMeterId())).append("\",");
                        sb.append("\"energyConsumed\":").append(formatDouble(r.getEnergyConsumed())).append(",");
                        sb.append("\"timestamp\":\"").append(r.getTimestamp()).append("\",");
                        sb.append("\"strategy\":\"").append(escapeJson(detector.getStrategyName())).append("\"");
                        sb.append("}");
                        anomalyCount++;
                    }
                }
            }
            sb.append("],");
            sb.append("\"totalCount\":").append(anomalyCount);
            sb.append("}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Evaluates repeated high-usage alerts grouped by meter ID.
     */
    private static class RepeatedAlertsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            double threshold = 100.0;
            int minViolations = 2;

            if (query.containsKey("threshold")) {
                try { threshold = Double.parseDouble(query.get("threshold")); } catch (NumberFormatException ignored) {}
            }
            if (query.containsKey("minViolations")) {
                try { minViolations = Integer.parseInt(query.get("minViolations")); } catch (NumberFormatException ignored) {}
            }

            Map<String, List<EnergyReading>> repeatedAlerts = manager.getRepeatedHighUsageMeters(threshold, minViolations);

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"threshold\":").append(formatDouble(threshold)).append(",");
            sb.append("\"minViolations\":").append(minViolations).append(",");
            sb.append("\"alerts\":[");

            int alertIdx = 0;
            for (Map.Entry<String, List<EnergyReading>> entry : repeatedAlerts.entrySet()) {
                String meter = entry.getKey();
                List<EnergyReading> violations = entry.getValue();

                if (alertIdx > 0) sb.append(",");
                sb.append("{");
                sb.append("\"meterId\":\"").append(escapeJson(meter)).append("\",");
                sb.append("\"violationCount\":").append(violations.size()).append(",");
                sb.append("\"violations\":[");
                for (int v = 0; v < violations.size(); v++) {
                    EnergyReading vr = violations.get(v);
                    sb.append(readingToJson(vr));
                    if (v < violations.size() - 1) sb.append(",");
                }
                sb.append("]}");
                alertIdx++;
            }

            sb.append("],");
            sb.append("\"offendingMetersCount\":").append(repeatedAlerts.size());
            sb.append("}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Calculates electricity bill estimate in Indian Rupees (₹) with per-meter breakdown.
     */
    private static class BillHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            double rate = 8.50;
            if (query.containsKey("rate")) {
                try {
                    rate = Double.parseDouble(query.get("rate"));
                } catch (NumberFormatException ignored) {}
            }

            double totalKwh = manager.calculateTotalEnergy();
            double estimatedBill = manager.calculateEstimatedBill(rate);
            Map<String, Double> meterSummary = manager.getEnergyConsumedByMeter();

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"ratePerUnit\":").append(formatDouble(rate)).append(",");
            sb.append("\"currency\":\"INR (₹)\",");
            sb.append("\"totalEnergyKwh\":").append(formatDouble(totalKwh)).append(",");
            sb.append("\"totalEstimatedBill\":").append(formatDouble(estimatedBill)).append(",");
            sb.append("\"disclaimer\":\"This calculation is strictly an ESTIMATE based on total recorded kWh and a flat unit rate. Actual utility electricity tariffs may differ due to slab-wise pricing, Time-of-Day (ToD) tariffs, fixed sanction load charges, power factor surcharges, and applicable state taxes.\",");
            sb.append("\"perMeterBreakdown\":[");

            int idx = 0;
            for (Map.Entry<String, Double> entry : meterSummary.entrySet()) {
                double mKwh = entry.getValue();
                double mCost = mKwh * rate;
                if (idx > 0) sb.append(",");
                sb.append("{");
                sb.append("\"meterId\":\"").append(escapeJson(entry.getKey())).append("\",");
                sb.append("\"kwh\":").append(formatDouble(mKwh)).append(",");
                sb.append("\"estimatedCost\":").append(formatDouble(mCost));
                sb.append("}");
                idx++;
            }
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Returns full domain hierarchy (Buildings, Rooms, Appliances, Meters).
     */
    private static class HierarchyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"buildings\":[");
            for (int i = 0; i < campusBuildings.size(); i++) {
                Building b = campusBuildings.get(i);
                sb.append("{\"id\":\"").append(escapeJson(b.getId())).append("\",\"name\":\"").append(escapeJson(b.getName())).append("\"}");
                if (i < campusBuildings.size() - 1) sb.append(",");
            }
            sb.append("],\"rooms\":[");
            for (int i = 0; i < campusRooms.size(); i++) {
                Room r = campusRooms.get(i);
                sb.append("{\"id\":\"").append(escapeJson(r.getId())).append("\",\"name\":\"").append(escapeJson(r.getName())).append("\",\"building\":\"").append(escapeJson(r.getBuilding().getName())).append("\"}");
                if (i < campusRooms.size() - 1) sb.append(",");
            }
            sb.append("],\"appliances\":[");
            for (int i = 0; i < campusAppliances.size(); i++) {
                Appliance a = campusAppliances.get(i);
                sb.append("{\"id\":\"").append(escapeJson(a.getId())).append("\",\"name\":\"").append(escapeJson(a.getName())).append("\",\"room\":\"").append(escapeJson(a.getRoom().getName())).append("\"}");
                if (i < campusAppliances.size() - 1) sb.append(",");
            }
            sb.append("],\"meters\":[");
            int mIdx = 0;
            for (Meter m : campusMeters.values()) {
                sb.append("{\"id\":\"").append(escapeJson(m.getMeterId())).append("\",\"appliance\":\"").append(escapeJson(m.getAppliance().getName())).append("\",\"room\":\"").append(escapeJson(m.getAppliance().getRoom().getName())).append("\"}");
                if (mIdx < campusMeters.size() - 1) sb.append(",");
                mIdx++;
            }
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Injects simulated historical readings on demand.
     */
    private static class SimulateHistoricalHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            LocalDate today = LocalDate.now();
            List<EnergyReading> simulated = new ArrayList<>();
            simulated.add(new EnergyReading("SIM-HIST-YEST-01", "MTR-101", 55.0, today.minusDays(1).atTime(10, 0)));
            simulated.add(new EnergyReading("SIM-HIST-YEST-02", "MTR-102", 120.0, today.minusDays(1).atTime(14, 30)));
            simulated.add(new EnergyReading("SIM-HIST-PREVWK-01", "MTR-101", 115.0, today.minusWeeks(1).atTime(9, 15)));
            simulated.add(new EnergyReading("SIM-HIST-PREVWK-02", "MTR-102", 130.0, today.minusWeeks(1).atTime(16, 45)));

            int added = 0;
            for (EnergyReading r : simulated) {
                try {
                    manager.addReading(r);
                    added++;
                } catch (InvalidReadingException | DuplicateReadingException ignored) {}
            }

            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Loaded simulated historical test readings.\",\"addedCount\":" + added + "}");
        }
    }

    /**
     * Simulates concurrent multi-threaded ingestion using MeterReadingTask and worker threads.
     */
    private static class SimulateConcurrentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            String randId1 = "R-SIM-" + System.currentTimeMillis() % 10000;
            String randId2 = "R-SIM-" + (System.currentTimeMillis() % 10000 + 1);

            EnergyReading r1 = new EnergyReading(randId1, "MTR-101", 45.5, LocalDateTime.now());
            EnergyReading r2 = new EnergyReading(randId2, "MTR-102", 72.3, LocalDateTime.now());

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
                sendJsonError(exchange, 500, "Concurrent simulation was interrupted.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true,\"message\":\"Concurrent meter readings successfully recorded by MeterThread-1 and MeterThread-2!\",");
            sb.append("\"readings\":[");
            sb.append(readingToJson(r1)).append(",");
            sb.append(readingToJson(r2));
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Dedicated endpoint for generating automatic simulated energy readings.
     * Reuses existing EnergyReadingManager, EnergyReading, and thread-safe collections.
     * Supports POST and GET. Accepts optional custom payload or generates realistic positive readings,
     * with occasional high-load spikes to exercise polymorphic anomaly detection.
     */
    private static class SimulateAutoHandler implements HttpHandler {
        private static final String[] MONITORED_METERS = {"MTR-101", "MTR-102", "MTR-ENG-01"};
        private static final java.util.Random RANDOM = new java.util.Random();
        private static final java.util.concurrent.atomic.AtomicLong SEQUENCE =
                new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis() % 100000);

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) return;

            if (!"POST".equalsIgnoreCase(method) && !"GET".equalsIgnoreCase(method)) {
                sendJsonError(exchange, 405, "Method not allowed. Use POST or GET.");
                return;
            }

            String meterId = null;
            Double energyConsumed = null;
            String readingId = null;
            String timestampStr = null;

            if ("POST".equalsIgnoreCase(method)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                if (body != null && !body.trim().isEmpty()) {
                    meterId = extractJsonString(body, "meterId");
                    energyConsumed = extractJsonDouble(body, "energyConsumed");
                    readingId = extractJsonString(body, "readingId");
                    timestampStr = extractJsonString(body, "timestamp");
                }
            }

            // Meter ID selection: randomly select from existing monitored meters if not provided
            if (meterId == null || meterId.trim().isEmpty()) {
                meterId = MONITORED_METERS[RANDOM.nextInt(MONITORED_METERS.length)];
            } else {
                meterId = meterId.trim();
            }

            // Energy value: realistic positive value, occasional high-usage spike (25% chance > 100 kWh)
            if (energyConsumed == null || energyConsumed <= 0) {
                boolean isSpike = RANDOM.nextDouble() < 0.25;
                if (isSpike) {
                    energyConsumed = 105.0 + (RANDOM.nextDouble() * 45.0); // 105.0 to 150.0 kWh
                } else {
                    energyConsumed = 22.0 + (RANDOM.nextDouble() * 45.0);  // 22.0 to 67.0 kWh
                }
                energyConsumed = Math.round(energyConsumed * 100.0) / 100.0;
            }

            // Unique reading ID prefixed with SIM-AUTO-
            if (readingId == null || readingId.trim().isEmpty()) {
                readingId = "SIM-AUTO-" + SEQUENCE.incrementAndGet();
            } else {
                readingId = readingId.trim();
                if (!readingId.startsWith("SIM-AUTO-")) {
                    readingId = "SIM-AUTO-" + readingId;
                }
            }

            LocalDateTime ts = LocalDateTime.now().withNano(0);
            if (timestampStr != null && !timestampStr.trim().isEmpty()) {
                try {
                    ts = LocalDateTime.parse(timestampStr.trim());
                } catch (DateTimeParseException ignored) {}
            }

            EnergyReading reading = new EnergyReading(readingId, meterId, energyConsumed, ts);

            try {
                manager.addReading(reading);
            } catch (InvalidReadingException | DuplicateReadingException e) {
                sendJsonError(exchange, 400, "Failed to record simulated reading: " + e.getMessage());
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true,\"message\":\"Automated simulated reading recorded successfully.\",");
            sb.append("\"isSimulated\":true,");
            sb.append("\"reading\":").append(readingToJson(reading));
            sb.append("}");

            sendJsonResponse(exchange, 201, sb.toString());
        }
    }

    // =========================================================================
    // Utilities
    // =========================================================================

    private static String readingToJson(EnergyReading r) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"readingId\":\"").append(escapeJson(r.getReadingId())).append("\",");
        sb.append("\"meterId\":\"").append(escapeJson(r.getMeterId())).append("\",");
        sb.append("\"energyConsumed\":").append(formatDouble(r.getEnergyConsumed())).append(",");
        sb.append("\"timestamp\":\"").append(r.getTimestamp() != null ? r.getTimestamp().toString() : "").append("\",");
        sb.append("\"isSimulated\":").append(r.getReadingId().startsWith("SIM-HIST-") || r.getReadingId().startsWith("R-SIM-") || r.getReadingId().startsWith("SIM-AUTO-"));
        sb.append("}");
        return sb.toString();
    }

    private static String computePercentageChange(double current, double previous) {
        if (previous == 0.0) {
            if (current == 0.0) return "0.00% (No energy consumed in either period)";
            return "N/A (Baseline was 0.00 kWh; new consumption recorded)";
        }
        double diff = current - previous;
        double pct = (diff / previous) * 100.0;
        if (pct > 0) return String.format("+%.2f%% increase", pct);
        if (pct < 0) return String.format("%.2f%% decrease", pct);
        return "0.00% (No change in consumption)";
    }

    private static void handleCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        handleCors(exchange);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendJsonError(HttpExchange exchange, int status, String errorMsg) throws IOException {
        String json = "{\"success\":false,\"error\":\"" + escapeJson(errorMsg) + "\"}";
        sendJsonResponse(exchange, status, json);
    }

    private static void sendResponse(HttpExchange exchange, int status, String contentType, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private static String getMimeType(String filename) {
        if (filename.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filename.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filename.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filename.endsWith(".json")) return "application/json; charset=UTF-8";
        if (filename.endsWith(".svg")) return "image/svg+xml; charset=UTF-8";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".ico")) return "image/x-icon";
        return "text/plain; charset=UTF-8";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String formatDouble(double d) {
        if (!Double.isFinite(d)) return "0.0";
        return String.format(java.util.Locale.US, "%.2f", d);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }

    private static String extractJsonString(String json, String field) {
        if (json == null) return null;
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx + key.length());
        if (colon == -1) return null;
        int startQuote = json.indexOf("\"", colon + 1);
        if (startQuote == -1) return null;
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return null;
        return json.substring(startQuote + 1, endQuote);
    }

    private static Double extractJsonDouble(String json, String field) {
        if (json == null) return null;
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx + key.length());
        if (colon == -1) return null;
        int i = colon + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        int start = i;
        while (i < json.length() && (Character.isDigit(json.charAt(i)) || json.charAt(i) == '.' || json.charAt(i) == '-')) i++;
        if (start == i) return null;
        try {
            return Double.parseDouble(json.substring(start, i));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
