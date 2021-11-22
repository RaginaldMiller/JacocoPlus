package org.jacoco.examples;

import org.jacoco.core.internal.diff.ClassInfo;
import org.jacoco.core.internal.diff.CodeDiff;
import org.jacoco.core.internal.diff.GitAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @autor ken
 * @date 2021/9/23
 */
public class CodeDiffTest {





    public static void main(String[] args) {

        String gitPath = "/Users/lexin/Desktop/dev/fenqile_app";
        String branchName = "develop_CR";
        String newTag = "65b1313e";
        String oldTag = "04e4ea50";
        GitAdapter.setCredentialsProvider("kenlu","@1990lfkLFK");
        List<ClassInfo> classInfoList1 = CodeDiff.diffTagToTag(gitPath, branchName,newTag , oldTag);
        List<ClassInfo> classInfoList2 = CodeDiff.diffTagToTag(gitPath, branchName,oldTag ,newTag );
        Set<String> classList = new HashSet<String>();
        for (ClassInfo classInfo : classInfoList1) {
            String className =  classInfo.getPackages() + "." + classInfo.getClassName();
            classList.add(className);

        }

        for (ClassInfo info : classInfoList2) {
            String name =  info.getPackages() + "." + info.getClassName();
            if(classList.contains(name)){
                System.out.print("");
            }else {
                System.out.println(name);
            }
        }

    }

}
