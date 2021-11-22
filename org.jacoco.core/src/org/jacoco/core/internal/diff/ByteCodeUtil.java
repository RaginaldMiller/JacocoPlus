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
import java.util.Map;

/**
 * @autor ken
 * @date 2021/9/10
 */
public class ByteCodeUtil {
    //[String a, int x, boolean isTrue]
    public static Map<String,String> sourceClassMap = new HashMap<>();
    static {
        sourceClassMap.put("byte","B");
        sourceClassMap.put("char","C");
        sourceClassMap.put("double","D");
        sourceClassMap.put("float","f");
        sourceClassMap.put("int","I");
        sourceClassMap.put("long","L");
        sourceClassMap.put("short","S");
        sourceClassMap.put("boolean","Z");
        sourceClassMap.put("void","V");
        sourceClassMap.put("String","L");
        // 数组和引用类型都是L？ 多个引用类型？ Landroid.view.View
        // 包装类是引用类型,引用类型带参数类名
        // 数组的引用，例如double[][]被标识为：[[D;String[]被标识为：[Ljava.lang.String;
        // (Ljava/util/Map;Ljava/util/List;)V
    }
}
