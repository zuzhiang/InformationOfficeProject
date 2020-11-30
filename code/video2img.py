# coding=UTF-8
import os
import shutil
import cv2
import glob
import sys
import string


def split_vedio(video_path, pic_path):
    file_name = video_path.split("/")[-1].split(".")[0]
    # -r 是每秒提取的图片帧数
    output = "ffmpeg -i " + video_path + " -r 0.5 " + pic_path + "/" + file_name + "_%d.jpg"
    print(output)
    os.system(output)


print("Hello video2img!!!!!!!!!!!!")
video_root = sys.argv[1]
big_img_path = sys.argv[2]
# small_img_path=sys.argv[3]
video_path = glob.glob(video_root + '.*')[0]
print("video path: ", video_path)

if not os.path.exists(big_img_path):
    os.makedirs(big_img_path)
# if not os.path.exists(small_img_path):
#    os.makedirs(small_img_path)

split_vedio(video_path, big_img_path)  # 将视频切分为图片
'''
# 将大图片切分为小图片，小图片的大小为450*450，重叠为150
paths = glob.glob(os.path.join(big_img_path, '*.jpg'))
for path in paths:
    img = cv2.imread(path)
    num = 0
    (h, w, c) = img.shape
    size=min(600,h,w)
    x_step=size/4*3
    y_step=size/2
    for x in range(0, h, int(x_step)):
        x2 = h if x+size*2>h else x + size
        if x2-x<size:
            break
        for y in range(0, w, int(y_step)):           
            num += 1
            y2 = w if y+size*2>w else y + size
            if y2 - y < size:
                break
            out = img[x:x2, y:y2, :]
            img_name=path.split("/")[-1][:-4] # linux下改为/
            out_name = small_img_path + "/" + img_name + "_" + str(num) +".jpg"
            cv2.imwrite(out_name, out)
'''
print("video2img end.")
