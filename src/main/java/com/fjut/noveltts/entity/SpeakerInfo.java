package com.fjut.noveltts.entity;

public class SpeakerInfo {
    String speaker;
    String emotion;
    String character;

    @Override
    public String toString() {
        return "SpeakerInfo{" +
                "speaker='" + speaker + '\'' +
                ", emotion='" + emotion + '\'' +
                ", character='" + character + '\'' +
                '}';
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public SpeakerInfo() {
    }

    public SpeakerInfo(String speaker, String emotion, String character) {
        this.speaker = speaker;
        this.emotion = emotion;
        this.character = character;
    }
}