package com.fjut.noveltts.entity;

public class SentenceInfo {
    //是否是对话
   private boolean dialogue;
   //批处理大小
   private Integer batchSize=1;
   //句子
   private String sentence;
    //说话者
   private String speaker;
   //配音角色
   private String character;
   //情感
   private String emotion="default";

   private double speed=1.0;

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public SentenceInfo(boolean isDialogue, String sentence) {
        this.dialogue = isDialogue;
        this.sentence = sentence;
    }

    public boolean isDialogue() {
        return dialogue;
    }

    public void setDialogue(boolean dialogue) {
        this.dialogue = dialogue;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public SentenceInfo() {
    }

    @Override
    public String toString() {
        return "SentenceInfo{" +
                "isDialogue=" + dialogue +
                ", batchSize=" + batchSize +
                ", sentence='" + sentence + '\'' +
                ", speaker='" + speaker + '\'' +
                ", character='" + character + '\'' +
                ", emotion='" + emotion + '\'' +
                ", speed=" + speed +
                '}';
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }
}