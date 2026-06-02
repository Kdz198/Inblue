import subprocess
import os
from pathlib import Path
from datetime import datetime
import google.generativeai as genai

# 1. Cấu hình thư mục và Global Index
base_dir = Path("docs/releases")
today = datetime.now()
date_str = today.strftime("%Y-%m-%d")

month_folder = today.strftime("%Y-%m")
output_dir = base_dir / month_folder
output_dir.mkdir(parents=True, exist_ok=True)

existing_files = list(base_dir.rglob("*.md"))
release_index = len(existing_files) + 1
release_number = f"#{release_index:03d}"

# 2. Lấy dữ liệu Commit từ Git
# Chỉ lấy hash, tác giả và tiêu đề commit.
result = subprocess.run(
    ["git", "log", "--pretty=format:%h - %an: %s", "-20"],
    capture_output=True,
    text=True
)
commit_history = result.stdout.strip()

if not commit_history:
    print("Không có commit nào để tạo release notes.")
    exit(0)

# 3. Cấu hình AI và Prompt
# Lấy API Key từ biến môi trường (Cài trong GitHub Secrets)
api_key = os.environ.get("GEMINI_API_KEY")
if not api_key:
    raise ValueError("Thiếu GEMINI_API_KEY trong biến môi trường!")

genai.configure(api_key=api_key)
model = genai.GenerativeModel('gemini-3.1-flash-lite')

prompt = f"""
Bạn là một Technical Product Manager chuyên nghiệp. Dưới đây là danh sách các commit trong đợt phát hành phần mềm mới nhất của chúng tôi:

{commit_history}

Hãy tạo ra một bản Release Note bằng tiếng Việt, chuẩn định dạng Markdown. Yêu cầu cấu trúc như sau:

## 🎯 Tổng quan mục tiêu
(Viết 1 đoạn ngắn khoảng 2-3 câu tóm tắt giá trị mang lại trong bản cập nhật này).

## ✨ Chi tiết thay đổi
Phân loại các thay đổi thành các mục (Ví dụ: 🚀 Tính năng mới, 🐛 Sửa lỗi, 🛠 Bảo trì hệ thống).
Với mỗi dòng commit, HÃY TRÌNH BÀY CHÍNH XÁC THEO ĐỊNH DẠNG SAU:
- <kbd>[mã hash]</kbd> **[Tên ngắn gọn]**: [Giải thích chi tiết 1 câu sao cho dễ hiểu, chuyên nghiệp].

Không thêm câu chào hỏi, không thêm phần kết luận, chỉ trả về đúng nội dung Markdown.
"""

# 4. Gọi AI để sinh nội dung
print("Đang gọi AI để phân tích và tạo Release Note...")
response = model.generate_content(prompt)
ai_generated_content = response.text

# 5. Xây dựng file hoàn chỉnh
# Thay thế dấu '-' thành '--' để API shields.io không bị lỗi hiển thị
badge_date = date_str.replace("-", "--")
badge_version = release_number.replace("#", "v") # Chuyển #002 thành v002

final_content = [
    f"# 📦 Báo Cáo Phát Hành (Release Notes) - {release_number}",
    "",
    f"![Date](https://img.shields.io/badge/Ngày_phát_hành-{badge_date}-0052CC?style=for-the-badge&logo=calendar) ![Version](https://img.shields.io/badge/Phiên_bản-{badge_version}-2ea44f?style=for-the-badge&logo=github)",
    "",
    "---",
    "",
    ai_generated_content
]

file_name = f"Release_{release_index:03d}_{date_str}.md"
file_path = output_dir / file_name

with open(file_path, "w", encoding="utf-8") as f:
    f.write("\n".join(final_content))

print(f"Đã tạo thành công file release chuẩn Enterprise: {file_path}")