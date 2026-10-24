package com.mspring.aop;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 切点表达式：execution(* 类名模式.方法名模式(..))；类名中 * 匹配一段、.. 匹配任意深度的包，方法名按正则处理
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class PointcutExpression {

    private static final Pattern EXPRESSION = Pattern.compile("^execution\\(\\S+\\s+(\\S+)\\(\\.\\.\\)\\)$");
    // .. 的占位符（控制字符，不会出现在类名中），避免与单点混淆
    private static final char ANY_DEPTH_PLACEHOLDER = '\u0001';

    private final Pattern classNamePattern;
    private final Pattern methodNamePattern;

    public PointcutExpression(String expression) {
        Matcher matcher = EXPRESSION.matcher(expression.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "非法的切点表达式，应为 execution(* 类名模式.方法名模式(..)) 形式：" + expression);
        }
        String classAndMethod = matcher.group(1);
        int separator = classAndMethod.lastIndexOf('.');
        if (separator <= 0 || separator == classAndMethod.length() - 1) {
            throw new IllegalArgumentException("切点表达式缺少类名模式或方法名模式：" + expression);
        }
        this.classNamePattern = Pattern.compile(toClassRegex(classAndMethod.substring(0, separator)));
        this.methodNamePattern = Pattern.compile(classAndMethod.substring(separator + 1).replace("*", ".*"));
    }

    private static String toClassRegex(String pattern) {
        String prepared = pattern.replace("..", String.valueOf(ANY_DEPTH_PLACEHOLDER));
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < prepared.length(); i++) {
            char c = prepared.charAt(i);
            if (c == ANY_DEPTH_PLACEHOLDER) {
                regex.append(".*");
            } else if (c == '*') {
                regex.append("[^.]*");
            } else if (c == '.') {
                regex.append("\\.");
            } else {
                regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return regex.toString();
    }

    public boolean matches(Class<?> clazz) {
        return classNamePattern.matcher(clazz.getName()).matches();
    }

    public boolean matches(Method method) {
        return matches(method.getDeclaringClass()) && methodNamePattern.matcher(method.getName()).matches();
    }
}
