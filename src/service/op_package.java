package service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

//文件夹操作
public class op_package {

    private String package_path;
    private File thisPackage;

    //进入基础路径
    public op_package(String package_path){
        try {
            thisPackage = new File(package_path);
            this.package_path = thisPackage.getAbsolutePath();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //进入非子文件夹的其他文件夹
    public boolean move_package(String package_path){
        boolean flag = false;
        if (package_path.equalsIgnoreCase("..")) {
            // 获取文件的上级目录
            String parentDirectory = this.thisPackage.getParent();
            if (parentDirectory != null) {
                thisPackage = new File(parentDirectory);
                this.package_path = this.thisPackage.getAbsolutePath();
                flag=true;
                return flag;
            } else {
                System.out.println("已经是根目录，无法再返回上级目录。");
                flag=false;
                return flag;
            }
        }

        try {
            File tempFile= new File(package_path);
            //判断是否存在
            if(tempFile.exists()) {
                this.package_path = package_path;
                thisPackage = tempFile;
                flag=true;
            }else {
                flag=false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    //进入子文件夹
    public boolean move_child_package(String package_name){
        boolean flag = false;
        try {
            File tempFile = new File(this.package_path,package_name);
            if(tempFile.exists()) {
                this.package_path = this.package_path + "\\" + package_name;
                thisPackage=tempFile;
                flag=true;
            }else {
                flag=false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    //创建子文件夹
    public boolean create_package(String package_name){
        boolean flag = false;
        try {
            File new_File = new File(this.package_path, package_name);
            flag = new_File.mkdir();
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    //删除子文件夹
    public boolean delete_package(String package_name) {
        boolean flag=false;
        try {
            File linshi=new File(this.package_path + "\\" + package_name);
            if(linshi.exists()) {
                deleteDir(this.package_path + "\\" + package_name);
                flag=true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    //绝对路径删除文件夹
    private void deleteDir(String dirPath) {
        File file = new File(dirPath);
        if(file.isFile()) {
            file.delete();
        }else {
            File[] files = file.listFiles();
            if(files == null) {
                file.delete();
            }else {
                for (int i = 0; i < files.length; i++)
                {
                    deleteDir(files[i].getAbsolutePath());
                }
                file.delete();
            }
        }
    }

    // 打开文本文件
    public void openFile(String fileName) {
        File file = new File(thisPackage, fileName);
        if (!file.exists() || !file.isFile()) {
            System.out.println("文件不存在或不是一个有效的文件。");
            return;
        }

        if (!isTextFile(file)) {
            System.out.println("您选择的文件不是一个文本文件，请重新选择。");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            System.out.println("文件内容：");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("读取文件时发生错误：" + e.getMessage());
        }
    }

    // 检查文件是否为文本文件
    private boolean isTextFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return true; // 如果能够成功读取文件，说明是文本文件
        } catch (IOException e) {
            return false; // 如果出现异常，则说明不是文本文件
        }
    }

    // 列出所有文件信息
    public void listAll() {
        // 获取当前目录下的所有文件
        File[] files = thisPackage.listFiles();

        // 根据文件名、文件大小、文件类型、文件日期等过滤特定类型的文件
        if (files.length == 0) {
            System.out.println("当前目录为空。");
            return;
        }

        files = filterFiles(files);
        if (files.length == 0) {
            System.out.println("没有找到符合条件的文件。");
            return;
        }
        sortFiles(files);

        System.out.println("当前目录下的文件有：");
        // 输出文件信息
        for (File file : files) {
            System.out.println(formatFileInfo(file));
        }
    }

    // 过滤文件
    private File[] filterFiles(File[] files) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("请输入自定义过滤条件（例如：.txt && size>100KB && name=example.txt && date>2022-01-01））:");
        String customFilter = scanner.nextLine();

        // 创建自定义过滤条件
        Predicate<File> filterCondition = file -> {
            String[] conditions = customFilter.split("\\s+&&\\s+");
            boolean result = true;
            for (String condition : conditions) {
                if (condition.startsWith(".")) { // 根据文件扩展名过滤
                    result = result && file.getName().toLowerCase().endsWith(condition.toLowerCase());
                } else {
                    String[] parts = condition.split("[><=]");
                    String operator = condition.replaceAll("[^><=]", "");
                    switch (parts[0]) {
                        case "size": // 根据文件大小过滤
                            try {
                                long fileSize = Long.parseLong(parts[1].replaceAll("[^0-9]", ""));
                                String sizeUnit = parts[1].replaceAll("[^A-Za-z]", "").toUpperCase();
                                long actualSize = getFileSize(file);
                                if (sizeUnit.equals("KB")) {
                                    actualSize /= 1024;
                                } else if (sizeUnit.equals("MB")) {
                                    actualSize /= (1024 * 1024);
                                }
                                switch (operator) {
                                    case ">":
                                        result = result && (actualSize > fileSize);
                                        break;
                                    case "<":
                                        result = result && (actualSize < fileSize);
                                        break;
                                    case ">=":
                                        result = result && (actualSize >= fileSize);
                                        break;
                                    case "<=":
                                        result = result && (actualSize <= fileSize);
                                        break;
                                    case "=":
                                        result = result && (actualSize == fileSize);
                                        break;
                                    default:
                                        System.out.println("输入格式不正确，请输入正确的条件（例如：size>100KB）");
                                        return false;
                                }
                            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                                System.out.println("输入格式不正确，请输入正确的文件大小条件（例如：size>100KB）");
                                return false;
                            }
                            break;
                        case "name": // 根据文件名过滤
                            result = result && file.getName().equals(parts[1]);
                            break;
                        case "date": // 根据文件日期过滤
                            try {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Date filterDate = dateFormat.parse(parts[1]);
                                Date fileDate = new Date(file.lastModified());
                                switch (parts[1].charAt(0)) {
                                    case '>':
                                        result = result && (fileDate.after(filterDate));
                                        break;
                                    case '<':
                                        result = result && (fileDate.before(filterDate));
                                        break;
                                    case '=':
                                        result = result && (fileDate.equals(filterDate));
                                        break;
                                }
                            } catch (ParseException e) {
                                System.out.println("输入格式不正确，请输入正确的日期条件（例如：date>2022-01-01）");
                                return false;
                            }
                            break;
                    }
                }
            }
            return result;
        };

        // 根据过滤条件过滤文件数组
        return Arrays.stream(files)
                .filter(filterCondition)
                .toArray(File[]::new);
    }

    // 排序文件
    private void sortFiles(File[] files) {
        Comparator<File> comparator = null;

        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入排序方式（name, size, type, date）:");
        String sortBy = scanner.nextLine().toLowerCase();

        // 根据不同条件排序
        switch (sortBy.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(File::getName);
                break;
            case "size":
                comparator = Comparator.comparingLong(this::getFileSize);
                break;
            case "type":
                comparator = Comparator.comparing(file -> {
                    if (file.isDirectory()) {
                        return "directory";
                    } else {
                        String fileName = file.getName();
                        int index = fileName.lastIndexOf('.');
                        if (index > 0) {
                            return fileName.substring(index + 1);
                        } else {
                            return "";
                        }
                    }
                });
                break;
            case "date":
                comparator = Comparator.comparingLong(File::lastModified);
                break;
        }

        // 如果比较器不为null，则进行排序
        if (comparator != null) {
            Arrays.sort(files, comparator);
        }
    }

    // 格式化文件信息
    private String formatFileInfo(File file) {
        StringBuilder fileInfo = new StringBuilder();
        if (file.isDirectory()) {
            fileInfo.append("目录\t\t");
            fileInfo.append("名称: ").append(String.format("%-25s", file.getName()));
            long folderSize = getFolderSize(file);
            fileInfo.append(", 大小: ").append(String.format("%10d", folderSize)).append(" bytes");
        } else {
            fileInfo.append("文件\t\t");
            fileInfo.append("名称: ").append(String.format("%-25s", file.getName()));
            fileInfo.append(", 大小: ").append(String.format("%10d", file.length())).append(" bytes");
        }
        fileInfo.append(", 修改日期: ").append(new Date(file.lastModified()));
        return fileInfo.toString();
    }

    // 获取文件或文件夹的大小
    private long getFileSize(File file) {
        if (file.isFile()) {
            return file.length();
        } else {
            return getFolderSize(file);
        }
    }

    // 统计文件夹大小
    private long getFolderSize(File folder) {
        long size = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    size += getFolderSize(file); // 递归调用，统计子文件夹的大小
                }
            }
        }
        return size;
    }

    // 压缩文件或文件夹
    public void zip(String fileName) {
        File soureFile = new File(thisPackage, fileName);
        if (!soureFile.exists()) {
            System.out.println("文件不存在，请重新选择文件");
            return;
        }
        if (soureFile.isDirectory()){
            try {
                compressFolder(soureFile.getAbsolutePath(), package_path+"\\" + fileName + ".zip");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            try {
                compressFile(soureFile.getAbsolutePath(), package_path+"\\" + fileName + ".zip");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 压缩文件
    private void compressFile(String sourceFilePath, String compressedFilePath) throws IOException {
        // 创建输入流读取源文件
        FileInputStream fis = new FileInputStream(sourceFilePath);
        // 创建输出流写入压缩文件
        FileOutputStream fos = new FileOutputStream(compressedFilePath);
        // 创建压缩输出流
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        // 将文件添加到压缩包中
        ZipEntry zipEntry = new ZipEntry(new File(sourceFilePath).getName());
        zipOut.putNextEntry(zipEntry);

        // 将源文件内容写入压缩包
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }

        // 关闭流
        zipOut.close();
        fos.close();
        fis.close();

        System.out.println("文件压缩完成：" + compressedFilePath);
    }

    // 压缩文件夹
    private void compressFolder(String sourceFolderPath, String compressedFilePath) throws IOException {
        // 创建输出流写入压缩文件
        FileOutputStream fos = new FileOutputStream(compressedFilePath);
        // 创建压缩输出流
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        // 递归压缩文件夹
        File sourceFolder = new File(sourceFolderPath);
        addFolderToZip(sourceFolder, sourceFolder.getName(), zipOut);

        // 关闭流
        zipOut.close();
        fos.close();

        System.out.println("文件夹压缩完成：" + compressedFilePath);
    }

    // 压缩文件夹的辅助方法
    private void addFolderToZip(File folder, String parentFolder, ZipOutputStream zipOut) throws IOException {
        // 创建表示文件夹的 ZipEntry
        ZipEntry folderEntry = new ZipEntry(parentFolder + "/");
        zipOut.putNextEntry(folderEntry);

        // 获取文件夹中的所有文件和子文件夹
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                // 如果是子文件夹，递归调用addFolderToZip
                addFolderToZip(file, parentFolder + "\\" + file.getName(), zipOut);
                continue;
            }
            // 如果是文件，将文件添加到压缩包中
            FileInputStream fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(parentFolder + "\\" + file.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
    }

    // 解压文件
    public void unzip(String fileName) {
        File soureFile = new File(thisPackage, fileName);
        if (!soureFile.exists()) {
            System.out.println("文件不存在，请重新选择文件");
            return;
        }

        try {
            decompressFile(soureFile.getAbsolutePath(), package_path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 解压文件
    private void decompressFile(String zipFilePath, String destDirectory) throws IOException {
        // 创建目标目录
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // 创建输入流读取压缩文件
        FileInputStream fis = new FileInputStream(zipFilePath);
        // 创建ZIP输入流
        ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(fis));

        // 获取压缩包中的每一个条目
        ZipEntry zipEntry = zipIn.getNextEntry();

        while (zipEntry != null) {
            // 构建解压后文件的路径
            String filePath = destDirectory + File.separator + zipEntry.getName();

            // 如果条目名称以斜杠结尾，表示文件夹
            if (zipEntry.getName().endsWith("/")) {
                // 创建文件夹
                File dir = new File(filePath);
                dir.mkdirs();
            } else { // 否则是文件，解压文件
                extractFile(zipIn, filePath);
            }

            // 关闭当前条目
            zipIn.closeEntry();
            // 获取下一个条目
            zipEntry = zipIn.getNextEntry();
        }

        // 关闭流
        zipIn.close();
        fis.close();

        System.out.println("文件解压完成：" + destDirectory);
    }

    // 解压文件的辅助方法
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        // 创建输出流写入解压后文件内容
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        // 读取压缩输入流并写入输出流
        byte[] bytes = new byte[1024];
        int length;
        while ((length = zipIn.read(bytes)) != -1) {
            bos.write(bytes, 0, length);
        }
        // 关闭流
        bos.close();
    }

    // 选择拷贝方式
    public void copy(String sourcePath, String targetPath) {
        sourcePath = this.thisPackage.getAbsolutePath() + "\\" + sourcePath;

        // 检查源文件是否存在
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            System.out.println("源文件不存在，请重新输入。");
            return;
        }

        // 检查目标文件夹是否存在，如果不存在则创建
        File targetFolder = new File(targetPath);
        if (!targetFolder.exists()) {
            if (!targetFolder.mkdirs()) {
                System.out.println("无法创建目标文件夹，请检查目标路径是否正确。");
                return;
            }
        } else {
            // 检查目标路径是否为文件夹
            if (!targetFolder.isDirectory()) {
                System.out.println("目标路径不是一个文件夹，请重新输入。");
                return;
            }
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("请选择前台拷贝或者后台拷贝：(foreground/background)");

        String option = scanner.next();

        while (!option.equalsIgnoreCase("foreground") && !option.equalsIgnoreCase("background")) {
            System.out.println("无效的选项，请重新选择：(foreground/background)");
            option = scanner.nextLine();
        }

        if (option.equalsIgnoreCase("foreground")) {
            copyInForeground(sourcePath, targetPath);
        } else if (option.equalsIgnoreCase("background")) {
            copyInBackground(sourcePath, targetPath);
        }
    }

    // 后台异步执行拷贝任务
    public void copyInBackground(String sourcePath, String targetPath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();// 创建单线程的线程池
        executor.execute(() -> { // 使用 lambda 表达式创建一个新的线程执行任务
            long startTime = System.currentTimeMillis(); // 记录开始时间
            copyWithoutProgress(sourcePath, targetPath);
            long endTime = System.currentTimeMillis(); // 记录结束时间
            System.out.println("后台拷贝完成，总耗时：" + (endTime - startTime) + "毫秒"); // 输出拷贝耗时
        });
        executor.shutdown(); // 关闭线程池
    }

    // 前台执行拷贝任务，并显示时间和进度
    public void copyInForeground(String sourcePath, String targetPath) {
        long startTime = System.currentTimeMillis(); // 记录开始时间
        if (isDirectory(sourcePath)) { // 判断源路径是否为文件夹
            copyDirectory(new File(sourcePath), new File(targetPath)); // 复制文件夹
        } else {
            copyFile(new File(sourcePath), new File(targetPath)); // 复制文件
        }
        long endTime = System.currentTimeMillis(); // 记录结束时间
        System.out.println("拷贝完成，总耗时：" + (endTime - startTime) + "毫秒"); // 输出拷贝耗时
    }

    // 拷贝文件或文件夹，不显示进度
    private void copyWithoutProgress(String sourcePath, String targetPath) {
        if (isDirectory(sourcePath)) { // 判断源路径是否为文件夹
            copyDirectoryWithoutProgress(new File(sourcePath), new File(targetPath)); // 后台执行拷贝文件夹任务，不显示进度
        } else {
            copyFileWithoutProgress(new File(sourcePath), new File(targetPath)); // 后台执行拷贝文件任务，不显示进度
        }
    }

    // 拷贝文件夹，并显示进度条
    private void copyDirectory(File source, File target) {
        try {
            long totalBytes = getFolderSize(source); // 计算总字节数
            final long[] copiedBytes = {0}; // 已拷贝的字节数数组

            Files.walk(source.toPath()) // 使用 Files.walk() 遍历源目录下的所有文件和子目录
                    .forEach(sourcePath -> {
                        try {
                            Path targetPath = Paths.get(target.getAbsolutePath(), sourcePath.toString()
                                    .substring(source.getAbsolutePath().length())); // 构建目标路径
                            if (Files.isDirectory(sourcePath)) {
                                Files.createDirectories(targetPath); // 创建目标文件夹
                            } else {
                                Files.copy(sourcePath, targetPath); // 拷贝文件
                                copiedBytes[0] += Files.size(sourcePath); // 更新已拷贝的字节数
                                displayProgressBar(copiedBytes[0], totalBytes); // 显示进度条
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            System.out.println(); // 换行
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 拷贝文件夹，不显示进度
    private void copyDirectoryWithoutProgress(File source, File target) {
        try {
            Files.walk(source.toPath()) // 使用 Files.walk() 遍历源目录下的所有文件和子目录
                    .forEach(sourcePath -> {
                        try {
                            Path targetPath = Paths.get(target.getAbsolutePath(), sourcePath.toString()
                                    .substring(source.getAbsolutePath().length())); // 构建目标路径
                            if (Files.isDirectory(sourcePath)) {
                                Files.createDirectories(targetPath); // 创建目标文件夹
                            } else {
                                Files.copy(sourcePath, targetPath); // 复制文件
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 拷贝文件，并显示进度条
    private void copyFile(File source, File target) {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[1024];
            int length;
            long totalBytes = source.length();
            long copiedBytes = 0;

            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
                copiedBytes += length;
                displayProgressBar(copiedBytes, totalBytes);
            }

            System.out.println(); // 换行
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 拷贝文件，不显示进度
    private void copyFileWithoutProgress(File source, File target) {
        try (InputStream in = new FileInputStream(source); // 使用 try-with-resources 语句打开输入流
             OutputStream out = new FileOutputStream(target)) { // 使用 try-with-resources 语句打开输出流
            byte[] buffer = new byte[1024]; // 缓冲区大小为 1KB
            int length;
            while ((length = in.read(buffer)) > 0) { // 读取源文件内容并写入目标文件
                out.write(buffer, 0, length);
            }
        } catch (IOException e) { // 捕获异常
            e.printStackTrace();
        }
    }

    // 显示进度条
    private void displayProgressBar(long copiedBytes, long totalBytes) {
        int progress = (int) (copiedBytes * 100 / totalBytes);
        StringBuilder progressBar = new StringBuilder("[");
        int progressLength = 50;
        int filledLength = progress * progressLength / 100;
        for (int i = 0; i < progressLength; i++) {
            if (i < filledLength) {
                progressBar.append("=");
            } else {
                progressBar.append(" ");
            }
        }
        progressBar.append("] ").append(progress).append("%");
        System.out.print("\r" + progressBar.toString());
    }

    // 判断是否为文件夹
    private boolean isDirectory(String path) {
        File file = new File(path); // 根据路径创建文件对象
        return file.isDirectory(); // 返回文件是否为文件夹
    }

    public String getPackage_path() {
        return package_path;
    }


}
