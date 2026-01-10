export interface Experience {
  totalYears?: number;
  level?: string;
  companies?: string[]; // Tuỳ chỉnh theo dữ liệu thực tế AI trả về
}

export interface CandidateProfile {
  id: number;
  cvFilePath?: string;
  skills: string[];
  experiences: Experience[];
  educations?: any[]; // Có thể định nghĩa chi tiết sau
  aboutMe?: string;
  phoneNumber?: string;
  address?: string;
  linkedInUrl?: string;
}

export interface CandidateResponse {
  message: string;
  data: CandidateProfile;
}