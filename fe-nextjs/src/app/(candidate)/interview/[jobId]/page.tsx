"use client";

import React, { useState, useEffect, useRef } from "react";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { interviewService } from "@/services/interviewService";
import { InterviewDTO, MessageDTO } from "@/types/interview";
import toast, { Toaster } from "react-hot-toast";
import {
  Send,
  Mic,
  ArrowLeft,
  Briefcase,
  User,
  Bot,
  LogOut,
  History,
  CheckCircle,
  AlertCircle,
} from "lucide-react";

export default function InterviewRoomPage() {
  const { jobId } = useParams();
  const router = useRouter();
  const searchParams = useSearchParams();
  const sessionIdParam = searchParams.get("sessionId");
  const isReviewParam = searchParams.get("review") === "true"; // Check xem có phải request xem lại từ URL không

  // State
  const [session, setSession] = useState<InterviewDTO | null>(null);
  const [messages, setMessages] = useState<MessageDTO[]>([]);
  const [inputStr, setInputStr] = useState("");
  const [loading, setLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);
  const [showHistory, setShowHistory] = useState(false); // State để toggle màn hình xem lại

  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isSending, showHistory]);

  // Sync URL param với state hiển thị
  useEffect(() => {
    if (isReviewParam) {
      setShowHistory(true);
    }
  }, [isReviewParam]);

  // 1. KHỞI TẠO PHÒNG
  useEffect(() => {
    const initSession = async () => {
      try {
        setLoading(true);
        let data: InterviewDTO;

        if (sessionIdParam) {
          const sId = Number(sessionIdParam);
          if (isNaN(sId) || sId <= 0) {
            toast.error("Mã phiên phỏng vấn không hợp lệ");
            setLoading(false);
            return;
          }
          data = await interviewService.getSession(sId);
          if (!isReviewParam) toast.success("Đã tải lại lịch sử");
        } else {
          if (!jobId) {
            toast.error("Không tìm thấy thông tin công việc");
            return;
          }
          data = await interviewService.startInterview(Number(jobId));
          toast.success("Đã kết nối với AI Interviewer");
        }

        setSession(data);

        if (data.messages && data.messages.length > 0) {
          setMessages(data.messages);
        } else if (!sessionIdParam) {
          setMessages([
            {
              sender: "AI",
              content: `Chào bạn, tôi là AI Interviewer đại diện cho công ty ${data.companyName}. Chúng ta sẽ bắt đầu phỏng vấn cho vị trí ${data.jobTitle}. Bạn đã sẵn sàng chưa?`,
              sentAt: new Date().toISOString(),
            },
          ]);
        }
      } catch (error: any) {
        console.error("Lỗi khởi tạo:", error);
        toast.error(
          error.response?.data?.message || "Không thể tải dữ liệu phỏng vấn",
        );
        setTimeout(() => router.back(), 3000);
      } finally {
        setLoading(false);
      }
    };

    if (jobId || sessionIdParam) {
      initSession();
    }
  }, [jobId, sessionIdParam, router, isReviewParam]);

  // 2. GỬI TIN NHẮN
  const handleSend = async (e?: React.FormEvent) => {
    e?.preventDefault();
    if (!inputStr.trim() || !session || isSending) return;

    if (session.status === "COMPLETED") {
      toast.error("Buổi phỏng vấn này đã kết thúc.");
      return;
    }

    const currentMsg = inputStr;
    setInputStr("");

    const userMsg: MessageDTO = {
      sender: "USER",
      content: currentMsg,
      sentAt: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, userMsg]);
    setIsSending(true);

    try {
      const aiResponse = await interviewService.sendMessage(
        session.id,
        currentMsg,
      );
      setMessages((prev) => [...prev, aiResponse]);
    } catch (error) {
      console.error("Lỗi gửi tin:", error);
      toast.error("Gửi tin thất bại. Vui lòng thử lại.");
      setInputStr(currentMsg);
    } finally {
      setIsSending(false);
    }
  };

  // 3. KẾT THÚC
  const handleEndInterview = async () => {
    if (!session) return;
    if (session.status === "COMPLETED") return;

    if (!confirm("Bạn chắc chắn muốn nộp bài và kết thúc buổi phỏng vấn này?"))
      return;

    try {
      const loadingToast = toast.loading("Đang tổng kết và chấm điểm...");
      const result = await interviewService.endInterview(session.id);
      setSession(result);
      // Khi kết thúc xong thì về màn hình Summary, tắt chế độ chat
      setShowHistory(false);
      toast.dismiss(loadingToast);
      toast.success("Phỏng vấn hoàn tất!");
    } catch (error) {
      toast.error("Lỗi khi kết thúc phỏng vấn");
    }
  };

  // --- RENDER ---

  if (loading) {
    return (
      <div className="h-screen flex flex-col items-center justify-center bg-gray-50">
        <div className="w-16 h-16 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mb-4"></div>
        <h2 className="text-xl font-semibold text-gray-700">
          Đang tải dữ liệu...
        </h2>
      </div>
    );
  }

  // =========================================================
  // CASE 1: MÀN HÌNH TỔNG KẾT (KHI ĐÃ COMPLETED & KHÔNG XEM LẠI)
  // =========================================================
  if (session?.status === "COMPLETED" && !showHistory) {
    return (
      <div className="min-h-screen bg-gray-50 p-6 flex items-center justify-center overflow-y-auto">
        <div className="max-w-3xl w-full bg-white rounded-3xl shadow-2xl p-8 md:p-12 border border-gray-100 relative overflow-hidden">
          {/* Background Decor */}
          <div className="absolute top-0 right-0 w-80 h-80 bg-green-50 rounded-full blur-3xl -mr-20 -mt-20 opacity-60"></div>
          <div className="absolute bottom-0 left-0 w-64 h-64 bg-blue-50 rounded-full blur-3xl -ml-16 -mb-16 opacity-60"></div>

          <div className="relative z-10">
            {/* Header Result */}
            <div className="text-center mb-10">
              <div className="inline-flex items-center justify-center w-20 h-20 bg-green-100 rounded-full mb-6 shadow-sm">
                <CheckCircle className="w-10 h-10 text-green-600" />
              </div>
              <h1 className="text-3xl font-extrabold text-gray-800 mb-2">
                Hoàn thành Phỏng vấn
              </h1>
              <div className="flex items-center justify-center gap-2 text-gray-500">
                <Briefcase size={16} />
                <span className="font-medium">{session.jobTitle}</span>
                <span className="mx-2">•</span>
                <span>{session.companyName}</span>
              </div>
            </div>

            {/* Score & Feedback Card */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
              {/* Score */}
              <div className="bg-gradient-to-br from-blue-50 to-indigo-50 p-6 rounded-2xl border border-blue-100 flex flex-col items-center justify-center text-center shadow-sm">
                <p className="text-sm font-bold text-blue-600 uppercase tracking-wider mb-2">
                  Điểm Đánh Giá
                </p>
                <div className="text-6xl font-black text-blue-700 tracking-tighter">
                  {session.score ?? "?"}
                  <span className="text-2xl text-blue-400 font-medium">
                    /10
                  </span>
                </div>
              </div>

              {/* Feedback */}
              <div className="md:col-span-2 bg-white p-6 rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition">
                <div className="flex items-center gap-2 mb-3">
                  <AlertCircle size={18} className="text-purple-600" />
                  <p className="text-sm font-bold text-gray-700 uppercase">
                    Nhận xét từ AI
                  </p>
                </div>
                <div className="text-gray-600 text-sm leading-relaxed max-h-40 overflow-y-auto pr-2 custom-scrollbar">
                  {session.feedback ||
                    "Chưa có nhận xét cụ thể cho bài phỏng vấn này."}
                </div>
              </div>
            </div>

            {/* Actions */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button
                onClick={() => router.push("/interview")}
                className="px-8 py-3.5 bg-gray-100 text-gray-700 font-bold rounded-xl hover:bg-gray-200 transition flex items-center justify-center gap-2"
              >
                <ArrowLeft size={18} />
                Về danh sách
              </button>

              <button
                onClick={() => setShowHistory(true)}
                className="px-8 py-3.5 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 transition shadow-lg shadow-blue-200 flex items-center justify-center gap-2"
              >
                <History size={18} />
                Xem lại lịch sử trò chuyện
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // =========================================================
  // CASE 2: GIAO DIỆN CHAT (ONGOING HOẶC REVIEW MODE)
  // =========================================================
  const isReviewMode = session?.status === "COMPLETED";

  return (
    <div className="flex flex-col h-[100dvh] overflow-hidden bg-gray-100">
      <Toaster position="top-center" />

      {/* HEADER */}
      <header className="bg-white px-6 py-4 flex justify-between items-center shadow-sm border-b border-gray-200 sticky top-0 z-20 shrink-0">
        <div className="flex items-center gap-4">
          {/* Nút Back: Nếu đang review thì quay lại màn hình kết quả, ngược lại quay lại trang trước */}
          <button
            onClick={() =>
              isReviewMode ? setShowHistory(false) : router.back()
            }
            className="p-2 hover:bg-gray-100 rounded-full transition"
          >
            <ArrowLeft size={20} className="text-gray-500" />
          </button>

          <div className="min-w-0">
            <h1 className="font-bold text-gray-800 flex items-center gap-2 truncate">
              <Briefcase size={16} className="text-blue-600 shrink-0" />
              <span className="truncate">
                {session?.jobTitle || "Phỏng vấn AI"}
              </span>
            </h1>
            <p className="text-xs text-gray-500 truncate">
              {isReviewMode ? (
                <span className="text-orange-600 font-medium">
                  (Chế độ xem lại)
                </span>
              ) : (
                session?.companyName
              )}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3 shrink-0">
          {!isReviewMode && (
            <div className="hidden md:flex items-center gap-2 bg-green-50 text-green-700 px-3 py-1 rounded-full text-xs font-bold border border-green-100">
              <div className="w-2 h-2 rounded-full bg-green-50 animate-pulse"></div>
              Live
            </div>
          )}

          {/* Nếu đang Review thì hiện nút "Đóng", đang Chat thì hiện "Kết thúc" */}
          {isReviewMode ? (
            <button
              onClick={() => setShowHistory(false)}
              className="flex items-center gap-2 bg-gray-100 text-gray-700 px-4 py-2 rounded-lg text-sm font-semibold hover:bg-gray-200 transition"
            >
              Quay lại kết quả
            </button>
          ) : (
            <button
              onClick={handleEndInterview}
              className="flex items-center gap-2 bg-red-50 text-red-600 px-3 py-2 rounded-lg text-sm font-semibold hover:bg-red-100 transition border border-red-100"
            >
              <LogOut size={16} />
              <span className="hidden sm:inline">Kết thúc</span>
            </button>
          )}
        </div>
      </header>

      {/* CHAT AREA */}
      <main className="flex-1 overflow-y-auto p-4 md:p-6 space-y-6 scroll-smooth bg-gray-50">
        {messages.map((msg, index) => {
          const isUser = msg.sender === "USER";
          return (
            <div
              key={index}
              className={`flex w-full ${isUser ? "justify-end" : "justify-start"}`}
            >
              <div
                className={`flex gap-3 max-w-[85%] md:max-w-[70%] ${isUser ? "flex-row-reverse" : "flex-row"}`}
              >
                <div
                  className={`w-8 h-8 md:w-10 md:h-10 rounded-full flex-shrink-0 flex items-center justify-center shadow-sm ${
                    isUser
                      ? "bg-blue-100"
                      : "bg-gradient-to-br from-indigo-500 to-purple-600"
                  }`}
                >
                  {isUser ? (
                    <User size={18} className="text-blue-600" />
                  ) : (
                    <Bot size={20} className="text-white" />
                  )}
                </div>
                <div
                  className={`flex flex-col ${isUser ? "items-end" : "items-start"}`}
                >
                  <div
                    className={`px-5 py-3.5 text-sm md:text-base leading-relaxed shadow-sm ${
                      isUser
                        ? "bg-blue-600 text-white rounded-2xl rounded-tr-none"
                        : "bg-white text-gray-800 border border-gray-200 rounded-2xl rounded-tl-none"
                    }`}
                  >
                    <p className="whitespace-pre-wrap break-words">
                      {msg.content}
                    </p>
                  </div>
                  <span className="text-[10px] text-gray-400 mt-1 px-1">
                    {msg.sentAt
                      ? new Date(msg.sentAt).toLocaleTimeString([], {
                          hour: "2-digit",
                          minute: "2-digit",
                        })
                      : "Vừa xong"}
                  </span>
                </div>
              </div>
            </div>
          );
        })}

        {isSending && (
          <div className="flex w-full justify-start">
            <div className="flex gap-3 max-w-[70%]">
              <div className="w-8 h-8 md:w-10 md:h-10 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-sm">
                <Bot size={20} className="text-white" />
              </div>
              <div className="bg-white border border-gray-200 px-4 py-3 rounded-2xl rounded-tl-none shadow-sm flex items-center gap-1.5 h-12">
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce delay-75"></div>
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce delay-150"></div>
              </div>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} className="h-4" />
      </main>

      {/* FOOTER INPUT (CHỈ HIỆN KHI CHƯA COMPLETED) */}
      {!isReviewMode ? (
        <div className="bg-white border-t border-gray-200 p-4 md:px-6 md:py-5 sticky bottom-0 z-20 shrink-0">
          <div className="max-w-4xl mx-auto flex gap-3 items-end">
            <button
              className="p-3 bg-gray-100 text-gray-500 rounded-full hover:bg-gray-200 transition shrink-0"
              title="Tính năng Voice đang phát triển"
            >
              <Mic size={20} />
            </button>

            <form onSubmit={handleSend} className="flex-1 relative">
              <textarea
                value={inputStr}
                onChange={(e) => setInputStr(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && !e.shiftKey) {
                    e.preventDefault();
                    handleSend();
                  }
                }}
                disabled={isSending}
                placeholder="Nhập câu trả lời của bạn..."
                className="w-full bg-gray-50 border border-gray-300 text-gray-800 rounded-2xl px-4 py-3 pr-12 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition resize-none max-h-32 min-h-[50px]"
                rows={1}
                style={{ minHeight: "50px" }}
              />
              <button
                type="submit"
                disabled={!inputStr.trim() || isSending}
                className="absolute right-2 bottom-2 p-2 bg-blue-600 text-white rounded-full hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition shadow-md"
              >
                <Send size={18} />
              </button>
            </form>
          </div>
          <p className="text-center text-xs text-gray-400 mt-2 hidden sm:block">
            Hãy trả lời tự nhiên. AI sẽ đánh giá nội dung và thái độ của bạn.
          </p>
        </div>
      ) : (
        // Footer khi ở chế độ Review Mode
        <div className="bg-gray-100 border-t border-gray-200 p-4 text-center text-sm text-gray-500 italic sticky bottom-0 z-20 shrink-0">
          Đây là lịch sử cuộc trò chuyện. Bạn không thể gửi thêm tin nhắn.
        </div>
      )}
    </div>
  );
}
