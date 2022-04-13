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

import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.StringUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.internal.diff.GitAdapter;
import org.jacoco.core.internal.diff.IncreceCodeRecord;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by liangjing on 2022/2/16.
 */
public class LXUnitCovReport {

    private String title;
    private File executionDataFile;
    private File classesDirectory;
    private File sourceDirectory;
    private File reportDirectory;
    private ExecFileLoader execFileLoader;
    private String username = "deploy";
    private String password = "lexin_V587";
    private String testBranchName;
    private String sourceBranchName;
    private String testCommitId;
    private String sourceCommitId;
    private String gitlabUrl;

    public LXUnitCovReport(String title, String testBranchName, String sourceBranchName, String testCommitId, String sourceCommitId, File executionDataFile, String classesDirectory, String sourceDirectory, String reportDirectory, String gitlabUrl) {
        this.title = title;
        this.executionDataFile = executionDataFile;
        this.classesDirectory = new File(classesDirectory);
        this.sourceDirectory = new File(sourceDirectory);
        this.reportDirectory = new File(reportDirectory);
        this.testBranchName = testBranchName;
        this.sourceBranchName = sourceBranchName;
        this.gitlabUrl = gitlabUrl;
        this.testCommitId = testCommitId;
        this.sourceCommitId = sourceCommitId;
    }

    public synchronized String generate() throws IOException, GitAPIException {
        this.loadExecutionData();
        IBundleCoverage bundleCoverage = this.analyzeStructure();
        return this.createReport(bundleCoverage);
    }

    private void loadExecutionData() throws IOException {
        this.execFileLoader = new ExecFileLoader();
        this.execFileLoader.load(this.executionDataFile);
    }

    private String createReport(IBundleCoverage bundleCoverage) throws IOException {
        String newReportDir = this.reportDirectory.getAbsolutePath() + File.separator + System.currentTimeMillis();
        File dir = new File(newReportDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        HTMLFormatter htmlFormatter = new HTMLFormatter();
        IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(dir));
        visitor.visitInfo(this.execFileLoader.getSessionInfoStore().getInfos(), this.execFileLoader.getExecutionDataStore().getContents());
        visitor.visitBundle(bundleCoverage, getISourceFileLocator());
        visitor.visitEnd();
        return newReportDir;
    }

    private IBundleCoverage analyzeStructure() throws IOException, GitAPIException {
        GitAdapter.setCredentialsProvider(this.username, this.password);
        GitAdapter.cloneSource(this.username, this.password, this.gitlabUrl, this.sourceDirectory.getAbsolutePath(), this.testBranchName);
        GitAdapter gitAdapter = new GitAdapter(this.sourceDirectory.getAbsolutePath());
        // 如果有传commitId的话，则以特性分支的两个commitId做代码比对；否则以特性分支和待比较分支做代码比对
        if (StringUtils.isEmptyOrNull(testCommitId) || StringUtils.isEmptyOrNull(sourceCommitId)) {
            Repository repo = gitAdapter.getRepository();
            Ref localBranchRef = repo.exactRef("refs/heads/" + this.sourceBranchName);
            gitAdapter.checkOutAndPull(localBranchRef, this.sourceBranchName);
            CoverageBuilder coverageBuilder = new CoverageBuilder(this.sourceDirectory.getAbsolutePath(), this.testBranchName, this.sourceBranchName);
            gitAdapter.checkOut(this.testBranchName);
            Analyzer analyzer = new Analyzer(this.execFileLoader.getExecutionDataStore(), coverageBuilder);
            analyzer.analyzeAll(this.classesDirectory);
            return coverageBuilder.getBundle(this.title);
        } else {
            CoverageBuilder coverageBuilder = new CoverageBuilder(this.sourceDirectory.getAbsolutePath(), this.testBranchName, this.testCommitId, this.sourceCommitId);
            gitAdapter.getGit().reset().setMode(ResetCommand.ResetType.HARD).setRef(this.testCommitId).call();
            Analyzer analyzer = new Analyzer(this.execFileLoader.getExecutionDataStore(), coverageBuilder);
            analyzer.analyzeAll(this.classesDirectory);
            return coverageBuilder.getBundle(this.title);
        }
    }

    /**
     * 处理单测多模块的报告生成
     */
    private ISourceFileLocator getISourceFileLocator() {
        MultiSourceFileLocator iSourceFileLocator = new MultiSourceFileLocator(4);
        File[] sourceFiles = this.sourceDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && !pathname.getName().equals(".git");
            }
        });
        if (sourceFiles == null || sourceFiles.length == 0) {
            throw new RuntimeException("工程的source目录为空！");
        }
        for (File file : sourceFiles) {
            iSourceFileLocator.add(new DirectorySourceFileLocator(new File(file.getAbsolutePath() + "/src/main/java"), "utf-8", 4));
        }
        return iSourceFileLocator;
    }


    public static void main(String[] args) throws IOException, GitAPIException {
        String reportPath = null;
        String unitCovPath = "/Users/liangjing/Documents/todorobo_handover";
        String classesDirectory = unitCovPath + "/class";
        String sourceDirectory = unitCovPath + "/source";
        String reportDirectory = unitCovPath + "/report";
        File executionDataFile = new File(unitCovPath + "/jacoco.exec");
//        LXUnitCovReport lxUnitCovReport = new LXUnitCovReport("coverage", "rel_fugailv_1261326", "master", null, null, executionDataFile, classesDirectory, sourceDirectory, reportDirectory, "http://gitlab.fenqile.com/foundation_platform_center/fund_allocate_java.git");
        LXUnitCovReport lxUnitCovReport = new LXUnitCovReport("todorobo_handover", "rel_financial_task_v3_1267346", "master", null, null, executionDataFile, classesDirectory, sourceDirectory, reportDirectory, "http://gitlab.fenqile.com/sys_group/todorobo.git");
        reportPath = lxUnitCovReport.generate();
        int fileTotal = 0;
        for (Map.Entry<String, int[]> fileCoverageEntry : IncreceCodeRecord.classIncreceCoverMap.entrySet()) {
            int[] covers = fileCoverageEntry.getValue();
            if (covers[0] > 0) {
                fileTotal++;
            }
        }
        System.out.println("增量已覆盖行：" + IncreceCodeRecord.totalIncreceCoverLine + "\n");
        System.out.println("增量总行：" + IncreceCodeRecord.totalIncreceLine + "\n");
        System.out.println("全量已覆盖行：" + IncreceCodeRecord.totalCoverLine + "\n");
        System.out.println("全量总行：" + IncreceCodeRecord.totalLine + "\n");
        System.out.println(reportPath + "\n");
        System.out.println(fileTotal);
    }
}
