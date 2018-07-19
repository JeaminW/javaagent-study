package com.github.decipher;

import com.alibaba.fastjson.JSON;
import com.github.decipher.model.ConfigItem;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassFileParser {

    public static final Charset DEFAULT_FILE_CHARSET = Charset.forName("UTF-8");
    public static final Pattern METHOD_PATTERN = Pattern.compile("(static)?\\s*String\\s+(\\w+)\\s*\\([^,();]*?int\\s+\\w+\\s*\\,[^,();]*?int\\s+\\w+\\s*\\)");
    public static final Pattern CLASS_PATTERN = Pattern.compile("public(\\s+\\w+)*?\\s+class(\\s+\\w+)*?\\s+(\\w+)");

    private final String classDir;

    public ClassFileParser(String classDir) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(classDir), "<classDir> couldn't be null");

        this.classDir = classDir;
    }

    public List<ConfigItem> parse() throws IOException {
        File parentDir = new File(classDir);
        Path parentPath = parentDir.toPath();
        Preconditions.checkState(parentDir.isDirectory(), "<%s> must be a directory", classDir);

        Deque<Iterator<File>> fileStack = new LinkedList<>();
        File[] topFiles = parentDir.listFiles();
        Preconditions.checkState(Objects.nonNull(topFiles) && topFiles.length > 0,
                                 "<%s> couldn't be empty", classDir);

        List<ConfigItem> items = new LinkedList<>();
        fileStack.push(Arrays.asList(topFiles).iterator());
        while (!fileStack.isEmpty()) {
            Iterator<File> fileIt = fileStack.peek();
            if (!fileIt.hasNext()) {
                fileStack.pop();
                continue;
            }

            File file = fileIt.next();
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (Objects.nonNull(files) && files.length > 0) {
                    fileStack.push(Arrays.asList(files).iterator());
                }
            } else if (file.getName().endsWith(".java")) {
                // handle java file
                Path relativePath = parentPath.relativize(file.toPath());
                items.addAll(createConfigItem(file.getPath(), relativePath));
            }
        }

        return items;
    }

    private List<ConfigItem> createConfigItem(String filePath, Path relativePath) throws IOException {
        List<ConfigItem> items = new LinkedList<>();
        final String content = Files.toString(new File(filePath), DEFAULT_FILE_CHARSET);
        final String className = pathToClassName(relativePath.toString(), content);

        Matcher matcher = METHOD_PATTERN.matcher(content);
        while (matcher.find()) {
            ConfigItem item = new ConfigItem();
            item.setClazz(className);
            item.setType("static".equals(matcher.group(1)) ? ConfigItem.STATIC_TYPE : ConfigItem.MEMBER_TYPE);
            item.setMethod(matcher.group(2));
            item.setList(createInvokePair(content, item.getMethod()));

            items.add(item);
            if (item.getList().isEmpty()) {
                System.out.println(String.format("<ConfigItem.list> is empty\n%s", JSON.toJSONString(item, true)));
            }
        }

        return items;
    }

    private List<ConfigItem.Pair> createInvokePair(final String content, final String methodName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(content), "<content> couldn't be null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(methodName), "<methodName> couldn't be null");

        List<ConfigItem.Pair> pairs = new LinkedList<>();
        Pattern pattern = Pattern.compile(String.format("\\W+%s\\s*\\(\\s*(-?\\d+)\\s*\\,\\s*(-?\\d+)\\s*\\)", methodName));
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            int first = Integer.parseInt(matcher.group(1));
            int second = Integer.parseInt(matcher.group(2));

            pairs.add(new ConfigItem.Pair(first, second));
        }

        return pairs;
    }

    private static String pathToClassName(String packagePath, final String content) {
        if (packagePath.endsWith(".java")) {
            packagePath = packagePath.substring(0, packagePath.length()-5);
        }

        if (!Strings.isNullOrEmpty(content)) {
            Matcher matcher = CLASS_PATTERN.matcher(content);
            if (matcher.find()) {
                final String realShortClassName = matcher.group(3);
                int lastDot = packagePath.lastIndexOf(File.separatorChar);
                packagePath = packagePath.substring(0, lastDot+1).concat(realShortClassName);
            }
        }

        return packagePath.replace(File.separatorChar, '.');
    }
}
