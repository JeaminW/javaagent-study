package com.github.decipher;

import com.github.decipher.model.ConfigItem;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import javassist.ClassPool;
import javassist.NotFoundException;
import sun.misc.JarFilter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class Main {

    private static final ClassPool CLASS_POOL = ClassPool.getDefault();

    public static void main(String[] args) throws NotFoundException, ClassNotFoundException,
            IllegalAccessException, InvocationTargetException, IOException {
        Preconditions.checkArgument(args.length == 2,
                                    "Must provider 2 args: <jar-file-path> <class-source-dir>");

        CLASS_POOL.insertClassPath(args[0]);
        addClassPath(args[0]);
        addClassPath("/Applications/IntelliJ IDEA.app/Contents/lib");
        addClassPath("/Users/jeaminw/Library/Application Support/IntelliJIdea2018.1/Iedis/lib");

        ClassFileParser classFileParser = new ClassFileParser(args[1]);
        List<ConfigItem> configItems = classFileParser.parse();
        for (ConfigItem item : configItems) {
            Preconditions.checkState(item.checkIsStatic(), "Only <STATIC> method supported currently");
            try {
                dumpStatic(item.getClazz(), item.getMethod(), item.getList());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private static void dumpStatic(String clazzName, String methodName, List<ConfigItem.Pair> pairList) throws
            ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = Class.forName(clazzName).getDeclaredMethod(methodName, new Class[]{int.class, int.class});
        method.setAccessible(true);
        System.out.println("\n" + clazzName);
        for (ConfigItem.Pair pair : pairList) {
            System.out.println(String.format("[ %d,%d ==> %s ]", pair.getFirst(), pair.getSecond(),
                                             method.invoke(null, new Object[]{pair.getFirst(), pair.getSecond()})));
        }
    }

    private static void addClassPath(String classPath) throws MalformedURLException,
            InvocationTargetException, IllegalAccessException {
        Method addURLMethod = null;
        try {
            addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        } catch (NoSuchMethodException e) {
            Throwables.propagate(e);
        }
        addURLMethod.setAccessible(true);

        File cp = new File(classPath);
        File[] targets = new File[] { cp };
        if (cp.isDirectory()) {
            targets = cp.listFiles(new JarFilter());
        }

        for (File jarFile : targets) {
            addURLMethod.invoke(ClassLoader.getSystemClassLoader(), jarFile.toURI().toURL());
        }
    }
}
