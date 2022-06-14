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

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @autor ken
 * @date 2021/9/10
 */
public class MethodProbesContext {

    // commitId
    private static TransmittableThreadLocal<String> UDID = new TransmittableThreadLocal<>();

    // 增量代码覆盖率统计数据
    private static TransmittableThreadLocal<IncreceCodeRecord> INCRECE_RECORD = new TransmittableThreadLocal<>();


    public static String getUDID() {
        return UDID.get();
    }

    public static void setUDID(String udid) {
        UDID.set(udid);
    }

    public static IncreceCodeRecord getIncreceRecord() {
        return INCRECE_RECORD.get();
    }

    public static void setIncreceRecord(IncreceCodeRecord increceRecord) {
        INCRECE_RECORD.set(increceRecord);
    }
}
