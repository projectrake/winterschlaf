package com.github.projectrake.winterschlaf;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 30.12.2017.
 */
public class Configuration {
    private boolean autoDiscovery = true;
    private Map<String, String> hibernateSettings = new HashMap<>();

    public Map<String, String> getHibernateSettings() {
        return hibernateSettings;
    }

    public void setHibernateSettings(Map<String, String> hibernateSettings) {
        this.hibernateSettings = hibernateSettings;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "hibernateSettings=" + hibernateSettings +
                '}';
    }

    public boolean isAutoDiscovery() {
        return autoDiscovery;
    }

    public void setAutoDiscovery(boolean autoDiscovery) {
        this.autoDiscovery = autoDiscovery;
    }
}
