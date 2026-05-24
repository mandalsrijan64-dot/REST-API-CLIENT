

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 *  WeatherService.java  –  Service / Business-Logic Layer
 * ------------------------------------------------------------
 *  Orchestrates two REST API calls:
 *
 *    Step 1 – Geocoding (Open-Meteo Geocoding API)
 *             City name  →  latitude / longitude
 *
 *    Step 2 – Weather   (Open-Meteo Forecast API)
 *             Lat / lon  →  WeatherData (current + 7-day forecast)
 *
 *  Both APIs are completely free and require no API key.
 *  Docs: https://open-meteo.com/en/docs
 * ============================================================
 */
public class WeatherService {

    // ── API base URLs ─────────────────────────────────────
    private static final String GEO_URL  =
            "https://geocoding-api.open-meteo.com/v1/search?name=%s&count=1&language=en&format=json";

    private static final String WEATHER_URL =
            "https://api.open-meteo.com/v1/forecast"
            + "?latitude=%s&longitude=%s"
            + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,"
            +          "precipitation,weather_code,wind_speed_10m,wind_direction_10m,is_day"
            + "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum"
            + "&timezone=auto"
            + "&forecast_days=7";

    private final HttpClient http = new HttpClient();

    // ── public API ────────────────────────────────────────

    /**
     * Fetches current weather and a 7-day forecast for the given city.
     *
     * @param cityName e.g. "London", "Kolkata", "New York"
     * @return a fully populated WeatherData object
     * @throws IOException   on network failure
     * @throws WeatherServiceException if the city is not found or data is malformed
     */
    public WeatherData fetchWeather(String cityName) throws IOException {

        // ── Step 1: resolve city → coordinates ───────────
        double[] coords = geocode(cityName);
        double lat = coords[0];
        double lon = coords[1];

        // ── Step 2: fetch weather at those coordinates ────
        String weatherJson = http.get(
                String.format(WEATHER_URL,
                        formatCoord(lat),
                        formatCoord(lon)));

        return parseWeather(weatherJson, cityName, lat, lon);
    }

    // ── geocoding ─────────────────────────────────────────

    /**
     * Calls the Open-Meteo Geocoding API and returns [latitude, longitude].
     *
     * @param cityName city to look up
     * @return double[2] { latitude, longitude }
     */
    private double[] geocode(String cityName) throws IOException {
        String url = String.format(GEO_URL, HttpClient.encode(cityName));
        String json = http.get(url);

        Map<String, Object> root = JsonParser.asObject(new JsonParser(json).parse());
        Object resultsObj = JsonParser.get(root, "results");

        if (resultsObj == null) {
            throw new WeatherServiceException(
                    "City not found: \"" + cityName + "\". Please check the spelling.");
        }

        List<Object> results = JsonParser.asArray(resultsObj);
        if (results.isEmpty()) {
            throw new WeatherServiceException(
                    "City not found: \"" + cityName + "\". Please check the spelling.");
        }

        Map<String, Object> first = JsonParser.asObject(results.get(0));
        double lat = JsonParser.asDouble(JsonParser.get(first, "latitude"));
        double lon = JsonParser.asDouble(JsonParser.get(first, "longitude"));
        return new double[]{lat, lon};
    }

    // ── JSON → WeatherData mapping ────────────────────────

    /**
     * Parses the Open-Meteo forecast JSON into a WeatherData object.
     */
    private WeatherData parseWeather(String json, String cityName,
                                     double lat, double lon) {
        Map<String, Object> root    = JsonParser.asObject(new JsonParser(json).parse());
        Map<String, Object> current = JsonParser.asObject(JsonParser.get(root, "current"));
        Map<String, Object> daily   = JsonParser.asObject(JsonParser.get(root, "daily"));

        WeatherData wd = new WeatherData();

        // location
        wd.setCityName(cityName);
        wd.setLatitude(lat);
        wd.setLongitude(lon);
        wd.setTimezone(JsonParser.asString(JsonParser.get(root, "timezone")));

        // current conditions
        wd.setTemperatureCelsius  (JsonParser.asDouble (JsonParser.get(current, "temperature_2m")));
        wd.setFeelsLikeCelsius    (JsonParser.asDouble (JsonParser.get(current, "apparent_temperature")));
        wd.setHumidity            (JsonParser.asDouble (JsonParser.get(current, "relative_humidity_2m")));
        wd.setPrecipitationMm     (JsonParser.asDouble (JsonParser.get(current, "precipitation")));
        wd.setWindSpeedKmh        (JsonParser.asDouble (JsonParser.get(current, "wind_speed_10m")));
        wd.setWindDirection       (JsonParser.asInt    (JsonParser.get(current, "wind_direction_10m")));
        int wmoCode = JsonParser.asInt(JsonParser.get(current, "weather_code"));
        wd.setWeatherCode(wmoCode);
        wd.setWeatherDescription(decodeWmoCode(wmoCode));
        wd.setDay(JsonParser.asInt(JsonParser.get(current, "is_day")) == 1);

        // 7-day forecast arrays
        List<Object> dates   = JsonParser.asArray(JsonParser.get(daily, "time"));
        List<Object> maxTemp = JsonParser.asArray(JsonParser.get(daily, "temperature_2m_max"));
        List<Object> minTemp = JsonParser.asArray(JsonParser.get(daily, "temperature_2m_min"));
        List<Object> precip  = JsonParser.asArray(JsonParser.get(daily, "precipitation_sum"));
        List<Object> codes   = JsonParser.asArray(JsonParser.get(daily, "weather_code"));

        int n = dates.size();
        String[] datesArr   = new String[n];
        double[] maxArr     = new double[n];
        double[] minArr     = new double[n];
        double[] precipArr  = new double[n];
        int[]    codesArr   = new int[n];

        for (int i = 0; i < n; i++) {
            datesArr[i]  = JsonParser.asString(dates.get(i));
            maxArr[i]    = JsonParser.asDouble(maxTemp.get(i));
            minArr[i]    = JsonParser.asDouble(minTemp.get(i));
            precipArr[i] = JsonParser.asDouble(precip.get(i));
            codesArr[i]  = JsonParser.asInt(codes.get(i));
        }

        wd.setForecastDates(datesArr);
        wd.setMaxTempCelsius(maxArr);
        wd.setMinTempCelsius(minArr);
        wd.setPrecipitationSum(precipArr);
        wd.setDominantWeatherCode(codesArr);

        return wd;
    }

    // ── WMO weather-code decoder ──────────────────────────

    /**
     * Converts a WMO Weather Interpretation Code into a human-readable
     * description.  Reference: https://open-meteo.com/en/docs#weathervariables
     */
    public static String decodeWmoCode(int code) {
        if (code == 0)  return "Clear sky";
        if (code == 1)  return "Mainly clear";
        if (code == 2)  return "Partly cloudy";
        if (code == 3)  return "Overcast";
        if (code == 45 || code == 48) return "Fog";
        if (code == 51) return "Light drizzle";
        if (code == 53) return "Moderate drizzle";
        if (code == 55) return "Dense drizzle";
        if (code == 61) return "Slight rain";
        if (code == 63) return "Moderate rain";
        if (code == 65) return "Heavy rain";
        if (code == 71) return "Slight snow";
        if (code == 73) return "Moderate snow";
        if (code == 75) return "Heavy snow";
        if (code == 80) return "Slight showers";
        if (code == 81) return "Moderate showers";
        if (code == 82) return "Violent showers";
        if (code == 95) return "Thunderstorm";
        if (code == 96 || code == 99) return "Thunderstorm with hail";
        return "Unknown (code " + code + ")";
    }

    // ── helpers ───────────────────────────────────────────

    /** Formats a coordinate to 4 decimal places for URL inclusion. */
    private static String formatCoord(double v) {
        return String.format("%.4f", v);
    }

    // ── inner exception ───────────────────────────────────

    /** Thrown for application-level weather errors (not network errors). */
    public static class WeatherServiceException extends IOException {
        public WeatherServiceException(String message) { super(message); }

        /**
         * @return
         */
        public String getStatusCode() {
            
            throw new UnsupportedOperationException("Unimplemented method 'getStatusCode'");
        }
    }
}
