# coding=UTF-8
import os
import sys
import time
import glob
import math
import json
import requests
import operator
import numpy as np

# from face_rcgn import face_detect

rtPath = "/home/jchen/zuzhiang/results/"


def get_IOU(bbox1, bbox2):
    # bbox1 = [x0,y0,x1,y1]
    x0, y0, x1, y1 = bbox1
    x2, y2, x3, y3 = bbox2
    s1 = (x1 - x0) * (y1 - y0)
    s2 = (x3 - x2) * (y3 - y2)
    w = max(0, min(x1, x3) - max(x0, x2))
    h = max(0, min(y1, y3) - max(y0, y2))
    inter = w * h
    # print("s1: ",s1,"  s2: ",s2,"  inter: ",inter )
    iou = inter / (s1 + s2 - inter + 1e-8)
    return iou


# 根据四点坐标获取向量夹角
def get_angle(x1, y1, x2, y2, x3, y3, x4, y4):
    a_x, a_y = x2 - x1, y2 - y1
    b_x, b_y = x4 - x3, y4 - y3
    ab = a_x * b_x + a_y * b_y
    len_a = math.sqrt(a_x ** 2 + a_y ** 2)
    len_b = math.sqrt(b_x ** 2 + b_y ** 2)
    cos_val = ab / (len_a * len_b + 1e-8)
    cos_val = np.clip(cos_val, -1.0, 1.0)
    angle = math.acos(cos_val)
    return angle / math.pi * 180


# 1和2构成线，判断3距离线的位置
def get_distance(x1, y1, x2, y2, x3, y3):
    k = (y2 - y1) / (x2 - x1 + 1e-8)
    b = y1 - k * x1
    return (k * x3 + b) - y3


# 判断双手是否在电脑/手机的bbox内，或到bbox的距离小于阈值
def get_bool(hand_x1, hand_y1, hand_x2, hand_y2, min_x, min_y, max_x, max_y, type):
    dist_th = 125.0 if type == "phone" else 175.0
    # 两只手到bbox四边的最小距离
    min_dist = min(abs(min_x - hand_x1), abs(max_x - hand_x1), abs(min_y - hand_y1), abs(max_y - hand_y1),
                   abs(min_x - hand_x2), abs(max_x - hand_x2), abs(min_y - hand_y2), abs(max_y - hand_y2))
    # print(min_dist, end="  ")
    # 如果手在物体bbox内，或到bbox的距离小于阈值
    if (hand_x1 >= min_x and hand_x1 <= max_x and hand_y1 >= min_y and hand_y1 <= max_y) or (
            hand_x1 >= min_x and hand_x1 <= max_x and hand_y1 >= min_y and hand_y1 <= max_y) or min_dist < dist_th:
        return True
    return False


def get_obj_info(root_path, index):
    threshold = 0.15
    hand_file = open(os.path.join(root_path, "results",
                                  "txt", index, "hand.txt"), "r")
    laptop_file = open(os.path.join(root_path, "results",
                                    "txt", index, "laptop.txt"), "r")
    phone_file = open(os.path.join(root_path, "results",
                                   "txt", index, "phone.txt"), "r")
    book_file = open(os.path.join(root_path, "results",
                                  "txt", index, "book.txt"), "r")
    hand = hand_file.readlines()  # 读入的hand文件以列表形式存储，列表的一个元素对应原文件的一行
    laptop = laptop_file.readlines()
    phone = phone_file.readlines()
    book = book_file.readlines()

    # 从pose.json文件里面读取数据
    f = open(os.path.join(root_path, "results",
                          "pose", index, "json", "pose.json"), "r")
    json_dict = json.load(f)

    object_lst = []
    IOU_lst = []
    for i in range(0, len(laptop)):  # 遍历每一行，即每个图片
        lst = laptop[i].split(" ")[0].split("/")  # os.sep
        file_name = lst[-2] + "/" + lst[-1]  # 如1/1127_1_1.jpg
        obj_dic = dict()  # 每张图片对应的字典
        # 包含物体检测信息的图像路径
        obj_dic.setdefault("address", os.path.join("img", index, file_name))
        obj_dic.setdefault("hand_exist", 0)
        obj_dic.setdefault("laptop_exist", 0)
        obj_dic.setdefault("phone_exist", 0)
        obj_dic.setdefault("book_exist", 0)
        obj_dic.setdefault("playphone_exist", 0)
        obj_dic.setdefault("playlaptop_exist", 0)
        obj_dic.setdefault("readbook_exist", 0)
        # 以文件名1127_1_1.jpg中第2个数*1000+第3个数作为时间戳
        name = file_name.split(".")[0]
        person_num = str(os.path.split(file_name)[0])
        obj_dic.setdefault(
            "time", person_num + str(int(name.split("_")[1]) * 100 + int(name.split("_")[2])))

        hand_lst = hand[i].split(" ")[1:]
        laptop_lst = laptop[i].split(" ")[1:]
        phone_lst = phone[i].split(" ")[1:]
        book_lst = book[i].split(" ")[1:]
        hand_dct = json_dict[file_name]
        # 根据姿态检测得到的两手的坐标
        hand_x1, hand_y1, hand_x2, hand_y2 = float(hand_dct[4 * 3]), float(hand_dct[4 * 3 + 1]), float(
            hand_dct[7 * 3]), float(hand_dct[7 * 3 + 1])

        # 手、笔记本电脑、手机、玩电脑、玩手机是否存在
        if float(phone_lst[0]) != 0. or float(phone_lst[1]) != 0. or float(phone_lst[2]) != 0. or float(
                phone_lst[3]) != 0.:
            obj_dic["phone_exist"] = 1
        if float(book_lst[0]) != 0. or float(book_lst[1]) != 0. or float(book_lst[2]) != 0. or float(
                book_lst[3]) != 0.:
            obj_dic["book_exist"] = 1

        if float(hand_lst[0]) != 0. or float(hand_lst[1]) != 0. or float(hand_lst[2]) != 0. or float(
                hand_lst[3]) != 0.:
            obj_dic["hand_exist"] = 1
            for j in range(0, len(hand_lst) // 6 * 6, 6):  # 每一个手
                # 判断是否在玩手机
                if obj_dic["phone_exist"] == 1:
                    hand_bbox = (
                        float(hand_lst[j]), float(hand_lst[j + 1]), float(hand_lst[j + 2]), float(hand_lst[j + 3]))
                    for k in range(0, len(phone_lst) // 6 * 6, 6):  # 每一个手机
                        phone_bbox = (
                            float(phone_lst[k]), float(phone_lst[k + 1]), float(phone_lst[k + 2]), float(phone_lst[k + 3]))
                        iou = get_IOU(phone_bbox, hand_bbox)
                        IOU_lst.append(iou)
                        if iou > 0.13:
                            obj_dic["playphone_exist"] = 1
                            # print("图片%s中的人正在玩手机！" % file_name)
                            break
                # 判断是否在读书
                if obj_dic["book_exist"] == 1:
                    hand_bbox = (
                        float(hand_lst[j]), float(hand_lst[j + 1]), float(hand_lst[j + 2]), float(hand_lst[j + 3]))
                    for k in range(0, len(book_lst) // 6 * 6, 6):  # 每一本书
                        book_bbox = (
                            float(book_lst[k]), float(book_lst[k + 1]), float(book_lst[k + 2]), float(book_lst[k + 3]))
                        iou = get_IOU(book_bbox, hand_bbox)
                        IOU_lst.append(iou)
                        if iou > 0.20:
                            obj_dic["readbook_exist"] = 1
                            # print("图片%s中的人正在读书！" % file_name)
                            break
        if float(laptop_lst[0]) != 0. or float(laptop_lst[1]) != 0. or float(laptop_lst[2]) != 0. or float(
                laptop_lst[3]) != 0.:
            obj_dic["laptop_exist"] = 1
            for k in range(0, len(laptop_lst) // 6 * 6, 6):  # 每一个笔记本电脑
                if get_bool(hand_x1, hand_y1, hand_x2, hand_y2, float(laptop_lst[k]), float(laptop_lst[k + 1]),
                            float(laptop_lst[k + 2]), float(laptop_lst[k + 3]), "laptop"):
                    obj_dic["playlaptop_exist"] = 1
                    # print("图片%s中的人正在玩笔记本电脑！" % file_name)
                    break
                # print("电脑  ", file_name)

        object_lst.append(obj_dic)
    # print(sorted(IOU_lst))
    return object_lst


"""
0：鼻尖  1：脖子 2：右肩  3：右肘  4：右腕  5：左肩  6：左肘  7：左腕
低头检测：鼻尖低于双肩的位置，或鼻尖到双肩距离的绝对值小于0.13
举手检测：左肘-左肩和左肘-左腕向量间或右肘-右肩和右肘-右腕向量间的夹角不大于40°；手腕与对应肩关节点相对高度差不大于0.48
侧身检测：两肩的斜率大于0.5
"""


def get_pose_info(root_path, index):
    # 从pose.json文件里面读取数据
    f = open(os.path.join(root_path, "results",
                          "pose", index, "json", "pose.json"), "r")
    json_dict = json.load(f)
    pose_lst = []
    for key in json_dict.keys():  # json中的每一个键，即每一张图片
        pose_dic = dict()  # 每张图片对应的字典
        pose_dic.setdefault("address", os.path.join("pose", index, "img", key))
        pose_dic.setdefault("raisehand_exist", 0)
        pose_dic.setdefault("bow_exist", 0)
        pose_dic.setdefault("lean_exist", 0)
        name = key.split(".")[0]
        person_num = str(os.path.split(key)[0])
        pose_dic.setdefault(
            "time", person_num + str(int(name.split("_")[1]) * 100 + int(name.split("_")[2])))
        point = json_dict[key]  # 共18个关节点，每个关节点3个数，共54个数
        x, y, conf = [], [], []  # 骨骼节点坐标和置信度
        for i in range(0, 3 * 18, 3):  # 只用到前8个骨骼点
            x.append(point[i])
            y.append(point[i + 1])
            conf.append(point[i + 2])

        # 低头抬头判断
        dist = get_distance(x[2], y[2], x[5], y[5], x[0], y[0]) / (
            math.sqrt((x[2] - x[5]) ** 2 + (y[2] - y[5]) ** 2) + 1e-8)
        if dist > 0 and abs(dist) > 0.15:
            pose_dic["bow_exist"] = 1
            # print("图片%s中的同学处于抬头听课状态" % key)

        # 如果出于低头状态，则一定不处于举手或侧身状态
        if pose_dic["bow_exist"] != 1:
            pose_lst.append(pose_dic)
            continue

        # 举手状态判断
        th_angle = 40.0
        angle1 = get_angle(x[3], y[3], x[2], y[2], x[3],
                           y[3], x[4], y[4])  # 节点顺序有要求：公共节点、节点1、公共节点、节点2
        angle2 = get_angle(x[6], y[6], x[5], y[5], x[6], y[6], x[7], y[7])
        h1 = abs((y[4] - y[2]) / (math.sqrt((x[2] - x[5])
                                            ** 2 + (y[2] - y[5]) ** 2) + 1e-8))
        h2 = abs((y[5] - y[7]) / (math.sqrt((x[2] - x[5])
                                            ** 2 + (y[2] - y[5]) ** 2) + 1e-8))
        w1 = abs(x[4] - (x[2] + x[5]) / 2) / abs(x[2] - x[5] + 1e-8)
        w2 = abs(x[7] - (x[2] + x[5]) / 2) / abs(x[2] - x[5] + 1e-8)
        if (angle1 < th_angle and h1 < 0.48 and w1 >= 0.6) or (angle2 < th_angle and h2 < 0.48 and w2 >= 0.6):
            pose_dic["raisehand_exist"] = 1
            # print("图片%s中的同学处于举手状态" % key)

        # 侧身状态判断
        resnn1 = (x[14] - x[16]) * (x[14] - x[16]) + \
            (y[14] - y[16]) * (y[14] - y[16])
        resnn2 = (x[15] - x[17]) * (x[15] - x[17]) + \
            (y[15] - y[17]) * (y[15] - y[17])
        resn = abs(resnn1 - resnn2) * 0.0001
        if resn >= 0.2 or abs(y[2] - y[5]) / abs(x[2] - x[5] + 1e-8) >= 0.5:
            pose_dic["lean_exist"] = 1
            # print("图片%s中的同学处于侧身状态" % key)
        pose_lst.append(pose_dic)
    return pose_lst

'''
def faceRecognition(face_path, unit):
    paths = glob.glob(os.path.join(face_path, "*.jpg"))
    url = "https://face-reg.ecnu.edu.cn/api/service/find/v3"
    timeStamp = "1602481107645"  # str(int(round(time.time() * 1000)))
    accessKey = "classroom-analysis"
    nonceStr = "test"
    sign = "A0CCA194328D79B858BC8C54B42FBF20"
    data = {"timeStamp": timeStamp, "accessKey": accessKey,
            "nonceStr": nonceStr, "sign": sign}

    face_dct = dict()
    for path in paths:
        file_name = os.path.split(path)[1]
        files = [("image", open(path, "rb"))]
        r = requests.post(url, data, files=files).text.replace("null", "None")
        result = eval(r)
        if result["code"] == 0:
            id = result["result"]["number"]
            name = result["result"]["name"]
            face_dct.setdefault(file_name, [id, name])
    return face_dct
'''


def faceRecognition(face_path, unit):
    path = glob.glob(os.path.join(face_path, "*.jpg"))
    url = "http://172.20.4.171:8082/api/service/queryInUnit"
    data = {"timeStamp": str(int(round(time.time() * 1000))), "accessKey": "test",
            "nonceStr": "test", "sign": "test", "unit": unit}

    face_dct = dict()
    for img in path:
        img_name = os.path.split(img)[1]
        files = [("image", open(img, "rb"))]
        r = eval(requests.post(url, data, files=files).text.replace("null", "''"))
        # print(r)
        if r["code"] == 0:
            cid = r["result"]["number"]
            name = r["result"]["name"]
            face_dct.setdefault(img_name, [cid, name])
    return face_dct



def get_person_name(root_path, index, unit):
    # 列表中每个元素有三个值，分别是学号、姓名、图像列表
    face_lst = []
    '''
    face_lst = [ ["学号1", "姓名1", "图像列表1"], ["学号2", "姓名2", "图像列表2"], ... ]
    '''
    '''
    face_dct = {"学号1": {
                "name": "张三",
                "images":["1127_2_1.jpg", "1127_3_5.jpg", "1127_4_1.jpg", "1127_5_4.jpg", "1127_6_2.jpg", "1127_7_5.jpg"]
                },
                ...}
    '''
    # 执行人脸识别代码，得到图片名和学号、人名的对应关系
    '''
    img_person={ "1127_2_1.jpg":[学号，姓名],
                    ...}
    '''
    for cur_path in glob.glob(os.path.join(root_path, "results", "face", index, "*")):
        img_person = faceRecognition(cur_path, unit)
        # print("img_person:\n",img_person)
        # 记录学号出现次数的字典，键为学号，值为[学号出现次数，学生名]
        num_dct = dict()
        for value in img_person.values():
            if value[0] in num_dct.keys():
                num_dct[value[0]][0] += 1
            else:
                num_dct[value[0]] = [1, value[1]]
        # 寻找出现次数最多的学号
        mx, stud_num = 0, 0
        for cur_stud_num, num in num_dct.items():
            if num[0] > mx and len(cur_stud_num) != 0:
                mx = num[0]
                stud_num = cur_stud_num
        # 获取每个人名对应的图像列表，并添加到字典中
        pic_lst, time_lst = glob.glob(
            os.path.join(root_path, "results", "img", index, cur_path.split(os.sep)[-1], "*.jpg")), []
        # print("pic_lst:\n",pic_lst)
        # 得到图像名对应的时间，并将图像名列表按时间序排列。同时将图像名改为相对路径
        for i, pic in enumerate(pic_lst):
            pic_lst[i] = pic.replace(rtPath, "")
            name = os.path.splitext(os.path.split(pic)[1])[0]
            time = int(name.split("_")[1]) * 100 + int(name.split("_")[2])
            time_lst.append(time)
        idx = np.argsort(time_lst)
        pic_lst = list(np.array(pic_lst)[idx])
        # print("stud_num: ",stud_num)
        if stud_num != 0:
            face_lst.append([stud_num, num_dct[stud_num][1], pic_lst])
        else:
            face_lst.append(["-", "未识别", pic_lst])
    return face_lst


if __name__ == '__main__':
    # root_path = r"C:\Users\zuzhiang\Desktop"
    # index = "c75face1-8721-46af-860c-982eecd59da4"

    root_path = sys.argv[1]
    index = str(sys.argv[2])
    unit = str(sys.argv[3])
    print("root_path: ", root_path)
    print("index: ", index)

    # 获取手机、电脑、手、书籍等物品，玩手机、玩电脑、读书等动作是否存在
    print("get_obj_info......")
    obj_lst = get_obj_info(root_path, index)
    print("len(obj_lst): ", len(obj_lst))

    # 获取低头、举手、侧身等信息
    print("get_pose_info......")
    pose_lst = get_pose_info(root_path, index)
    print("len(pose_lst): ", len(pose_lst))
    obj_lst = sorted(obj_lst, key=operator.itemgetter('time'))
    pose_lst = sorted(pose_lst, key=operator.itemgetter('time'))
    # print("obj:\n",obj_lst)
    # print("pose:\n",pose_lst)
    # 两个列表整体按时间排序，则按人取的时候也是有序的
    # 获取图像与人的对应关系
    print("get_person_name......")
    face_lst = get_person_name(root_path, index, unit)
    print("face_lst:\n", len(face_lst))

    # 生成最终的返回字典
    return_dct = dict()
    # 记录每个学号出现的次数
    number_dct = dict()
    vis = [0 for _ in range(len(obj_lst))]
    # 学号和图像名列表
    print("get_return_json......")
    for item in face_lst:
        number, name, img_lst = item[0], item[1], item[2]
        # 当前学号对应的字典
        person_dct = dict()
        object, pose = [], []
        # f0, f1, f2 = 0, 0, 0
        ply_phone_num, ply_laptop_num, read_book_num, raise_hand_num, bow_num, lean_num = 0, 0, 0, 0, 0, 0
        # 遍历属于当前人名的图片
        for pic_name in img_lst:
            for i in range(len(obj_lst)):
                # print("pic num: ", pic_name, "  address: ", obj_lst[i]["address"], "  equal: ",
                #      pic_name == obj_lst[i]["address"])

                if vis[i] == 0 and (pic_name == obj_lst[i]["address"]):
                    vis[i] = 1
                    object.append(obj_lst[i])
                    pose.append(pose_lst[i])
                    # 如果出于抬头状态，则不能同时出于其他状态
                    if pose_lst[i]["bow_exist"] == 1:
                        obj_lst[i]["playphone_exist"] = 0
                        obj_lst[i]["playlaptop_exist"] = 0

                    if obj_lst[i]["playphone_exist"] == 1:
                        ply_phone_num += 1
                    if obj_lst[i]["playlaptop_exist"] == 1:
                        ply_laptop_num += 1
                    if obj_lst[i]["readbook_exist"] == 1:
                        read_book_num += 1

                    if pose_lst[i]["raisehand_exist"] == 1:
                        # if f0 == 0:
                        raise_hand_num += 1
                    # f0 = pose_lst[i]["raisehand_exist"]
                    if pose_lst[i]["bow_exist"] == 1:
                        # if f1 == 0:
                        bow_num += 1
                    # f1 = pose_lst[i]["bow_exist"]
                    if pose_lst[i]["lean_exist"] == 1:
                        # if f2 == 0:
                        lean_num += 1
                    # f2 = pose_lst[i]["lean_exist"]
        person_dct.setdefault("name", name)
        person_dct.setdefault("playphone_num", ply_phone_num)
        person_dct.setdefault("playlaptop_num", ply_laptop_num)
        person_dct.setdefault("readbook_num", read_book_num)
        person_dct.setdefault("raisehand_num", raise_hand_num)
        person_dct.setdefault("bow_num", bow_num)
        person_dct.setdefault("lean_num", lean_num)
        person_dct.setdefault("object", object)
        person_dct.setdefault("pose", pose)
        if number in number_dct.keys():
            number_dct[number] += 1
            return_dct.setdefault(number+"_"+str(number_dct[number]), person_dct)
        else:
            number_dct[number] = 0
            return_dct.setdefault(number, person_dct)

    # print("return_dct:\n",return_dct)
    save_path = os.path.join(root_path, "results", "return")
    if not os.path.exists(save_path):
        os.makedirs(save_path)
    f = open(os.path.join(save_path, index + ".txt"), "w")
    json.dump(return_dct, f)
    f.close()

    print("Json file has written")
