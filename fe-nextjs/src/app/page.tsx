"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function LandingPage() {
  const [isScrolled, setIsScrolled] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const router = useRouter();

  // Xử lý hiệu ứng scroll navbar
  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 50);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // Xử lý hiệu ứng Fade-in khi cuộn (Intersection Observer)
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("opacity-100", "translate-y-0");
            entry.target.classList.remove("opacity-0", "translate-y-10");
          }
        });
      },
      { threshold: 0.1 }
    );

    const elements = document.querySelectorAll(".animate-on-scroll");
    elements.forEach((el) => observer.observe(el));

    return () => observer.disconnect();
  }, []);

  const scrollToSection = (id: string) => {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: "smooth" });
      setIsMobileMenuOpen(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 font-sans selection:bg-indigo-100">
      {/* --- NAVBAR --- */}
      <nav
        className={`fixed top-0 w-full z-50 transition-all duration-300 ${
          isScrolled
            ? "bg-white/90 backdrop-blur-md shadow-lg py-3"
            : "bg-transparent py-5"
        }`}
      >
        <div className="max-w-7xl mx-auto px-6 flex justify-between items-center">
          {/* Logo */}
          <div className="text-2xl font-bold bg-gradient-to-r from-indigo-500 to-purple-600 bg-clip-text text-transparent flex items-center gap-2 cursor-pointer" onClick={() => window.scrollTo({ top: 0, behavior: 'smooth'})}>
            🚀 CareerMate
          </div>

          {/* Desktop Menu */}
          <div className="hidden md:flex items-center gap-8 font-medium">
            <button onClick={() => scrollToSection("features")} className="hover:text-indigo-600 transition">
              Tính năng
            </button>
            <button onClick={() => scrollToSection("how-it-works")} className="hover:text-indigo-600 transition">
              Cách hoạt động
            </button>
            <button onClick={() => scrollToSection("pricing")} className="hover:text-indigo-600 transition">
              Bảng giá
            </button>
            <Link href="/login" className="text-indigo-600 border border-indigo-600 px-5 py-2 rounded-full hover:bg-indigo-600 hover:text-white transition">
              Đăng nhập
            </Link>
            <Link href="/register" className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white px-5 py-2 rounded-full shadow-lg hover:shadow-indigo-500/30 hover:-translate-y-0.5 transition">
              Đăng ký miễn phí
            </Link>
          </div>

          {/* Mobile Menu Button */}
          <button
            className="md:hidden text-2xl text-slate-700"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          >
            {isMobileMenuOpen ? "✕" : "☰"}
          </button>
        </div>

        {/* Mobile Menu Dropdown */}
        <div
          className={`md:hidden absolute top-full left-0 w-full bg-white shadow-xl flex flex-col items-center gap-6 py-8 transition-all duration-300 ${
            isMobileMenuOpen ? "opacity-100 visible" : "opacity-0 invisible"
          }`}
        >
          <button onClick={() => scrollToSection("features")} className="text-lg font-medium">Tính năng</button>
          <button onClick={() => scrollToSection("how-it-works")} className="text-lg font-medium">Cách hoạt động</button>
          <button onClick={() => scrollToSection("pricing")} className="text-lg font-medium">Bảng giá</button>
          <Link href="/login" className="text-indigo-600 font-medium">Đăng nhập</Link>
          <Link href="/register" className="bg-indigo-600 text-white px-6 py-2 rounded-full">Đăng ký ngay</Link>
        </div>
      </nav>

      {/* --- HERO SECTION --- */}
      <section className="relative pt-32 pb-20 lg:pt-48 lg:pb-32 overflow-hidden bg-gradient-to-br from-indigo-500 to-purple-600">
        {/* Background Animation Circle */}
        <div className="absolute top-0 right-0 w-full h-full overflow-hidden pointer-events-none">
           <div className="absolute -top-[50%] -right-[50%] w-[100%] h-[100%] bg-white/10 rounded-full animate-pulse scale-150 blur-3xl"></div>
        </div>

        <div className="max-w-7xl mx-auto px-6 flex flex-col lg:flex-row items-center gap-12 relative z-10">
          <div className="flex-1 text-center lg:text-left text-white animate-on-scroll opacity-0 translate-y-10 transition-all duration-1000">
            <h1 className="text-4xl lg:text-6xl font-bold leading-tight mb-6">
              Bạn đồng hành ứng tuyển <br /> thông minh với AI
            </h1>
            <p className="text-lg lg:text-xl text-indigo-100 mb-8 max-w-2xl mx-auto lg:mx-0">
              Nâng tầm sự nghiệp với công nghệ AI - Phân tích CV, Cố vấn nghề nghiệp, Phỏng vấn thử và Kết nối việc làm.
            </p>
            <div className="flex flex-wrap justify-center lg:justify-start gap-4">
              <Link href="/register" className="bg-white text-indigo-600 px-8 py-3 rounded-full font-bold shadow-lg hover:shadow-xl hover:bg-gray-50 transition transform hover:-translate-y-1">
                Bắt đầu miễn phí
              </Link>
              <button onClick={() => scrollToSection("how-it-works")} className="border-2 border-white text-white px-8 py-3 rounded-full font-bold hover:bg-white/10 transition">
                Tìm hiểu thêm
              </button>
            </div>
          </div>

          <div className="flex-1 flex justify-center animate-on-scroll opacity-0 translate-y-10 transition-all duration-1000 delay-200">
            {/* SVG Image converted to JSX */}
            <svg width="500" height="400" viewBox="0 0 500 400" fill="none" className="max-w-full drop-shadow-2xl animate-[float_3s_ease-in-out_infinite]">
              <circle cx="250" cy="200" r="150" fill="rgba(255,255,255,0.1)" />
              <circle cx="250" cy="200" r="120" fill="rgba(255,255,255,0.15)" />
              <rect x="150" y="120" width="200" height="160" rx="10" fill="white" opacity="0.95" />
              <rect x="170" y="140" width="160" height="10" rx="5" fill="#6366f1" />
              <rect x="170" y="160" width="120" height="8" rx="4" fill="#e2e8f0" />
              <rect x="170" y="175" width="140" height="8" rx="4" fill="#e2e8f0" />
              <rect x="170" y="200" width="160" height="30" rx="15" fill="#6366f1" />
            </svg>
          </div>
        </div>
      </section>

      {/* --- STATS SECTION --- */}
      <section className="py-16 bg-white relative -mt-10 mx-6 rounded-3xl shadow-xl z-20 max-w-7xl lg:mx-auto">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8 px-8">
          {[
            { num: "10K+", label: "Sinh viên tin dùng" },
            { num: "500+", label: "Công ty tuyển dụng" },
            { num: "95%", label: "Tỷ lệ hài lòng" },
            { num: "24/7", label: "Hỗ trợ AI" },
          ].map((stat, idx) => (
            <div key={idx} className="text-center group hover:-translate-y-2 transition duration-300">
              <div className="text-4xl lg:text-5xl font-bold bg-gradient-to-r from-indigo-500 to-purple-600 bg-clip-text text-transparent mb-2">
                {stat.num}
              </div>
              <div className="text-slate-500 font-medium">{stat.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* --- FEATURES SECTION --- */}
      <section id="features" className="py-24 bg-slate-50">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16 animate-on-scroll opacity-0 translate-y-10 transition-all duration-700">
            <h2 className="text-4xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-4">
              Tính năng nổi bật
            </h2>
            <p className="text-xl text-slate-500">
              Công cụ AI toàn diện hỗ trợ hành trình nghề nghiệp của bạn
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {[
              { icon: "📄", title: "Phân tích CV bằng AI", desc: "Đánh giá chi tiết CV, nhận phản hồi tức thì về cấu trúc và nội dung để tăng cơ hội trúng tuyển." },
              { icon: "🤖", title: "Chatbot Cố vấn", desc: "Tư vấn nghề nghiệp 24/7, gợi ý kỹ năng cần học và lộ trình phát triển bản thân." },
              { icon: "💼", title: "Sàn việc làm AI", desc: "Kết nối trực tiếp với nhà tuyển dụng, gợi ý công việc phù hợp dựa trên hồ sơ của bạn." },
              { icon: "🎯", title: "Phỏng vấn thử", desc: "Luyện tập phỏng vấn với AI, nhận đánh giá chi tiết về câu trả lời và kỹ năng giao tiếp." },
              { icon: "📚", title: "Trung tâm Học tập", desc: "Khóa học và lộ trình được chọn lọc kỹ để nâng cao kỹ năng chuyên môn." },
              { icon: "🏆", title: "Gamification", desc: "Hoàn thành thử thách, nhận huy hiệu và cạnh tranh trên bảng xếp hạng." },
            ].map((feature, idx) => (
              <div key={idx} className="animate-on-scroll opacity-0 translate-y-10 transition-all duration-700 bg-white p-8 rounded-2xl shadow-sm hover:shadow-xl hover:-translate-y-2 border border-transparent hover:border-indigo-100 transition group">
                <div className="w-16 h-16 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl flex items-center justify-center text-3xl text-white mb-6 group-hover:scale-110 transition">
                  {feature.icon}
                </div>
                <h3 className="text-xl font-bold text-slate-800 mb-3">{feature.title}</h3>
                <p className="text-slate-500 leading-relaxed">{feature.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* --- HOW IT WORKS --- */}
      <section id="how-it-works" className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-slate-800 mb-4">Cách hoạt động</h2>
            <p className="text-xl text-slate-500">4 bước đơn giản để bắt đầu</p>
          </div>

          <div className="grid md:grid-cols-4 gap-8">
            {[
              { step: 1, title: "Đăng ký", desc: "Tạo tài khoản miễn phí chỉ trong vài phút" },
              { step: 2, title: "Upload CV", desc: "Nhận phân tích chi tiết từ AI ngay lập tức" },
              { step: 3, title: "Tư vấn AI", desc: "Nhận lộ trình sự nghiệp và gợi ý việc làm" },
              { step: 4, title: "Ứng tuyển", desc: "Kết nối với nhà tuyển dụng và nhận việc" },
            ].map((item, idx) => (
              <div key={idx} className="animate-on-scroll opacity-0 translate-y-10 transition-all duration-700 text-center relative">
                <div className="w-20 h-20 mx-auto bg-gradient-to-br from-indigo-500 to-purple-600 rounded-full flex items-center justify-center text-3xl font-bold text-white mb-6 shadow-lg shadow-indigo-200">
                  {item.step}
                </div>
                <h3 className="text-xl font-bold text-slate-800 mb-2">{item.title}</h3>
                <p className="text-slate-500">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* --- PRICING --- */}
      <section id="pricing" className="py-24 bg-slate-50">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-slate-800 mb-4">Bảng giá linh hoạt</h2>
            <p className="text-xl text-slate-500">Chọn gói phù hợp với nhu cầu của bạn</p>
          </div>

          <div className="grid md:grid-cols-3 gap-8 items-center">
            {/* Free Plan */}
            <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-100 animate-on-scroll opacity-0 translate-y-10 transition-all duration-700">
              <h3 className="text-2xl font-bold text-slate-800 mb-2">Miễn phí</h3>
              <div className="text-4xl font-bold text-indigo-600 mb-1">0₫</div>
              <p className="text-slate-400 mb-6">Mãi mãi miễn phí</p>
              <ul className="space-y-4 mb-8 text-slate-500">
                {['Phân tích CV cơ bản', '5 lượt chat AI/tháng', 'Xem việc làm cơ bản', '1 lần phỏng vấn thử'].map((feat, i) => (
                  <li key={i} className="flex items-center gap-2">
                    <span className="text-indigo-500 font-bold">✓</span> {feat}
                  </li>
                ))}
              </ul>
              <Link href="/register" className="block w-full py-3 border-2 border-indigo-500 text-indigo-600 font-bold text-center rounded-xl hover:bg-indigo-50 transition">
                Bắt đầu ngay
              </Link>
            </div>

            {/* Premium Plan */}
            <div className="bg-white p-10 rounded-2xl shadow-2xl border-2 border-indigo-500 relative transform md:scale-105 z-10 animate-on-scroll opacity-0 translate-y-10 transition-all duration-700 delay-100">
              <div className="absolute top-0 right-0 bg-gradient-to-r from-indigo-500 to-purple-600 text-white text-xs font-bold px-3 py-1 rounded-bl-lg rounded-tr-lg uppercase tracking-wider">
                Phổ biến nhất
              </div>
              <h3 className="text-2xl font-bold text-slate-800 mb-2">Premium</h3>
              <div className="text-5xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-1">199K</div>
              <p className="text-slate-400 mb-6">/tháng</p>
              <ul className="space-y-4 mb-8 text-slate-600">
                {['Phân tích CV không giới hạn', 'Chat AI không giới hạn', 'Gợi ý việc làm thông minh', 'Phỏng vấn thử không giới hạn', 'Truy cập khóa học', 'Huy hiệu & Gamification'].map((feat, i) => (
                  <li key={i} className="flex items-center gap-2">
                    <span className="text-indigo-500 font-bold text-xl">✓</span> {feat}
                  </li>
                ))}
              </ul>
              <Link href="/register" className="block w-full py-4 bg-gradient-to-r from-indigo-500 to-purple-600 text-white font-bold text-center rounded-xl shadow-lg hover:shadow-indigo-500/40 hover:-translate-y-1 transition">
                Nâng cấp ngay
              </Link>
            </div>

            {/* Enterprise Plan */}
            <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-100 animate-on-scroll opacity-0 translate-y-10 transition-all duration-700 delay-200">
              <h3 className="text-2xl font-bold text-slate-800 mb-2">Doanh nghiệp</h3>
              <div className="text-4xl font-bold text-slate-800 mb-1">Liên hệ</div>
              <p className="text-slate-400 mb-6">Giải pháp tuyển dụng</p>
              <ul className="space-y-4 mb-8 text-slate-500">
                {['Tài khoản nhà tuyển dụng', 'Đăng tin tuyển dụng', 'Database ứng viên', 'Báo cáo phân tích', 'Hỗ trợ 24/7'].map((feat, i) => (
                  <li key={i} className="flex items-center gap-2">
                    <span className="text-indigo-500 font-bold">✓</span> {feat}
                  </li>
                ))}
              </ul>
              <button className="block w-full py-3 border-2 border-slate-200 text-slate-600 font-bold text-center rounded-xl hover:border-indigo-500 hover:text-indigo-500 transition">
                Liên hệ Sales
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* --- CTA SECTION --- */}
      <section className="py-20 bg-gradient-to-r from-indigo-600 to-purple-600 text-white text-center px-6">
        <h2 className="text-3xl md:text-5xl font-bold mb-6">Sẵn sàng bắt đầu hành trình nghề nghiệp?</h2>
        <p className="text-xl text-indigo-100 mb-10">Tham gia cùng hàng nghìn sinh viên đã thành công với CareerMate</p>
        <Link href="/register" className="inline-block bg-white text-indigo-600 px-10 py-4 rounded-full font-bold text-lg shadow-xl hover:shadow-2xl hover:bg-gray-50 transition transform hover:-translate-y-1">
          Đăng ký miễn phí ngay
        </Link>
      </section>

      {/* --- FOOTER --- */}
      <footer className="bg-slate-900 text-slate-300 py-16">
        <div className="max-w-7xl mx-auto px-6 grid md:grid-cols-4 gap-12 mb-12">
          <div>
            <div className="text-2xl font-bold text-white mb-4">🚀 CareerMate</div>
            <p className="text-slate-400 leading-relaxed">
              Bạn đồng hành ứng tuyển thông minh với AI - Giải pháp toàn diện cho sinh viên và nhà tuyển dụng.
            </p>
          </div>
          <div>
            <h4 className="text-white font-bold text-lg mb-6">Sản phẩm</h4>
            <ul className="space-y-3">
              {['Phân tích CV', 'AI Career Coach', 'Sàn việc làm', 'Khóa học'].map(item => (
                <li key={item}><a href="#" className="hover:text-white transition">{item}</a></li>
              ))}
            </ul>
          </div>
          <div>
            <h4 className="text-white font-bold text-lg mb-6">Hỗ trợ</h4>
            <ul className="space-y-3">
              {['Trung tâm trợ giúp', 'Hướng dẫn sử dụng', 'FAQ', 'Liên hệ'].map(item => (
                <li key={item}><a href="#" className="hover:text-white transition">{item}</a></li>
              ))}
            </ul>
          </div>
          <div>
            <h4 className="text-white font-bold text-lg mb-6">Pháp lý</h4>
            <ul className="space-y-3">
              {['Điều khoản dịch vụ', 'Chính sách bảo mật', 'Cookie Policy'].map(item => (
                <li key={item}><a href="#" className="hover:text-white transition">{item}</a></li>
              ))}
            </ul>
          </div>
        </div>
        <div className="max-w-7xl mx-auto px-6 pt-8 border-t border-slate-800 text-center text-slate-500">
          &copy; 2024 CareerMate. All rights reserved.
        </div>
      </footer>
    </div>
  );
}