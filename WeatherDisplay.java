



/**
 * ============================================================
 *  WeatherDisplay.java  –  Presentation Layer
 * ------------------------------------------------------------
 *  Responsible ONLY for formatting and printing WeatherData
 *  to the console.  All business logic lives in WeatherService;
 *  all HTTP logic lives in HttpClient.
 *
 *  Output sections:
 *    ① Header / location
 *    ② Current conditions box
 *    ③ 7-day forecast table
 * ============================================================
 */
public class WeatherDisplay {

    // ── ANSI colour codes (work on most terminals) ────────
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String BLUE   = "\u001B[34m";
   
    private static final String WHITE  = "\u001B[37m";

    private static final int WIDTH = 58; // box width

    // ── public API ────────────────────────────────────────

    /**
     * Prints the full weather report (header + current + forecast).
     *
     * @param wd populated WeatherData object
     */
    public void display(WeatherData wd) {
        System.out.println();
        printHeader(wd);
        printCurrentConditions(wd);
        printForecast(wd);
        printFooter();
    }

    // ── ① header ─────────────────────────────────────────

    private void printHeader(WeatherData wd) {
        String dayNight = wd.isDay() ? "☀  Day" : "🌙 Night";
        printDivider('═');
        printCentered(BOLD + CYAN + "🌍  WEATHER REPORT" + RESET, WIDTH);
        printCentered(BOLD + wd.getCityName().toUpperCase() + RESET, WIDTH);
        printCentered(WHITE + String.format("Lat %.4f  |  Lon %.4f  |  %s",
                wd.getLatitude(), wd.getLongitude(), dayNight) + RESET, WIDTH);
        printCentered(WHITE + "Timezone: " + wd.getTimezone() + RESET, WIDTH);
        printDivider('═');
    }

    // ── ② current conditions ─────────────────────────────

    private void printCurrentConditions(WeatherData wd) {
        System.out.println(BOLD + YELLOW + "  CURRENT CONDITIONS" + RESET);
        printDivider('─');

        // weather description with large temperature
        System.out.printf("  %-30s %s%s%.1f°C%s%n",
                wd.getWeatherDescription(),
                BOLD, YELLOW, wd.getTemperatureCelsius(), RESET);

        printRow("Feels Like",
                String.format("%.1f°C", wd.getFeelsLikeCelsius()), WHITE);

        printRow("Humidity",
                String.format("%.0f%%", wd.getHumidity()), BLUE);

        printRow("Wind Speed",
                String.format("%.1f km/h  %s",
                        wd.getWindSpeedKmh(),
                        bearingToCompass(wd.getWindDirection())), GREEN);

        printRow("Precipitation",
                String.format("%.1f mm", wd.getPrecipitationMm()), BLUE);

        printRow("Wind Direction",
                String.format("%d° (%s)",
                        wd.getWindDirection(),
                        bearingToCompass(wd.getWindDirection())), WHITE);
        printDivider('─');
    }

    // ── ③ 7-day forecast ──────────────────────────────────

    private void printForecast(WeatherData wd) {
        System.out.println(BOLD + YELLOW + "  7-DAY FORECAST" + RESET);
        printDivider('─');

        // header row
        System.out.printf("  %-12s %-22s %7s %7s %8s%n",
                "Date", "Condition", "Max°C", "Min°C", "Rain mm");
        printDivider('·');

        String[] dates  = wd.getForecastDates();
        double[] maxT   = wd.getMaxTempCelsius();
        double[] minT   = wd.getMinTempCelsius();
        double[] precip = wd.getPrecipitationSum();
        int[]    codes  = wd.getDominantWeatherCode();

        for (int i = 0; i < dates.length; i++) {
            String colour = (i == 0) ? BOLD + CYAN : WHITE;
            String label  = (i == 0) ? dates[i] + " (today)" : dates[i];
            System.out.printf(colour + "  %-12s %-22s %6.1f %7.1f %8.1f%n" + RESET,
                    label,
                    shorten(WeatherService.decodeWmoCode(codes[i]), 21),
                    maxT[i],
                    minT[i],
                    precip[i]);
        }
        printDivider('─');
    }

    // ── ④ footer ─────────────────────────────────────────

    private void printFooter() {
        printCentered(WHITE + "Data: open-meteo.com  |  Free & No API Key Required" + RESET, WIDTH);
        printDivider('═');
        System.out.println();
    }

    // ── formatting helpers ────────────────────────────────

    private void printRow(String label, String value, String colour) {
        System.out.printf("  %-20s %s%s%s%n", label + ":", colour, value, RESET);
    }

    private void printDivider(char ch) {
        System.out.println("  " + String.valueOf(ch).repeat(WIDTH));
    }

    private void printCentered(String text, int width) {
        // strip ANSI codes to get printable length
        int visible = text.replaceAll("\u001B\\[[;\\d]*m", "").length();
        int pad = Math.max(0, (width - visible) / 2);
        System.out.println(" ".repeat(pad + 2) + text);
    }

    /**
     * Shortens a string to {@code maxLen} chars with "…" if it overflows.
     */
    private static String shorten(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }

    /**
     * Converts a wind direction in degrees to a compass point abbreviation.
     * (N, NE, E, SE, S, SW, W, NW)
     */
    public static String bearingToCompass(int degrees) {
        String[] points = {"N","NE","E","SE","S","SW","W","NW"};
        int idx = (int) Math.round(degrees / 45.0) % 8;
        return points[idx];
    }
}
