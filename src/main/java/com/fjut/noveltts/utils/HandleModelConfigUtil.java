package com.fjut.noveltts.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;

//生成模型配置文件
public class HandleModelConfigUtil {
    //要转化的模型来自https://space.bilibili.com/518098961/?spm_id_from=333.999.0.0,格式如下
    //    参考音频(文件夹)
    //    xxx.pth(文件)
    //    xxx.ckpt(文件)
    //    训练日志.log(文件)

    //以下方法用于用于创建模型配置文件，GPT-Sovits-Interface项目中的模型配置文件格式如下
    //    {
    //        "emotion_list": {
    //        "default": {
    //            "prompt_language": "中文",
    //                    "prompt_text": "激动说话-既然密钥找到了就赶快开始吧，电梯的权限用那边的操作台就能解锁。",
    //                    "ref_wav_path": "参考音频\\激动说话-既然密钥找到了就赶快开始吧，电梯的权限用那边的操作台就能解锁。.wav"
    //        }
    //    },
    //        "简介": "这是一个配置文件适用于https://github.com/X-T-E-R/TTS-for-GPT-soVITS，是一个简单好用的前后端项目",
    //            "sovits_path": "阿兰_e25_s525.pth",
    //            "version": "1.0.1",
    //            "gpt_path": "阿兰-e10.ckpt"
    //    }

    //创建配置文件
    public static void setConfigFile(String filePath){
        try {
            //打开模型存放文件夹，如"E:\GPT-Sovits\GPT-SoVITS-Inference\trained"
            File modelFolders = new File(filePath);
            File[] modelsFolders = modelFolders.listFiles(File::isDirectory);
            for (File modelFolder : modelsFolders) {
                //遍历文件夹，路径如"E:\GPT-Sovits\GPT-SoVITS-Inference\trained\阿兰
                File modelPath = modelFolder.getCanonicalFile();

                //设置基础配置
                HashMap<String, Object> configMap = new HashMap<>();
                setBaseConfig(modelFolder,configMap);
                //设置情感配置
                HashMap<String, Object> emoConfig = setEmoConfig(modelFolder);
                if(emoConfig==null)
                    continue;
                configMap.put("emotion_list",emoConfig);
                // 将对象转化为 JSON 格式的字符串
                String json = new ObjectMapper().writeValueAsString(configMap);

                //将文件写入到指定文件夹下的infer_config.json
                File out = new File(modelPath + "\\"  + "infer_config.json");
                //如果文件不存在则创建文件
                if (!out.exists())
                    out.createNewFile();

                //写入文件,刷新缓冲区，关闭文件
                FileWriter fileWriter = new FileWriter(out);
                fileWriter.write(json);
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (IOException e) {
            System.err.println("setConfigFile=>"+e);
        }
    }

    //创建基础配置文件
    private static void setBaseConfig(File file, HashMap<String, Object>  configMap){
        //file路径如"E:\GPT-Sovits\GPT-SoVITS-Inference\trained\阿兰
        configMap.put("version","1.0.1");
        configMap.put("简介","这是一个配置文件适用于https://github.com/X-T-E-R/TTS-for-GPT-soVITS，是一个简单好用的前后端项目");
        String[] list = file.list((dir, name) -> name.endsWith(".ckpt") || name.endsWith(".pth"));
        //遍历文件夹下的文件，将.pth文件赋值给sovits_path，将.ckpt文件赋值给gpt_path
        for (String path : list) {
            if(path.endsWith(".ckpt"))
                configMap.put("gpt_path",path);
            else
                configMap.put("sovits_path",path);
        }
    }

    //用于创建情感配置文件
    private static HashMap<String, Object> setEmoConfig(File file){
        //file路径如E:\GPT-Sovits\GPT-SoVITS-Inference\trained\阿兰
        HashMap<String, Object> res = new HashMap<>();
        File[] files = file.listFiles(File::isDirectory);
        if(files==null||files.length<=0)
            return null;
        File[] audioDirList = files[0].listFiles();
        //开始遍历参考音频，audioDir路径为E:\GPT-Sovits\GPT-SoVITS-Inference\trained\阿兰\参考音频
        for (File audioDir : audioDirList) {
            HashMap<String, String> map = new HashMap<>();
            map.put("prompt_language","中文");
            String dirName=audioDir.getName();
            //如果是文件，则直接赋值给default，路径为E:\GPT-Sovits\GPT-SoVITS-Inference\trained\阿兰\参考音频\xxx.wav
            if(audioDir.isFile()){
                String[] split = dirName.split("-");
                map.put("ref_wav_path","参考音频/"+dirName);
                if(dirName.contains("-")){
                    map.put("prompt_text",split[1].replace(".wav",""));
                    res.put(split[0],map);
                }else{
                    map.put("prompt_text",dirName.replace(".wav",""));
                    res.put("default",map);
                }
            }else{
                //如果是文件夹，则遍历文件夹下的文件，找出最大的文件
                File[] audioFileList = audioDir.listFiles();
                File maxFile = null;
                long maxSize = 0;
                for (File audioFile : audioFileList) {
                    if (audioFile.length() > maxSize&&!audioFile.getName().replace(".wav","").matches(".*[a-zA-Z].*")) {
                        maxSize = audioFile.length();
                        maxFile = audioFile;
                    }
                }
                if(maxFile!=null){
                    //ref_wav_path路径形如E:\GPT-Sovits\GPT-SoVITS-Inference\trained\阿兰\参考音频\开心(或其他情绪)\xxx.wav
                    map.put("ref_wav_path","参考音频/"+dirName+"/"+maxFile.getName());
                    map.put("prompt_text",maxFile.getName().replace(".wav","").replaceAll("【.*?】", ""));
                    res.put(dirName,map);
                }
            }
        }
        return res;
    }
}