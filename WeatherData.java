

/**
 * ============================================================
 *  WeatherData.java  –  Model / Data Transfer Object
 * ------------------------------------------------------------
 *  Holds all weather information parsed from the
 *  Open-Meteo JSON response.  Plain Java (no external libs).
 * ============================================================
 */
public class WeatherData {

    // ── location ──────────────────────────────────────────
    private String cityName;
    private double latitude;
    private double longitude;
    private String timezone;

    // ── current conditions ────────────────────────────────
    private double temperatureCelsius;
    private double feelsLikeCelsius;
    private double windSpeedKmh;
    private int    windDirection;        // degrees 0-360
    private double humidity;             // %
    private double precipitationMm;
    private int    weatherCode;          // WMO code
    private String weatherDescription;  // decoded from WMO code
    private boolean isDay;

    // ── daily forecast (7 days) ───────────────────────────
    private String[] forecastDates;
    private double[] maxTempCelsius;
    private double[] minTempCelsius;
    private double[] precipitationSum;
    private int[]    dominantWeatherCode;

    // ── constructors ──────────────────────────────────────
    public WeatherData() {}

    // ── getters & setters ─────────────────────────────────

    public String getCityName()                    { return cityName; }
    public void   setCityName(String v)            { this.cityName = v; }

    public double getLatitude()                    { return latitude; }
    public void   setLatitude(double v)            { this.latitude = v; }

    public double getLongitude()                   { return longitude; }
    public void   setLongitude(double v)           { this.longitude = v; }

    public String getTimezone()                    { return timezone; }
    public void   setTimezone(String v)            { this.timezone = v; }

    public double getTemperatureCelsius()          { return temperatureCelsius; }
    public void   setTemperatureCelsius(double v)  { this.temperatureCelsius = v; }

    public double getFeelsLikeCelsius()            { return feelsLikeCelsius; }
    public void   setFeelsLikeCelsius(double v)    { this.feelsLikeCelsius = v; }

    public double getWindSpeedKmh()                { return windSpeedKmh; }
    public void   setWindSpeedKmh(double v)        { this.windSpeedKmh = v; }

    public int    getWindDirection()               { return windDirection; }
    public void   setWindDirection(int v)          { this.windDirection = v; }

    public double getHumidity()                    { return humidity; }
    public void   setHumidity(double v)            { this.humidity = v; }

    public double getPrecipitationMm()             { return precipitationMm; }
    public void   setPrecipitationMm(double v)     { this.precipitationMm = v; }

    public int    getWeatherCode()                 { return weatherCode; }
    public void   setWeatherCode(int v)            { this.weatherCode = v; }

    public String getWeatherDescription()          { return weatherDescription; }
    public void   setWeatherDescription(String v)  { this.weatherDescription = v; }

    public boolean isDay()                         { return isDay; }
    public void    setDay(boolean v)               { this.isDay = v; }

    public String[] getForecastDates()             { return forecastDates; }
    public void     setForecastDates(String[] v)   { this.forecastDates = v; }

    public double[] getMaxTempCelsius()            { return maxTempCelsius; }
    public void     setMaxTempCelsius(double[] v)  { this.maxTempCelsius = v; }

    public double[] getMinTempCelsius()            { return minTempCelsius; }
    public void     setMinTempCelsius(double[] v)  { this.minTempCelsius = v; }

    public double[] getPrecipitationSum()          { return precipitationSum; }
    public void     setPrecipitationSum(double[] v){ this.precipitationSum = v; }

    public int[]    getDominantWeatherCode()       { return dominantWeatherCode; }
    public void     setDominantWeatherCode(int[] v){ this.dominantWeatherCode = v; }
}
