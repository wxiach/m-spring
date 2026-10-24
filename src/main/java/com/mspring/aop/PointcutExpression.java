package com.mspring.aop;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 切点表达式：execution(* 类名模式.方法名模式(..))；类名中 * 匹配一段、.. 匹配任意深度的包，方法名按正则处理
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class PointcutExpression {

    private static final String PREFIX = "execution(* ";
    private static final String SUFFIX = "(..))";

    private final Pattern classNamePattern;
    private final Pattern methodNamePattern;

    public PointcutExpression(String expression) {
        String body = expression.trim();
        if (!body.startsWith(PREFIX) || !body.endsWith(SUFFIX)) {
            throw new IllegalArgumentException("非法的切点表达式：" + expression);
        }
        String classAndMethod = body.substring(PREFIX.length(), body.length() - SUFFIX.length());
        int separator = classAndMethod.lastIndexOf('.');
        if (separator <= 0 || separator == classAndMethod.length() - 1) {
            throw new IllegalArgumentException("非法的切点表达式：" + expression);
        }
        this.classNamePattern = Pattern.compile(toClassRegex(classAndMethod.substring(0, separator)));
        this.methodNamePattern = Pattern.compile(classAndMethod.substring(separator + 1).replace("*", ".*"));
    }

    // .. 切段，段间用 .* 连接；段内 . 是字面点、* 匹配一段
    private static String toClassRegex(String pattern) {
        return Arrays.stream(pattern.split("\\.\\.", -1))
                .map(segment -> segment.replace("$", "\\$").replace(".", "\\.").replace("*", "[^.]*"))
                .collect(Collectors.joining(".*"));
    }

    public boolean matches(Class<?> clazz) {
        return classNamePattern.matcher(clazz.getName()).matches();
    }

    public boolean matches(Method method) {
        return matches(method.getDeclaringClass()) && methodNamePattern.matcher(method.getName()).matches();
    }
}
