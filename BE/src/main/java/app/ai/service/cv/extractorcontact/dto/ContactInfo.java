package app.ai.service.cv.extractorcontact.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactInfo {
        private String email;
        private String phoneNumber;
        @Override
        public String toString() {
            return "ContactInfo [email=" + email + '\'' + ", phoneNumber=" + phoneNumber +'\'' + "]";
        }

    }