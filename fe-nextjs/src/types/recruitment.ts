// Đường dẫn: fe-nextjs/src/types/recruitment.ts

export enum JobStatus {
  OPEN = "OPEN",
  CLOSED = "CLOSED",
  DRAFT = "DRAFT"
}

export enum ApplicationStatus {
  APPLIED = "APPLIED",
  SCREENING = "SCREENING",
  INTERVIEW = "INTERVIEW",
  OFFERED = "OFFERED",
  REJECTED = "REJECTED"
}

export interface JobPosting {
  id: number;
  title: string;
  location: string;
  salaryRange: string;
  deadline: string;
  status: JobStatus;
  applicationCount?: number;
  description?: string;
  requirements?: string;
  createdAt?: string;
}

export interface CandidateApplication {
  id: number;
  candidateName: string;
  email: string;
  matchScore: number;
  status: ApplicationStatus;
  cvUrl: string;
}

export interface JobCreateRequest {
  title: string;
  description: string;
  requirements: string;
  location: string;
  salaryRange: string;
  deadline: string;
}