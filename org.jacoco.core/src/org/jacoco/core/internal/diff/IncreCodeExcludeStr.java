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

import java.util.ArrayList;
import java.util.List;

/**
 * @autor ken
 * @date 2021/9/7
 */
public class IncreCodeExcludeStr {

    public static List<String> strList = new ArrayList<String>();

    static {
        strList.add("{");
        strList.add("}");
        strList.add("import");
        strList.add("private");
        strList.add("protected");
        strList.add("public");
        strList.add("package");
        strList.add("/*");
        strList.add("*");
        strList.add("private");
        strList.add("@");
        strList.add("else {");
        strList.add("case ");
        strList.add("try");
        strList.add("//");

    }

    /**
     * 判断代码行是否为无效增量行
     * @param str
     * @return true 无效 false 有效
     */
    public static boolean isInvalidStr(String str){
        str = str.trim();
        if(str.isEmpty()){
            return true;
        }
        for (String s : strList) {
            if(str.startsWith(s) || str.endsWith("*/")){
              return true;
            }
        }
        return false;
    }

}
