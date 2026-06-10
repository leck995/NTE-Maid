package cn.tealc.ntemaid.model.system.resources;

import java.util.List;
import java.util.Map;


public class RootResource {
    private String version;
    private Map<String, List<Resource>> resources;


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, List<Resource>> getResources() {
        return resources;
    }

    public void setResources(Map<String, List<Resource>> resources) {
        this.resources = resources;
    }
}