package app.config;

/**
 * CHỨC NĂNG: Nạp dữ liệu từ file skills-format.yml để cấu hình hiển thị kỹ năng
 */

import app.ai.factory.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "skill-config")
@PropertySource(value = "classpath:skills-format.yml", factory = YamlPropertySourceFactory.class)
public class SkillDisplayConfig {
    private Map<String, String> specialCases;

    public Map<String, String> getSpecialCases() { return specialCases; }
    public void setSpecialCases(Map<String, String> specialCases) { this.specialCases = specialCases; }
}
