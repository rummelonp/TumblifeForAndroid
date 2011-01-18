package jp.mitukiii.tumblife.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import jp.mitukiii.tumblife.Main;
import jp.mitukiii.tumblife.exeption.TLSDCardNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

public class TLExplorer
{
  public static final String         FILE_SCHEME     = "file:";

  public static final String         SD_CARD         = Environment.getExternalStorageDirectory().getPath() + "/";
  public static final String         APP_DIR         = SD_CARD + Main.APP_NAME.replace(" ", "_") + "/";

  public static final String         DATA_DIR        = APP_DIR + "data/";
  public static final String         HTML_DIR        = APP_DIR + "html/";
  public static final String         CSS_DIR         = APP_DIR + "css/";
  public static final String         JS_DIR          = APP_DIR + "js/";
  public static final String         IMAGE_DIR       = APP_DIR + "img/";

  public static final String         HTML_EXTENSION  = "html";

  public static final String         IMAGE_GIF_EXTENSION = "gif";

  public static final CompressFormat IMAGE_PNG_FORMAT    = CompressFormat.PNG;
  public static final String         IMAGE_PNG_EXTENSION = IMAGE_PNG_FORMAT.toString().toLowerCase();
  public static final int            IMAGE_PNG_QUALITY   = 100;

  public static final int            IO_BUFFER_SIZE  = 4 * 1024;

  public static String makeSerializeFile(String fileName, Object object)
    throws TLSDCardNotFoundException, FileNotFoundException, IOException
  {
    if (!isSDCardWriteble() ||
        !makeDirectory(new File(APP_DIR)) ||
        !makeDirectory(new File(DATA_DIR)))
    {
      throw new TLSDCardNotFoundException();
    }
    String filePath = DATA_DIR + fileName;
    TLLog.d("TLExplorer / makeSerializeFile : fileName / " + fileName);
    FileOutputStream fileOutput = null;
    ObjectOutputStream objectOutput = null;
    try {
      fileOutput = new FileOutputStream(filePath);
      objectOutput = new ObjectOutputStream(fileOutput);
      objectOutput.writeObject(object);
      objectOutput.close();
    } finally {
      if (fileOutput != null) {
        try {
          fileOutput.close();
        } catch (IOException e) {
          TLLog.i("TLExplorer / makeSerializeFile :", e);
        }
      }
      if (objectOutput != null) {
        try {
          objectOutput.close();
        } catch (IOException e) {
          TLLog.i("TLExplorer / makeSerializeFile :", e);
        }
      }
    }
    return filePath;
  }

  public static Object readSerializeFile(String fileName)
    throws FileNotFoundException, IOException, ClassNotFoundException
  {
    String filePath = DATA_DIR + fileName;
    TLLog.d("TLExplorer / readSerializeFile : fileName / " + fileName);
    FileInputStream fileInput = null;
    ObjectInputStream objectInput = null;
    Object object = null;
    try {
      fileInput = new FileInputStream(filePath);
      objectInput = new ObjectInputStream(fileInput);
      object = objectInput.readObject();
    } finally {
      if (fileInput != null) {
        try {
          fileInput.close();
        } catch (IOException e) {
          TLLog.i("TLExplorer / readSerializeFile :", e);
        }
      }
      if (objectInput != null) {
        try {
          objectInput.close();
        } catch (IOException e) {
          TLLog.i("TLExplorer / readSerializeFile :", e);
        }
      }
    }
    objectInput.close();
    return object;
  }

  public static String makeFile(String fileDir, String fileName, String fileString, boolean force)
    throws TLSDCardNotFoundException, FileNotFoundException, IOException
  {
    if (!isSDCardWriteble() ||
        !makeDirectory(new File(APP_DIR)) ||
        !makeDirectory(new File(fileDir)))
    {
      throw new TLSDCardNotFoundException();
    }
    String filePath = fileDir + fileName;
    String fileUrl = FILE_SCHEME + filePath;
    if (force) {
      TLLog.d("TLExplorer / makeFile : fileDir " + fileDir + " : fileName / " + fileName);
    } else if (new File(filePath).exists()) {
      TLLog.d("TLExplorer / makeFile : file exits. : fileDir " + fileDir + " : fileName / " + fileName);
      return fileUrl;
    }
    FileOutputStream fileOutput = null;
    try {
      fileOutput = new FileOutputStream(filePath);
      fileOutput.write(fileString.getBytes());
      fileOutput.flush();
    } finally {
      if (fileOutput != null) {
        try {
          fileOutput.close();
        } catch (IOException e) {
          TLLog.i("TLExplorer / makeFile :", e);
        }
      }
    }
    return fileUrl;
  }

  public static String makeFile(String fileDir, String fileName, InputStream fileInput, boolean force)
    throws TLSDCardNotFoundException, FileNotFoundException, IOException
  {
    byte[] bytes = new byte[fileInput.available()];
    fileInput.read(bytes);
    String fileString = new String(bytes); 
    return makeFile(fileDir, fileName, fileString, force);
  }

  public static String makeHtmlFile(String fileName, String fileString, boolean force)
    throws TLSDCardNotFoundException, FileNotFoundException, IOException
  {
    return makeFile(HTML_DIR, fileName, fileString, force);
  }

  public static String makeGifImageFile(String urlString, String fileName)
    throws TLSDCardNotFoundException, MalformedURLException, FileNotFoundException, IOException
  {
    if (!isSDCardWriteble() ||
        !makeDirectory(new File(APP_DIR)) ||
        !makeDirectory(new File(IMAGE_DIR)))
    {
      throw new TLSDCardNotFoundException();
    }
    String filePath = IMAGE_DIR + fileName;
    String fileUrl = FILE_SCHEME + filePath;
    if (new File(filePath).exists()) {
      TLLog.d("TLExplorer / makeGifImageFile : file exits. : fileName / " + fileName);
      return fileUrl;
    } else {
      TLLog.d("TLExplorer / makeGifImageFile : fileName / " + fileName);
    }
    HttpURLConnection con = null;
    InputStream input = null;
    FileOutputStream fileOutput = null;
    try {
      con = TLConnection.get(urlString);
      input = new BufferedInputStream(con.getInputStream(), IO_BUFFER_SIZE);
      fileOutput = new FileOutputStream(filePath, false);
      byte[] b = new byte[IO_BUFFER_SIZE];
      int read;
      while ((read = input.read(b)) != -1) {
        fileOutput.write(b, 0, read);
      }
      fileOutput.flush();
    } catch (OutOfMemoryError e) {
      TLLog.e("TLExplorer / makeGifImageFile : fileName / " + fileName, e);
      throw new IOException(e.getMessage());
    } finally {
      try {
        if (fileOutput != null) {
          fileOutput.close();
        }
      } catch (IOException e) {
        TLLog.i("TLExplorer / makeGifImageFile :", e);
      }
      try {
        if (input != null) {
          input.close();
        }
      } catch (IOException e) {
        TLLog.i("TLExplorer / makeGifImageFile :", e);
      }
      if (con != null) {
        con.disconnect();
      }
    }
    return fileUrl;
  }

  public static String makePngImageFile(String urlString, String fileName)
    throws TLSDCardNotFoundException, MalformedURLException, FileNotFoundException, IOException
  {
    if (!isSDCardWriteble() ||
        !makeDirectory(new File(APP_DIR)) ||
        !makeDirectory(new File(IMAGE_DIR)))
    {
      throw new TLSDCardNotFoundException();
    }
    String filePath = IMAGE_DIR + fileName;
    String fileUrl = FILE_SCHEME + filePath;
    if (new File(filePath).exists()) {
      TLLog.d("TLExplorer / makePngImageFile : file exits. : fileName / " + fileName);
      return fileUrl;
    } else {
      TLLog.d("TLExplorer / makePngImageFile : fileName / " + fileName);
    }
    HttpURLConnection con = null;
    InputStream input = null;
    FileOutputStream fileOutput = null;
    BufferedOutputStream output = null;
    try {
      con = TLConnection.get(urlString);
      input = new BufferedInputStream(con.getInputStream(), IO_BUFFER_SIZE);
      ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
      output = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
      byte[] b = new byte[IO_BUFFER_SIZE];
      int read;
      while ((read = input.read(b)) != -1) {
        output.write(b, 0, read);
      }
      output.flush();
      byte[] data = dataStream.toByteArray();
      Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
      if (image == null) {
        throw new FileNotFoundException("Image decoding failed.");
      }
      fileOutput = new FileOutputStream(filePath, false);
      image.compress(IMAGE_PNG_FORMAT, IMAGE_PNG_QUALITY, fileOutput);
    } catch (OutOfMemoryError e) {
      TLLog.e("TLExplorer / makePngImageFile : fileName / " + fileName, e);
      throw new IOException(e.getMessage());
    } finally {
      try {
        if (output != null) {
          output.close();
        }
      } catch (IOException e) {
        TLLog.i("TLExplorer / makePngImageFile :", e);
      }
      try {
        if (fileOutput != null) {
          fileOutput.close();
        }
      } catch (IOException e) {
        TLLog.i("TLExplorer / makePngImageFile :", e);
      }
      try {
        if (input != null) {
          input.close();
        }
      } catch (IOException e) {
        TLLog.i("TLExplorer / makePngImageFile :", e);
      }
      if (con != null) {
        con.disconnect();
      }
    }
    return fileUrl;
  }

  public static String getPreffix(String fileName) {
    fileName = new File(fileName).getName();
    int point = fileName.lastIndexOf(".");
    if (point != -1) {
      fileName = fileName.substring(0, point);
    } 
    return fileName;
  }

  public static boolean isSDCardWriteble()
  {
    return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
  }

  public static boolean makeDirectory(File dir)
  {
    if (dir.exists()) {
      return dir.isDirectory();
    } else {
      return dir.mkdir();
    }
  }

  public static void deleteFiles(File[] files)
  {
    for (File f: files) {
      deleteFiles(f);
    }
  }

  public static void deleteFiles(File file)
  {
    TLLog.i("TLExplorer / deleteFiles : fileName /" + file.getPath());

    if (!file.exists()) {
      return;
    }
    if (file.isFile()) {
      file.delete();
    }
    if (file.isDirectory()) {
      for (File f: file.listFiles()) {
        deleteFiles(f);
      }
    }
  }
}
