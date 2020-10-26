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
//        Process proc;
//        try {
//            String[] arguments = new String[]{"python", root_path + "/code/video2img.py", video_path, big_img_path};
//            proc = Runtime.getRuntime().exec(arguments);// 执行py文件
//            //用输入输出流来截取结果
//            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//            String line = null;
//            while ((line = in.readLine()) != null) {
//                System.out.println(line);
//            }
//            in.close();
//            proc.waitFor();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


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

    // 检测是否有手、笔记本电脑、笔三种物品
    public static void detect_objects(String weight, String in_path, String out_path, String txt_path) {
        try {
            String weight_path = root_path + "/yolo_objects/checkpoints/" + weight;
            String[] cmd = new String[]{"python", root_path + "/yolo_objects/detect.py", in_path, out_path, txt_path, "--weights_path", weight_path};
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

    // 检测是否有手机
    public static void detect_phone(String in_path, String out_path, String txt_path) {
        try {
            String[] cmd = new String[]{"python", root_path + "/yolo_phone/detect.py", in_path, out_path, txt_path};
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
    public static void pose(String in_path, String outdir, String json_path) {
        try {
            String[] cmd = new String[]{"python", root_path + "/AlphaPose/demo.py", "--indir", in_path, "--outdir", outdir, "--json_path", json_path, "--save_img", "--format", "cmu"};
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

    // 对所有图片进行评分，包括检测到的物体和手的IOU得分，以及根据骨架姿势得到的行为得分
    public static void get_score(String txt_root, String json_path, String index) {
        try {
            String[] cmd = new String[]{"python", root_path + "/code/get_score_new.py", root_path, txt_root, json_path, index};
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


    public static void videoHandle(String index) {
        System.out.println("index:     " + index);

        //String index=new String("1127"); //args[0];
        // String index = args[0];

        // 将视频切分成大图片，再切分为小图片
        String video_path = root_path + new String("/videos/") + index;
        String big_img_path = root_path + new String("/results/img/") + index;//root_path+ new String("/images/")+index;
        // String small_img_path=root_path+ new String("/results/img/")+index;
        System.out.println("\n\nVideo to images.............");
        video2img(video_path, big_img_path, index);

        int i;
        String weight, txt_path, img_path, json_path;
        String in_path = big_img_path;//root_path+ new String("/results/img/")+index;
        String out_path = root_path + new String("/results/img/") + index;
        String pose_out_path = root_path + new String("/results/pose/") + index;
        String txt = root_path + new String("/results/txt/") + index;
        File file = new File(txt);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }

        //姿态检测
        System.out.println("\n\npose");
        json_path = pose_out_path + "/json";
        pose(in_path, pose_out_path, json_path);
        System.out.println("\n\nCrop images.........................");
        crop_img(index);

        // 检测手和笔记本电脑
        String[] weights_list = {"hand.pth", "laptop.pth"};
        for (i = 0; i < 2; i++) {
            if (i == 0) {
                //out_path=out+"/hand";
                txt_path = txt + "/hand.txt";
            } else if (i == 1) {
                //out_path=out+"/laptop";
                txt_path = txt + "/laptop.txt";
            } else {
                //out_path=out+"/pen";
                txt_path = txt + "/pen.txt";
            }
            weight = weights_list[i];
            System.out.println("\n\n" + weight);
            detect_objects(weight, in_path, out_path, txt_path);
        }

        // 检测手机
        System.out.println("\n\nphone");
        //out_path=out+"/phone";
        txt_path = txt + "/phone.txt";
        detect_phone(in_path, out_path, txt_path);

        // 计算视频的得分
        System.out.println("\n\nGet score");
        get_score(txt, json_path, index);

        System.out.println("end");
    }
}
