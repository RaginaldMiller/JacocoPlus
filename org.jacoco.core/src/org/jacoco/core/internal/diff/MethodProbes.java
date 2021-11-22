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
public class MethodProbes {
    // commitId,methodDataMap<methodId,probes[]>
    public static Map<String ,Map<String,int[]>> methodProbsMap = new HashMap<String ,Map<String,int[]>>();

}
