package io.github.avstarodub.mosd.scheduler.api.dto;

public record RateSelectionUsed(RateSelectionMode mode, RateOptionUsed option, RateOverrideUsed override) {
    public static RateSelectionUsed option(String key, String title, int units, int minutes) {
        return new RateSelectionUsed(RateSelectionMode.OPTION, new RateOptionUsed(key, title, units, minutes), null);
    }

    public static RateSelectionUsed override(int units, int minutes, String reason) {
        return new RateSelectionUsed(RateSelectionMode.OVERRIDE, null, new RateOverrideUsed(units, minutes, reason));
    }
}
