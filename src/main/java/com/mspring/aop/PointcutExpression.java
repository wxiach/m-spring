package com.mspring.aop;

import com.mspring.beans.BeansException;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * 支持 execution(* 类名模式.方法名模式(..))。
 * 类名中的 * 匹配当前一段，.. 匹配任意包层级；方法名中的 * 匹配任意字符。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public class PointcutExpression {

    private static final String PREFIX = "execution(* ";
    private static final String SUFFIX = "(..))";

    private final Pattern classNamePattern;
    private final Pattern methodNamePattern;

    public PointcutExpression(String expression) {
        String body = expression.trim();
        if (!body.startsWith(PREFIX) || !body.endsWith(SUFFIX)) {
            throw new BeansException("非法的切点表达式：" + expression);
        }

        String classAndMethod = body.substring(PREFIX.length(), body.length() - SUFFIX.length());
        int separator = classAndMethod.lastIndexOf('.');
        if (separator <= 0 || separator == classAndMethod.length() - 1) {
            throw new BeansException("非法的切点表达式：" + expression);
        }

        classNamePattern = Pattern.compile(toClassRegex(classAndMethod.substring(0, separator)));
        methodNamePattern = Pattern.compile(toMethodRegex(classAndMethod.substring(separator + 1)));
    }

    /**
     * 匹配 Bean 的实际类型，供创建代理前筛选通知。
     */
    public boolean matches(Class<?> beanClass) {
        return classNamePattern.matcher(beanClass.getName()).matches();
    }

    /**
     * 同时匹配实际 Bean 类型和方法名，继承方法也按实际 Bean 类型判断。
     */
    public boolean matches(Class<?> beanClass, Method method) {
        return matches(beanClass) && methodNamePattern.matcher(method.getName()).matches();
    }

    // ========================================================================
    // 切点表达式转换：通配符转换成正则，其余字符按字面量处理
    // ========================================================================

    private String toClassRegex(String pattern) {
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char character = pattern.charAt(i);
            if (character == '.' && i + 1 < pattern.length() && pattern.charAt(i + 1) == '.') {
                // 两个点作为一个通配符，匹配零层或多层子包。
                regex.append("\\.(?:[^.]+\\.)*");
                i++;
            } else if (character == '*') {
                // 类名中的星号不能跨越包名分隔符。
                regex.append("[^.]*");
            } else {
                regex.append(Pattern.quote(String.valueOf(character)));
            }
        }

        return regex.toString();
    }

    private String toMethodRegex(String pattern) {
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char character = pattern.charAt(i);
            if (character == '*') {
                regex.append(".*");
            } else {
                regex.append(Pattern.quote(String.valueOf(character)));
            }
        }

        return regex.toString();
    }
}
