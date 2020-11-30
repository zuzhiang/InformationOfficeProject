import os
import sys
import cv2
import json
import glob
import numpy as np


def make_dirs(path):
    if not os.path.exists(path):
        os.makedirs(path)


def compute_IOU(ObjectBound, HandBound):
    cx1 = ObjectBound[0]
    cy1 = ObjectBound[1]
    cx2 = ObjectBound[2]
    cy2 = ObjectBound[3]

    gx1 = HandBound[0]
    gy1 = HandBound[1]
    gx2 = HandBound[2]
    gy2 = HandBound[3]

    carea = (cx2 - cx1) * (cy2 - cy1)  # C的面积
    garea = (gx2 - gx1) * (gy2 - gy1)  # G的面积

    x1 = max(cx1, gx1)
    y1 = max(cy1, gy1)
    x2 = min(cx2, gx2)
    y2 = min(cy2, gy2)
    w = max(0, x2 - x1)
    h = max(0, y2 - y1)
    area = w * h  # C∩G的面积

    # iou = area / (carea + garea - area)
    if area == 0 or (carea + garea - area) == 0:
        iou = 0
    else:
        iou = area / (carea + garea - area + 1e-8)
    return iou


if __name__ == "__main__":
    th_value = 0.60  # 关节点置信度的阈值
    th_num = 5  # 超过阈值的个数的阈值
    # index = str(1127)
    # root_path = r"C:\Users\zuzhiang\Desktop"
    index = str(sys.argv[1])
    root_path = sys.argv[2]
    big_img_path = os.path.join(root_path, "images", index)
    obj_img_path = os.path.join(root_path, "results", "img", index)
    pose_img_path = os.path.join(root_path, "results", "pose", index, "img")
    face_path = os.path.join(root_path, "results", "face", str(index))
    json_path = os.path.join(root_path, "results", "pose", index, "json", "alphapose-results.json")
    out_json_path = os.path.join(root_path, "results", "pose", index, "json", "pose.json")
    make_dirs(obj_img_path)
    make_dirs(face_path)
    shape = cv2.imread(
        glob.glob(os.path.join(big_img_path, "*.jpg"))[0]).shape  # [height,width,channel] 或 [y, x, channel]
    print("shape: ", shape)

    # 矩形框的集合（实际用列表实现），其中每个矩形的格式为：[ 当前图像名，(min_x, min_y, max_x, max_y) ]
    rect_st = []
    # 记录同一位置（每个人）下有哪些图片的字典，其键为图像名，值为矩形框的列表
    # 字典中的矩形框格式为：[ 原图像名，(min_x, min_y, max_x, max_y), (face_min_x, face_min_y, face_max_x, face_max_y), [关键点1的x坐标,关节点1的y坐标,关节点1的置信度,……] ]
    rect_dict = dict()
    # 要返回的pose.json对应的字典，其键为图像名，值为关节点坐标
    pose_dict = dict()
    iou_th = 0.4  # 矩形框IOU的阈值，若两个矩形框的IOU超过该阈值则认为是同一个人，值在0.2396~0.4418之间
    jt_lst = [0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 14, 15, 16, 17]  # 确定上半身矩形框的关节点下标
    face_jt = [0, 1, 14, 15, 16, 17]  # 确定人脸矩形框的关节点下标
    iou_lst = []

    with open(json_path, 'r') as f:
        dct = json.load(f)
        for img_name, value in dct.items():  # 遍历每张图片，key是图像名
            # print(cur_img_name)
            person_num = 0
            name, ext = os.path.splitext(img_name)
            for body in value["bodies"]:  # 遍历图片中的每个人
                confid = np.array(body['joints'])[2::3]  # 关节点的置信度
                num = len(np.where(confid > th_value)[0])  # 置信度超过阈值的个数
                if num < th_num:
                    continue
                person_num += 1
                cur_img_name = name + "_" + str(person_num) + ext

                # 寻找人脸所在的矩形框
                face_x = np.array(body['joints'])[0::3][face_jt]  # 关节点的x坐标值
                face_y = np.array(body['joints'])[1::3][face_jt]  # 关节点的y坐标值
                # 找到当前骨架对应的人脸所在的bounding box，并往四周扩展一定范围
                min_x, max_x, min_y, max_y = np.min(face_x), np.max(face_x), np.min(face_y), np.max(face_y)
                left, right = (max_x - min_x) * 0.33, (max_x - min_x) * 0.33
                top, down = (max_y - min_y) * 0.8, (max_y - min_y) * 1.3
                face_min_x, face_max_x = int(np.clip(min_x - left, 0, shape[1])), int(
                    np.clip(max_x + right, 0, shape[1]))
                face_min_y, face_max_y = int(np.clip(min_y - down, 0, shape[0])), int(np.clip(max_y + top, 0, shape[0]))

                # 寻找人上半身所在的矩形框
                x = np.array(body['joints'])[0::3][jt_lst]  # 关节点的x坐标值
                y = np.array(body['joints'])[1::3][jt_lst]  # 关节点的y坐标值
                # 找到当前骨架对应的人（上半身）所在的bounding box，并往四周扩展一定范围
                min_x, max_x, min_y, max_y = np.min(x), np.max(x), np.min(y), np.max(y)
                left, right = (max_x - min_x) * 0.25, (max_x - min_x) * 0.25
                top, down = (max_y - min_y) * 0.33, (max_y - min_y) * 0.33
                min_x, max_x = int(np.clip(min_x - left, 0, shape[1])), int(np.clip(max_x + right, 0, shape[1]))
                min_y, max_y = int(np.clip(min_y - down, 0, shape[0])), int(np.clip(max_y + top, 0, shape[0]))
                flag = 0  # 当前矩形框和rect_st中的矩形框的IOU均不超过阈值
                for rect in rect_st:
                    IOU = compute_IOU(rect[1], (min_x, min_y, max_x, max_y))
                    if IOU != 0:
                        iou_lst.append(-IOU)
                    if (IOU >= iou_th):  # 若IOU大于阈值则加入rect_dict中rect对应的列表中
                        flag = 1
                        rect_dict[rect[0]].append(
                            [img_name, (min_x, min_y, max_x, max_y), (face_min_x, face_min_y, face_max_x, face_max_y),
                             body["joints"]])
                        break
                if flag == 0:  # 如果均不大于阈值，则加入rect_st中，并在rect_dict生成新的键值对
                    rect_st.append([cur_img_name, (min_x, min_y, max_x, max_y)])
                    rect_dict[cur_img_name] = [
                        [img_name, (min_x, min_y, max_x, max_y), (face_min_x, face_min_y, face_max_x, face_max_y),
                         body["joints"]], ]
            print("person num: ", person_num)
        # print("len(iou_lst): ", len(iou_lst))
        # print("IOU list: ", sorted(iou_lst)[50:])

        person_num = 0
        for rect_lst in rect_dict.values():  # 遍历rect_dict的每个值，即每个位置对应的[图像名，上半身矩形框，人脸矩形框，关节点列表]的列表
            person_num += 1
            cur_obj_path = os.path.join(obj_img_path, str(person_num))
            cur_pose_path = os.path.join(pose_img_path, str(person_num))
            cur_face_path = os.path.join(face_path, str(person_num))
            if len(rect_lst) == 0:
                continue
            make_dirs(cur_obj_path)
            make_dirs(cur_pose_path)
            make_dirs(cur_face_path)
            pic_num = 0
            for rect in rect_lst:  # 遍历当前位置（人）对应的所有图片，即遍历每个[图像名，上半身矩形框，人脸矩形框，关节点列表]
                pic_num += 1
                # 裁剪图片并加以保存
                name, ext = os.path.splitext(rect[0])
                out_img_name = name + "_" + str(pic_num) + ext

                obj_img = cv2.imread(os.path.join(big_img_path, rect[0]))
                img = obj_img[rect[1][1]:rect[1][3], rect[1][0]:rect[1][2]]
                face_img = obj_img[rect[2][1]:rect[2][3], rect[2][0]:rect[2][2]]
                if (img.shape[0] != 0 and img.shape[1] != 0 and img.shape[2] != 0 and face_img.shape[0] != 0 and
                        face_img.shape[1] != 0 and face_img.shape[2] != 0):
                    cv2.imwrite(os.path.join(cur_obj_path, out_img_name), img)
                    cv2.imwrite(os.path.join(cur_face_path, out_img_name), face_img)
                    pose_dict[os.path.join(str(person_num), out_img_name)] = rect[
                        3]  # 往字典里插入新的键值对，键为crop后的图片名，值为人对应的关节点信息
                    if os.path.exists(os.path.join(pose_img_path, rect[0])):
                        pose_img = cv2.imread(os.path.join(pose_img_path, rect[0]))  # 读取key对应的图像
                        img = pose_img[rect[1][1]:rect[1][3], rect[1][0]:rect[1][2]]
                    cv2.imwrite(os.path.join(cur_pose_path, out_img_name), img)
    f.close()
    # 保存包含图片信息的json
    out_json = open(out_json_path, 'w')
    json.dump(pose_dict, out_json)
    out_json.close()
    print("End")

    # 删除原来的大图
    for key in dct.keys():
        if os.path.exists(os.path.join(pose_img_path, key)):
            os.remove(os.path.join(pose_img_path, key))
    # os.remove(os.path.join(big_img_path, key))
