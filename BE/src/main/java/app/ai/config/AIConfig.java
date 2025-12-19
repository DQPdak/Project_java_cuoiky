package app.ai.config;
/**
 * CHỨC NĂNG 
 * - Cấu hình các thông số cho hệ thống AI
 * - Tạo bean RestTemplate để gọi API bên ngoài
 * - Load config từ file application.properties
 * 
 * SỬ DỤNG:
 * - Spring Boot tự động scan và load cấu hình này khi khơi động
 * - Các service khác nạp (inject) AICV để sử dụng 
 * 
 * THƯ VIỆN SỬ DỤNG:
 * - Spring-boot-starter-web (RestTemplate, HTTP client)
 * - httpclient (Apache HTTP Components cho timeout config)
 * */

// import từ Spring Framework 
import org.springframework.beans.factory.annotation.Value; // đọc giá trị từ properties file
import org.springframework.context.annotation.Bean; // Đánh dấu phương thức tạo bean
import org.springframework.context.annotation.Configuration; // Đánh dấu lớp cấu hình

// import từ thư viện HTTP client
import org.springframework.web.client.RestTemplate; // thư vỉện gọi API REST
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory; // cấu hình timeout HTTP

@Configuration
public class AIConfig {
    // @value: nạp giá trị từ application.properties
    
    @Value("${ai.openai.api-key}")
    private String openAiApiKey; // lưu trữ API key của OpenAI

    @Value("${ai.openai.model:gpt-4}")
    private String model; // lưu trữ model AI, mặc định là gpt-4

    @Value("${ai.timeout.cv-analysis:5000}")
    private int cvAnalysisTimeout; // timeout cho CV analysis, mặc định 5s

    @Value("${ai.timeout.chatbot:3500}")
    private int chatBotTimeout; // timeout cho Chatbot, mặc định 3.5s

    /**
     * Tạo bean RestTemplate với cấu hình timeout
     * RestTemplate: thư viện Spring để gọi HTTP APIs
    */

    @Bean
    public RestTemplate aiRestTemplate(){
        // HttpComponentsClientHttpRequestFactory: cấu hình timeout cho HTTP requests
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        factory.setConnectTimeout(2000); // timeout kết nối server 2s
        factory.setReadTimeout(5000); // timeout đọc dữ liệu 5s

        return new RestTemplate(factory); // trả về RestTemplate với cấu hình timeout
    }

    /**
     * Tạo Bean chứa các properties AI
     * Dùng để nạp vào các service khác
    */

    @Bean
    public AIProperties aiProperties(){
        AIProperties props = new AIProperties(); // tạo đối tượng AiProperties 
        props.setApiKey(openAiApiKey); // set API key
        props.setModel(model); // set model AI
        props.setCvAnalysisTimeout(cvAnalysisTimeout); // set timeout CV analysis
        props.setChatBotTimeout(chatBotTimeout); // set timeout Chatbot
        return props; // trả về đối tượng AiProperties đã cấu hình
    }

    /**
     * CLASS AIProperties
     * 
     * CHỨC NĂNG:
     * - Chứa các thuộc tính cấu hình cho AI
     * - Được nạp vào các service khác để sử dụng
     * 
     * PATTERN: JavaBean (POJO với getter/setter)
     */

    public class AIProperties {
        private String apiKey;
        private String model;
        private int cvAnalysisTimeout;
        private int chatBotTimeout;
        // Mặc định contructor không khai báo là rỗng

        // các hàm getter
        public String getApiKey() {
            return apiKey;
        }
        public String getModel() {
            return model;
        }
        public int getCvAnalysisTimeout() {
            return cvAnalysisTimeout;
        }
        public int getChatBotTimeout() {
            return chatBotTimeout;
        }

        // các hàm setter

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        public void setModel(String model) {
            this.model = model;
        }
        public void setCvAnalysisTimeout(int timeout) {
            this.cvAnalysisTimeout = timeout;
        }
        public void setChatBotTimeout(int timeout) {
            this.chatBotTimeout = timeout;
        }
    }
}
