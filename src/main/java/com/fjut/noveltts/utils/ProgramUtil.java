package com.fjut.noveltts.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fjut.noveltts.entity.SpeakerInfo;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

//用于读写txt文件中写入配置
public class ProgramUtil {
    private static String path="./config.txt";
    //向配置文件中添加配置
    public static void addConfig(Map<String, String> map) {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(path));
            properties.putAll(map);
            properties.store(new FileWriter(path), null);
        } catch (IOException e) {
            System.err.println("addConfig=>"+e);
        }
    }
    //向配置文件中添加配置
    public static void addConfig(String key,String value) {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(path));
            properties.setProperty(key,value);
            properties.store(new FileWriter(path), null);
        } catch (IOException e) {
            System.err.println("addConfig=>"+e);
        }
    }

    //返回配置文件中的所有配置
    public static Map<String,String> getconfig() {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(path));
            Map<String, String> stringMap = new HashMap<>();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                stringMap.put(entry.getKey().toString(), entry.getValue().toString());
            }
            return stringMap;
        } catch (IOException e) {
            System.err.println("getconfig=>"+e);
        }
        return null;
    }

    //返回配置文件中所有的角色语音、情感配置
    public static Map<String, String> getCharacterConfig() {
        Map<String, String> map = getconfig();
        if(map!=null)
            map.entrySet().removeIf(entry -> !entry.getValue().contains("@&"));
        return map;
    }

    //从配置文件中，通过key得到value
    public static String getConfigByKey(String key) {
        Map<String, String> config = getconfig();
        if(config==null||config.get(key)==null)
            return null;
        return config.get(key);
    }

    //得到角色及角色情感列表
    public static Map<String, List<String>> getCharacterList() {
        try {
            String url = ProgramUtil.getConfigByKey("url");
            if(url==null||url.isEmpty())
                throw new IOException("未配置url");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url+"character_list"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<String>> map = mapper.readValue(json, Map.class);
            return map;
        } catch (Exception e) {
            System.err.println("getCharacterList=>"+e);
        }
        return null;
    }


    public static void deleteConfig(String speaker) {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(path));
            properties.remove(speaker);
            properties.store(new FileWriter(path), null);
        } catch (IOException e) {
            System.err.println("getconfig=>"+e);
        }
    }
}
