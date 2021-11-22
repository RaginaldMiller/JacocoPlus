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
public class CommitIdContext {

    // commitId
    private static TransmittableThreadLocal<String> commitId = new TransmittableThreadLocal<>();

    public static String getCommitId() {
        return commitId.get();
    }

    public static void setCommitId(String cid) {
        commitId.set(cid);
    }

}
