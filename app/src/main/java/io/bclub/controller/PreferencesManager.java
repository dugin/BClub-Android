package io.bclub.controller;

import android.content.SharedPreferences;

public class PreferencesManager {

    private SharedPreferences sharedPreferences;

    public PreferencesManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public String getCityId() {
        return sharedPreferences.getString(CITY_ID_KEY, null);
    }

    public String getCityName() {
        return sharedPreferences.getString(CITY_NAME_KEY, null);
    }

    public void setCityInfo(String cityId, String cityName) {
        sharedPreferences
                .edit()
                .putString(CITY_ID_KEY, cityId)
                .putString(CITY_NAME_KEY, cityName)
                .apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(USER_ID_KEY, null);
    }

    public void setUserId(String userId) {
        sharedPreferences
                .edit()
                .putString(USER_ID_KEY, userId)
                .apply();
    }

    public void setTutorialDone(boolean tutorialDone) {
        sharedPreferences
                .edit()
                .putBoolean(TUTORIAL_KEY, tutorialDone)
                .apply();
    }

    public boolean isTutorialDone() {
        return sharedPreferences.getBoolean(TUTORIAL_KEY, false);
    }

    public void storeUserInfo(String userId, String email) {
        sharedPreferences
                .edit()
                .putString(USER_ID_KEY, userId)
                .putString(EMAIL_KEY, email)
                .apply();
    }

    public void clearsUserInfo() {
        sharedPreferences.
                edit()
                .remove(USER_ID_KEY)
                .remove(EMAIL_KEY)
                .apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.contains(USER_ID_KEY);
    }

    private static final String CITY_ID_KEY = "CITY_ID";
    private static final String CITY_NAME_KEY = "CITY_NAME";

    private static final String USER_ID_KEY = "USER_ID";
    private static final String EMAIL_KEY = "EMAIL";
    private static final String TUTORIAL_KEY = "TUTORIAL";
}
