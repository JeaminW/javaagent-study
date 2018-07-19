package com.github.liaojiacan.demo;

import com.github.liaojiacan.config.ClassPaths;
import javassist.*;

import java.io.IOException;

public class IedisCracker {

	public static void main(String[] args) {
		try {
			ClassPool.getDefault().appendClassPath(ClassPaths.IDEA_LIB);
			ClassPool.getDefault().appendClassPath(ClassPaths.IDEIS_LIB);

			CtClass clazz = ClassPool.getDefault().getCtClass("com.seventh7.widget.iedis.a.p");

			CtMethod[] mds = clazz.getDeclaredMethods();
			for(CtMethod method : mds){
				if(method.getLongName().startsWith("com.seventh7.widget.iedis.a.p.f")){
					System.out.println("Inject :: SUCCESS!");
					try {
						method.insertBefore("if(true){return true;} ");
					} catch (CannotCompileException e) {
						e.printStackTrace();
					}
					continue;
				}
			}

			clazz.writeFile("/tmp/p.class");

		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (CannotCompileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
