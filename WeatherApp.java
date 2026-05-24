

import java.io.IOException;
import java.util.Scanner;

/**
 * ============================================================
 *  WeatherApp.java  –  Application Entry Point
 * ------------------------------------------------------------
 *  Wires together the three layers:
 *    HttpClient  →  WeatherService  →  WeatherDisplay
 *
 *  Run modes:
 *    1. Interactive  – run with no arguments; prompts for city
 *    2. Batch        – pass city names as command-line arguments
 *
 *  Usage:
 *    Compile:
 *      javac -d out src/model/*.java src/client/*.java \
 *            src/parser/*.java src/service/*.java \
 *            src/display/*.java src/WeatherApp.java
 *
 *    Interactive:
 *      java -cp out WeatherApp
 *
 *    Batch (multiple cities):
 *      java -cp out WeatherApp London Kolkata "New York"
 *
 *  No external libraries required – pure JDK (Java 11+).
 * ============================================================
 */
public class WeatherApp {

    public static void main(String[] args) {

        WeatherService service = new WeatherService();
        WeatherDisplay display = new WeatherDisplay();

        if (args.length > 0) {
            // ── Batch mode: cities from command line ──────
            for (String city : args) {
                fetchAndDisplay(city, service, display);
            }
        } else {
            // ── Interactive mode: prompt loop ─────────────
            runInteractive(service, display);
        }
    }

    // ── interactive REPL ──────────────────────────────────

    private static void runInteractive(WeatherService service, WeatherDisplay display) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║      Java Weather REST API Client    ║");
        System.out.println("║   Powered by open-meteo.com (free)   ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("  Type a city name and press Enter.");
        System.out.println("  Type 'quit' or press Ctrl+C to exit.\n");

        while (true) {
            System.out.print("  City: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")
                    || input.equalsIgnoreCase("exit")
                    || input.equalsIgnoreCase("q")) {
                System.out.println("\n  Goodbye!\n");
                break;
            }

            if (input.isEmpty()) {
                System.out.println("  Please enter a city name.");
                continue;
            }

            fetchAndDisplay(input, service, display);

            // ask whether to continue
            System.out.print("  Check another city? (yes/no): ");
            String again = scanner.nextLine().trim().toLowerCase();
            if (again.startsWith("n")) {
                System.out.println("\n  Goodbye!\n");
                break;
            }
        }

        scanner.close();
    }

    // ── fetch + display with error handling ───────────────

    /**
     * Fetches weather for one city and displays it, handling all known
     * error cases gracefully without crashing the application.
     */
    private static void fetchAndDisplay(String city,
                                        WeatherService service,
                                        WeatherDisplay display) {
        System.out.println("\n  Fetching weather for \"" + city + "\" …");

        try {
            long start = System.currentTimeMillis();
            WeatherData data = service.fetchWeather(city);
            long elapsed = System.currentTimeMillis() - start;

            display.display(data);
            System.out.printf("  (Fetched in %d ms)%n", elapsed);

        } catch (WeatherService.WeatherServiceException e) {
            // city not found or malformed data
            // HTTP 4xx / 5xx
            printError("HTTP " + e.getStatusCode(), e.getMessage());

        } catch (java.net.UnknownHostException e) {
            // no network / DNS failure
            printError("Network error",
                    "Cannot reach the server. Check your internet connection.");

        } catch (java.net.SocketTimeoutException e) {
            printError("Timeout", "The request timed out. Please try again.");

        } catch (IOException e) {
            printError("I/O error", e.getMessage());
        }
    }

    private static void printError(String type, String message) {
        System.out.println("\n  \u001B[31m[" + type + "]\u001B[0m " + message + "\n");
    }
}
