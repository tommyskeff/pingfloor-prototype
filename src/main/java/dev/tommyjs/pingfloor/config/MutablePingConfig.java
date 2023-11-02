package dev.tommyjs.pingfloor.config;

public class MutablePingConfig {

    private boolean enabled;
    private int threshold;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

}
