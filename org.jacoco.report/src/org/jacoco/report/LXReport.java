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
package org.jacoco.report;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.internal.diff.ClassInfo;
import org.jacoco.core.internal.diff.CodeDiff;
import org.jacoco.core.internal.diff.GitAdapter;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @autor ken
 * @date 2021/9/16
 */
public class LXReport {

    public String title;

    public File executionDataFile;
    public File classesDirectory;
    public File sourceDirectory;
    public File reportDirectory;
    public ExecFileLoader execFileLoader;
    public String username;
    public String password;
    public String baseCommit;
    public String testCommit;
    public File projectDirectory;
    public String testBranch;


    public LXReport(String projectDirectory,String username,String password,String testBranch,String baseCommit,String testCommit,File executionDataFile,String classesDirectory,String sourceDirectory,String reportDirectory) {
        this.projectDirectory = new File(projectDirectory);
        this.title = this.projectDirectory.getName();
        this.executionDataFile = executionDataFile;
        this.classesDirectory = new File(classesDirectory);
        this.sourceDirectory = new File(sourceDirectory);
        this.reportDirectory = new File(reportDirectory);
        this.username = username;
        this.password = password;
        this.testBranch = testBranch;
        try {
            initCommitId(projectDirectory,testCommit,baseCommit);
        }catch (Exception e){

        }

    }
    public void initCommitId(String projectDirectory,String testCommit,String baseCommit) throws GitAPIException {
        /*
        List<ClassInfo> classInfoList1 = CodeDiff.diffTagToTag(projectDirectory, testBranch, testCommit, baseCommit);
        List<ClassInfo> classInfoList2 = CodeDiff.diffTagToTag(projectDirectory, testBranch, baseCommit, testCommit);
        if(classInfoList1.size() >= classInfoList2.size()){
            this.testCommit = testCommit;
            this.baseCommit = baseCommit;
        }else {
            this.testCommit = baseCommit;
            this.baseCommit = testCommit;
        }*/
        GitAdapter gitAdapter = new GitAdapter(projectDirectory);
        GitAdapter.setCredentialsProvider(username, password);
        List<RevCommit> branchRevCommitList = gitAdapter.getBranchRevCommitList(testBranch);
        int newIndex = 0;
        int oldIndex = 1;
        for (int i = 0; i < branchRevCommitList.size(); i++) {
            RevCommit revCommit = branchRevCommitList.get(i);
            if(revCommit.getName().startsWith(testCommit)){
                newIndex = i;
            }
            if(revCommit.getName().startsWith(baseCommit)){
                oldIndex = i;
            }
        }
        if(newIndex > oldIndex){
            this.testCommit = baseCommit;
            this.baseCommit = testCommit;
        }else {
            this.testCommit = testCommit;
            this.baseCommit = baseCommit;
        }
    }

    public synchronized  String generate() throws IOException {
        loadExecutionData();
        IBundleCoverage bundleCoverage = analyzeStructure();
        return createReport(bundleCoverage);
    }
    private void loadExecutionData() throws IOException {
        execFileLoader = new ExecFileLoader();
        execFileLoader.load(executionDataFile);
    }
    private String createReport(final IBundleCoverage bundleCoverage)throws IOException {
        String newReportDir = reportDirectory.getAbsolutePath() + File.separator + System.currentTimeMillis();
        File dir = new File(newReportDir);
        if(!dir.exists()){
            dir.mkdir();
        }
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(dir));
        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),execFileLoader.getExecutionDataStore().getContents());
        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDirectory, "utf-8", 4));
        visitor.visitEnd();
        return newReportDir;
    }

    private IBundleCoverage analyzeStructure() throws IOException {
        GitAdapter.setCredentialsProvider(username, password);
        CoverageBuilder coverageBuilder = new CoverageBuilder(projectDirectory.getAbsolutePath(),testBranch, testCommit, baseCommit);
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
        analyzer.analyzeAll(classesDirectory);
        return coverageBuilder.getBundle(title);
    }
}
