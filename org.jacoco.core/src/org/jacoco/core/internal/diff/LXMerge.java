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

import org.eclipse.jgit.api.errors.GitAPIException;
import org.jacoco.core.tools.ExecFileLoader;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information.
 * 
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class LXMerge {

	public static final String MERGE_TYPE_VERSION = "versionmerge";
	public static final String MERGE_TYPE_COMMIT = "commitmerge";
	public static final String MERGE_TYPE_DEVICE = "devicemerge";

	public String appName;
	public String baseBranch;
	public String testBranch;
	public GitAdapter gitAdapter;
	public String ecFileDir;
	public File oldEcFile;
	public File newEcFile;
	public String oldCommitId;
	public String newCommitId;
	public String gitProjectLocalPath;
	public String gitUserName;
	public String gitUserPsw;
	public String mergeType;

	public LXMerge(){}
	// mergeType versionmerge commitmerge decvicemerge
	public LXMerge(String appName,String gitProjectLocalPath,String gitUserName,String gitUserPsw,String baseBranch,String testBranch,String ecFileDir,File oldEcFile,File newEcFile,String mergeType){
		this.gitProjectLocalPath = gitProjectLocalPath;
		this.mergeType = mergeType;
		this.testBranch = testBranch;
		this.baseBranch = baseBranch;
		GitAdapter.setCredentialsProvider(gitUserName, gitUserPsw);
		this.gitUserName = gitUserName;
		this.gitUserPsw = gitUserPsw;
		gitAdapter = new GitAdapter(gitProjectLocalPath);
		this.oldEcFile = oldEcFile;
		this.newEcFile = newEcFile;
		this.appName = appName;
		this.ecFileDir = ecFileDir;
		try {
			switchFile();
		} catch (GitAPIException e) {
			e.printStackTrace();
			System.out.println("比较新老ec文件commitId出错！");
			return;
		}
	}

	// 根据commitId比较文件new old
	private void switchFile() throws GitAPIException {
		String oldFileCommitId = JacocoFileUtils.getFileCommitId(oldEcFile);
		String newFileCommitId = JacocoFileUtils.getFileCommitId(newEcFile);
		if(oldFileCommitId.equals(newFileCommitId)){
			this.oldCommitId = newFileCommitId;
			this.newCommitId = oldFileCommitId;
			return;
		}
		List<ClassInfo> classInfoList1 = CodeDiff.diffTagToTag(gitProjectLocalPath, testBranch, newFileCommitId, oldFileCommitId);
		List<ClassInfo> classInfoList2 = CodeDiff.diffTagToTag(gitProjectLocalPath, testBranch, oldFileCommitId, newFileCommitId);
		//if(gitAdapter.compareCommitId(this.testBranch,oldFileCommitId,newFileCommitId)){
		if(classInfoList1.size() < classInfoList2.size()){
			File temp = oldEcFile;
			oldEcFile = newEcFile;
			newEcFile = temp;
			this.oldCommitId = newFileCommitId;
			this.newCommitId = oldFileCommitId;
		}else{
			this.oldCommitId = oldFileCommitId;
			this.newCommitId = newFileCommitId;
		}
	}
	// analyzeAll 拿到变更类的所有方法探针数组起止坐标
	// loadExecutionData merge
	// 返回合并后的ec文件  格式 merge-version-commitId-time-jacoco.ec
	public String mergeDiffCommitIdEcFiles(String desDir) throws Exception {
		analyzeEcFile(newEcFile);
		analyzeEcFile(oldEcFile);
		// 根据commitid再传file参数值
		ExecFileLoader fileLoader = new ExecFileLoader();
		// PS：先load新版本数据，再load旧版本数据
		CommitIdContext.setCommitId(newCommitId);
		fileLoader.load(newEcFile);
		CommitIdContext.setCommitId(oldCommitId);
		fileLoader.load(oldEcFile);
		String version = JacocoFileUtils.getEcFileVersion(newEcFile);
		String shortCommitId = newEcFile.getName().split("-")[2];
		String mergeFileName = mergeType +"-" + version + "-" + shortCommitId + "-" + System.currentTimeMillis() + "-jacoco.ec";
		File destinFile = new File(desDir,mergeFileName);
		fileLoader.save(destinFile,true);
		return destinFile.getAbsolutePath();
	}

	public synchronized void analyzeEcFile(File file) throws IOException {
		String commitId = JacocoFileUtils.getFileCommitId(file);
		CommitIdContext.setCommitId(commitId);
		String classFilePath = JacocoFileUtils.getClassFilePathByCommitId(ecFileDir, commitId, appName);
		AnalyzeClassMetodProbe analyzeClassMetodProbe = new AnalyzeClassMetodProbe(file,new File(classFilePath),gitUserName,gitUserPsw,gitProjectLocalPath,baseBranch,testBranch,oldCommitId,newCommitId);
		analyzeClassMetodProbe.analyze();
	}

	/**
	 * 合并相同commitId的ec文件列表
	 * @param list 相同commitId的ec文件列表
	 * @param desDir 合并文件输出目录
	 * @return 合并文件完整路径
	 * @throws IOException
	 */
	public String mergeSameCommitIdFiles(List<File> list,String desDir) throws IOException {
		ExecFileLoader fileLoader = new ExecFileLoader();
		File ecFile1 = list.get(0);
		String commitId = ecFile1.getName().split("-")[2];
		String version = JacocoFileUtils.getEcFileVersion(ecFile1);
		CommitIdContext.setCommitId(commitId);
		for (File file : list) {
			String name = file.getName();
			if(!name.endsWith(".ec")){
				continue;
			}
			fileLoader.load(file);
		}
		String mergeFileName = mergeType + "-" + version + "-" + commitId + "-" + System.currentTimeMillis() + "-jacoco.ec";
		File destinFile = new File(desDir,mergeFileName);
		fileLoader.save(destinFile,true);
		return destinFile.getAbsolutePath();
	}

	public String merge(String ouputDir) throws Exception {
		if(oldCommitId.equals(newCommitId)){
			List<File> list = new ArrayList<>();
			list.add(newEcFile);
			list.add(oldEcFile);
			return mergeSameCommitIdFiles(list,ouputDir);
		}else {
			return mergeDiffCommitIdEcFiles(ouputDir);
		}

	}
	public static void main( String[] args) throws Exception {

		String gitUserName = "kenlu";
		String gitUserPsw = "@1990LFKlfk";
		String gitProjectLocalPath = "/Users/lexin/Desktop/dev/fenqile_app";
		String baseBranch="master";
		String testBranch="develop_CR";
		String ecFiledir = "/Users/lexin/Desktop/test/6.10.1/";
		String appName = "fenqile_app";
		String outputDir = "/Users/lexin/Desktop/test/6.10.1/";
		File b = new File(ecFiledir,"4907349a865d7285-6.7.0-c8c97f24-20210903102659-jacoco.ec");
		File c = new File(ecFiledir,"4907349a865d7285-6.7.0-c8c97f24-20210903102659-jacoco.ec");
		String oldCommitId = "9dc7ec4d";
		String newCommitId = "61184da6b";
		String classFilePath = "/Users/lexin/Desktop/test/class/";

		LXMerge lxMerge = new LXMerge("fenqile_app",gitProjectLocalPath,gitUserName,gitUserPsw,baseBranch,testBranch,ecFiledir,b,c,LXMerge.MERGE_TYPE_VERSION);
		lxMerge.merge(outputDir);


	}

}
