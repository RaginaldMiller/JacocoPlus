/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    kenlu - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.diff;

import org.jacoco.core.tools.ExecFileLoader;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.*;


/**
 * jacoco覆盖率文件工具类
 * @autor ken
 * @date 2021/9/3
 *  源码和class文件名格式：  构建号-commitid-fenqile-class/source.zip
 *  ec文件名格式：  设备ID-app版本-commitId短-生成时间-jacoco.ec
 */
public class JacocoFileUtils {

    /**
     *  根据版本号获取所有的覆盖率文件
     * @param filesPath 覆盖率文件所在目录
     * @param version app版本号
     * @return 相同version的覆盖率文件List
     */
    public static List<File> getEcFilesByVersion(String filesPath,String version){
        List<File> files = new ArrayList<>();
        File targetDirectory = new File(filesPath);
        File[] targetFiles = targetDirectory.listFiles();
        for (File targetFile : targetFiles) {
            String fileName = targetFile.getName();
            if(fileName.contains("jacoco.ec") && !fileName.startsWith("merge") && fileName.contains(version) )
                files.add(targetFile);
        }
        return files;
    }

    /**
     *  根据commitId获取所有的覆盖率文件
     * @param filesPath 覆盖率文件所在目录
     * @param commitId 代码commitId前八位
     * @return 相同CommitId的覆盖率文件List
     */
    public static List<File> getEcFilesByCommitId(String filesPath,String commitId){
        List<File> files = new ArrayList<>();
        File targetDirectory = new File(filesPath);
        File[] targetFiles = targetDirectory.listFiles();
        for (File targetFile : targetFiles) {
            String fileName = targetFile.getName();
            if(fileName.endsWith("jacoco.ec") && !fileName.startsWith("merge")  && fileName.contains(commitId))
                files.add(targetFile);
        }
        return files;
    }

    /**
     * 通过CommitId获取源码文件夹路径
     * @param fileDir
     * @param commitId
     * @param appName
     * @return
     */
    public static String getClassFilePathByCommitId(String fileDir,String commitId,String appName){
        File unzipFile = null ;
        File targetDirectory = new File(fileDir);
        File[] targetFiles = targetDirectory.listFiles();

        for (File targetFile : targetFiles) {
            String fileName = targetFile.getName();
            if(fileName.contains("class") && fileName.contains(commitId)){
                // 若已有相同的文件夹 不再解压缩
                if(fileName.contains(".zip")){
                    unzipFile = targetFile;
                }else{
                    return targetFile.getAbsolutePath();
                }
            }
        }
        if(unzipFile == null){
            return "";
        }
        return unZipFile(unzipFile,fileDir,commitId,appName);
    }

    public static String getEcFileVersion(File file){
        return  file.getName().split("-")[1];
    }


    // 很关键  所有地方必须统一 不然会出bug 全部用短
    public static String getFileCommitId(File file){
        String fileName = file.getName();
        File parentFile = file.getParentFile();
        if(fileName.endsWith(".zip") ){
//            return fileName.split("-")[1];
            return fileName.split("-")[1].substring(0,8);
        }else{
//            String commitId = fileName.split("-")[2];
//            File[] files = parentFile.listFiles();
//            for (File fi : files) {
//                String name = fi.getName();
//                if(name.contains("zip") && name.contains(commitId)){
//                    commitId = name.split("-")[1];
//                }
//            }
//            return commitId;
            String commitId = fileName.split("-")[2];
            return commitId;
        }
    }

    /**
     *  根据commitId获取源码文件夹路径
     * @return 文件夹路径
     * @desc
     *  创建临时文件夹
     *  解压到临时文件夹内
     *  返回文件路径
     *
     *  用完建议删除
     */
    public static String getSourceFilePathByCommitId(String fileDir,String commitId,String appName){
        File unzipFile = null ;
        File targetDirectory = new File(fileDir);
        File[] targetFiles = targetDirectory.listFiles();

        for (File targetFile : targetFiles) {
            String fileName = targetFile.getName();
            if(fileName.contains("source") && fileName.contains(commitId)){
                // 若已有相同的文件夹 不再解压缩
                if(fileName.contains(".zip")){
                    unzipFile = targetFile;
                }else{
                    return targetFile.getAbsolutePath();
                }
            }
        }
        if(unzipFile == null){
            return "";
        }
        return unZipFile(unzipFile,fileDir,commitId,appName);
    }

    /**
     * 解压ZIP文件
     * @param zipFile 压缩文件
     * @param outputFolder 解压目录
     * @param keyName version 或者 commitId
     * @param appName 应用名称
     * @return
     */
    public static String unZipFile(File zipFile, String outputFolder,String keyName,String appName)  {

        String folderName = appName + "-" + System.currentTimeMillis() + "";
        if(zipFile.getName().contains("class")){
            folderName = appName + "-" + keyName + "-class-" + System.currentTimeMillis();
        }
        if(zipFile.getName().contains("source")){
            folderName = appName + "-" + keyName + "-source-" + System.currentTimeMillis();
        }
        String filesFolderPath = outputFolder + File.separator + folderName;
        File filesFolder = new File(filesFolderPath);
        if (!filesFolder.exists()) {
            filesFolder.mkdirs();
        }
        String cmd = "tar -zxvf " + zipFile.getAbsolutePath() + " -C " + filesFolderPath;
        exec_block(cmd);
        return filesFolderPath;
    }
    public static void exec_block(String cmd) {
        try {
            Process pr = Runtime.getRuntime().exec(cmd);
            printMessage(pr.getInputStream());
            printMessage(pr.getErrorStream());
            pr.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printMessage(final InputStream input) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader bf = new BufferedReader(new InputStreamReader(input));
                String line = null;
                try {
                    while((line=bf.readLine())!=null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public synchronized static String generateReportByLocalCommands(String cliJarPath,String ecFilePath,String sourcePath,String classPath ,String reportPath,String commitId,String appName){
        String reportFolderName = appName + "-" + commitId + "-report" + System.currentTimeMillis();
        String path = reportPath + File.separator + reportFolderName;
        File file = new File(path);
        if(!file.exists()){
            file.mkdir();
        }
        String cmd = "java -jar " + cliJarPath + " report " + ecFilePath + " --sourcefiles " + sourcePath + " --classfiles " + classPath + " --html " + path;
        System.out.println(cmd);
        try {
            Runtime.getRuntime().exec(cmd);
        }catch (Exception e){
            System.out.println("报告生成失败！");
            e.printStackTrace();
            return "";
        }

        return path;
    }

    public static Map<String,List<File>> getAllEcFilesByVersion(String dir,String version){
        Map<String,List<File>> filesMap = new HashMap<>();
        File directory = new File(dir);
        File[] files = directory.listFiles();
        for (File file : files) {
            String name = file.getName();
            if(name.contains("jacoco.ec") && !name.startsWith("merge") && name.split("-")[1].equals(version)){
                String commitId = getFileCommitId(file);
                if(filesMap.containsKey(commitId)){
                    List<File> list = filesMap.get(commitId);
                    list.add(file);
                }else{
                    List<File> list = new ArrayList<>();
                    list.add(file);
                    filesMap.put(commitId,list);
                }
            }
        }
        return filesMap;
    }

    public static String getEcFileAppName(String fileName,String dir){
        // 最多遍历两层
        List<File> zipFiles = getZipFiles(dir);
        String cmid = fileName.split("-")[2];
        for (File file : zipFiles) {
            String name = file.getName();
            if(name.endsWith(".zip") && name.contains(cmid)){
                return name.split("-")[2];
            }
        }
        return "";
    }

    private static List<File> getZipFiles(String dir){
        List<File> list = new ArrayList<>();
        File diretory = new File(dir);
        File[] files = diretory.listFiles();
        for (File file : files) {
            if(file.isFile()){
                if(file.getName().endsWith(".zip")){
                    list.add(file);
                }
            }else {
               list.addAll(getZipFiles(file.getAbsolutePath()));
            }
        }
        return list;
    }
    /**
     * 返回版本最新的合并文件
     * @param type 版本version 提交commitid 设备deviceid
     * @param key 版本 提交 设备 的值
     * @param dir 查找目录
     * @return
     */
    public static File getAppLatestMergeEcFile(String type,String key,String dir){
        File directory = new File(dir);
        File[] files = directory.listFiles();
        for (File file : files) {
            String name = file.getName();
            if(type.equals(LXMerge.MERGE_TYPE_DEVICE) && name.startsWith(LXMerge.MERGE_TYPE_DEVICE) && name.contains(key)){
                return file;
            }
            if(type.equals(LXMerge.MERGE_TYPE_VERSION) && name.startsWith(LXMerge.MERGE_TYPE_VERSION) && name.contains(key)){
                return file;
            }
            if(type.equals(LXMerge.MERGE_TYPE_COMMIT) && name.startsWith(LXMerge.MERGE_TYPE_COMMIT) && name.contains(key)){
                return file;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String filePath = "/Users/lexin/Desktop/jacocofiles/fenqile_app/52-04e4ea5053ce3843d33bff85b8f2cb4bf627014b-fenqile_app-class-develop_CR.zip";
        String descPath = "/Users/lexin/Desktop/jacocowork/fenqile_app";
        unZipFile(new File(filePath),descPath,"04e4ea50","fenqile_app");
    }

}
