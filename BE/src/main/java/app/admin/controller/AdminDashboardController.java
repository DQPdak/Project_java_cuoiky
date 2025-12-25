// package app.admin.controller;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import app.auth.dto.response.MessageResponse;
// import app.admin.dto.ApplicationsByDayResponse;
// import app.admin.dto.DashboardSummaryResponse;
// import app.admin.service.AdminDashboardService;

// @RestController
// @RequestMapping("/api/admin/dashboard")
// @RequiredArgsConstructor
// @Slf4j
// public class AdminDashboardController {

//     private final AdminDashboardService adminDashboardService;

//     @GetMapping("/summary")
//     public ResponseEntity<MessageResponse> summary() {
//         log.info("Admin dashboard summary requested");
//         DashboardSummaryResponse response = adminDashboardService.getSummary();
//         return ResponseEntity.ok(MessageResponse.success("Lấy dashboard summary thành công", response));
//     }

//     @GetMapping("/applications-by-day")
//     public ResponseEntity<MessageResponse> applicationsByDay(
//             @RequestParam(defaultValue = "7") int days
//     ) {
//         log.info("Admin applications-by-day requested, days={}", days);
//         ApplicationsByDayResponse response = adminDashboardService.getApplicationsByDay(days);
//         return ResponseEntity.ok(MessageResponse.success("Lấy thống kê applications theo ngày thành công", response));
//     }
// }
