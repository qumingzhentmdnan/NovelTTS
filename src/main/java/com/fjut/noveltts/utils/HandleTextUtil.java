package com.fjut.noveltts.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fjut.noveltts.entity.SentenceInfo;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandleTextUtil {



    //将一本小说按照“第xxx章  标题”，分割成多个文件，每个文件对应一章
    public static void splitArticle(String path){
        try {
            int i=1;
            //创建文件夹
            File resFile = new File(path.replace(".txt", ""));
            if (!resFile.exists()) {
                resFile.mkdirs();
            }

            //创建文件读取流
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            //用于储存读取到的行内容
            String line = null;
            //在读取到章节名称之后，储存章节名，用于在while中(nextLine != null)利用||截断特性，跳过此处line的读取
            String nextLine = null;

            //定义一个正则表达式，匹配“第xxx章  标题”
            Pattern chapterPattern = Pattern.compile("^第.*章.*$");

            //读取文件内容
            while ((nextLine != null) || (line = bufferedReader.readLine()) != null) {
                //如果匹配到章节名称
                if (chapterPattern.matcher(line).matches()) {
                    //以章节名创建文件,i用于文件排序
                    String chapterName = (i++)+line;
                    File chapterFile = new File(resFile, chapterName + ".txt");
                    //创建文件写入流
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(chapterFile));
                    // 将章节名称写入到文件中，并换行，使用“。”包裹，避免对话切割时与正文相连
                    bufferedWriter.write('“' + chapterName.replace("“","").replace("”","") + "。”");
                    bufferedWriter.newLine();

                    //从标题后开始读取，直到下一个章节名称之前
                    while ((nextLine = bufferedReader.readLine()) != null) {
                        //如果匹配到章节名称，赋值给nextLine和line，跳出循环
                        if (chapterPattern.matcher(nextLine).matches()) {
                            line = nextLine;
                            break;
                        }
                        bufferedWriter.write(nextLine.replace("?","？").replace("!","！")
                                .replace(".","。").replace(";","；").replace("「","“").replace("」","”").replace("……？","啊"));
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.close();
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            System.err.println("splitArticle=>"+e);
        }
    }


    //读取一章的内容，并将其分隔为对话和非对话两部分
    //对话部分以“ 。”或 “ ！”或 “ ……”或 “ ？”包裹，非对话部分以对话部分之间的内容。如果文章未按照规范格式书写，可能会出现错误
    public static ArrayList<SentenceInfo> formatArticle(String path) {
        try {
            //创建集合储存对话和非对话
            ArrayList<SentenceInfo> SentenceInfos = new ArrayList<>();
            //根据文件名创建文件读取流
            FileReader fileReader = new FileReader(path);
            char[] str = new char[10000];
            //用于替换换行符
            String wrap = "\r\n";
            int legth = 0;
            //读取文件内容
            while ((legth=fileReader.read(str)) != -1) {
                StringBuilder string = new StringBuilder(new String(str));
                // 定义一个正则表达式，匹配任何以句号、感叹号或问号结束的字符串
                Pattern pattern = Pattern.compile("“[^“”]*?(“[^“”]*?”[^“”]*?)*?[，；。！？……—]”");
                // 获取一个 Matcher，用于匹配当前行
                Matcher matcher = pattern.matcher(string);
                int begin = 0;
                // 查找匹配的字符串
                while (matcher.find()) {
                    //如果匹配的字符串不是从头开始的，将匹配字符串之前的字符串加入到集合中，对话之前的就是非对话
                    if (matcher.start() != begin) {
                        //截取非对话部分，替换无效字符
                        String replace = string.substring(begin, matcher.start()).replace(wrap, "").replace("　", "").replace(" ","");//replace((char)12288,' ').replace(" ","");
                        //如果不为空，将非对话部分加入到集合中
                        if (!replace.isEmpty())
                            SentenceInfos.add(new SentenceInfo(false, replace));
                    }
                    //截取对话部分，替换无效字符
                    String replace = matcher.group().replace(wrap, "").replace("　", "").replace(" ","");//replace((char)12288,' ').replace(" ","");
                    SentenceInfos.add(new SentenceInfo(true, replace));
                    begin = matcher.end();
                }
                if(begin!=legth)
                    SentenceInfos.add(new SentenceInfo(false, string.substring(begin,legth)));
            }
            //关闭文件读取流
            fileReader.close();

            setSpeaker(SentenceInfos);
            //返回结果集
            return SentenceInfos;
        } catch (IOException e) {
            System.err.println("formatArticle=>"+e);
            return null;
        }
    }

    public static void saveFormatArticle(String path,ArrayList<SentenceInfo> sentenceInfo) {
        try {
            // 创建 ObjectMapper 对象
            ObjectMapper mapper = new ObjectMapper();
            // 将 ArrayList 对象转换为 JSON 字符串
            String json = mapper.writeValueAsString(sentenceInfo);
            // 创建 FileWriter 对象
            FileWriter writer = new FileWriter(path);
            // 将 JSON 字符串写入到文件中
            writer.write(json);
            // 关闭 FileWriter
            writer.close();
        } catch (IOException e) {
            System.err.println("saveFormatArticle=>"+e);
        }
    }

    //从前后旁白中找出说话者
    private static void setSpeaker(ArrayList<SentenceInfo> list) {
        int index = 0, size = list.size();
        SentenceInfo tmp = null;
        //读取默认配置
        double speed= ProgramUtil.getConfigByKey("speed") == null ? 1.0 : Double.parseDouble(ProgramUtil.getConfigByKey("speed"));
        int batchSize= ProgramUtil.getConfigByKey("batchSize") == null ? 1 : Integer.parseInt(ProgramUtil.getConfigByKey("batchSize"));

        String defaultNarrationCharacter = ProgramUtil.getConfigByKey("defaultNarrationCharacter");
        String defaultNarrationEmotion = ProgramUtil.getConfigByKey("defaultNarrationEmotion");
        //遍历集合中的所有句子
        for (SentenceInfo sentenceInfo : list) {
            sentenceInfo.setSpeed(speed);
            sentenceInfo.setBatchSize(batchSize);
            sentenceInfo.setSpeaker("默认旁白角色");
            sentenceInfo.setCharacter(defaultNarrationCharacter);
            sentenceInfo.setEmotion(defaultNarrationEmotion);
            //如果当前句子是对话，从前后旁白中找出说话者（近似查找
            if (sentenceInfo.isDialogue()) {
                //如果前一个存在，且不是对话，从前一个句子中找出说话者，否则从后一个句子中找出说话者，如果都不存在，不做处理
                if (index - 1 >= 0 && (!((tmp = list.get(index - 1)).isDialogue()))) {
                    getMinSpeaker(tmp.getSentence(), sentenceInfo);
                } else if (index + 1 < size && (!((tmp = list.get(index + 1)).isDialogue()))) {
                    getMinSpeaker(tmp.getSentence(), sentenceInfo);
                }
            }
            index++;
        }
    }

    //找到传入句子（对话的前一句或者后一句）中存在与set集合中，第一个出现的说话者
    private static void getMinSpeaker(String lastOrNextSentence, SentenceInfo nowSentence) {
        //获取所有配置文件
        Map<String, String> characterMap = ProgramUtil.getCharacterConfig();
        Set<String> speakerSet =characterMap.keySet();

        //如果是章节名，不做处理
        Pattern chapterPattern = Pattern.compile("^“.*第.*章.*。”$");
        String sentence = nowSentence.getSentence();
        if (chapterPattern.matcher(sentence).matches()) {
            String res = sentence.replace("“", "").replace("”", "")
                    .replace("。", "").replace("章", "章。")
                    .replaceAll("\\d", "");
            nowSentence.setSentence(res);
            nowSentence.setDialogue(false);
            return;
        }

        int minIndex = Integer.MAX_VALUE;
        String minSpeaker = null;
        //找到传入句子（对话的前一句或者后一句）中存在与set集合中，第一个出现的说话者
        for (String s : speakerSet) {
            int i = lastOrNextSentence.indexOf(s);
            if (i != -1 && i < minIndex) {
                minIndex = i;
                minSpeaker = s;
            }
        }
        //如果找到了说话者，将说话者赋值给当前句子，否则不做处理
        if (minSpeaker != null) {
            String[] split= characterMap.get(minSpeaker).split("@&");
            nowSentence.setCharacter(split[0]);
            nowSentence.setEmotion(split[1]);
            nowSentence.setSpeaker(minSpeaker);
        }else{
            nowSentence.setSpeaker("默认对话角色");
            nowSentence.setCharacter(ProgramUtil.getConfigByKey("defaultDialogueCharacter"));
            nowSentence.setEmotion(ProgramUtil.getConfigByKey("defaultDialogueEmotion"));
        }
    }


}