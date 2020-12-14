import os
import sys
import glob
from PIL import Image

root_path = sys.argv[1]
index = sys.argv[2]
for file in glob.glob(os.path.join(root_path, "images", index, "*")):
    if file.endswith(".jpg"):
        try:
            Image.open(file).load()
        except:
            print(file)
            os.remove(file)
    else:
        print(file)
        os.remove(file)
print("Test image end......")
