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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CodeDiff 结果缓存
 * @autor ken
 * @date 2021/9/23
 */
public class CodeDiffRecord {
    // key branch-branch || commitid-commutid value List<ClassInfo>
    public static Map<String, List<ClassInfo>> codeDiffMap = new HashMap<>();

    public static List<ClassInfo> getCodeDiffResult(String newStr ,String oldStr){
        String key = newStr + "-" + oldStr;
        return codeDiffMap.get(key);
    }

    public static void saveCodeDiffResult(String newStr ,String oldStr,List<ClassInfo> list){
        String key = newStr + "-" + oldStr;
        codeDiffMap.put(key,list);
    }

}
