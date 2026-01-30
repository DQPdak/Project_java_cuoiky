export interface Experience {
  id?: number; // ID từ database
  company: string; // Tên công ty
  role: string; // Vị trí/Vai trò
  startDate: string; // Đổi từ 'from' thành 'startDate' để khớp BE
  endDate: string; // Đổi từ 'to' thành 'endDate' để khớp BE
  description: string; // Mô tả công việc
}

export interface CandidateProfile {
  id: number;
  fullName: string; // Bổ sung: Tên đầy đủ
  email: string; // Bổ sung: Email liên hệ
  avatarUrl?: string; // Bổ sung: Link ảnh đại diện
  phoneNumber?: string;
  address?: string;
  aboutMe?: string;
  linkedInUrl?: string;
  websiteUrl?: string;
  cvFilePath?: string;
  skills: string[];
  experiences: Experience[]; // Danh sách các công việc đã làm
  educationJson?: string; // Backend đang lưu dạng chuỗi JSON
}

export interface CandidateResponse {
  message: string;
  data: CandidateProfile;
}
