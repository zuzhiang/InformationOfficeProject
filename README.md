### 文件夹

- `/home/jchen/zuzhiang`：根目录，原为`/home/syzhou/zuzhiang`

- `./code`：代码文件夹

  - `test_img.py`：将不能正确读取的图片进行删除

  - `video2img.py`：用ffmpg将视频分割为大图片，保存在`.results/img/视频编号`下，改为上传图片而不是上传视频后已弃用

  - `crop_img.py`：按照姿态检测的结果，确定人上半身所在的bounding box，据此裁剪图片并保存，当不同图片中的bbox的IOU超过阈值时，则认为是同一个人，即按照位置确定是否是同一个人。每个裁剪后人对应一个文件夹，文件夹里是当前位置所有的人物图片，并形成新的pose.json文件。此外还要裁剪人脸图片，并只保存最多100张，以节约时间
  - `draw_bbox.py`：根据手机、电脑、书籍的检测结果绘制bounding box，这样是为了避免前面的检测对后面的检测产生影响
  - `get_score.py`：根据hand.txt/laptop.txt/phone.txt获取手机、电脑、手等物品，玩手机、玩电脑等动作是否存在；根据pose.json获取低头、举手、侧身等信息；使用人脸识别算法获取图像和人名的对应关系；根据以上得到的信息得到最终的返回字典，并以json格式保存

* `./videos`：存放视频数据，每个视频有唯一的编号（改为上传图片后已弃用）

* `./images`：存放上传的原始图片数据，每组数据对应一个文件夹，文件夹下有若干图片

* `./results`：存放结果图片和json文件等
  
  * `img/编号`：先存放根据位置截得的图片，每个人对应一个文件夹，文件夹下是当前人对应的所有图片。然后对其进行物品检测，并覆盖原来的图片
  * `txt`：存放物品检测的3个txt文件，里面保存了物品的坐标
  * `pose/编号`：存放姿态检测的图片和json文件，有两个子文件夹
    * `img`：姿态检测的小图片结果，其结构与`./results/img`类似
    * `json`：姿态检测的json结果，`alphapose-results.json`是姿态识别生成的原始json文件，`pose.json`是由`crop_img.py`产生的简化版json文件
  * `face/编号`：存放每人人脸图片
  
  - `return`：存放最终返回的JSON文件
  
* `FaceNet`：存放人脸识别相关的代码（使用学校的人脸识别接口后已弃用）
  
  - `face_rcgn.py`：人脸识别的代码，将给定路径下的图片与数据库（`emb_img`文件夹下的）图片进行对比，返回结果是图像名:人名的字典
  
* `yolo_hand`：检测手的代码
* `yolov4`：检测手机、电脑、书籍的代码，所有检测结果标注在同一张图片中
* `AlphaPose`：姿态检测的代码，生成骨架图以及骨架节点的json文件
* `project`：存放前后端、数据库的SpringBoot的相关代码

### 物品检测和姿态检测结果

yolo检测物品产生的txt文件中的数值分别表示：左上角和右下角的坐标、类别、置信度，一共有6个数。如果一张图片有多个数，则在同一行内写入。

---

![pose](https://s1.ax1x.com/2020/09/24/0pBzxe.png)

姿态检测产生的原始JSON文件的格式为：

```json
{
    "1127_4_19.jpg": {
        "version": "AlphaPose v0.2", 
        "bodies": [
            {
                "joints": [
                    关节点x轴坐标,
                    关节点y轴坐标，
                    关节点置信度，
                    ...
                ]
            },
            ...
        ]
    },
    "1127_2_28.jpg": {
        ...
    },
    ...
}
```

每张图片有多个joints，一个joints对应一个人，joints当中共有54个数字，每三个一组，分别是横坐标、纵坐标和置信度，共18个骨骼节点。

---

姿态识别产生的最终pose.json文件格式如下：

```
{
	"图片1名":[
		关节点1的x坐标,
		关节点1的y坐标,
		关节点1的置信度,
		...
	],
	"图片2名":[
	    ...
	],
	...
}
```

即每张图片对应的18个关节点信息

### 返回文件格式

加入人脸识别后为：

```json
{
    "学号1":{
        "name":"人名1",
        "playphone_num": "1", //玩手机次数
        "playlaptop_num": "1", //玩电脑次数
        "readbook_num": "1", //读书次数
        "raisehand_num": "1", //举手的次数
        "bow_num": "1", //低头的次数
        "lean_num": "1", //侧身的次数
        "object":[
    		{"address":"/path", //图像路径
    		 "hand_exist": "1", //是否存在手             
             "phone_exist": "1", //是否存在手机
             "laptop_exist": "1", //是否存在笔记本电脑
             "book_exist": "1", //是否存在书籍
             "playphone_exist": "1", //是否存在玩手机
             "playlaptop_exist": "1", //是否存在玩电脑 
             "readbook_exist": "1", //是否存在读书
             "time":"1"
    		},
            ...
    	],		
        "pose":[
            {"address":"/path", //图像路径
    		 "raisehand_exist": "1", //是否存在举手
             "bow_exist": "1", //是否存在低头
             "lean_exist": "1", //是否存在侧身
             "time":"1"
    		},
            ...
        ]
	},
	"学号2":{
        ...
    },
    ...
}
```

### 代码流程

1. 上传一组图片到服务器，原图片名为`ch01_00000000018018000.jpg`，上传后图片的命名为`UUID_00000000018018000.jpg`；
2. 对图片进行筛查，去除掉不能正确读取的图片；
3. 对大图片进行姿态检测，保存在`./results/pose/课程编号/img`目录下，生成的json文件名为`alphapose-results.json`，格式如上述；
4. 根据姿态检测的json文件对每套骨骼节点进行判断，如果有超过5个置信度超过阈值的则说明检测到了人，然后根据关节点的位置确定人的上半身所在的bounding box区域（因下半身通常有遮挡，不准确），并据此裁剪图片，并根据位置确定是否为同一人，小图片命名为`UUID_00000000018018000_当前人物下第几张图片.jpg`。此外还会裁剪人脸图片，并至多保存100张，以节约时间；
5. 再对小图片进行物品（手、笔记本电脑、手机、书籍）检测，保存在`.results/img/课程编号`文件夹下，并生成相应的txt文件保存在`./results/txt/课程编号`目录下；
6. 根据物品检测的txt文件判断手和物品的IOU是否超过阈值，是否玩电脑用手腕的关节点是否距离电脑的bbox足够近来判断，并记录物品数和行为数；
7. 根据姿态检测的pose.json文件判断是否有举手、低头、侧身等动作，并记录每个动作的次数；
8. 统计每个人对应的图片列表和相关信息，并利用人脸识别接口确认这些图像对应哪个人，按时间进行排序并统计各个动作出现的次数；
9. 根据第6、7、8步产生相应的json文件并写入到`./results/return/课程编号.txt`文档中，等待前台读取并处理。

### 单步执行

图片筛查

```bash
python /home/jchen/zuzhiang/code/test_img.py /home/jchen/zuzhiang 1127
```

姿态检测

```bash
python /home/jchen/zuzhiang/AlphaPose/demo.py --indir /home/jchen/zuzhiang/images/1127 --outdir /home/jchen/zuzhiang/results/pose/1127 --json_path /home/jchen/zuzhiang/results/pose/1127/json --save_img --format cmu
```

根据姿态检测结果裁剪图片

```bash
python /home/jchen/zuzhiang/code/crop_img.py 1127 /home/jchen/zuzhiang
```

手机的检测

```bash
python /home/jchen/zuzhiang/yolov4/detect_phone.py --index 1127 --device 0,1
```

笔记本电脑的检测

```bash
python /home/jchen/zuzhiang/yolov4/detect_laptop.py --index 1127 --device 0,1
```

书籍的检测

```bash
python /home/jchen/zuzhiang/yolov4/detect_book.py --index 1127 --device 0,1
```

手的检测

```bash
python /home/jchen/zuzhiang/yolo_hand/detect.py --source /home/jchen/zuzhiang/results/img/1127 --txtPath /home/jchen/zuzhiang/results/txt/1127/laptop.txt
```

根据手机、电脑、书籍的检测结果画矩形框

```bash
python /home/jchen/zuzhiang/code/draw_bbox.py --index 1127
```

计算得分

```bash
python /home/jchen/zuzhiang/code/get_score.py /home/jchen/zuzhiang 1127 WF401
```

---

视频转图片（已弃用）

```bash
python /home/jchen/zuzhiang/code/video2img.py /home/jchen/zuzhiang/videos/1127  /home/jchen/zuzhiang/results/img/1127
```

FaceNet人脸识别（已弃用）

```bash
python /home/jchen/zuzhiang/FaceNet/face_rcgn.py /home/jchen/zuzhiang/results/face/1127/json 1127
```

---

## 数据库

**MySQL数据库地址**：`172.20.4.55`，端口：`3306`， 用户名：`root`，密码：`classEcnu123`

**数据库名**：`informationoffice`

**表**：一个课程有多个人，一个人有多张图片；所以一个video条目对应多个person条目，一个person条目对应多个image条目；

- `class`：暂时没用到
- `student`：学生对应的表，用于学号和姓名的自动关联
  - `student_id`：学生编号
  - `number`：学号
  - `name`：姓名
  - `unit`：班级代码
  - `teacher`：教师名
- `image`：图片界面对应的表
  - `image_id`：图片编号
  - `video_id`：图片所属的视频（课程）编号
  - `person_id`：图片所属的人物编号
  - `obj_path`：物体检测图片的路径
  - `pose_path`：姿态识别图片的路径
  - `hand_exist`：手是否存在
  - `phone_exist`：手机是否存在
  - `laptop_exist`：笔记本电脑是否存在
  - `play_phone_exist`：玩手机是否存在
  - `play_laptop_exist`：玩电脑是否存在
  - `raise_hand_exist`：举手是否存在
  - `bow_exist`：抬头听课（低头）是否存在
  - `lean__exist`：侧身是否存在
  - `time`：时间戳，用于排序
- `person`：人物列表界面对应的人物表
  - `person_id`：人物编号
  - `video_id`：人物所属的视频编号
  - `number`：学号
  - `name`：姓名
  - `play_phone_num`：玩手机次数
  - `play_laptop_num`：玩电脑次数
  - `raise_hand_num`：举手次数
  - `bow_num`：抬头听课次数
  - `lean_num`：侧身次数
- `user`：用户管理界面对应的用户表
  - `username`：用户名
  - `password`：密码
  - `role`：角色
- `video`：课程管理界面对应的课程（视频）表
  - `video_id`：课程编号
  - `teacher`：教师名
  - `place`：上课地点
  - `upload_date`：上传日期
  - `course_date`：课程日期
  - `state`：是否处理完
  - `owner`：所有者（由谁上传）

```mysql
# 根据课程编号查询所有person信息
select * from person where video_id="课程编号" order by number asc
# 例子
select * from person where video_id="63ab412c-11fb-4106-9bdd-5392f5c9c26d" order by number asc

# 根据课程编号查询所有image信息
select * from image where video_id="课程编号" order by time asc
# 例子
select * from image where video_id="63ab412c-11fb-4106-9bdd-5392f5c9c26d" order by time asc
```

