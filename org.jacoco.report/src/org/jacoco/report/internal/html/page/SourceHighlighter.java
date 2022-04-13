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
package org.jacoco.report.internal.html.page;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.core.internal.diff.ClassInfo;
import org.jacoco.core.internal.diff.IncreceCodeRecord;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.resources.Styles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Locale;

/**
 * Creates a highlighted output of a source file.
 */
final class SourceHighlighter {

	private final Locale locale;

	private String lang;

	/**
	 * Creates a new highlighter with default settings.
	 *
	 * @param locale
	 *            locale for tooltip rendering
	 */
	public SourceHighlighter(final Locale locale) {
		this.locale = locale;
		lang = "java";
	}

	/**
	 * Specifies the source language. This value might be used for syntax
	 * highlighting. Default is "java".
	 *
	 * @param lang
	 *            source language identifier
	 */
	public void setLanguage(final String lang) {
		this.lang = lang;
	}

	/**
	 * Highlights the given source file.
	 *
	 * @param parent
	 *            parent HTML element
	 * @param source
	 *            highlighting information
	 * @param contents
	 *            contents of the source file
	 * @throws IOException
	 *             problems while reading the source file or writing the output
	 */
	public void render(final HTMLElement parent, final ISourceNode source,
			final Reader contents) throws IOException {
		final HTMLElement pre = parent.pre(Styles.SOURCE + " lang-" + lang
				+ " linenums");
		final BufferedReader lineBuffer = new BufferedReader(contents);
		String classPath = ((SourceFileCoverageImpl) source).getPackageName() + "." + source.getName().replaceAll(".java","");
		classPath = classPath.replaceAll("/",".");
		String line;
		int nr = 0;
		while ((line = lineBuffer.readLine()) != null) {
			nr++;
			renderCodeLine(pre, line, source.getLine(nr), nr,classPath);
		}
		//TODO 剔除掉无效行后存在增量行为0的情况
		if(IncreceCodeRecord.classIncreceCoverMap.size() != 0 && IncreceCodeRecord.classIncreceCoverMap.get(classPath) != null){
			System.out.println("类名：" + classPath + "  增量行：" + IncreceCodeRecord.classIncreceCoverMap.get(classPath)[0] + " ,覆盖行：" + IncreceCodeRecord.classIncreceCoverMap.get(classPath)[1]);
		}
	}

	private void renderCodeLine(final HTMLElement pre, final String linesrc,
			final ILine line, final int lineNr, final String classPath) throws IOException {
		if (CoverageBuilder.classInfos == null || CoverageBuilder.classInfos.isEmpty()) {
			//	全量覆盖
			highlight(pre, line, lineNr).text(linesrc);
			pre.text("\n");
		} else {
			//	增量覆盖
			boolean existFlag = true;
			for (ClassInfo classInfo : CoverageBuilder.classInfos) {
				String tClassPath = classInfo.getPackages() + "." + classInfo.getClassName();
				if (classPath.equals(tClassPath)) {
					//	新增的类
					if ("ADD".equalsIgnoreCase(classInfo.getType())) {
						if(IncreceCodeRecord.isNoNeedTestLine(classPath,lineNr)){
							highlight(pre, line, lineNr).text("免测" + linesrc);
						}else{
							highlight(pre, line, lineNr).text("+" + linesrc);
						}
						pre.text("\n");
					} else {
						//	修改的类
						boolean flag = false;
						List<int[]> addLines = classInfo.getAddLines();
						for (int[] ints: addLines) {
							if (ints[0] <= lineNr &&  lineNr <= ints[1]){
								flag = true;
								break;
							}
						}
						if (flag) {
							if(IncreceCodeRecord.isNoNeedTestLine(classPath,lineNr)){
								highlight(pre, line, lineNr).text("免测" + linesrc);
							}else{
								highlight(pre, line, lineNr).text("+" + linesrc);
							}
							pre.text("\n");
						} else {
							highlight(pre, line, lineNr).text(" " + linesrc);
							pre.text("\n");
						}
					}
					// 统计有效增量行
					IncreceCodeRecord.getCodeLineStatus(classInfo ,line,linesrc,lineNr,classPath);

					existFlag = false;
					break;
				}
			}
			if (existFlag) {
				highlight(pre, line, lineNr).text(" " + linesrc);
				pre.text("\n");
			}
		}
	}

	HTMLElement highlight(final HTMLElement pre, final ILine line,
			final int lineNr) throws IOException {
		final String style;
		switch (line.getStatus()) {
		case ICounter.NOT_COVERED:
			style = Styles.NOT_COVERED;
			break;
		case ICounter.FULLY_COVERED:
			style = Styles.FULLY_COVERED;
			break;
		case ICounter.PARTLY_COVERED:
			style = Styles.PARTLY_COVERED;
			break;
		default:
			return pre;
		}

		final String lineId = "L" + Integer.toString(lineNr);
		final ICounter branches = line.getBranchCounter();
		switch (branches.getStatus()) {
		case ICounter.NOT_COVERED:
			return span(pre, lineId, style, Styles.BRANCH_NOT_COVERED,
					"All %2$d branches missed.", branches);
		case ICounter.FULLY_COVERED:
			return span(pre, lineId, style, Styles.BRANCH_FULLY_COVERED,
					"All %2$d branches covered.", branches);
		case ICounter.PARTLY_COVERED:
			return span(pre, lineId, style, Styles.BRANCH_PARTLY_COVERED,
					"%1$d of %2$d branches missed.", branches);
		default:
			return pre.span(style, lineId);
		}
	}

	private HTMLElement span(final HTMLElement parent, final String id,
			final String style1, final String style2, final String title,
			final ICounter branches) throws IOException {
		final HTMLElement span = parent.span(style1 + " " + style2, id);
		final Integer missed = Integer.valueOf(branches.getMissedCount());
		final Integer total = Integer.valueOf(branches.getTotalCount());
		span.attr("title", String.format(locale, title, missed, total));
		return span;
	}

}
