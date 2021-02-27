package com.ecnu.utils;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class Utils {
    // 读取json文件，返回json串
    public static String readJsonFile(String fileName) {
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 递归创建文件夹
    public void makeDirs(String path) {
        File myPath = new File(path);
        if (!myPath.exists()) {
            myPath.mkdirs();
        }
    }

    // 递归删除文件夹下的所有文件
    public void deleteFile(File file) {
        if (file.exists()) {
            if (!file.isFile()) {
                for (File f : file.listFiles()) {
                    deleteFile(f);
                }
            }
        }
        file.delete();
    }

    // 搜索文件
    public static List<File> searchFiles(File folder, final String keyword) {
        List<File> result = new ArrayList<File>();
        if (folder.isFile())
            result.add(folder);

        File[] subFolders = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                if (file.getName().toLowerCase().contains(keyword)) {
                    return true;
                }
                return false;
            }
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isFile()) {
                    // 如果是文件则将文件添加到结果列表中
                    result.add(file);
                } else {
                    // 如果是文件夹，则递归调用本方法，然后把所有的文件加到结果列表中
                    result.addAll(searchFiles(file, keyword));
                }
            }
        }
        return result;
    }

    // 计算统计信息（各个动作的得分等）的第一步
    public double getExpScore(int x, List<Integer> list) {
        int size = list.size();
        double max = list.get(size - 1);
        double mid;
        if (size % 2 == 1) {
            mid = list.get((size - 1) / 2);
        } else {
            mid = (list.get(size / 2) + list.get(size / 2 - 1)) / 2.0;
        }
        return 3.0 * Math.exp((x - mid) / (max - mid) * Math.log(5.0 / 3.0));
    }

    // 计算统计信息（各个动作的得分等）的第二步
    public double getMeanScore(double x, List<Double> list) {
        int size = list.size();
        double min = list.get(0), max = list.get(size - 1);
        double sum = 0;
        for (Double itm : list) {
            sum += itm;
        }
        double avg = sum / size;
        if (x == avg) {
            return 3;
        } else if (x < avg) {
            return 3 - 2.0 * (avg - x) / (avg - min);
        } else {
            return 3 + 2.0 * (x - avg) / (max - avg);
        }
    }
}
