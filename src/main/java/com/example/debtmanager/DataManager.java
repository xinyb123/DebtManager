package com.example.debtmanager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    // 数据保存在"我的文档"文件夹，不会丢失
    private static final String DATA_FILE = System.getProperty("user.home") + "/Documents/debt_records.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 保存所有欠账记录
    public static void saveData(List<Debt> debtList) {
        try {
            objectMapper.writeValue(new File(DATA_FILE), debtList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 加载所有欠账记录
    public static List<Debt> loadData() {
        try {
            File file = new File(DATA_FILE);
            if (file.exists()) {
                return objectMapper.readValue(file, new TypeReference<List<Debt>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 文件不存在返回空列表
    }
}