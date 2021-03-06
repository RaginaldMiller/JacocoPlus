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

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @autor ken
 * @date 2021/9/7
 */
public class IncreceCodeRecord {
    public static Map<String,int[]> classIncreceCoverMap = new HashMap<String, int[]>();
    public static int totalIncreceLine = 0;
    public static int totalIncreceCoverLine = 0;
    // 免测需求行
    public static Map<String,Integer[]> classNoNeedTestMap = new HashMap<String, Integer[]>();
    public static int totalNoNeedTestLine = 0;
    public static void clear(){
        classIncreceCoverMap.clear();
        totalIncreceLine = 0;
        totalIncreceCoverLine = 0;
        classNoNeedTestMap.clear();
        totalNoNeedTestLine = 0;
    }

    public static void getCodeLineStatus(ClassInfo classInfo, ILine line , String linesrc,int lineNr,String classPath){
        if(isNoNeedTestLine(classPath,lineNr)){
            return;
        }
        if(IncreCodeExcludeStr.isInvalidStr(linesrc)){
            return;
        }
        int[] data = null;
        if(classIncreceCoverMap.containsKey(classPath)){
            data = classIncreceCoverMap.get(classPath);
        }else{
            data = new int[2];
            classIncreceCoverMap.put(classPath,data);
        }
        String type = classInfo.getType();
        if(type.equals("ADD")){
            data[0]+=1;
            totalIncreceLine++;
            if(isCovered(line)){
                data[1] += 1;
                totalIncreceCoverLine++;
                //System.out.println(linesrc);
            }
        }

        List<int[]> addLines = classInfo.getAddLines();
        if(type.equals("REPLACE") && addLines!=null && isInline(addLines,lineNr)){
            data[0]+=1;
            totalIncreceLine++;
            if(isCovered(line)){
                data[1] += 1;
                totalIncreceCoverLine++;
                //System.out.println(linesrc);
            }
        }


    }
    public static boolean isNoNeedTestLine(String classPath,int lineNr){
        Integer[] lines = classNoNeedTestMap.get(classPath);
        if(lines != null){
            for (int line : lines) {
                if(line == lineNr){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCovered(ILine line){
        int status = line.getStatus();
        if((status == ICounter.FULLY_COVERED) || (status == ICounter.PARTLY_COVERED)){
            return true;
        }
        return false;
    }
    public static boolean isInline(List<int[]> lines,int lineNr){
        for (int[] addLine : lines) {
            int startLine = addLine[0];
            int endLine = addLine[addLine.length-1];
            if(lineNr >= startLine && lineNr <= endLine){
                return true;
            }

        }
        return false;
    }


}
