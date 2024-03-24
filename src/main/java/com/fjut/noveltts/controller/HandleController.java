package com.fjut.noveltts.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fjut.noveltts.entity.ArticleData;
import com.fjut.noveltts.entity.SentenceInfo;
import com.fjut.noveltts.entity.SentenceVo;
import com.fjut.noveltts.entity.SpeakerInfo;
import com.fjut.noveltts.utils.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/handle")
@CrossOrigin
public class HandleController {

    @GetMapping("/index")
    public String toIndex(){
        return "index";
    }

    //将小说角色和朗读声音角色进行绑定添加到配置文件中
    @PutMapping("/addBundleConfig")
    @ResponseBody
    public Result addCharacter(@RequestBody SpeakerInfo[] speakerInfo){
        HashMap<String, String> map = new HashMap<>();
        for (SpeakerInfo info : speakerInfo) {
            if(info.getSpeaker().equals("默认旁白角色")||info.getSpeaker().equals("默认对话角色"))
                continue;
            map.put(info.getSpeaker(),info.getCharacter()+"@&"+info.getEmotion());
        }
        ProgramUtil.addConfig(map);
        return Result.ok().message("数据绑定成功");
    }

    @ResponseBody
    //得到所有配音角色和朗读声音角色的绑定
    @GetMapping("/getBundleConfig")
    public Result getBundleConfig(){
        ArrayList<SpeakerInfo> speakerInfos = new ArrayList<>();
        Map<String, String> map = ProgramUtil.getconfig();
        if(map==null)
            return null;
        for (Map.Entry<String, String> next : map.entrySet()) {
            if(next.getValue().contains("@&")){
                String[] split = next.getValue().split("@&");
                speakerInfos.add(new SpeakerInfo(next.getKey(), split[1], split[0]));
            }
        }
        speakerInfos.add(new SpeakerInfo("默认对话角色",ProgramUtil.getConfigByKey("defaultDialogueEmotion"),ProgramUtil.getConfigByKey("defaultDialogueCharacter")));
        speakerInfos.add(new SpeakerInfo("默认旁白角色",ProgramUtil.getConfigByKey("defaultNarrationEmotion"),ProgramUtil.getConfigByKey("defaultNarrationCharacter")));
        return Result.ok().data("speakerInfos",speakerInfos);
    }

    @ResponseBody
    //添加配置文件
    @PutMapping("/addConfig")
    public Result addCharacter(@RequestBody HashMap<String,String> configMap){
        ProgramUtil.addConfig(configMap);
        return Result.ok().message("配置文件添加成功");
    }

    @ResponseBody
    //得到所有配音角色
    @GetMapping("/getAllCharacter")
    public Result getAllCharacter(){
        Map<String, List<String>> characterList = ProgramUtil.getCharacterList();
        Set<String> strings = characterList.keySet();
        return Result.ok().data("characterList",strings);
    }

    @ResponseBody
    //根据配音角色得到所有配音角色所有的感情
    @GetMapping("/getEmotionByCharacter")
    public Result getEmotionByCharacter(String character){
        Map<String, List<String>> characterList = ProgramUtil.getCharacterList();
        List<String> strings = characterList.get(character);
        return Result.ok().data("emotionList",strings);
    }

    @ResponseBody
    //查询配置文件中的所有配音角色及所有情感
    @GetMapping("/getAllCharacterAndEmotion")
    public Result getCharacterInfo(){
        Map<String, List<String>> characterList = ProgramUtil.getCharacterList();
        return Result.ok().data("characterInConfig",characterList);
    }

    @ResponseBody
    @DeleteMapping("/deleteConfig")
    public Result deleteConfig(String speaker){
        ProgramUtil.deleteConfig(speaker);
        return Result.ok().message("删除成功");
    }

    @ResponseBody
    //转换模型配置文件
    @GetMapping("/handleModelConfig")
    public Result handleModelConfig(String modelPath){
        if(!Files.isDirectory(Paths.get(modelPath)))
            return Result.error().message("未配置文件夹路径或错误的文件夹路径");
        HandleModelConfigUtil.setConfigFile(modelPath);
        return Result.ok().message("模型配置文件转换成功");
    }


    @ResponseBody
    //进行小说文件切分，以章切分为一个文件
    @GetMapping("/splitArticle")
    public Result splitArticle(String novelPath){
        //写入配置文件
        ProgramUtil.addConfig("novelPath",novelPath.replace(".txt",""));
        if(!novelPath.endsWith(".txt"))
            return Result.error().message("错误的文件格式，小说需为txt类型");
        if(!Files.isRegularFile(Paths.get(novelPath)))
            return Result.error().message("小说未配置文件路径或错误的文件路径");
        HandleTextUtil.splitArticle(novelPath);
        return Result.ok().message("小说切分成功，请前往"+novelPath.replace(".txt","")+"查看");
    }

    @ResponseBody
    //得到所有切分后的文件名
    @GetMapping("/getArticleList")
    public Result getArticleList(){
        String novelPath = ProgramUtil.getConfigByKey("novelPath");
        if(novelPath==null)
            return Result.error().message("未配置小说文件路径");
        String[] list = new File(novelPath).list();
        if(list==null)
            return Result.error().message("未找到切分后的小说文件");
        Arrays.sort(list, (s1, s2) -> {
            Pattern p = Pattern.compile("^\\d+");
            Matcher m1 = p.matcher(s1);
            Matcher m2 = p.matcher(s2);
            if (m1.find() && m2.find()) {
                return Integer.compare(Integer.parseInt(m1.group()), Integer.parseInt(m2.group()));
            }
            return s1.compareTo(s2);
        });
        return Result.ok().data("getArticleList",list);
    }

    @ResponseBody
    //返回一章小说切分和说话人，并且预测结果
    @GetMapping("/getHandledArticle")
    public Result getHandledArticle(String articleName){
        String novelPath = ProgramUtil.getConfigByKey("novelPath");
        if(novelPath==null)
            return Result.error().message("未配置小说文件路径");
        ArrayList<SentenceInfo> sentenceInfos = HandleTextUtil.formatArticle(novelPath.replace(".txt", "") + "\\" + articleName);
        if(sentenceInfos==null)
            return Result.error();
        return Result.ok().data("sentenceInfos",sentenceInfos);
    }

    @ResponseBody
    //接受调整成功的小说内容，对其进行转语音
    //测试
    @PostMapping("/saveFormatArticle")
    public Result saveFormatArticle(@RequestBody ArticleData articleData){
        if(articleData.getNovelName()==null)
            return Result.error().message("小说名不能为空");
        String novelPath = ProgramUtil.getConfigByKey("novelPath")+"format\\";
        new File(novelPath).mkdirs();
        HandleTextUtil.saveFormatArticle(novelPath+articleData.getNovelName(),articleData.getSentenceInfo());
        return Result.ok();
    }

    @ResponseBody
    @GetMapping("/novelTTS")
    public Result novelTTS(){
        try {
            String novelPath = ProgramUtil.getConfigByKey("novelPath");
            ArrayList<SentenceInfo> sentenceInfo = null;
            File[] files = new File(novelPath+"format\\").listFiles();
            for (File file : files) {
                // 创建 ObjectMapper 对象
                ObjectMapper mapper = new ObjectMapper();
                // 读取 JSON 文件并转换为 ArrayList<SentenceInfo> 对象
                sentenceInfo = mapper.readValue(file, new TypeReference<ArrayList<SentenceInfo>>(){});
                HandleAudioUtil.textToAudio(sentenceInfo,novelPath);
                boolean delete = file.delete();
                if(delete)
                    System.out.println(file+"已合成，删除文件成功");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.ok();
    }
}