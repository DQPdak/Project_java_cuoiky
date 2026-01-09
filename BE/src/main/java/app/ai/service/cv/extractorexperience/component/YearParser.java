package app.ai.service.cv.extractorexperience.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class YearParser {
    // Pattern tìm số + chữ "năm" hoặc "years"
    private static final Pattern PATTERN = Pattern.compile("(\\d+)\\s*(?:\\+)?\\s*(?:years?|năm)", Pattern.CASE_INSENSITIVE);

    public int parse(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int maxYears = 0;
        while (matcher.find()) {
            try {
                int years = Integer.parseInt(matcher.group(1));
                maxYears = Math.max(maxYears, years);
            } catch (NumberFormatException ignored) {}
        }
        return maxYears;
    }
}

