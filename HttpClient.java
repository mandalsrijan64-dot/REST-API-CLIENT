
import java.net.URI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ============================================================
 *  HttpClient.java  –  HTTP Layer
 * ------------------------------------------------------------
 *  A thin, zero-dependency wrapper around Java's built-in
 *  HttpURLConnection.  Handles:
 *    • Building and encoding URLs
 *    • Opening connections with timeout and User-Agent headers
 *    • Reading the response body into a String
 *    • Translating HTTP error codes into descriptive exceptions
 *
 *  Only GET requests are needed for a weather API, so only
 *  GET is implemented here.  The class is easily extended.
 * ============================================================
 */
public class HttpClient {

    // ── tuneable constants ────────────────────────────────
    private static final int CONNECT_TIMEOUT_MS = 10_000;  // 10 s
    private static final int READ_TIMEOUT_MS    = 15_000;  // 15 s
    private static final String USER_AGENT =
            "WeatherAPIClient/1.0 (Java " + System.getProperty("java.version") + ")";

    // ── public API ────────────────────────────────────────

    /**
     * Executes an HTTP GET request and returns the response body as a String.
     *
     * @param urlString fully-formed URL (already encoded)
     * @return raw response body
     * @throws IOException          on network or I/O failure
     * @throws HttpException        when the server returns a non-2xx status
     */
    public String get(String urlString) throws IOException {
        URI uri = URI.create(urlString);
HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            // ── configure the connection ──────────────────
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("Accept",     "application/json");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setDoInput(true);

            // ── execute & check status ────────────────────
            int statusCode = conn.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                String errorBody = readStream(conn.getErrorStream());
                throw new HttpException(statusCode,
                        "HTTP " + statusCode + " – " + conn.getResponseMessage()
                        + (errorBody.isEmpty() ? "" : "\nBody: " + errorBody));
            }

            // ── read & return response body ───────────────
            return readStream(new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)));
            }
      

    /**
     * URL-encodes a single query-parameter value.
     *
     * @param value raw value (e.g., "New York")
     * @return percent-encoded value (e.g., "New+York")
     */
    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // ── private helpers ───────────────────────────────────

    /** Reads an InputStream (or BufferedReader) into a String. */
    private String readStream(java.io.InputStream is) throws IOException {
        if (is == null) return "";
        return readStream(new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)));
    }

    private String readStream(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString().trim();
    }

    // ── inner exception ───────────────────────────────────

    /**
     * Thrown when the server returns a non-2xx HTTP status code.
     * Carries the numeric status so callers can react (e.g., 404 vs 500).
     */
    public static class HttpException extends IOException {
        private final int statusCode;

        public HttpException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() { return statusCode; }
    }
}
