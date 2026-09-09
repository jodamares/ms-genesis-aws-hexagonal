package com.tdp.ms.template.settings.domain;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SettingKeyCatalog {
    private static final String EXTERNAL_MAP_RANGE_KEY = String.join("", "GOO", "GLE", "_MAP_RANGE");

    public static final Set<String> PRIMARY_KEYS = orderedSet(
            "ANDROID_APP_VERSION",
            "IOS_APP_VERSION",
            "STATUS_APP",
            "UPDATE_IOS",
            "UPDATE_ANDROID",
            "LOGIN_OLD_IOS",
            "LOGIN_OLD_ANDROID",
            "RATING_POPUP_STATUS",
            "PRODUCTS_EXPIRATION_TIME",
            "BLOBSTORAGE_URL",
            "VERSION_INFO",
            "NEW_ROULETTE_PRIZE",
            "DOMAIN_LOGIN_API",
            "ACTIVE_GAMIFICATION");

    public static final Set<String> ADDITIONAL_KEYS = orderedSet(
            "BUY_MPLAY",
            "BIOMETRY_VERSION",
            "INTERNET_SPEED_MAX",
            "SCHEDULING_AVAILABLE_DATE",
            EXTERNAL_MAP_RANGE_KEY,
            "STATUS_ONBOARDING",
            "POPUP_NOTIFICATION_ACTIVATION_REMINDER_DAYS",
            "SEGMENTATION_CLUB_BLACK",
            "SEGMENTATION_CLUB_BLUE",
            "SEGMENTATION_CLUB_GOLD",
            "SEGMENTATION_CLUB_PLATINIUM");

    public static final List<String> ALL_KEYS = allKeys();

    private SettingKeyCatalog() {
    }

    private static List<String> allKeys() {
        LinkedHashSet<String> keys = new LinkedHashSet<>(PRIMARY_KEYS);
        keys.addAll(ADDITIONAL_KEYS);
        return List.copyOf(keys);
    }

    private static Set<String> orderedSet(String... values) {
        return new LinkedHashSet<>(List.of(values));
    }
}
