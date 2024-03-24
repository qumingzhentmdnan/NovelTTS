package com.fjut.noveltts.entity;

import java.util.ArrayList;

public class ArticleData {
    private ArrayList<SentenceInfo> sentenceInfo;
    private String novelName;

    public ArrayList<SentenceInfo> getSentenceInfo() {
        return sentenceInfo;
    }

    public void setSentenceInfo(ArrayList<SentenceInfo> sentenceInfo) {
        this.sentenceInfo = sentenceInfo;
    }

    public String getNovelName() {
        return novelName;
    }

    public void setNovelName(String novelName) {
        this.novelName = novelName;
    }

    // getters and setters
}