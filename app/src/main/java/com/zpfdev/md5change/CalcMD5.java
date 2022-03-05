package com.zpfdev.md5change;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class CalcMD5 {
    
    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    /**
     * 计算文件 MD5
     * @param file
     * @return 返回文件的md5字符串，如果计算过程中任务的状态变为取消或暂停，返回null， 如果有其他异常，返回空字符串
     */
    protected static String calcMD5(File file) {
        try (InputStream stream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buf = new byte[8192];
            int len;
            while ((len = stream.read(buf)) > 0) {
                digest.update(buf, 0, len);
            }
            return toHexString(digest.digest());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public static String toHexString(byte[] data) {
          StringBuilder r = new StringBuilder(data.length * 2);
          for (byte b : data) {
              r.append(hexCode[(b >> 4) & 0xF]);
              r.append(hexCode[(b & 0xF)]);
          }
          return r.toString();
      }

}