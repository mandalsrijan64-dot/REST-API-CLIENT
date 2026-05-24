# Java Weather REST API Client

A **zero-dependency** Java application that fetches live weather data from a public REST API and displays it in a structured, coloured console report. No API key required.

---

## Project Structure

```
WeatherAPIClient/
├── src/
│   ├── WeatherApp.java              ← Entry point (main)
│   ├── model/
│   │   └── WeatherData.java         ← Data Transfer Object
│   ├── client/
│   │   └── HttpClient.java          ← HTTP GET + error handling
│   ├── parser/
│   │   └── JsonParser.java          ← Hand-written JSON parser
│   ├── service/
│   │   └── WeatherService.java      ← Geocoding + weather logic
│   └── display/
│       └── WeatherDisplay.java      ← Console formatting
├── build_and_run.sh                 ← One-step build script
└── README.md
```

---

## Architecture – Layered Design

```
┌──────────────────────────────────────────────────────┐
│  WeatherApp (main)  – entry point, error handling     │
└────────────────────────┬─────────────────────────────┘
                         │
           ┌─────────────▼──────────────┐
           │     WeatherService          │  ← orchestration
           │  geocode() + fetchWeather() │
           └──────┬──────────┬──────────┘
                  │          │
       ┌──────────▼──┐  ┌────▼────────┐
       │  HttpClient  │  │  JsonParser │
       │  (HTTP GET)  │  │  (parsing)  │
       └─────────────┘  └────────────┘
                         │
              ┌──────────▼──────────┐
              │    WeatherData       │  ← plain model object
              └──────────┬──────────┘
                         │
              ┌──────────▼──────────┐
              │   WeatherDisplay     │  ← console output
              └─────────────────────┘
```

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Java JDK    | 11 +    |

No Maven, Gradle, or external JARs needed.

---

## Compile & Run

### Option A — Shell script (Linux / macOS)

```bash
chmod +x build_and_run.sh

# interactive mode (prompts for city)
./build_and_run.sh

# batch mode
./build_and_run.sh London Kolkata "New York"
```

### Option B — Manual (all platforms)

```bash
mkdir -p out

javac -d out \
  src/model/WeatherData.java     \
  src/client/HttpClient.java     \
  src/parser/JsonParser.java     \
  src/service/WeatherService.java \
  src/display/WeatherDisplay.java \
  src/WeatherApp.java

# interactive
java -cp out WeatherApp

# batch
java -cp out WeatherApp London "New York" Tokyo
```

---

## Sample Output

```
  ══════════════════════════════════════════════════════════
           🌍  WEATHER REPORT
                  KOLKATA
       Lat 22.5726  |  Lon 88.3639  |  ☀  Day
              Timezone: Asia/Kolkata
  ══════════════════════════════════════════════════════════
  CURRENT CONDITIONS
  ──────────────────────────────────────────────────────────
  Partly cloudy                  34.2°C
  Feels Like:          33.8°C
  Humidity:            72%
  Wind Speed:          14.3 km/h  SW
  Precipitation:       0.0 mm
  Wind Direction:      225° (SW)
  ──────────────────────────────────────────────────────────
  7-DAY FORECAST
  ──────────────────────────────────────────────────────────
  Date          Condition              Max°C   Min°C   Rain mm
  ··············································
  2025-08-01 (today)  Partly cloudy    34.2    27.1      0.0
  2025-08-02          Moderate rain    31.5    26.4     12.3
  ...
  ══════════════════════════════════════════════════════════
```

---

## REST API Details

### Geocoding – resolve city name to coordinates

```
GET https://geocoding-api.open-meteo.com/v1/search
    ?name=London&count=1&language=en&format=json
```

### Weather Forecast – current + 7-day

```
GET https://api.open-meteo.com/v1/forecast
    ?latitude=51.5085&longitude=-0.1257
    &current=temperature_2m,apparent_temperature,...
    &daily=weather_code,temperature_2m_max,...
    &timezone=auto&forecast_days=7
```

Both endpoints are **completely free** and require **no API key**.  
Full documentation: https://open-meteo.com/en/docs

---

## Key Concepts Demonstrated

| Concept | Where |
|---------|-------|
| `HttpURLConnection` GET request | `HttpClient.java` |
| Connect / read timeouts | `HttpClient.java` |
| HTTP error status handling | `HttpClient.java` – `HttpException` |
| JSON object/array/number/string parsing | `JsonParser.java` |
| Multi-step REST workflow (geocode → weather) | `WeatherService.java` |
| WMO weather code decoding | `WeatherService.decodeWmoCode()` |
| Layered architecture (model / client / parser / service / display) | all files |
| Graceful error handling (network, 4xx, city not found) | `WeatherApp.java` |
| ANSI colour terminal output | `WeatherDisplay.java` |
| Interactive REPL + batch CLI modes | `WeatherApp.java` |

---

## Extending the Project

- **Add Gson / Jackson** – replace `JsonParser` with `new Gson().fromJson(...)` for even simpler mapping.
- **Add unit tests** – mock `HttpClient` and test `WeatherService` and `JsonParser` independently.
- **Hourly forecast** – add the `hourly` parameter to the Open-Meteo URL and map additional fields.
- **GUI** – swap `WeatherDisplay` for a Swing or JavaFX panel without touching any other class.
