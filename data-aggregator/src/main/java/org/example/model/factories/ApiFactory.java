package org.example.model.factories;

import org.example.api.ApiSource;
import org.example.api.types.CountriesApi;
import org.example.api.types.MuseumApi;
import org.example.api.types.WeatherApi;
import org.example.model.apitype.ApiType;

public class ApiFactory {
    public ApiSource newSource(ApiType type) {
        return switch (type) {
            case WEATHER -> new WeatherApi();
            case MUSEUM -> new MuseumApi();
            case COUNTRIES -> new CountriesApi();
        };
    }
}
