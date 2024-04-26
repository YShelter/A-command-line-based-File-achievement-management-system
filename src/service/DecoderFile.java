package service;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;

public class DecoderFile {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    private final String filePath;
    private final String targetPath;
    private final String key;

    public DecoderFile(String filePath, String targetPath, String key) {
        this.filePath = filePath;
        this.targetPath = targetPath;
        this.key = key;
    }

    // 解密文件
    public void decode() {
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            FileInputStream inputStream = new FileInputStream(filePath);
            FileOutputStream outputStream = new FileOutputStream(targetPath);
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = cipherInputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, bytesRead);
            }

            cipherInputStream.close();
            inputStream.close();
            outputStream.close();
            System.out.println("文件解密完成。");
        } catch (Exception e) {
            System.out.println("解密过程中出现错误：" + e.getMessage());
        }
    }

}
