package com.common.utils.common_utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/***
 * 文件相关操作的工具类
 * 
 * @author shuguang.wen
 * 
 */
public class FileUtil {
	private static final String TAG = FileUtil.class.getName();

	/**
	 * 得到DB路径
	 * 
	 * @param context
	 * @param strDBname
	 *            数据库名称
	 * @return
	 */
	public static String getDataBaseFilePath(Context context, String strDBname) {

		String strDBPath = context.getFilesDir().getParent() + File.separator
				+ "databases" + File.separator + strDBname;
		return strDBPath;
	}

	/**
	 * 判断路径指向的文件是否存在
	 * 
	 * @param strFilePath
	 * @return
	 */
	public static boolean cheackFileExisted(String strFilePath) {
		File databaseFile = new File(strFilePath);
		if (databaseFile.exists())
			return true;
		return false;
	}

	/**
	 * 初始化数据库文件，如果数据库文件不存在，则从资源文件中解压出来
	 * 
	 * @param context
	 * @param isNeedUnzip
	 *            表示是否需要解压
	 * @param strDBname
	 *            数据库名称
	 * @param resRawId
	 *            资源文件中数据库文件的ID
	 * @param needSize
	 *            数据库解压后的大小(单位：B),这个值最好比数据库的解压后的大小大些
	 * @return 0:表示数据库文件已经存在，1:表示数据库文件解压成功，2:表示数据库文件解压失败，3:表示内部存储空间不足
	 *         4:表示数据库文件复制成功,5:表示数据库文件复制失败;
	 */
	public static int initDatabaseFile(Context context, boolean isNeedUnzip,
                                       String strDBname, int resRawId, long needSize) {
		File databaseFile = new File(context.getFilesDir().getParent()
				+ File.separator + "databases" + File.separator + strDBname);
		System.out.println("databaseFile=" + context.getFilesDir().getParent()
				+ File.separator + "databases" + File.separator + strDBname);
		if (databaseFile.exists()) {// 数据库文件已经存在
			Log.i(TAG, "数据库文件已经存在");
			return 0;
		} else {

			if (!databaseFile.getParentFile().exists()) {// 数据库所在的文件夹未创建
				databaseFile.getParentFile().mkdirs();
			}

			if (isNeedUnzip) {// 需要解压
				if (getAvailableInternalMemorySize(needSize)) {// 内存空间足够
					if (unZip(context, resRawId, databaseFile.getParent())) {
						return 1;
					}
					return 2;
				} else {
					return 3;
				}
			} else {
				try {
					copyDataBase(context, resRawId, databaseFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 5;
				}
				return 4;
			}
		}
	}

	/**
	 * 把数据库文件复制到SD卡应用名文件夹下
	 * 
	 * @param context
	 * @param strDBname
	 *            数据库的名字
	 * @param appNameString
	 *            app的名称
	 */

	public static void databaseToSD(Context context, String strDBname,
                                    String appNameString) {

		String srcFilePath = context.getFilesDir().getParent() + File.separator
				+ "databases" + File.separator + strDBname;

		String targetFolderPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ appNameString
				+ File.separator + "databases";

		copyFile(srcFilePath, targetFolderPath);
	}

	/**
	 * 检测SD卡是否可用
	 * 
	 * @return
	 */
	public static boolean SDCardable() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			return true;
		}
		return false;
	}

	/**
	 * 数据路径指向的文件不存在，则新建一个
	 * 
	 * @param path
	 */
	public static File createFileIfNeed(String path) {
		File file = new File(path);
		if (!file.exists()) {
			File parentFolder = file.getParentFile();
			if (!parentFolder.exists()) {
				parentFolder.mkdirs();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return file;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering byte stream.
	 * */
	private static void copyDataBase(Context context, int resRawId,
                                     File databaseFile) throws IOException {
		if (!databaseFile.getParentFile().exists()) {
			System.out.println("!databaseFile.getParentFile().exists()");
			databaseFile.getParentFile().mkdirs();
		}
		if (!databaseFile.exists()) {
			databaseFile.createNewFile();
		}
		// Open your local db as the input stream
		InputStream myInput = context.getResources().openRawResource(resRawId);
		// Path to the just created empty db
		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(
				databaseFile.getAbsolutePath());
		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	/**
	 * 复制一个文件到目标文件夹内
	 * 
	 * @param srcFilePath
	 *            源文件
	 * @param targetFolderPath
	 *            目标文件夹
	 */
	public static void copyFile(String srcFilePath, String targetFolderPath) {
		File srcFile = new File(srcFilePath);
		if (!srcFile.exists()) {
			throw new IllegalArgumentException("复制文件失败:源文件不存在");
		}

		File targetFolder = new File(targetFolderPath);
		if (!targetFolder.exists()) {
			targetFolder.mkdirs();
		}

		String fileName = srcFile.getName();

		File targetFile = new File(targetFolder, fileName);
		FileInputStream srcIS = null;
		FileChannel fcin;
		FileChannel fcout;
		try {
			srcIS = new FileInputStream(srcFile);
			fcin = srcIS.getChannel();
			fcout = new FileOutputStream(targetFile).getChannel();
			fcin.transferTo(0, fcin.size(), fcout);
			fcin.close();
			fcout.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (srcIS != null) {
				try {
					srcIS.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取手机内部剩余存储空间
	 * 
	 * @return
	 */
	public static boolean getAvailableInternalMemorySize(long needSize) {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		if (availableBlocks * blockSize > needSize) {
			return true;
		} else {
			Log.v(TAG, "系统存储剩余空间:" + availableBlocks * blockSize / 1024 / 1024
					+ "M");
			return false;
		}
	}

	/**
	 * 解压文件到指定目录下
	 * 
	 * @param srcPaht
	 * @param purposePath
	 * @return
	 */
	public static boolean unZip(String srcPaht, String purposePath) {

		File srcFile = new File(srcPaht);
		if (!srcFile.exists()) {
			return false;
		}

		File purposeFolder = new File(purposePath);
		if (!purposeFolder.exists()) {
			Log.v(TAG, "解压目标文件夹不存在");
			Log.v(TAG, "解压目标文件夹创建" + purposeFolder.mkdirs());
		}

		InputStream is = null;
		ZipInputStream zipIn = null;
		ZipEntry zipEntry = null;
		byte buf[] = new byte[4096];
		FileOutputStream fileOut = null;
		File file = null;
		int readedBytes = 0;

		try {
			is = new FileInputStream(srcFile);

			zipIn = new ZipInputStream(is);
			while ((zipEntry = zipIn.getNextEntry()) != null) {
				file = new File(purposeFolder, zipEntry.getName());
				createFileIfNeed(file.getAbsolutePath());
				Log.v(TAG, "zipfile file_path:" + file.getAbsolutePath());
				if (!file.isDirectory()) {
					fileOut = new FileOutputStream(file);
					while ((readedBytes = zipIn.read(buf)) > 0) {
						fileOut.write(buf, 0, readedBytes);
					}
					fileOut.close();
				}
			}
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (zipIn != null) {
					zipIn.closeEntry();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buf = null;
		}
	}

	/**
	 * 解压raw文件夹里的文件到指定目录下
	 * 
	 * @param context
	 * @param fileName
	 * @param purposePath
	 */
	public static boolean unZip(Context context, int fileId, String purposePath) {

		File purposeFolder = new File(purposePath);
		if (!purposeFolder.exists()) {
			Log.v(TAG, "解压目标文件夹不存在");
			Log.v(TAG, "解压目标文件夹创建" + purposeFolder.mkdirs());
		}

		InputStream is = null;
		ZipInputStream zipIn = null;
		ZipEntry zipEntry = null;
		byte buf[] = new byte[4096];
		FileOutputStream fileOut = null;
		File file = null;
		int readedBytes = 0;

		is = context.getResources().openRawResource(fileId);

		zipIn = new ZipInputStream(is);

		try {
			while ((zipEntry = zipIn.getNextEntry()) != null) {
				file = new File(purposeFolder, zipEntry.getName());
				Log.v(TAG, "zipfile file_path:" + file.getAbsolutePath());
				fileOut = new FileOutputStream(file);
				while ((readedBytes = zipIn.read(buf)) > 0) {
					fileOut.write(buf, 0, readedBytes);
				}
				fileOut.close();
			}
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (zipIn != null) {
					zipIn.closeEntry();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buf = null;
		}

	}

	/**
	 * 按n行数据放一个文件，把源文件的内容分成多个文件存储
	 * 
	 * @param srcPath
	 *            源文件路径
	 * @param targetFolderPath
	 *            分割后的文件存放的文件夹路径
	 * @param targetFileName
	 *            会以这个文件名加上序号来命名分割后的文件
	 * @param numLine
	 *            一个文件最多存放多少行数据
	 * @return 返回是否分割成功
	 */
	public static boolean splitFile(String srcPath, String targetFolderPath,
                                    String targetFileName, int numLine) {

		File srcFile = new File(srcPath);
		if (!srcFile.exists()) { // 源文件不存在
			return false;
		}

		File targetFolder = new File(targetFolderPath);
		if (!targetFolder.exists()) {
			targetFolder.mkdirs();
		}

		FileInputStream in = null;
		BufferedReader reader = null;
		try {
			in = new FileInputStream(srcPath);
			reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			int i = 0;

			boolean isFinish = false;

			while (!isFinish) {
				int j = 0;

				FileWriter writer = new FileWriter(targetFolderPath
						+ File.separator + targetFileName + i);
				while ((line = reader.readLine()) != null && j < numLine) {
					if (j != 0) {
						writer.write("\n");
					}
					writer.write(line);
					j++;
				}
				writer.close();
				if (line == null) {
					isFinish = true;
				}
				i++;
			}

			return true;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return false;
	}

	/**
	 * 把字符串写入文件中
	 * 
	 * @param filePath
	 *            绝对路径
	 * @param text
	 *            字符串
	 */
	public static void writeStringToFile(String filePath, String text) {
		createFileIfNeed(filePath);
		try {
			FileOutputStream fout = new FileOutputStream(filePath);
			byte[] bytes = text.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 把字符器写入文件中
	 * 
	 * @param filePath
	 *            相对SD根目录的路径
	 * @param text
	 *            要写入的字符串
	 */
	public static void writeStringToSDFile(String filePath, String text) {
		String absolutePath = Environment.getExternalStorageDirectory()
				+ File.separator + filePath;
		writeStringToFile(absolutePath, text);
	}
}
