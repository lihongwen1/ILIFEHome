package com.ilife.home.robot.bean;

public class VoiceLanguageBean {
    private String language;
    private int languageCode;

    public VoiceLanguageBean(String language, int languageCode) {
        this.language = language;
        this.languageCode = languageCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


    public int getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(int languageCode) {
        this.languageCode = languageCode;
    }
}
