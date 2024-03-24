package com.fjut.noveltts.entity;

public class SentenceVo {
    //句子
    private String sentence;
    //说话者
    private String speaker;

    public SentenceVo(String sentence, String speaker) {
        this.sentence = sentence;
        this.speaker = speaker;
    }

    public SentenceVo() {
    }
}