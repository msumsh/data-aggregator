package org.example.model;

public enum ApiType {
    WEATHER("Weather", "open_meteo_api"),
    MUSEUM("Museum", "metropolitan_museum_api"),
    COUNTRIES("Countries", "rest_countries_api");

    private final String displayName;
    private final String codeName;

    ApiType(String displayName, String codeName) {
        this.displayName = displayName;
        this.codeName = codeName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getCodeName() {
        return this.codeName;
    }
}
