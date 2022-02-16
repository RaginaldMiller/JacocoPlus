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
package org.jacoco.core.data;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.internal.diff.*;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Execution data for a single Java class. While instances are immutable care
 * has to be taken about the probe data array of type <code>boolean[]</code>
 * which can be modified.
 */
public final class ExecutionData {

	private final long id;

	private final String name;

	private final boolean[] probes;

	public String UDID;

	public String getUDID() {
		return UDID;
	}

	public void setUDID(String UDID) {
		this.UDID = UDID;
	}

	/**
	 * Creates a new {@link ExecutionData} object with the given probe data.
	 * 
	 * @param id
	 *            class identifier
	 * @param name
	 *            VM name
	 * @param probes
	 *            probe data
	 */
	public ExecutionData(final long id, final String name,
			final boolean[] probes) {
		this.id = id;
		this.name = name;
		this.probes = probes;
		/*
		if(this.name.equals("com/fenqile/base/BaseActivity")){
			if(this.probes[433]){
				boolean a = this.probes[432];
				boolean a1 = this.probes[433];
				boolean a2 = this.probes[434];
			}
		}
		*/
	}

	/**
	 * Creates a new {@link ExecutionData} object with the given probe data
	 * length. All probes are set to <code>false</code>.
	 * 
	 * @param id
	 *            class identifier
	 * @param name
	 *            VM name
	 * @param probeCount
	 *            probe count
	 */
	public ExecutionData(final long id, final String name, final int probeCount) {
		this.id = id;
		this.name = name;
		this.probes = new boolean[probeCount];
	}

	/**
	 * Return the unique identifier for this class. The identifier is the CRC64
	 * checksum of the raw class file definition.
	 * 
	 * @return class identifier
	 */
	public long getId() {
		return id;
	}

	/**
	 * The VM name of the class.
	 * 
	 * @return VM name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the execution data probes. A value of <code>true</code> indicates
	 * that the corresponding probe was executed.
	 * 
	 * @return probe data
	 */
	public boolean[] getProbes() {
		return probes;
	}

	/**
	 * Sets all probes to <code>false</code>.
	 */
	public void reset() {
		Arrays.fill(probes, false);
	}

	/**
	 * Checks whether any probe has been hit.
	 * 
	 * @return <code>true</code>, if at least one probe has been hit
	 */
	public boolean hasHits() {
		for (final boolean p : probes) {
			if (p) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Merges the given execution data into the probe data of this object. I.e.
	 * a probe entry in this object is marked as executed (<code>true</code>) if
	 * this probe or the corresponding other probe was executed. So the result
	 * is
	 * 
	 * <pre>
	 * A or B
	 * </pre>
	 * 
	 * The probe array of the other object is not modified.
	 * 
	 * @param other
	 *            execution data to merge
	 */
	public void merge(final ExecutionData other) {
		merge(other, true);
	}

	/**
	 * Merges the given execution data into the probe data of this object. A
	 * probe in this object is set to the value of <code>flag</code> if the
	 * corresponding other probe was executed. For <code>flag==true</code> this
	 * corresponds to
	 * 
	 * <pre>
	 * A or B
	 * </pre>
	 * 
	 * For <code>flag==false</code> this can be considered as a subtraction
	 * 
	 * <pre>
	 * A and not B
	 * </pre>
	 * 
	 * The probe array of the other object is not modified.
	 * 
	 * @param other
	 *            execution data to merge
	 * @param flag
	 *            merge mode
	 */
	public void merge(final ExecutionData other, final boolean flag) {
		assertCompatibility(other.getId(), other.getName(),
				other.getProbes().length);
		final boolean[] otherData = other.getProbes();
		for (int i = 0; i < probes.length; i++) {
			if (otherData[i]) {
				probes[i] = flag;
			}
		}
	}

	public void mergeClassByMethod(final ExecutionData other){

		String name = other.getName();
		String currentUDID = MethodProbesContext.getUDID();
		Map<String, int[]> newMethodProMap = MethodProbes.methodProbsMap.get(UDID);
		Map<String, int[]> oldMethodProMap = MethodProbes.methodProbsMap.get(currentUDID);
		// methodProMap 是覆盖率文件所有方法探针数据
		// 先判断当前类在两个map中是否存在
		// 新 老  有 有 ==》 合并
		// 新 老  有 无 ==》 不动 以新文件为主
		// 新 老  无 有 ==》 合并
		// 新 老  无 无 ==》 不动 以新文件为主
		// 合并逻辑： 遍历类下的方法列表，判断方法是否为变更方法，若是，以新文件为主，否则合并未变更的方法探针数组
		boolean oldExist = false;
		if(oldMethodProMap != null && containsShortKey(oldMethodProMap,name)){
			oldExist = true;
		}
		if(oldExist){
			for (Map.Entry<String, int[]> entry : oldMethodProMap.entrySet()) {
				String methodKey = entry.getKey();
				String methodName = methodKey.split("\\.")[1];
				String className = methodKey.split("\\.")[0];
				if(className.equals(name)){

					int[] newMethodProbes = newMethodProMap.get(methodKey);
					int[] oldMethodProbes = oldMethodProMap.get(methodKey);
					/*
					if(methodKey.equals("com/fenqile/base/BaseActivity.getStatusBarHeight.()I")){
						// 遍历this
						// 遍历other
						for (int i = newMethodProbes[0]; i <= newMethodProbes[1]; i++) {
							System.out.println("index:" + i + this.probes[i]);
							if(this.probes[i]){
								System.out.print("");
							}
						}

						for (int i = oldMethodProbes[0]; i <= oldMethodProbes[1]; i++) {
							System.out.println("index:" + i + other.probes[i]);
							if(other.probes[i]){
								System.out.print("");
							}
						}

					}*/
					// 理论上不可能是空 除非analyzeAll方法没有运行完成，导致数据缺失！！！ 深坑
					if(newMethodProbes!=null && !isDiffMethod(methodName,CoverageBuilder.classInfos)){
						//不为空 且 不是变更方法 merge ==》 变更方法。不动
						int newArrayLenght = newMethodProbes[1] - newMethodProbes[0] + 1;
						int oldArrayLenght = oldMethodProbes[1] - oldMethodProbes[0] + 1;
						if(oldMethodProbes != null && newArrayLenght == oldArrayLenght){
							int newStartIndex = newMethodProbes[0];
							int oldStartIndex = oldMethodProbes[0];
							for(int i = 0;i < newArrayLenght ;i++){
								try {
									this.probes[newStartIndex+i] = (this.probes[newStartIndex+i] | other.probes[oldStartIndex + i]);
								}catch (Exception e){
									// TODO 备份原数据 当合并发生异常时，还原数据，跳出当前循环
									System.out.println("当前方法合并发生异常：" + methodKey );
									//e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}



//		for (Map.Entry<String, int[]> entry : methodProMap1.entrySet()) {
//			// class.method.params
//			String methodKey = entry.getKey();
//			String methodName = methodKey.split("\\.")[1];
//			// 变更方法探针不动。合并未变更方法探针
//			if(methodKey.contains(name) && !isDiffMethod(methodName, CoverageBuilder.classInfos)){
//				System.out.println("合并方法:::::::::::::::::::::::::::" + methodKey);
//				// 执行合并操作
//				int[] newProbes = entry.getValue();
//				int[] oldProbes = methodProMap2.get(methodKey);
//				int newArrayLenght = newProbes[1] - newProbes[0] + 1;
//				int oldArrayLenght = oldProbes[1] - oldProbes[0] + 1;
//				if(oldProbes != null && newArrayLenght == oldArrayLenght){
//					int newStartIndex = newProbes[0];
//					int oldStartIndex = oldProbes[0];
//					for(int i = 0;i < newArrayLenght ;i++){
//						this.probes[newStartIndex+i] = (this.probes[newStartIndex+i] | other.probes[oldStartIndex + i]);
//					}
//				}
//			}
//		}


	}
	private boolean containsShortKey(Map<String, int[]> methodProMap,String className){
		for (Map.Entry<String, int[]> entry : methodProMap.entrySet()) {
			if(entry.getKey().contains(className)){
				return true;
			}
		}
		return false;
	}

	private boolean isDiffMethod(String currentMethod, List<ClassInfo> classInfos) {
		if (classInfos== null || classInfos.isEmpty()) {
			return true;
		}
		String currentClassName = name.replaceAll("/",".");
		for (ClassInfo classInfo : classInfos) {
			String className = classInfo.getPackages() + "." + classInfo.getClassName();
			if (currentClassName.equals(className)) {
				for (MethodInfo methodInfo: classInfo.getMethodInfos()) {
					String methodName = methodInfo.getMethodName();
					if (currentMethod.equals(methodName)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Asserts that this execution data object is compatible with the given
	 * parameters. The purpose of this check is to detect a very unlikely class
	 * id collision.
	 * 
	 * @param id
	 *            other class id, must be the same
	 * @param name
	 *            other name, must be equal to this name
	 * @param probecount
	 *            probe data length, must be the same as for this data
	 * @throws IllegalStateException
	 *             if the given parameters do not match this instance
	 */
	public void assertCompatibility(final long id, final String name,
			final int probecount) throws IllegalStateException {
		if (this.id != id) {
			throw new IllegalStateException(format(
					"Different ids (%016x and %016x).", Long.valueOf(this.id),
					Long.valueOf(id)));
		}
		if (!this.name.equals(name)) {
			throw new IllegalStateException(format(
					"Different class names %s and %s for id %016x.", this.name,
					name, Long.valueOf(id)));
		}
		if (this.probes.length != probecount) {
			throw new IllegalStateException(format(
					"Incompatible execution data for class %s with id %016x.",
					name, Long.valueOf(id)));
		}
	}

	@Override
	public String toString() {
		return String.format("ExecutionData[name=%s, id=%016x]", name,
				Long.valueOf(id));
	}

}
