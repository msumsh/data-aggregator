package org.example.api;

import org.example.model.ApiType;
import java.util.HashMap;
import java.util.Map;

public class MuseumApi extends ApiSource {
    public MuseumApi() {
        super("https://collectionapi.metmuseum.org/public/collection/v1/objects",
                ApiType.MUSEUM.getCodeName());
    }

    @Override
    public String buildUrl() {
       Map<String, String> params = getDefaultParams();
       int defaultId = 1;

       return super.getBaseUrl() + "/" + defaultId + "?" + buildQuery(params);
    }

    @Override
    public Map<String, String> getDefaultParams() {
        Map<String, String> params = new HashMap<>();
        params.put("departmentIds", "1");
        params.put("metadataDate", "2026-01-01");

        return params;
    }
}
