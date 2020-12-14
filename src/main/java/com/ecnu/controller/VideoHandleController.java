package com.ecnu.controller;

import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class VideoHandleController {
    static String root_path = new String("/home/jchen/zuzhiang");

    // 打印输出信息
    private static void printMessage(final InputStream input) {
        new Thread(new Runnable() {
            public void run() {
                Reader reader = new InputStreamReader(input);
                BufferedReader bf = new BufferedReader(reader);
                String line = null;
                try {
                    while ((line = bf.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 将视频切分成图片
    public static void video2img(String video_path, String big_img_path, String videoId) {
        // TODO Auto-generated method stub
        try {
            String[] cmd = new String[]{"python", root_path + "/code/video2img.py", video_path, big_img_path};
            final Process process = Runtime.getRuntime().exec(cmd);
            printMessage(process.getInputStream());
            printMessage(process.getErrorStream());
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    // 筛查图片，去除掉非.jpg文件和不能正常读取的图像
    public static void test_img(String index) {
        try {
            String[] cmd = new String[]{"python", root_path + "/code/test_img.py", root_path, index};
            final Process process = Runtime.getRuntime().exec(cmd);
            printMessage(process.getInputStream());
            printMessage(process.getErrorStream());
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 检测人物的骨架姿势，并得到骨骼点的坐标json文件和图像文件
    public static void pose(String in_path, String outdir, String json_path, String device) {
        try {
            String[] cmd = new String[]{"python", root_path + "/AlphaPose/demo.py", "--indir", in_path, "--outdir", outdir, "--json_path", json_path, "--device", device, "--save_img", "--format", "cmu"};
            final Process process = Runtime.getRuntime().exec(cmd);
            printMessage(process.getInputStream());
            printMessage(process.getErrorStream());
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 删除没有检测到人的图片
    public static void crop_img(String index) {
        try {
            String[] cmd = new String[]{"python", root_path + "/code/crop_img.py", index, root_path};
            final Process process = Runtime.getRuntime().exec(cmd);
            printMessage(process.getInputStream());
            printMessage(process.getErrorStream());
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 检测是否有手机/笔记本电脑/书籍
    public static void detect_objects(String cls, String index, String device) {
        try {
            String[] cmd = null;
            if (cls.equals("phone")) {
                cmd = new String[]{"python", root_path + "/yolov4/detect_phone.py", "--index", index, "--device", device};
            } else if (cls.equals("laptop")) {
                cmd = new String[]{"python", root_path + "/yolov4/detect_laptop.py", "--index", index, "--device", device};
            } else if (cls.equals("book")) {
                cmd = new String[]{"python", root_path + "/yolov4/detect_book.py", "--index", index, "--device", device};
            }
            final Process process = Runtime.getRuntime().exec(cmd);
            printMessage(process.getInputStream());
            printMessage(process.getErrorStream());
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 检测是否有手
    public static void detect_hand(String in_path, String txt_path, String device) {
        try {
            String[] cmd = new String[]{"python", root_path + "/yolo_hand/detect.py", "--source", in_path, "--txtPath", txt_path, "--device", device};
            final Process process = Runtime.getRuntime().exec(cmd);
            printMessage(process.getInputStream());
            printMessage(process.getErrorStream());
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 根据电脑和手机的检测结果画bbox
    public static void draw_bbox(String index) {
        try {
            // String[] cmd = new String[]{"python", root_path + "/yolo_laptop/detect.py","--source", in_path,"--txtPath" , txt_path};
            String[] cmd = new String[]{"python", root_path + "/code/draw_bbox.py", "--index", index};
            final Process process = Runtime.getRuntime().exec(cmd);
            printMessage(process.getInputStream());
            printMessage(process.getErrorStream());
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 对所有图片进行评分，包括检测到的物体和手的IOU得分，以及根据骨架姿势得到的行为得分
    public static void get_score(String index, String unit) {
        try {
            String[] cmd = new String[]{"python", root_path + "/code/get_score.py", root_path, index, unit};
            final Process process = Runtime.getRuntime().exec(cmd);
            printMessage(process.getInputStream());
            printMessage(process.getErrorStream());
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void videoHandle(String index, String unit) {
        System.out.println("index:     " + index);

        //String index=new String("1127"); //args[0];
        // String index = args[0];

        // 将视频切分成大图片，再切分为小图片
        String device = "0,1";
        String video_path = root_path + new String("/videos/") + index;
        String big_img_path = root_path + new String("/images/") + index;//root_path+ new String("/images/")+index;
        // String small_img_path=root_path+ new String("/results/img/")+index;
        // 直接上传图片，去除视频转图片的过程
        //System.out.println("\n\nVideo to images.............");
        //video2img(video_path, big_img_path, index);

        String json_path;
        String in_path = big_img_path;//root_path+ new String("/results/img/")+index;
        String out_path = root_path + new String("/results/img/") + index;
        String pose_out_path = root_path + new String("/results/pose/") + index;
        String txt = root_path + new String("/results/txt/") + index;
        File file = new File(txt);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }

        // 筛查图片，去除掉非.jpg文件和不能正常读取的图像
        System.out.println("\n\ntest_img");
        test_img(index);

        //姿态检测
        System.out.println("\n\npose");
        json_path = pose_out_path + "/json";
        pose(in_path, pose_out_path, json_path, device);
        System.out.println("\n\nCroping images......");
        crop_img(index);

        // 检测手机/笔记本电脑/书籍
        String[] classes = {"phone", "laptop", "book"};
        for (String cls : classes) {
            System.out.println("\n\n" + cls);
            detect_objects(cls, index, device);
        }

        // 检测手
        System.out.println("\n\nhand.pth");
        detect_hand(out_path, txt, device);

        // 根据手机/笔记本电脑/书籍的检测结果画bbox
        System.out.println("\n\ndraw bbox......");
        draw_bbox(index);

        // 计算视频的得分
        System.out.println("\n\nGet score");
        get_score(index, unit);

        System.out.println("end");
    }
}
