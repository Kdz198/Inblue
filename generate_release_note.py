from pathlib import Path
from datetime import datetime
import subprocess

output_dir = Path("docs/releases")
output_dir.mkdir(parents=True, exist_ok=True)

today = datetime.now().strftime("%Y-%m-%d")

result = subprocess.run(
    ["git", "log", "--pretty=format:%s", "-20"],
    capture_output=True,
    text=True
)

commits = result.stdout.splitlines()

content = [
    f"# Release Note - {today}",
    "",
    "## Changes",
    ""
]

for commit in commits:
    content.append(f"- {commit}")

file_path = output_dir / f"{today}.md"

with open(file_path, "w", encoding="utf-8") as f:
    f.write("\n".join(content))