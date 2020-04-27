package io.openems.edge.consolinno.leaflet.mainmodule.api;

public abstract class AbstractPcaMainModuleProvider implements PcaMainModuleProvider {
    String version;
    String moduleId;

    public AbstractPcaMainModuleProvider(String version, String moduleId) {
        this.version = version;
        this.moduleId = moduleId;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String getModuleId() {
        return this.moduleId;
    }
}
