package org.example.api;

import org.example.model.ApiType;

import java.util.HashMap;
import java.util.Map;

public class CountriesApi extends ApiSource {
    public CountriesApi() {
        super("https://restcountries.com/v3.1",
                ApiType.COUNTRIES.getCodeName());
    }

    @Override
    public String buildUrl() {
        Map<String, String> params = getDefaultParams();
        String defaultEndpoint = "name";
        String defaultCountry = "peru";

        return super.getBaseUrl() + "/" + defaultEndpoint
        + "/" + defaultCountry
        + "?" + buildQuery(params);
    }

    @Override
    public Map<String, String> getDefaultParams() {
        Map<String, String> params = new HashMap<>();
        params.put("fields", "name,population,area,capital,continents");

        return params;
    }
}
