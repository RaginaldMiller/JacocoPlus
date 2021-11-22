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

import java.io.File;
import java.io.IOException;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.internal.diff.GitAdapter;
import org.jacoco.core.internal.diff.IncreceCodeRecord;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information.
 * 
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class ReportGenerator {

	private final String title;
	private final File executionDataFile;
	private final File classesDirectory;
	private final File sourceDirectory;
	private final File reportDirectory;
	private ExecFileLoader execFileLoader;

	public ReportGenerator(final File projectDirectory) {
		this.title = projectDirectory.getName();
//		this.executionDataFile = new File("/Users/lexin/Desktop/mergetest/6.9.0/merged-04e4ea50.exec");
		this.executionDataFile = new File("/Users/lexin/Desktop/test/6.10.1/merged-61184da6.exec");
//		this.classesDirectory = new File("/Users/lexin/Desktop/mergetest/6.9.0/fenqile-04e4ea50-class-1633686258505/");
		this.classesDirectory = new File("/Users/lexin/Desktop/test/class/");
//		this.sourceDirectory = new File("/Users/lexin/Desktop/mergetest/6.9.0/fenqile-04e4ea50-source-1633686257266/");
		this.sourceDirectory = new File("/Users/lexin/Desktop/test/source/");
		this.reportDirectory = new File("/Users/lexin/Desktop/test/report1/");
	}

	public void create() throws IOException {
		loadExecutionData();
		final IBundleCoverage bundleCoverage = analyzeStructure();
		createReport(bundleCoverage);
	}

	private void createReport(final IBundleCoverage bundleCoverage)throws IOException {
		final HTMLFormatter htmlFormatter = new HTMLFormatter();
		final IReportVisitor visitor = htmlFormatter
				.createVisitor(new FileMultiReportOutput(reportDirectory));
		visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
				execFileLoader.getExecutionDataStore().getContents());
		visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(
				sourceDirectory, "utf-8", 4));
		visitor.visitEnd();
	}

	private void loadExecutionData() throws IOException {
		execFileLoader = new ExecFileLoader();
		execFileLoader.load(executionDataFile);
	}

	private IBundleCoverage analyzeStructure() throws IOException {

		GitAdapter.setCredentialsProvider("kenlu", "@1990LFKlfk");
		final CoverageBuilder coverageBuilder = new CoverageBuilder("/Users/lexin/Desktop/dev/fenqile_app","develop_CR","61184da6b","9dc7ec4d");
//		CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(
				execFileLoader.getExecutionDataStore(), coverageBuilder);
		analyzer.analyzeAll(classesDirectory);
		return coverageBuilder.getBundle(title);
	}

	public static void main(final String[] args) throws IOException {
		final ReportGenerator generator = new ReportGenerator(new File("/Users/lexin/Desktop/dev/fenqile_app"));
		generator.create();

		float rate = (float)IncreceCodeRecord.totalIncreceCoverLine / IncreceCodeRecord.totalIncreceLine;
		System.out.println("IncreceCodeRecord.totalIncreceCoverLine:" + IncreceCodeRecord.totalIncreceCoverLine + ",IncreceCodeRecord.totalIncreceLine:"+ IncreceCodeRecord.totalIncreceLine + "  "+ rate);


	}

}
