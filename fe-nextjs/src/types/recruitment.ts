// Đường dẫn: fe-nextjs/src/types/recruitment.ts

export enum JobStatus {
  PUBLISHED = "PUBLISHED", 
  CLOSED = "CLOSED",
  DRAFT = "DRAFT",
  OPEN = "OPEN" 
}

export enum ApplicationStatus {
  APPLIED = "APPLIED",
  PENDING = "PENDING",    
  SCREENING = "SCREENING",
  INTERVIEW = "INTERVIEW",
  OFFERED = "OFFERED",
  REJECTED = "REJECTED",
  HIRED = "HIRED"
}

export interface JobPosting {
  id: number;
  title: string;
  location: string;
  salaryRange: string;
  expiryDate: string;    
  status: JobStatus;
  applicationCount?: number;
  description?: string;
  requirements?: string;
  benefits?: string;
  createdAt?: string;
  extractedSkills?: string[]; 

  // Thống tin cơ sở
  companyId?: number;
  companyName?: string;
  companyLogo?: string;
  companyWebsite?: string;
  companyDescription?: string;
  companyAddress?: string;
}

export interface CandidateApplication {
  id: number;
  studentName?: string;  
  candidateName?: string; 
  matchScore: number;
  status: ApplicationStatus;
  cvUrl: string;
  jobTitle?: string;
  appliedAt?: string;
}

export interface JobCreateRequest {
  title: string;
  description: string;
  requirements: string;
  location: string;
  salaryRange: string;
  expiryDate: string;
}
export interface CandidateSearchResult {
  id: number;
  fullName: string;      // Tên ứng viên
  title: string;         // Chức danh (VD: Java Developer)
  skills: string[];      // Danh sách kỹ năng (VD: ["Java", "Spring"])
  matchScore?: number;   // Điểm phù hợp (nếu có trả về từ AI)
  avatar?: string;       // Ảnh đại diện (nếu có)
  cvUrl?: string;        // Link CV
}
export interface AIAnalysisDetail {
  id?: number;
  
  // 1. Các chỉ số điểm & đánh giá
  matchPercentage?: number;   // BE có thể trả về null, FE handle || 0
  evaluation?: string;        // Đánh giá tổng quan
  learningPath?: string;      // Lộ trình học tập
  careerAdvice?: string;      // Lời khuyên sự nghiệp

  // 2. Các danh sách kỹ năng (Mảng String)
  // Lưu ý: Tên trường phải khớp với JSON Backend trả về
  matchedSkillsList?: string[];
  missingSkillsList?: string[];
  otherHardSkillsList?: string[];
  otherSoftSkillsList?: string[];
  recommendedSkillsList?: string[];

  // 3. Các số lượng đếm (Count)
  matchedSkillsCount?: number;
  missingSkillsCount?: number;
  otherHardSkillsCount?: number;
  otherSoftSkillsCount?: number;
  recommendedSkillsCount?: number;

  // 4. Thông tin bổ sung (nếu có dùng ở chỗ khác)
  candidateName?: string;
  studentName?: string; // Đôi khi BE trả về studentName thay vì candidateName
  jobTitle?: string;
}