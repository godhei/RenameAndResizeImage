package com.fengyang.util;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

@SuppressWarnings ("restriction")
public class OperateImage {
   private String srcpath; // 原图路径
   private String subpath; // 目标存放路径
   private String imageType; // 图片类型
   private int x;
   private int y;
   private int width; // 图片目标宽度
   private int height; // 图片目标高度

   public OperateImage() {
   }

   public OperateImage(String srcpath, int x, int y, int width, int height) {
      this.srcpath = srcpath;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public int getHeight() {
      return height;
   }

   public void setHeight(int height) {
      this.height = height;
   }

   public String getSrcpath() {
      return srcpath;
   }

   public void setSrcpath(String srcpath) {
      this.srcpath = srcpath;
      if (srcpath != null) {
         this.imageType = srcpath.substring(srcpath.indexOf(".") + 1, srcpath.length());
      }
   }

   public String getSubpath() {
      return subpath;
   }

   public void setSubpath(String subpath) {
      this.subpath = subpath;
   }

   public int getWidth() {
      return width;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public int getX() {
      return x;
   }

   public void setX(int x) {
      this.x = x;
   }

   public int getY() {
      return y;
   }

   public void setY(int y) {
      this.y = y;
   }

   public String getImageType() {
      return imageType;
   }

   public void setImageType(String imageType) {
      this.imageType = imageType;
   }

   public String cut() throws IOException {
      FileInputStream is = null;
      ImageInputStream iis = null;
      try {
         is = new FileInputStream(srcpath);
         Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(this.imageType);
         ImageReader reader = it.next();
         iis = ImageIO.createImageInputStream(is);
         reader.setInput(iis, true);
         ImageReadParam param = reader.getDefaultReadParam();
         Rectangle rect = new Rectangle(x, y, width, height);
         param.setSourceRegion(rect);
         BufferedImage bi = reader.read(0, param);
         // 实际高度大于目标高度或者实际宽度大于目标宽度则进行剪切
         File o = new File(srcpath);
         BufferedImage bii = ImageIO.read(o);
         int itempWidth = bii.getWidth(); // 实际宽度
         int itempHeight = bii.getHeight(); // 实际高度
         if ((itempHeight > height) || (itempWidth > width)) {
            UploadImageUtil.mkdir(subpath);
            ImageIO.write(bi, this.imageType, new File(subpath));
         }
      }
      finally {
         if (is != null) is.close();
         if (iis != null) iis.close();
      }
      return subpath;
   }

   /**
    * 创建图片缩略图(等比缩放)
    * 
    * @param src 源图片文件完整路径
    * @param dist 目标图片文件完整路径
    * @param width 缩放的宽度
    * @param height 缩放的高度
    */
   public static String createThumbnail(String src, String dist, String index, float width, float height) {
      String newUrl = dist + index;
      UploadImageUtil.mkdir(dist);
      try {
         File srcfile = new File(src);
         if (!srcfile.exists()) {
            System.out.println("文件不存在");
            return dist;
         }
         BufferedImage image = ImageIO.read(srcfile);

         // 获得缩放的比例
         double ratio = 1.0;
         // 判断如果高、宽都不大于设定值，则不处理
         if (image.getHeight() > height || image.getWidth() > width) {
            if (image.getHeight() > image.getWidth()) {
               ratio = height / image.getHeight();
            }
            else {
               ratio = width / image.getWidth();
            }
         }
         // 计算新的图面宽度和高度
         int newWidth = (int) (image.getWidth() * ratio);
         int newHeight = (int) (image.getHeight() * ratio);

         BufferedImage bfImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
         bfImage.getGraphics().drawImage(image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);

         FileOutputStream os = new FileOutputStream(newUrl);
         JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
         encoder.encode(bfImage);
         os.close();
         System.out.println("创建缩略图成功");
      }
      catch (Exception e) {
         System.out.println("创建缩略图发生异常" + e.getMessage());
      }
      return newUrl;
   }
}
