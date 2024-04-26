package controller;
import service.*;

import java.util.Scanner;

import static service.EncoderFile.encrypt;


public class management {
    private String key = "O#kF4&tZ9kq^8Dx!3TmWpYg$eFs@Jh*N"; // 随机产生的密钥


    public void run() {
        Scanner scanner = new Scanner(System.in);
        showHelp();
        op_package opPackage = new op_package("src\\WorkSpace");
        while (true){
            System.out.println("当前路径为:"+opPackage.getPackage_path());
            System.out.println("请输入操作：(输入help查看帮助)");
            String ins = scanner.nextLine();
            String[] insArr = ins.split(" ");
            String op = insArr[0];
            switch (op) {
                case "help":
                    showHelp();
                    break;
                case "cd":
                    if (checkArgs(insArr, 2)) {
                        break;
                    }
                    boolean flag1 = opPackage.move_package(insArr[1]);
                    if(flag1) {
                        System.out.println("文件夹打开成功");
                    }else {
                        System.out.println("文件夹打开失败");
                    }
                    break;
                case "to":
                    if (checkArgs(insArr, 2)) {
                        break;
                    }
                    boolean flag2 = opPackage.move_child_package(insArr[1]);
                    if(flag2) {
                        System.out.println("子文件夹打开成功");
                    }else {
                        System.out.println("子文件夹打开失败");
                    }
                    break;
                case "open":
                    if (checkArgs(insArr, 2)) {
                        break;
                    }
                    opPackage.openFile(insArr[1]);
                    break;
                case "ls":
                    opPackage.listAll();
                    break;
                case "mkdir":
                    if (checkArgs(insArr, 2)) {
                        break;
                    }
                    boolean flag3 = opPackage.create_package(insArr[1]);
                    if (flag3){
                        System.out.println("文件夹创建成功");
                    }else {
                        System.out.println("文件夹创建失败");
                    }
                    break;
                case "del":
                    if (checkArgs(insArr, 2)) {
                        break;
                    }
                    boolean flag4 = opPackage.delete_package(insArr[1]);
                    if (flag4){
                        System.out.println("文件删除成功");
                    }else {
                        System.out.println("文件删除失败");
                    }
                    break;
                case "copy":
                    if (checkArgs(insArr, 3)) {
                        break;
                    }
                    opPackage.copy(insArr[1],insArr[2]);
                    break;
                case "zip":
                    if (checkArgs(insArr, 2)) {
                        break;
                    }
                    opPackage.zip(insArr[1]);
                    break;
                case "unzip":
                    if (checkArgs(insArr, 2)) {
                        break;
                    }
                    opPackage.unzip(insArr[1]);
                    break;
                case "encode":
                    if (checkArgs(insArr, 3)) {
                        break;
                    }
                    try {
                        encrypt(key, opPackage.getPackage_path()+"\\" + insArr[1], opPackage.getPackage_path()+ "\\" + insArr[2]);
                        System.out.println("文件加密完成。");
                    } catch (Exception e) {
                        System.out.println("加密过程中出现错误：" + e.getMessage());
                    }
                    break;
                case "decode":
                    if (checkArgs(insArr, 3)) {
                        break;
                    }
                    DecoderFile decoder = new DecoderFile(opPackage.getPackage_path() + "\\" + insArr[1], opPackage.getPackage_path()+"\\" + insArr[2], key);
                    decoder.decode();
                    break;
                case "quit":
                    System.out.println("欢迎再次使用文件管理系统，再见！");
                    return;
                default:
                    System.out.println("输入错误,请重新输入");
            }
        }

    }

    // 展示提示
    public void showHelp() {
        System.out.println("欢迎使用文件管理系统，本系统操作如下:");
        System.out.println("进入指定文件夹:            cd 绝对路径( .. 表示返回上一级)");
        System.out.println("进入子文件夹:              to 子文件夹名称");
        System.out.println("打开文本文件:              open 文件名");
        System.out.println("查看当前目录下的文件:       ls");
        System.out.println("在当前目录下创建文件夹:      mkdir 文件夹名称");
        System.out.println("删除当前目录下的文件夹:      del 文件夹名称");
        System.out.println("复制当前目录下的文件或文件夹: copy 文件名称 目标绝对路径");
        System.out.println("压缩当前目录下的文件或文件夹: zip 文件名称");
        System.out.println("解压当前目录下的压缩包：     unzip 文件名称");
        System.out.println("加密当前路径下的文件:       encode 文件名称 加密后的文件名");
        System.out.println("解密当前路径下的文件:       decode 文件名称 解密后的文件名");
        System.out.println("退出系统:                 quit");
    }

    // 检查输入的参数是否足够
    public boolean checkArgs(String[] insArr, int num) {
        if (insArr.length < num) {
            System.out.println("输入的参数不足，请重新输入");
            return true;
        }
        return false;
    }

}
