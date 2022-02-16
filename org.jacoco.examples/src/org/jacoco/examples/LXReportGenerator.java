/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.examples;

import org.jacoco.core.internal.diff.JacocoFileUtils;
import org.jacoco.core.internal.diff.LXMerge;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information.
 * 
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class LXReportGenerator {

	static String ecFileDir = "/Users/lexin/Desktop/mergetest/";
	static String appName = "fenqile_app";
	static String gitUserName="kenlu";
	static String gitUserPsw="@1990lfkLFK";
	static String projectPath="/Users/lexin/Desktop/dev/fenqile_app";
	static String baseBranch = "master";
	static String testBranch = "develop_CR";
	static String version = "6.8.0";

	public static void main(String[] args) throws Exception {

		Map<String, List<File>> allEcFilesByVersion = JacocoFileUtils.getAllEcFilesByVersion(ecFileDir, version);
		List<String> list = new ArrayList<String>();

		File oldFile = new File(list.get(0));
		File newFile = new File(list.get(1));
		String mergeFile = merge(newFile ,oldFile);
		for (int i = 2; i < list.size(); i++) {
			File newEcFile = new File(list.get(i));
			mergeFile = merge(newEcFile ,new File(mergeFile));
		}

	}

	public static String merge(File newFile ,File oldFile) throws Exception {
		LXMerge lxMerge = new LXMerge(appName,projectPath,gitUserName,gitUserPsw,baseBranch,testBranch,ecFileDir,oldFile,newFile,"versionmerge","");
		String dir = ecFileDir;
		return  lxMerge.merge(dir);
	}

}
