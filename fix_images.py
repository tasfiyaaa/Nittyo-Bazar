import os
import base64

# A valid 1x1 transparent PNG image base64
png_b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
png_data = base64.b64decode(png_b64)

directory = "app/src/main/res/drawable-nodpi/"
for filename in os.listdir(directory):
    if filename.endswith(".jpg"):
        with open(os.path.join(directory, filename), "wb") as f:
            f.write(png_data)
        print(f"Fixed {filename}")

