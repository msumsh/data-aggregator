package org.example.api;

import org.example.model.ApiType;
import java.util.HashMap;
import java.util.Map;

public class WeatherApi extends ApiSource {
    public WeatherApi() {
        super("https://api.met.no/weatherapi/locationforecast/2.0/complete",
                ApiType.WEATHER.getCodeName());
    }

    @Override
    public String buildUrl() {
        Map<String, String> params = getDefaultParams();
        return super.getBaseUrl() + "?" + buildQuery(params);
    }

    @Override
    public Map<String, String> getDefaultParams() {
        Map<String, String> params = new HashMap<>();
        params.put("lon", "30.314");
        params.put("lat", "59.939");

        return params;
    }
}
