import re

with open("app/src/main/java/com/example/ui/screens/AdminPanelScreen.kt", "r") as f:
    lines = f.readlines()

def fix(line_no, old_str, new_str):
    idx = line_no - 1
    lines[idx] = lines[idx].replace(old_str, new_str)

fix(310, "18.sp", "14.sp")
fix(340, "18.sp", "14.sp")
fix(353, "15.sp", "14.sp")
fix(447, "18.sp", "16.sp")
fix(448, "18.sp", "14.sp")
fix(449, "18.sp", "14.sp")
fix(507, "18.sp", "14.sp")
fix(522, "18.sp", "14.sp")
fix(530, "15.sp", "14.sp")
fix(554, "18.sp", "14.sp")
fix(577, "15.sp", "13.sp")
fix(588, "15.sp", "13.sp")
fix(616, "18.sp", "14.sp")
fix(693, "18.sp", "16.sp")
fix(697, "18.sp", "14.sp")
fix(706, "18.sp", "14.sp")

with open("app/src/main/java/com/example/ui/screens/AdminPanelScreen.kt", "w") as f:
    f.writelines(lines)
