import api from "./api";
import { InterviewDTO, MessageDTO } from "@/types/interview";

// Định nghĩa lại response wrapper nếu backend trả về dạng { status, message, data }
interface ApiResponse<T> {
  status: string;
  message: string;
  data: T;
}

export const interviewService = {
  // Bắt đầu phỏng vấn mới
  startInterview: async (jobId: number): Promise<InterviewDTO> => {
    const response = await api.post<ApiResponse<InterviewDTO>>(
      "/interview/start",
      { jobId },
    );
    return response.data.data;
  },

  // Gửi tin nhắn
  sendMessage: async (
    sessionId: number,
    message: string,
  ): Promise<MessageDTO> => {
    const response = await api.post<ApiResponse<MessageDTO>>(
      `/interview/${sessionId}/chat`,
      { message },
    );
    return response.data.data;
  },

  // Kết thúc phỏng vấn
  endInterview: async (sessionId: number): Promise<InterviewDTO> => {
    const response = await api.post<ApiResponse<InterviewDTO>>(
      `/interview/${sessionId}/end`,
    );
    return response.data.data;
  },

  // Lấy lịch sử (nếu cần hiển thị list các lần phỏng vấn trước đó)
  getHistory: async (jobId: number): Promise<InterviewDTO[]> => {
    const response = await api.get<ApiResponse<InterviewDTO[]>>(
      `/interview/history`,
      {
        params: { jobId },
      },
    );
    return response.data.data;
  },

  // Lấy chi tiết session (trong trường hợp refresh trang)
  getSession: async (sessionId: number): Promise<InterviewDTO> => {
    const response = await api.get<ApiResponse<InterviewDTO>>(
      `/interview/${sessionId}`,
    );
    return response.data.data;
  },
};
