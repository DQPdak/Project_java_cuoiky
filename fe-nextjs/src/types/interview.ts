export interface MessageDTO {
  sender: "USER" | "AI";
  content: string;
  sentAt: string;
}

export interface InterviewDTO {
  id: number;
  status: "ONGOING" | "COMPLETED";
  score?: number;
  feedback?: string;
  createdAt: string;
  jobId: number;
  jobTitle: string;
  companyName: string;
  candidateId: number;
  candidateName: string;
  messages?: MessageDTO[];
}
