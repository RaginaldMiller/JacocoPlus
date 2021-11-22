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

import java.io.File;
import java.io.IOException;

/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information.
 * 
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class AllCoverageReportGenerator {

	private final String title;

	private final File executionDataFile;
	private final File classesDirectory;
	private final File sourceDirectory;
	private final File reportDirectory;

	private ExecFileLoader execFileLoader;

	/**
	 * Create a new generator based for the given project.
	 *
	 * @param projectDirectory
	 */
	public AllCoverageReportGenerator(final File projectDirectory) {
		this.title = projectDirectory.getName();
		//this.executionDataFile = new File(projectDirectory, "jacoco.exec");
		//this.classesDirectory = new File(projectDirectory, "bin");
		//this.sourceDirectory = new File(projectDirectory, "src");
		//this.reportDirectory = new File(projectDirectory, "coveragereport");

		this.executionDataFile = new File("/Users/lexin/Desktop/mergetest/6.9.0/merged-04e4ea50.exec");
		this.classesDirectory = new File("/Users/lexin/Desktop/mergetest/6.9.0/fenqile-04e4ea50-class-1632383418377/");
		this.sourceDirectory = new File("/Users/lexin/Desktop/mergetest/6.9.0/fenqile-04e4ea50-source-1632383417989/");
		this.reportDirectory = new File("/Users/lexin/Desktop/mergetest/6.9.0/report/");

	}

	/**
	 * Create the report.
	 * 
	 * @throws IOException
	 */
	public void create() throws IOException {

		// Read the jacoco.exec file. Multiple data files could be merged
		// at this point
		loadExecutionData();

		// Run the structure analyzer on a single class folder to build up
		// the coverage model. The process would be similar if your classes
		// were in a jar file. Typically you would create a bundle for each
		// class folder and each jar you want in your report. If you have
		// more than one bundle you will need to add a grouping node to your
		// report
		final IBundleCoverage bundleCoverage = analyzeStructure();

		createReport(bundleCoverage);

	}

	private void createReport(final IBundleCoverage bundleCoverage)
			throws IOException {

		// Create a concrete report visitor based on some supplied
		// configuration. In this case we use the defaults
		final HTMLFormatter htmlFormatter = new HTMLFormatter();
		final IReportVisitor visitor = htmlFormatter
				.createVisitor(new FileMultiReportOutput(reportDirectory));

		// Initialize the report with all of the execution and session
		// information. At this point the report doesn't know about the
		// structure of the report being created
		visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
				execFileLoader.getExecutionDataStore().getContents());

		// Populate the report structure with the bundle coverage information.
		// Call visitGroup if you need groups in your report.
		visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(
				sourceDirectory, "utf-8", 4));

		// Signal end of structure information to allow report to write all
		// information out
		visitor.visitEnd();

	}

	private void loadExecutionData() throws IOException {
		execFileLoader = new ExecFileLoader();
		execFileLoader.load(executionDataFile);
	}

	private IBundleCoverage analyzeStructure() throws IOException {

		GitAdapter.setCredentialsProvider("kenlu", "@1990lfkLFK");
		final CoverageBuilder coverageBuilder = new CoverageBuilder("/Users/lexin/Desktop/dev/fenqile_app","develop_CR","master");
//		final CoverageBuilder coverageBuilder = new CoverageBuilder("/Users/lexin/Desktop/dev/fenqile_app","develop_CR","5cbb713d","04e4ea50");
//		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(
				execFileLoader.getExecutionDataStore(), coverageBuilder);

		analyzer.analyzeAll(classesDirectory);

		return coverageBuilder.getBundle(title);
	}

	/**
	 * Starts the report generation process
	 * 
	 * @param args
	 *            Arguments to the application. This will be the location of the
	 *            eclipse projects that will be used to generate reports for
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
//		for (int i = 0; i < args.length; i++) {
//			final ReportGenerator generator = new ReportGenerator(new File(
//					args[i]));
//			generator.create();
//		}

		final AllCoverageReportGenerator generator = new AllCoverageReportGenerator(new File("/Users/lexin/Desktop/dev/fenqile_app"));
		generator.create();

		float rate = (float)IncreceCodeRecord.totalIncreceCoverLine / IncreceCodeRecord.totalIncreceLine;
		System.out.println("IncreceCodeRecord.totalIncreceCoverLine:" + IncreceCodeRecord.totalIncreceCoverLine + ",IncreceCodeRecord.totalIncreceLine:"+ IncreceCodeRecord.totalIncreceLine + "  "+ rate);


	}

}
