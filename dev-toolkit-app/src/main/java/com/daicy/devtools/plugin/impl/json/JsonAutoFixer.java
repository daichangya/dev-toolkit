package com.daicy.devtools.plugin.impl.json;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON自动修复工具类
 */
public class JsonAutoFixer {
    private final List<String> fixLogs = new ArrayList<>();

    /**
     * 自动修复JSON字符串
     *
     * @param input 输入的JSON字符串
     * @return 修复后的JSON字符串
     */
    public String fix(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        fixLogs.clear();
        String result = input;

        // 0. 处理转义的JSON字符串
        result = handleEscapedJson(result);

        // 1. 修复基本格式问题
        result = fixBasicFormat(result);

        // 2. 修复空字符串值
        result = fixEmptyValues(result);

        // 3. 修复未闭合的引号
        result = fixUnclosedQuotes(result);

        // 4. 修复多余或缺少的逗号
        result = fixCommas(result);

        // 5. 修复未闭合的括号
        result = fixUnclosedBrackets(result);

        // 6. 修复布尔值和null值的格式
        result = fixBooleanAndNull(result);

        // 7. 修复非标准的JSON格式
        result = fixNonStandardFormat(result);

        return result;
    }

    /**
     * 获取修复日志
     *
     * @return 修复操作的日志列表
     */
    public List<String> getFixLogs() {
        return new ArrayList<>(fixLogs);
    }

    /**
     * 是否进行了修复
     *
     * @return 如果进行了任何修复操作返回true，否则返回false
     */
    public boolean hasFixed() {
        return !fixLogs.isEmpty();
    }

    private String fixBasicFormat(String input) {
        String result = input.trim();
        String original = result;
        if (!result.startsWith("{")) {
            result = "{".concat(result);
            fixLogs.add("添加了缺失的开始大括号");
        }
        if (!result.endsWith("}")) {
            result = result.concat("}");
            fixLogs.add("添加了缺失的结束大括号");
        }
        if (!result.equals(original)) {
            fixLogs.add("修复了基本格式");
        }
        return result;
    }

    private String fixUnclosedBrackets(String input) {
        StringBuilder result = new StringBuilder();
        int curlyCount = 0;
        int squareCount = 0;
        boolean fixed = false;
        char[] chars = input.toCharArray();

        // 第一遍扫描，标记需要删除的多余括号
        boolean[] toDelete = new boolean[chars.length];
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '{') {
                curlyCount++;
            } else if (c == '}') {
                if (curlyCount > 0) {
                    curlyCount--;
                } else {
                    toDelete[i] = true;
                    fixed = true;
                }
            } else if (c == '[') {
                squareCount++;
            } else if (c == ']') {
                if (squareCount > 0) {
                    squareCount--;
                } else {
                    toDelete[i] = true;
                    fixed = true;
                }
            }
        }

        // 构建结果，跳过标记为删除的括号
        for (int i = 0; i < chars.length; i++) {
            if (!toDelete[i]) {
                result.append(chars[i]);
            }
        }

        // 添加缺失的闭合括号
        while (curlyCount > 0) {
            result.append('}');
            curlyCount--;
            fixed = true;
        }

        while (squareCount > 0) {
            result.append(']');
            squareCount--;
            fixed = true;
        }

        if (fixed) {
            fixLogs.add("修复了未闭合的括号");
        }

        return result.toString();
    }

    private String fixEmptyValues(String input) {
        String result = input;
        String original = result;
        result = result.replaceAll(":,", ":null,")
                .replaceAll(":\\s*,", ":null,")
                .replaceAll(":\\s*}", ":null}")
                .replaceAll(":\\s*\\[", ":[")
                .replaceAll(":\\s*\\]", ":null]");
        if (!result.equals(original)) {
            fixLogs.add("修复了空值为null");
        }
        return result;
    }

    private String fixUnclosedQuotes(String input) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        boolean fixed = false;
        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (escaped) {
                result.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                result.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                result.append(c);
            } else {
                result.append(c);
                // 如果在字符串中，并且下一个字符不是引号或转义字符，检查是否需要闭合
                if (inString && i == chars.length - 1) {
                    result.append('"');
                    fixed = true;
                    inString = false;
                }
            }
        }

        if (inString) {
            result.append('"');
            fixed = true;
        }

        if (fixed) {
            fixLogs.add("修复了未闭合的引号");
        }

        return result.toString();
    }

    private String fixCommas(String input) {
        String result = input;
        String original = result;
        result = result.replaceAll(",\\s*}", "}")
                .replaceAll(",\\s*\\]", "]");
        if (!result.equals(original)) {
            fixLogs.add("移除了多余的逗号");
        }
        return result;
    }

    private String fixBooleanAndNull(String input) {
        String result = input;
        String original = result;
        result = result.replaceAll("\\bTRUE\\b", "true")
                .replaceAll("\\bFalse\\b", "false")
                .replaceAll("\\bFALSE\\b", "false")
                .replaceAll("\\bTrue\\b", "true")
                .replaceAll("\\bNULL\\b", "null")
                .replaceAll("\\bundefined\\b", "null");
        if (!result.equals(original)) {
            fixLogs.add("修复了布尔值和null值的格式");
        }
        return result;
    }

    private String fixNonStandardFormat(String input) {
        String result = input;
        String original = result;
        
        // 修复单引号为双引号
        result = result.replaceAll("'", "\"");
        
        // 修复没有引号的键
        result = result.replaceAll("([{,])\\s*(\\w+)\\s*:", "$1\"$2\":");
        
        // 修复数字值为字符串
        result = result.replaceAll(":\\s*(\\d+)", ":\"$1\"");
        
        if (!result.equals(original)) {
            fixLogs.add("修复了非标准的JSON格式");
        }
        return result;
    }

    private String handleEscapedJson(String input) {
        String result = input;
        String original = result;

        // 检查是否是转义的JSON字符串
        if (result.contains("\\\"")) {
            // 移除多余的转义
            result = result.replaceAll("\\\\\\\"", "\"");
            
            if (!result.equals(original)) {
                fixLogs.add("处理了转义的JSON字符串");
            }
        }

        return result;
    }
}