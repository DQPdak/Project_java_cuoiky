package app.ai.service.cv.extractorexperience.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class CompanyParser {
    private final String[] KEYWORDS = {"worked at", "work at", "làm việc tại", "công ty", "company"};

    public List<String> parse(String text) {
        Set<String> results = new HashSet<>();
        for (String line : text.split("\n")) {
            for (String kw : KEYWORDS) {
                if (line.toLowerCase().contains(kw)) {
                    String cleanName = extractAndClean(line, kw);
                    if (isValid(cleanName)) results.add(cleanName);
                }
            }
        }
        return new ArrayList<>(results);
    }

    private String extractAndClean(String line, String keyword) {
        return line.replaceAll("(?i)" + keyword, "").trim()
                   .replaceAll("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$", "");
    }

    private boolean isValid(String name) {
        return name.length() >= 2 && !name.matches("^\\d+$");
    }
}
