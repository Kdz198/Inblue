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
# Chỉ lấy hash, tác giả và tiêu đề commit. Không nên lấy git diff toàn bộ file
# vì sẽ làm quá tải giới hạn token của AI nếu đợt release quá lớn.
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

Hãy tạo ra một bản Release Note bằng tiếng Việt, chuẩn định dạng Markdown, bao gồm:
1. Một đoạn giới thiệu ngắn gọn (khoảng 2-3 câu) tóm tắt mục tiêu chính của đợt phát hành này dựa trên các commit.
2. Phân loại các thay đổi thành các mục rõ ràng (ví dụ: 🚀 Tính năng mới, 🐛 Sửa lỗi, 🛠 Bảo trì & Nâng cấp).
3. Viết lại các dòng commit message sao cho dễ hiểu, chuyên nghiệp. Không bê nguyên xi các câu lủng củng. Giữ lại mã hash commit ở đầu mỗi dòng.

Chỉ trả về nội dung Markdown, không thêm các câu giải thích thừa.
"""

# 4. Gọi AI để sinh nội dung
print("Đang gọi AI để phân tích và tạo Release Note...")
response = model.generate_content(prompt)
ai_generated_content = response.text

# 5. Xây dựng file hoàn chỉnh
final_content = [
    f"# 📦 Release {release_number}",
    f"> **Ngày phát hành:** {date_str}",
    "> **Trạng thái:** Tự động tạo bởi GitHub CI & AI",
    "",
    "---",
    ai_generated_content,
    "",
    "---",
    f"*Tài liệu được sinh tự động vào lúc {today.strftime('%Y-%m-%d %H:%M:%S')}*"
]

file_name = f"Release_{release_index:03d}_{date_str}.md"
file_path = output_dir / file_name

with open(file_path, "w", encoding="utf-8") as f:
    f.write("\n".join(final_content))

print(f"Đã tạo thành công file release bằng AI: {file_path}")