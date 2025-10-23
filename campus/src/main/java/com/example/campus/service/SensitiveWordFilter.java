package com.example.campus.service;

import com.example.campus.config.ModerationProperties;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SensitiveWordFilter {

    private final ModerationProperties properties;

    public SensitiveWordFilter(ModerationProperties properties) {
        this.properties = properties;
    }

    public Result sanitize(String text) {
        if (!StringUtils.hasText(text)) {
            return new Result("", false);
        }
        String sanitized = text;
        boolean flagged = false;
        List<String> words = properties.getWords();
        if (words != null) {
            for (String raw : words) {
                if (!StringUtils.hasText(raw)) {
                    continue;
                }
                String patternText = Pattern.quote(raw.trim());
                Pattern pattern = Pattern.compile(patternText, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                Matcher matcher = pattern.matcher(sanitized);
                StringBuffer buffer = new StringBuffer();
                while (matcher.find()) {
                    flagged = true;
                    String replacement = mask(raw.length());
                    matcher.appendReplacement(buffer, replacement);
                }
                matcher.appendTail(buffer);
                sanitized = buffer.toString();
            }
        }
        return new Result(sanitized, flagged);
    }

    private String mask(int length) {
        int actual = Math.max(length, 3);
        return "*".repeat(actual);
    }

    public record Result(String content, boolean flagged) {
    }
}
