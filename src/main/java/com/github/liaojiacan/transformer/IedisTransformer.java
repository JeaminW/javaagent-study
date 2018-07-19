package com.github.liaojiacan.transformer;

import com.github.liaojiacan.config.ClassPaths;
import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * 给iedis的加密字符串函数 插入打印代码
 */
public class IedisTransformer implements ClassFileTransformer {

	public IedisTransformer() {
		try {
			ClassPool.getDefault().appendClassPath(ClassPaths.IDEA_LIB);
			ClassPool.getDefault().appendClassPath(ClassPaths.IDEIS_LIB);
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		if(className.startsWith("com/seventh7/widget/iedis")){
			try {
				CtClass clazz = ClassPool.getDefault().makeClass(new ByteArrayInputStream(classfileBuffer));
				CtMethod[] methods = clazz.getDeclaredMethods();
				CtClass string = ClassPool.getDefault().getCtClass(String.class.getName());
				for(CtMethod method :methods){

					if(method.getLongName().startsWith("com.seventh7.widget.iedis.a.p.f")){
						System.out.println("Inject :: SUCCESS!");
						method.insertBefore("if(true){return true;} ");
						continue;
					}

					if(method.getReturnType().equals(string)){
						String name = method.getLongName();
						System.out.println("transform the iedis method:"+name);
						method.insertAfter("System.out.println(\"--------------------\");" +
									" System.out.println(\""+name+"\"); " +
									" System.out.println(java.util.Arrays.toString($args)); " +
									" System.out.println(\"return:\"+$_);");
					}
				}

				return clazz.toBytecode();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (CannotCompileException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}
