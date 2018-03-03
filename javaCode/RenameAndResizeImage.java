package main;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class RenameAndResizeImage {
   public static void main(String[] args) throws IOException {
      Long count=0L;
      // local test
      File file = new File("C:/data/image/CHILD_PRODUCT");
      File goalsFile = new File("C:/data/image/offline");
      // linux run
      // File file = new File("/data/image/productimgdata");
      // File goalsFile = new File("/data/image/offline");
      if (!goalsFile.exists()) {
         mkDir(goalsFile);
      }
      int length = file.getAbsolutePath().length();
      String goalsPath = goalsFile.getAbsolutePath();// file to copy
      if (file.isDirectory()) {
         File[] files = file.listFiles();// first content
         System.out.println("first content have " + files.length + " files");
         for (File f : files) {
            File[] second = f.listFiles();
            count=count+second.length ;
            System.out.println("this " + f.getPath() + " content have " + second.length + " image files");
            for (File fileFrom : second) {
               String fromFile = fileFrom.getAbsolutePath();
               String fileName = fileFrom.getName();// image name
               String extension = fileName.lastIndexOf(".") != -1 ? fileName.substring(fileName.lastIndexOf(".")) : "";
               String name = fromFile.substring(length);
               name = "http://img.che.com/CHILD_PRODUCT" + name;
               // name = "http://beta.imagesvr.che.com/CHILD_PRODUCT" + name;
               // System.out.println("need hash name: " + name);
               String toFileName = goalsPath + File.separator + Md5(name) + extension;
               copyFile(fromFile, toFileName, extension.substring(1)); // copy and rename image

            }
         }
         System.out.println("copy and rename ok! have "+count+"image ...");
      }
   }

   /**
    * copy file from oldPath to newPath
    *
    * @param oldPath
    * @param newPath
    * @throws IOException
    */
   public static void copyFile(String oldPath, String newPath, String extension) throws IOException {
      try {
         File oldfile = new File(oldPath);
         if (oldfile.exists()) {
            File fromFile = new File(oldPath);
            File saveFile = new File(newPath);
            BufferedImage srcImage;
            if ("PNG".equalsIgnoreCase(extension)) {
               srcImage = ImageIO.read(fromFile);
               srcImage = resizePNG(srcImage, 300, 300);
            }
            else {
               Image src = Toolkit.getDefaultToolkit().getImage(fromFile.getPath());
               srcImage = toBufferedImage(src);
               srcImage = resizeJPG(srcImage, 400, 400);
            }
            ImageIO.write(srcImage, extension, saveFile);
         }
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * new file
    *
    * @param file
    * @throws IOException
    */
   public static void mkDir(File file) throws IOException {
      if (file.getParentFile().exists()) {
         file.mkdir();
      }
      else {
         mkDir(file.getParentFile());
         file.mkdir();
      }
   }

   /**
    * 16bit hash MD5
    *
    * @param plainText
    * @return
    */
   private static String Md5(String plainText) {
      String code = "";
      try {
         MessageDigest md = MessageDigest.getInstance("MD5");
         md.update(plainText.getBytes());
         byte b[] = md.digest();
         int i;
         StringBuffer buf = new StringBuffer("");
         for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0) i += 256;
            if (i < 16) buf.append("0");
            buf.append(Integer.toHexString(i));
         }
         code = buf.toString();
         // System.out.println("result: " + buf.toString());//32bit hash value

      }
      catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
      }
      return code;
   }

   /**
    * resize JPG image
    *
    * @param source
    * @param targetW
    * @param targetH
    * @return
    */
   public static BufferedImage resizeJPG(BufferedImage source, int targetW, int targetH) {
      int type = source.getType();
      double sx = (double) targetW / source.getWidth();
      double sy = (double) targetH / source.getHeight();
      BufferedImage target = new BufferedImage(targetW, targetH, type);
      Graphics2D g = target.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
      g.dispose();
      return target;
   }

   /**
    * resize PNG image
    *
    * @param source
    * @param targetW
    * @param targetH
    * @return
    */
   public static BufferedImage resizePNG(BufferedImage source, int targetW, int targetH) {
      BufferedImage to = new BufferedImage(targetW, targetH, source.getType());
      Graphics2D g2d = to.createGraphics();
      to = g2d.getDeviceConfiguration().createCompatibleImage(targetW, targetH, Transparency.TRANSLUCENT);
      g2d.dispose();
      g2d = to.createGraphics();
      @SuppressWarnings ("static-access")
      Image from = source.getScaledInstance(targetW, targetH, source.SCALE_FAST);
      g2d.drawImage(from, 0, 0, null);
      g2d.dispose();
      return to;
   }
   /**
    *
    * @param image
    * @return
    */
   public static BufferedImage toBufferedImage(Image image) {
      if (image instanceof BufferedImage) {
         return (BufferedImage) image;
      }
      // This code ensures that all the pixels in the image are loaded
      image = new ImageIcon(image).getImage();
      BufferedImage bimage = null;
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      try {
         int transparency = Transparency.OPAQUE;
         GraphicsDevice gs = ge.getDefaultScreenDevice();
         GraphicsConfiguration gc = gs.getDefaultConfiguration();
         bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
      }
      catch (HeadlessException e) {
         // The system does not have a screen
      }
      if (bimage == null) {
         // Create a buffered image using the default color model
         int type = BufferedImage.TYPE_INT_RGB;
         bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
      }
      // Copy image to buffered image
      Graphics g = bimage.createGraphics();
      // Paint the image onto the buffered image
      g.drawImage(image, 0, 0, null);
      g.dispose();
      return bimage;
   }
}
