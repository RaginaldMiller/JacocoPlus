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
package org.jacoco.core.internal.flow;

import org.jacoco.core.internal.diff.ClassInfo;
import org.jacoco.core.internal.diff.MethodInfo;
import org.jacoco.core.internal.diff.MethodProbes;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AnalyzerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link org.objectweb.asm.ClassVisitor} that calculates probes for every
 * method.
 */
public class ClassProbesAdapter extends ClassVisitor implements
        IProbeIdGenerator {

    private static final MethodProbesVisitor EMPTY_METHOD_PROBES_VISITOR = new MethodProbesVisitor() {
    };

    private final ClassProbesVisitor cv;

    private final boolean trackFrames;

    private int counter = 0;

    private String name;

    /**
     * Creates a new adapter that delegates to the given visitor.
     *
     * @param cv          instance to delegate to
     * @param trackFrames if <code>true</code> stackmap frames are tracked and provided
     */
    public ClassProbesAdapter(final ClassProbesVisitor cv,
                              final boolean trackFrames) {
        super(InstrSupport.ASM_API_VERSION, cv);
        this.cv = cv;
        this.trackFrames = trackFrames;
    }

    @Override
    public void visit(final int version, final int access, final String name,
                      final String signature, final String superName,
                      final String[] interfaces) {
        this.name = name;
//        if(name.contains("Test")){
//            System.out.println(name);
//        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    // 此处只处理增量方法，所以速度会较快一些，但是在HTMLFormatter类的visitBundle中统计不到准确的全量行以及全量已覆盖行
	/*@Override
	public final MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		final MethodProbesVisitor methodProbes;
		final MethodProbesVisitor mv = cv.visitMethod(access, name, desc,
				signature, exceptions);
        //	增量计算覆盖率
		String currentUDID = MethodProbesContext.getUDID();
		String methodId = this.name + "." + name + "." + desc;

		//boolean isDiffMe = isDiffMethod(name, CoverageBuilder.classInfos);
		boolean isDiffClass = isDiffClass( CoverageBuilder.classInfos);
		int finalIndexStart = counter;
		if (mv !=null && isDiffClass) {
		//if (mv !=null) {
            methodProbes = mv;

//			if(this.name.equals("com/fenqile/base/BaseActivity")){
//				System.out.println("methodId:"+ methodId +"Start============:counter=" + counter);
//			}
		} else {
            // We need to visit the method in any case, otherwise probe ids
            // are not reproducible
            methodProbes = EMPTY_METHOD_PROBES_VISITOR;
        }
		return new MethodSanitizer(null, access, name, desc, signature,
				exceptions) {

			@Override
			public void visitLineNumber(final int line, final Label start) {
				// Here we rely on the usage of the info fields by the tree API. If the
				// labels have been properly used before the info field contains a
				// reference to the LabelNode, otherwise null.

				if (start.info != null) {
					super.visitLineNumber(line, start);
				}

//				if(name.equals("com/fenqile/base/BaseActivity")){
//					System.out.println(line);
//				}

			}

			@Override
			public void visitEnd() {
				super.visitEnd();
				LabelFlowAnalyzer.markLabels(this);
				final MethodProbesAdapter probesAdapter = new MethodProbesAdapter(
						methodProbes, ClassProbesAdapter.this);
				if (trackFrames) {
					final AnalyzerAdapter analyzer = new AnalyzerAdapter(
							ClassProbesAdapter.this.name, access, name, desc,
							probesAdapter);
					probesAdapter.setAnalyzer(analyzer);
					methodProbes.accept(this, analyzer);
				} else {
					methodProbes.accept(this, probesAdapter);
				}

//				if(methodId.startsWith("com/fenqile/base/BaseActivity")){
//					System.out.println("methodId:"+ methodId +"end  ============:counter=" + counter-1);
//				}
				// 保存方法探针结果数组下标数据  accept 之后counter++ 所以下面-1
				if(currentUDID != null && !isAccessMethod(this.name)){
					saveMethodProbesIndex(currentUDID,methodId, finalIndexStart,counter-1);
				}
			}
		};
	}*/

    // 此处处理全量方法，因为需要在HTMLFormatter类的visitBundle中统计全量行以及全量已覆盖行，如果只处理增量方法的话，则统计数目不对
    @Override
    public final MethodVisitor visitMethod(final int access, final String name,
                                           final String desc, final String signature, final String[] exceptions) {
        final MethodProbesVisitor methodProbes;
        final MethodProbesVisitor mv = cv.visitMethod(access, name, desc,
                signature, exceptions);

        if (mv == null) {
            methodProbes = EMPTY_METHOD_PROBES_VISITOR;
        } else {
            methodProbes = mv;
        }
        return new MethodSanitizer(null, access, name, desc, signature,
                exceptions) {
            @Override
            public void visitEnd() {
                super.visitEnd();
                LabelFlowAnalyzer.markLabels(this);
                final MethodProbesAdapter probesAdapter = new MethodProbesAdapter(
                        methodProbes, ClassProbesAdapter.this);
                if (trackFrames) {
                    final AnalyzerAdapter analyzer = new AnalyzerAdapter(
                            ClassProbesAdapter.this.name, access, name, desc,
                            probesAdapter);
                    probesAdapter.setAnalyzer(analyzer);
                    methodProbes.accept(this, analyzer);
                } else {
                    methodProbes.accept(this, probesAdapter);
                }
            }
        };
    }

    public boolean isAccessMethod(String methodName) {
        if (methodName.startsWith("access$")) {
            return true;
        }
        return false;
    }

    public void saveMethodProbesIndex(String currentUDID, String methodId, int indexStart, int indexEnd) {
        Map<String, int[]> probeMap = MethodProbes.methodProbsMap.get(currentUDID);
        if (probeMap == null) {
            Map<String, int[]> newProbeMap = new HashMap<String, int[]>();
            //String key = methodProbes.
            int[] indexData = new int[2];
            indexData[0] = indexStart;
            indexData[1] = indexEnd;
            newProbeMap.put(methodId, indexData);
            MethodProbes.methodProbsMap.put(currentUDID, newProbeMap);
        } else {
            int[] ints = probeMap.get(methodId);

            if (ints == null) {
                int[] indexData = new int[2];
                indexData[0] = indexStart;
                indexData[1] = indexEnd;
                probeMap.put(methodId, indexData);
            } else {
				/*
				if(ints[0]!=indexStart || ints[1]!=indexEnd){
					System.out.println(">>>>>>>>>>>>>>>>>>> 当前方法数组下标数据不为空：" + methodId + " " + ints[0] + "," + ints[1]);
					System.out.println(">>>>>>>>>>>>>>>>>>> 当前方法数组下标新数据为：" + methodId + " " + indexStart + "," + indexEnd);
				}

				if(methodId.equals("com/fenqile/ui/scan/ScanActivity.access$500.(Lcom/fenqile/ui/scan/ScanActivity;)I")){
					System.out.println(">>>>>>>>>>>>>>>>>>> com/fenqile/ui/scan/ScanActivity.access$500.(Lcom/fenqile/ui/scan/ScanActivity;)I：" + methodId + " " + ints[0] + "," + ints[1]);
					System.out.println(">>>>>>>>>>>>>>>>>>> com/fenqile/ui/scan/ScanActivity.access$500.(Lcom/fenqile/ui/scan/ScanActivity;)I：" + methodId + " " + indexStart + "," + indexEnd);
				}
				 */
                int[] indexData = new int[2];
                indexData[0] = indexStart;
                indexData[1] = indexEnd;
                probeMap.put(methodId, indexData);
            }

        }
    }

    @Override
    public void visitEnd() {
        cv.visitTotalProbeCount(counter);
		/*
		if(this.name.equals("com/fenqile/ui/scan/ScanActivity")){
			System.out.println(MethodProbesContext.getUDID() + ":com/fenqile/ui/scan/ScanActivity:" + counter);
		}*/
        super.visitEnd();
    }

    // === IProbeIdGenerator ===

    @Override
    public int nextId() {
        return counter++;
    }

    private boolean isDiffClass(List<ClassInfo> classInfos) {
        if (classInfos == null || classInfos.isEmpty()) {
            return true;
        }
        for (ClassInfo classInfo : classInfos) {
            String currentClassName = name.replaceAll("/", ".");
            String className = classInfo.getPackages() + "." + classInfo.getClassName();
            // 以className开头的包括匿名内部类
            if (currentClassName.startsWith(className)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDiffMethod(String currentMethod, List<ClassInfo> classInfos) {
        if (classInfos == null || classInfos.isEmpty()) {
            return true;
        }
        String currentClassName = name.replaceAll("/", ".");
        for (ClassInfo classInfo : classInfos) {
            String className = classInfo.getPackages() + "." + classInfo.getClassName();
//			if(currentClassName.startsWith(className+"$")){
//				return true;
//			}
            if (currentClassName.equals(className)) {
                for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                    String methodName = methodInfo.getMethodName();
                    if (currentMethod.equals(methodName)) {
                        //TODO 不严谨，方法重载没有排除在外
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
