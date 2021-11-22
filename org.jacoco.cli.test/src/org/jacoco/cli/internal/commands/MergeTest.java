/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.jacoco.cli.internal.CommandTestBase;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.internal.diff.JacocoFileUtils;
import org.jacoco.core.tools.ExecFileLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link Merge}.
 */
public class MergeTest extends CommandTestBase {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void should_print_usage_when_no_options_are_given()
			throws Exception {
		execute("merge");

		assertFailure();
		assertContains("\"--destfile\"", err);
		assertContains("java -jar jacococli.jar merge [<execfiles> ...]", err);
	}

	@Test
	public void should_print_warning_when_no_exec_files_are_provided()
			throws Exception {
		File dest = new File(tmp.getRoot(), "merged.exec");
		execute("merge", "--destfile", dest.getAbsolutePath());

		assertOk();
		assertContains("[WARN] No execution data files provided.", out);
		Set<String> names = loadExecFile(dest);
		assertEquals(Collections.emptySet(), names);
	}

	@Test
	public void should_merge_exec_files() throws Exception {
		ArrayList<String> argsList = new ArrayList<String>();
		String commitId = "61184da6b";
		String appName = "fenqile";
		String ecFileFolder = "/Users/lexin/Desktop/jacoco-develop/jacocofiles/fenqile_app";
		String outputFolder = "/Users/lexin/Desktop/";
		File dest = new File(outputFolder, "merged-61184da6b.exec");
		argsList.add("merge");
		argsList.add("--destfile");
		argsList.add(dest.getAbsolutePath());
		// add files
		List<File> ecFiles = JacocoFileUtils.getEcFilesByCommitId(ecFileFolder, commitId);
		for (File ecFile : ecFiles) {
			argsList.add(ecFile.getAbsolutePath());
		}
		String[] args = new String[argsList.size()];
		for (int i = 0; i < args.length; i++) {
			args[i] = argsList.get(i);
		}
		execute(args);


//		String sourceFilePath = "/Users/lexin/Desktop/mergetest/6.9.0/52-04e4ea5053ce3843d33bff85b8f2cb4bf627014b-fenqile-class.zip";
//		String classFilePath = "/Users/lexin/Desktop/mergetest/6.9.0/52-04e4ea5053ce3843d33bff85b8f2cb4bf627014b-fenqile-source.zip";
//
//		String sourceFilePathByCommitId = JacocoFileUtils.getSourceFilePathByCommitId(ecFileFolder, commitId, appName);
//		String classFilePathByCommitId = JacocoFileUtils.getClassFilePathByCommitId(ecFileFolder, commitId, appName);


	}



	@Test
	public void should_merge_exec_files2() throws Exception {
		File a = new File("/Users/lexin/Desktop/mergetest/c2e6648f82721495-6.8.0-3ea75184-20210915183339-jacoco.ec");
		File b = new File("/Users/lexin/Desktop/mergetest/c2e6648f82721495-6.8.0-3ea75184-20210909170208-jacoco.ec");
		File dest = new File("/Users/lexin/Desktop/mergetest/", "merged122222212222.ec");

		execute("merge", "--destfile", dest.getAbsolutePath(),a.getAbsolutePath(), b.getAbsolutePath());
//
//		assertOk();
//		Set<String> names = loadExecFile(dest);
		//assertEquals(new HashSet<String>(Arrays.asList("a", "b")), names);
	}



	private File createExecFile(String name) throws IOException {
		File file = new File(tmp.getRoot(), name + ".exec");
		final FileOutputStream execout = new FileOutputStream(file);
		ExecutionDataWriter writer = new ExecutionDataWriter(execout);
		writer.visitClassExecution(new ExecutionData(name.hashCode(), name,
				new boolean[] { true }));
		execout.close();
		return file;
	}

	private Set<String> loadExecFile(File file) throws IOException {
		ExecFileLoader loader = new ExecFileLoader();
		loader.load(file);
		Set<String> names = new HashSet<String>();
		for (ExecutionData d : loader.getExecutionDataStore().getContents()) {
			names.add(d.getName());
		}
		return names;
	}

}
