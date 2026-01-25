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
  createdAt?: string;
  extractedSkills?: string[]; 
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