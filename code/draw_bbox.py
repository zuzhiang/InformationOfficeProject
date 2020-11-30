#!/usr/bin/env python
# -*- coding: UTF-8 -*-
import os
import re
import cv2
import sys
import glob
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("--index", type=str, help="class index", dest="index")
args = parser.parse_args()

# 在原图片上绘制bbox
def draw_bbox(img_path, box_lst, cls):
    img = cv2.imread(img_path)
    for box in box_lst:
        x1, y1, x2, y2 = int(box[0]), int(box[1]), int(box[2]), int(box[3])
        if cls=="laptop":
            cv2.rectangle(img, (x1, y1), (x2, y2), (255, 0, 0), 1)
        elif cls=="phone":
            cv2.rectangle(img, (x1, y1), (x2, y2), (0, 255, 0), 1)
        else:
            cv2.rectangle(img, (x1, y1), (x2, y2), (0, 0, 255), 1)
        # 设置显示字体
        font = cv2.FONT_HERSHEY_SIMPLEX
        if cls=="laptop":
            cv2.putText(img, cls, (x1, y1), font, 0.5, (255, 0 ,0), 2)
        elif cls=="phone":
            cv2.putText(img, cls, (x1, y1), font, 0.5, (0, 255, 0), 2)
        else:
            cv2.putText(img, cls, (x1, y1), font, 0.5, (0, 0, 255), 2)            
    cv2.imwrite(img_path, img)


def read_draw(file, cls):
    for i in range(len(file)):
        lst = file[i].split(" ")
        if float(lst[1]) == 0. and float(lst[2]) == 0. and float(lst[3]) == 0. and float(lst[4]) == 0.:
            continue
        box_lst=[]
        for j in range(1,len(lst),6):
            box_lst.append((float(lst[j]), float(lst[j+1]), float(lst[j+2]), float(lst[j+3])))
        draw_bbox(lst[0], box_lst, cls)


if __name__=="__main__":
    index=args.index
    root_path = "/home/jchen/zuzhiang/results"
    in_path = os.path.join(root_path, "img", index)    
    
    # 根据txt文件中的坐标值绘制矩形框
    txt_lst=["laptop.txt","phone.txt","book.txt"]
    for txt in txt_lst:
        file=open(os.path.join(root_path, "txt", index, txt))
        txt_file = file.readlines()
        read_draw(txt_file,txt.replace(".txt",""))
        file.close()
