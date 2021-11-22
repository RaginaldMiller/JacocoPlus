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
package org.jacoco.core.internal.diff;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.util.Map;


public class AnalyzeClassMetodProbe {


	private File executionDataFile;
	private File classesDirectory;
	private ExecFileLoader execFileLoader;
	private String gitUserName;
	private String gitUserPsw;
	private String projectPath;
	private String baseBranch;
	private String testBranch;
	private String oldCommitId;
	private String newCommitId;
	private CoverageBuilder coverageBuilder;

	public AnalyzeClassMetodProbe(File ecFile, File classDir, String gitUserName, String gitUserPsw, String projectPath, String baseBranch, String testBranch, String oldCommitId, String newCommitId) {
		this.executionDataFile = ecFile;
		this.classesDirectory = classDir;
		this.gitUserName = gitUserName;
		this.gitUserPsw = gitUserPsw;
		this.projectPath = projectPath;
		this.baseBranch = baseBranch;
		this.testBranch = testBranch;
		this.newCommitId = newCommitId;
		this.oldCommitId = oldCommitId;
//		coverageBuilder = new CoverageBuilder(projectPath,testBranch,baseBranch);
		coverageBuilder = new CoverageBuilder(projectPath,testBranch,newCommitId,oldCommitId);
	}

	public void analyze() throws IOException {
		loadExecutionData();
		final IBundleCoverage bundleCoverage = analyzeStructure();
	}

	private void loadExecutionData() throws IOException {
		execFileLoader = new ExecFileLoader();
		execFileLoader.load(executionDataFile);
	}

	private IBundleCoverage analyzeStructure() throws IOException {
		GitAdapter.setCredentialsProvider(gitUserName, gitUserPsw);
		final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
		analyzer.analyzeAll(classesDirectory);
		File file = new File(projectPath);
		return coverageBuilder.getBundle(file.getName());
	}

}
