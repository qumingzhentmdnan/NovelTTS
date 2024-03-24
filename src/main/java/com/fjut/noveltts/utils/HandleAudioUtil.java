package com.fjut.noveltts.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fjut.noveltts.entity.SentenceInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static com.fjut.noveltts.utils.HandleTextUtil.formatArticle;

@Component
public class HandleAudioUtil {
    //将一段文本转换为音频
    private static boolean getAudio(String filePath,String character,String emotion,String text,Integer batch_size,Double speed) throws IOException {

//        String url = ProgramUtil.getConfigByKey("url");
//        if(url==null||url.isEmpty())
//            System.err.println("HandleAudioUtil=>getAudio:未配置url");
//        URL obj = new URL(url+"/tts");
        URL obj = new URL("http://localhost:6006/tts");
        HttpURLConnection  con = (HttpURLConnection) obj.openConnection();

        // 设置请求方法
        con.setRequestMethod("POST");
        // 发送Post请求
        con.setDoOutput(true);
        // 创建一个Map来存储参数
        Map<String, Object> params = new HashMap<>();
        if(character!=null&&!character.isEmpty())
            params.put("character", character);
        if(emotion!=null&&!emotion.isEmpty())
            params.put("emotion", emotion);
        if(batch_size!=null)
            params.put("batch_size", batch_size);
        if(text!=null&&!text.isEmpty())
            params.put("text", text);
        if(speed!=null)
            params.put("speed", speed);
        params.put("text_language","中英混合");
        // 将Map转换为JSON格式的字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonParams = objectMapper.writeValueAsString(params);
        System.out.println(jsonParams);
        // 设置请求头，表示我们发送的是JSON格式的数据
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        OutputStream os = con.getOutputStream();
        os.write(jsonParams.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();
        // 发送请求并获取响应码
        int responseCode = con.getResponseCode();

        // 获取输入流
        InputStream inputStream = con.getInputStream();

        // 创建一个输出流，用于写入文件
        OutputStream outputStream = new FileOutputStream(filePath);
        // 创建一个缓冲区
        byte[] buffer = new byte[1024];
        int bytesRead;
        // 在循环中读取数据，直到没有更多数据可读
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        // 关闭输入流和输出流
        inputStream.close();
        outputStream.close();
        return responseCode== 200;
    }

    public static boolean textToAudio(ArrayList<SentenceInfo> text, String path)   {
        try {
            int index = 0;
            String title=text.get(0).getSentence().replace("。","");
            String dir = path + "audio\\" +title+ "\\";
            File file = new File(dir);
            if(!file.exists())
                file.mkdirs();

            for (SentenceInfo sentenceInfo : text) {
                String sentence = sentenceInfo.getSentence();
                String[] split = sentence.split("(?<=[。！？；……])");
                for (String s : split) {
                    if(!s.matches(".*[\u4e00-\u9fa5]+.*"))
                        continue;
                    System.out.println(s);
                    boolean audio = getAudio(dir+(index++)  + ".wav", sentenceInfo.getCharacter(), sentenceInfo.getEmotion(), s, sentenceInfo.getBatchSize(), sentenceInfo.getSpeed());
                }
//                System.out.println(sentenceInfo.getSentence());
//                boolean audio = getAudio(dir+(index++)  + ".wav", sentenceInfo.getCharacter(), sentenceInfo.getEmotion(), sentenceInfo.getSentence(), sentenceInfo.getBatchSize(), sentenceInfo.getSpeed());
            }
            mergeAndDeleteAudioFiles(path + "audio\\", title);
            file.delete();
        } catch (Exception e) {
            System.err.println("textToAudio=>"+e);
        }
        return true;
    }

    private static void mergeAndDeleteAudioFiles(String directoryPath, String title) throws Exception {
        // 创建一个File对象，表示指定路径的目录
        File dir = new File(directoryPath+title);
        // 获取目录下所有以.wav结尾的文件
        File[] files = dir.listFiles((d, name) -> name.endsWith(".wav"));
        // 如果没有找到任何.wav文件，抛出异常
        if (files == null || files.length == 0) {
            throw new Exception("No wav files found in the directory.");
        }
        Arrays.sort(files, Comparator.comparingInt(file -> Integer.parseInt(file.getName().replace(".wav", ""))));

        // 获取第一个.wav文件的音频输入流和音频文件格式
        AudioInputStream audioInputStream;
        AudioFileFormat format = AudioSystem.getAudioFileFormat(files[0]);

        // 创建一个字节数组输出流，用于存储所有.wav文件的数据
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 创建一个缓冲区
        byte[] buffer = new byte[1024];
        int read;

        // 遍历所有.wav文件
        for (File file : files) {
            // 获取当前.wav文件的音频输入流
            audioInputStream = AudioSystem.getAudioInputStream(file);
            // 读取音频输入流的数据，并写入到字节数组输出流中
            while ((read = audioInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
            }
            // 关闭音频输入流
            audioInputStream.close();
        }

        // 将字节数组输出流转换为字节数组
        byte[] outputBytes = outputStream.toByteArray();
        // 创建一个字节数组输入流
        InputStream byteArrayInputStream = new ByteArrayInputStream(outputBytes);
        // 将字节数组输入流转换为音频输入流
        AudioInputStream outputAudioInputStream = new AudioInputStream(byteArrayInputStream, format.getFormat(), outputBytes.length / format.getFormat().getFrameSize());
        // 将音频输入流写入到一个新的.wav文件中
        AudioSystem.write(outputAudioInputStream, format.getType(), new File(directoryPath + title + ".wav"));

        // 删除原先的.wav文件
        for (File file : files) {
            Files.delete(file.toPath());
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader("E:\\test\\新建 文本文档.txt"));
        String str;
        int index=0;
        while((str=bufferedReader.readLine())!=null){
            System.out.println(str);
            getAudio("E:\\test\\audio\\"+(index++)+".wav","莱欧斯利","中立",str,200,1.0);
        }
    }
}