# Module 2.4 - Practice Module

## Kiến thức trong `noidung.md` đã áp dụng

- Compose Fundamentals: tách UI thành các composable nhỏ, dùng `Column`, `Row`, `Box`, `Modifier`, `Text`, `Button`, `OutlinedTextField`, theme và `@Preview`.
- State & Recomposition: UI thay đổi theo state, state hoisting và luồng dữ liệu một chiều.
- Navigation: thêm route `practice/{deckId}`, truyền `deckId` nhẹ giữa các màn hình và giữ back stack.
- MVVM: `QuizScreen` chỉ hiển thị UI và gửi event; `PracticeViewModel` xử lý state; `GenerateQuizUseCase` xử lý logic sinh câu hỏi.
- ViewModel & State Management: một `PracticeUiState` được phát ra bằng một `StateFlow`, có loading, error, dữ liệu, feedback và trạng thái lưu kết quả.
- Coroutines & Async Programming: dùng `viewModelScope.launch` để thu thập dữ liệu và lưu kết quả lên Firestore mà không chặn UI.
- Repository Pattern: `PracticeViewModel` lấy card qua `CardRepository` và lưu lượt làm qua `PracticeRepository`, không truy cập Firestore trực tiếp.
- Dependency Injection: repository và use case được cung cấp từ bên ngoài qua constructor.
- Testing & Debugging: unit test cho logic trắc nghiệm và điền từ, gồm cả trường hợp thiếu dữ liệu.
- Performance & Optimization: UI tách nhỏ, state chỉ chứa dữ liệu cần hiển thị.

## Chi tiết kỹ thuật ngoài phạm vi `noidung.md`

Những mục dưới đây là chi tiết cần thiết để hiện thực yêu cầu 2.4 trong project Android hiện tại. Chúng không phải chủ đề mới của bài học trong `noidung.md`.

- Hilt annotation: `@HiltViewModel`, `@Inject` và hàm `hiltViewModel()` là cách project hiện tại hiện thực nguyên lý Dependency Injection.
- `SavedStateHandle`: đọc `deckId` từ navigation argument trong `PracticeViewModel`.
- Kotlin collection utilities: `filter`, `map`, `distinctBy`, `shuffled`, `take` và `randomOrNull` để lấy ngẫu nhiên 1 đáp án đúng và 3 đáp án gây nhiễu.
- Kotlin string utilities: `contains(..., ignoreCase = true)`, `replace(..., ignoreCase = true)` và `equals(..., ignoreCase = true)` để tạo và chấm bài điền từ.
- `collectAsState()`: nối `StateFlow` của ViewModel với Compose UI để recomposition tự động.
- Firebase Firestore write API: dùng batch write và `await()` để lưu bất đồng bộ một lượt làm bài cùng card đã cập nhật.
- Firebase Security Rules: backend cần cho phép người dùng đã đăng nhập ghi document có `userId` trùng với `request.auth.uid`. Project Android hiện không chứa tệp triển khai rules.
- DTO mapping cho Firestore: chuyển `PracticeAttempt` ở domain thành `PracticeAttemptDto` ở data layer trước khi ghi cơ sở dữ liệu.
- UUID và thao tác lưu idempotent: tạo một `UUID` phía client cho mỗi lượt làm. Khi người dùng nhấn `Thử lưu lại`, ứng dụng dùng lại ID cũ để ghi đè đúng bản ghi thay vì tạo lượt trùng.
- Trạng thái lỗi khi lưu: khóa nút chuyển câu trong lúc chưa lưu thành công, hiển thị tiến trình và cho phép thử lại nếu Firestore trả lỗi.
- State machine cho phiên luyện tập: dùng ba trạng thái `SETUP`, `IN_PROGRESS`, `COMPLETED` để tách rõ bước chọn chế độ/deck/dạng bài, bước trả lời và bước xem tổng kết.
- Điều hướng quay lại trong Practice: khi đang làm bài hoặc xem tổng kết, nút quay lại của header và nút Back hệ thống đưa người dùng về `SETUP` của Practice trước; chỉ rời tab khi đã ở `SETUP`.
- UI thiết lập rút gọn: chỉ giữ chế độ luyện, deck khi cần, dạng bài, số liệu hôm nay và nút bắt đầu có số câu của phiên.
- Hai chế độ tạo phiên: `SPACED_REPETITION` lấy các từ đã vào lịch SM-2 (`sm2Interval > 0`) và đến hạn trong toàn bộ kho từ của người dùng; `DECK_PRACTICE` lấy toàn bộ từ hợp lệ trong deck đã chọn, kể cả card mới hoặc chưa đến hạn.
- Dạng ôn Flashcard: Flashcard là lựa chọn ngang hàng với trắc nghiệm và điền từ. Khi chọn Flashcard, ứng dụng mở lại màn lật thẻ của Module 2.3; phạm vi card phụ thuộc chế độ `SPACED_REPETITION` hoặc `DECK_PRACTICE`.
- Flashcard SM-2: chế độ `SPACED_REPETITION` tạo hàng đợi card đã học đến hạn trên toàn bộ deck. Flashcard theo từng deck vẫn truy cập được từ menu `Học Flashcard` của deck.
- Flashcard hợp lệ: `FilterUsableFlashcardsUseCase` dùng chung cho hàng đợi và màn lật thẻ để bỏ card không có `word`, ID lặp và từ lặp không phân biệt hoa thường. Màn thêm card cũng chặn lưu khi chưa nhập `Word`; import CSV bỏ qua dòng không có từ.
- Trạng thái đã vào lịch SM-2: dùng `sm2Interval > 0` thay cho `sm2Repetitions > 0`. Card mới chọn `Again` hoặc `Hard` bị reset repetitions về `0` nhưng vẫn giữ lịch ôn sau 1 ngày, nên không bị mất khỏi hàng đợi.
- Hoàn thành Flashcard: màn lật thẻ chỉ duyệt mỗi card một lần trong phiên. Sau card cuối, UI hiển thị số từ vừa hoàn thành, mốc `dd/MM/yyyy HH:mm` sớm nhất của các từ vừa ôn và nút `Hoàn tất`.
- Biểu đồ dự báo ôn tập: `GetReviewForecastUseCase` đọc `nextReviewTime` đã được SM-2 tính, rồi gom card đã vào lịch thành 5 cột: đang đến hạn, `24h`, `48h`, `72h`, `96h`. Mỗi cột tương lai biểu diễn một cửa sổ 24 giờ liên tiếp; UI vẽ chart bằng Compose `Canvas`.
- Hàng đợi không lặp trong phiên: `BuildPracticeQueueUseCase` lọc card hợp lệ, loại ID và từ khóa trùng nhau, giữ bản có hạn ôn sớm nhất, sau đó xáo danh sách một lần khi bắt đầu phiên.
- Đáp án gây nhiễu: ưu tiên card trong phạm vi phiên; nếu chưa đủ 4 nghĩa khác nhau thì dùng thêm card thuộc chính người dùng hiện tại.
- Tích hợp SM-2 theo đánh giá của người dùng: sau khi xem kết quả đúng hoặc sai ở cả trắc nghiệm và điền từ, người dùng chọn `AGAIN (q = 0)`, `HARD (q = 1)`, `GOOD (q = 2)` hoặc `EASY (q = 3)`. Practice dùng chung `CalculateSm2NextReviewUseCase` với Flashcard.
- Firestore batch write: sau khi người dùng chọn mức SM-2, ứng dụng ghi `practiceAttempts` và cập nhật card SM-2 trong cùng một batch để tránh trạng thái chỉ lưu được một nửa.
- Tổng hợp lịch ôn: `GetReviewScheduleUseCase` đếm số từ đã học cần ôn ngay trên toàn bộ kho từ và gom đợt ôn tương lai gần nhất theo ngày để UI hiển thị ngày ôn cùng số lượng từ.
- Daily Learning Plan: màn hình thiết lập hiển thị số từ mới (`sm2Interval == 0`), số từ đã vào lịch cần ôn ngay và đợt ôn tiếp theo gần nhất trên toàn bộ kho từ.
- Lượt Flashcard: mỗi lần người dùng chọn `Again`, `Hard`, `Good` hoặc `Easy`, ứng dụng ghi một document `practiceAttempts` và cập nhật card SM-2 trong cùng batch Firestore như hai dạng quiz.

## Dữ liệu Firestore được lưu bởi Module 2.4

Mỗi câu được chấm và đánh giá SM-2 là một lượt luyện tập hoàn chỉnh, được lưu thành một document trong collection `practiceAttempts`. Module 2.4 chỉ lưu dữ liệu thô; các module thống kê có thể đọc collection này để tổng hợp dashboard mà không trộn logic analytics vào màn hình quiz.

| Trường | Kiểu dữ liệu | Ý nghĩa |
| --- | --- | --- |
| `id` | `String` | UUID của lượt làm, đồng thời là document ID |
| `sessionId` | `String` | UUID chung của các lượt làm thuộc cùng một phiên luyện tập |
| `userId` | `String` | UID Firebase Auth của người làm |
| `deckId` | `String` | Bộ từ nguồn của card được dùng để tạo câu hỏi |
| `cardId` | `String` | Thẻ được dùng để tạo câu hỏi |
| `quizType` | `String` | `FLASHCARD`, `MULTIPLE_CHOICE` hoặc `FILL_IN_THE_BLANK` |
| `sessionMode` | `String` | `SPACED_REPETITION` hoặc `DECK_PRACTICE` |
| `correct` | `Boolean` | Kết quả chấm đúng hoặc sai |
| `qualityScore` | `Int` | Mức SM-2 do người dùng chọn: `0..3` tương ứng `Again`, `Hard`, `Good`, `Easy` |
| `sm2IntervalDays` | `Int` | Khoảng cách đến lần ôn tiếp theo theo SM-2 |
| `sm2EaseFactor` | `Double` | Ease factor sau khi SM-2 được cập nhật |
| `nextReviewTime` | `Timestamp` | Thời điểm ôn tiếp theo sau khi SM-2 được cập nhật |
| `answeredAt` | `Timestamp` | Thời điểm hoàn thành câu hỏi |
