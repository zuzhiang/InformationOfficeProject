import os
import time
import glob
import requests
import pandas as pd


def add2DB(face_path, unit):
    url = "http://172.20.4.171:8082/api/service/add"
    xls_path = r"C:\Data\1.Work\02.Code\zhaochun\students.xlsx"
    xls_file = pd.read_excel(xls_path).ix[:, [1, 2]].values
    dct = dict()
    for cid, name in xls_file:
        dct[str(cid)] = name

    data = {"timeStamp": str(int(round(time.time() * 1000))), "accessKey": "test",
            "nonceStr": "test", "sign": "test", "unit": unit}

    for path in glob.glob(os.path.join(face_path, "*")):
        cid = path.split("\\")[-1]
        name = dct[cid]
        print("cid: ", cid, "  name: ", name)
        data["cid"], data["name"] = cid, name
        for img in glob.glob(os.path.join(path, "*.jpg")):
            print(img)
            files = [("image", open(img, "rb"))]
            try:
                r = eval(requests.post(url, data, files=files).text)
                print(r)
            except:
                print("没有检测到人脸！！！！！！！！！！")
    print("end")


def faceDetect(face_path, unit):
    path = glob.glob(os.path.join(face_path, "*.jpg"))
    url = "http://172.20.4.171:8082/api/service/queryInUnit"
    data = {"timeStamp": str(int(round(time.time() * 1000))), "accessKey": "test",
            "nonceStr": "test", "sign": "test", "unit": unit}

    face_dct = dict()
    for img in path:
        img_name = os.path.split(img)[1]
        files = [("image", open(img, "rb"))]
        r = eval(requests.post(url, data, files=files).text.replace("null", "''"))
        print(r)
        if r["code"] == 0:
            cid = r["result"]["number"]
            name = r["result"]["name"]
            face_dct.setdefault(img_name, [cid, name])
        else:
            face_dct.setdefault(img_name, ["-", "未识别"])
    return face_dct


if __name__ == "__main__":
    face_path = r"C:\Data\1.Work\02.Code\zhaochun\FaceNet\test_img"
    face_dct = faceDetect(face_path,"WF401")
    print(face_dct)
