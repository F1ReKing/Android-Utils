package me.hyh.f_demos.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

/**
 * 图片压缩工具类，提供各种图片压缩方法：包括尺寸压缩、质量压缩，以及针对照片的旋转摆正功能。
 * <p>
 * <b>注意：</b>图片占用内存的大小不等同于图片文件本身的大小，在内存中的占用空间一般要远大于文件本身，其内存大小主要由图片的像素和每个像素的色位决定的
 * 。 假设一张1024*1024像素的图片，每个像素色位为4个字节， 这张图片在内存中的大小就为1024*
 * 1024*4/1024/1024=4M。如果显示图片时不进行压缩，就很容易导致OOM异常的发生，防止OOM异常，请采用尺寸压缩方式。
 * <p>
 * <p>
 * 当服务器对上传的图片大小有限制时，请采用质量压缩的方式来降低图片文件的大小。
 * </p>
 * <p>
 * 照片的旋转摆正将根据照片的相机信息进行旋转摆正处理，比如横向照片将正向显示。
 * </p>
 * <p>
 * 如果需要实现缩略图，系统提供了类ThumbnailUtils来实现。
 * </p>
 * <p>
 * 最后，在不使用bitmap对象时，请及时调用{@link #recycleBitmap(Bitmap)}来释放资源
 * </p>
 * 
 * 
 * @author WuRS
 */
public class ImageCompressUtils {

	private static final String TAG = ImageCompressUtils.class.getSimpleName();

	public static SoftReference<Bitmap> compressToLimitBytes(InputStream is,
			CompressFormat format, int limitBytes) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, opts);
		// 得到图片的宽度、高度；
		int imgWidth = opts.outWidth;
		int imgHeight = opts.outHeight;
		int bitdepth = 4; // 位深，最大4byte
		int maxOccupy = imgWidth * imgHeight * bitdepth;
		opts.inJustDecodeBounds = false;
		Bitmap bitmap = null;
		long availableMemory = Runtime.getRuntime().totalMemory();
		Log.d(TAG, "availableMemory=" + availableMemory + ",maxOccupy="
				+ maxOccupy);
		if (maxOccupy < availableMemory) {
			bitmap = BitmapFactory.decodeStream(is, null, opts);
			return compressToLimitBytes(bitmap, format, limitBytes);
		} else {
			Log.w(TAG,
					"load the original bitmap may cause OOM,so compress size firstly");
			int inSampleSize = (int) Math.ceil(Math.sqrt(maxOccupy * 1.0
					/ availableMemory * 2));
			// return compressSize(is, inSampleSize);
			bitmap = compressSize(is, inSampleSize).get();
			return compressToLimitBytes(bitmap, format, limitBytes);
		}
	}

	/**
	 *
	 * @param imagePath
	 * @param format
	 * @param limitBytes
	 * @return
	 */
	public static SoftReference<Bitmap> compressToLimitBytes(String imagePath,
			CompressFormat format, int limitBytes) {
		long availableMemory = Runtime.getRuntime().totalMemory();
		// 防止OOM
		Options opts = new Options();
		// options.inJustDecodeBounds设回true，只读取图片边界信息
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, opts);// 此时返回位图为空
		// 得到图片的宽度、高度；
		int imgWidth = opts.outWidth;
		int imgHeight = opts.outHeight;
		int bitdepth = 4;
		int maxOccupy = imgWidth * imgHeight * bitdepth;
		opts.inJustDecodeBounds = false;
		Bitmap bitmap = null;
		Log.d(TAG, "availableMemory=" + availableMemory + ",maxOccupy="
				+ maxOccupy);
		if (maxOccupy < availableMemory) {
			bitmap = BitmapFactory.decodeFile(imagePath, opts);
			return compressToLimitBytes(bitmap, format, limitBytes);
		} else {
			Log.w(TAG, "load the original bitmap will cause OOM,compress size.");
		}
		return null;
	}

	/**
	 * 压缩图片的质量使之不超过指定大小
	 * 
	 * @param bitmap
	 *            要压缩的位图对象
	 * @param format
	 *            压缩的格式
	 * @param limitBytes
	 *            压缩后图片文件大小限制值，单位byte
	 * @return bitmap软引用对象
	 */
	public static SoftReference<Bitmap> compressToLimitBytes(Bitmap bitmap,
			CompressFormat format, int limitBytes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int quality = 100;
		bitmap.compress(format, quality, baos);
		boolean isCompressed = false;
		Log.d(TAG, "original baos.size()=" + baos.size());
		while (baos.size() > limitBytes) {
			quality -= 1;
			// quality = (int) Math.floor(limitBytes * 100.0 / baos.size());
			baos.reset();
			bitmap.compress(format, quality, baos);
			Log.d(TAG, "compress quality to " + quality + ",baos.size()="
					+ baos.size());
			isCompressed = true;
		}
		if (isCompressed) {
			recycleBitmap(bitmap);
			bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0,
					baos.size());
		}
		try {
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new SoftReference<Bitmap>(bitmap);
	}

	/**
	 * 压缩图片的质量
	 * 
	 * @param bitmap
	 *            位图对象
	 * @param format
	 *            图片压缩格式
	 * @param quality
	 *            质量
	 * @return 压缩成功，返回bitmap的软引用对象，此时会回收传入的bitmap对象；否则，返回null
	 */
	public static SoftReference<Bitmap> compressQuality(Bitmap bitmap,
			CompressFormat format, int quality) {
		if (quality < 0) {
			quality = 0;
		} else if (quality > 100) {
			quality = 100;
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		if (bitmap.compress(format, quality, os)) {
			recycleBitmap(bitmap);
			bitmap = BitmapFactory.decodeByteArray(os.toByteArray(), 0,
					os.size());
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new SoftReference<Bitmap>(bitmap);
		}
		return null;
	}

	/**
	 * 将图片尺寸按比例压缩到一个宽高范围内，如果图片的宽或高超出边界值。
	 * 
	 * @param imagePath
	 *            图片路径
	 * @param limitWidth
	 *            宽边界值，被压缩的图片宽小于或等于该值
	 * @param limitHeight
	 *            高边界值，被压缩的图片高小于或等于该值
	 * @return bitmap软引用对象
	 */
	public static SoftReference<Bitmap> compressSize(String imagePath,
			int limitWidth, int limitHeight) {
		return compressSize(imagePath, limitWidth, limitHeight, false);
	}

	/**
	 * 将图片尺寸按比例压缩到一个宽高范围内，如果图片的宽或高超出边界值。
	 * 
	 * @param imagePath
	 *            图片路径
	 * @param limitWidth
	 *            宽边界值，被压缩的图片宽小于或等于该值
	 * @param limitHeight
	 *            高边界值，被压缩的图片高小于或等于该值
	 * @param rotate
	 *            是否需要摆正处理
	 * @return bitmap软引用对象
	 */
	public static SoftReference<Bitmap> compressSize(String imagePath,
			int limitWidth, int limitHeight, boolean rotate) {
		Options opts = new Options();
		// options.inJustDecodeBounds设回true，只读取图片边界信息
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, opts);// 此时返回位图为空
		// 得到图片的宽度、高度；
		int imgWidth = opts.outWidth;
		int imgHeight = opts.outHeight;
		int inSampleSize = (int) Math.ceil(Math.max(
				imgWidth * 1.0 / limitWidth, imgHeight * 1.0 / limitHeight));
		return compressSize(imagePath, inSampleSize, rotate);
	}

	/**
	 * 按倍数压缩图片的尺寸
	 * 
	 * @param imagePath
	 *            图片路径
	 * @param inSampleSize
	 *            像素数量将被压缩为原来1/(inSampleSize*inSampleSize)；<b>注意：
	 *            inSampleSize将被重新取值，值为不小于2^n,n=0,1,2...</b>
	 * @return bitmap软引用对象
	 */
	public static SoftReference<Bitmap> compressSize(String imagePath,
			int inSampleSize) {
		return compressSize(imagePath, inSampleSize, false);
	}

	/**
	 * 按倍数压缩图片的尺寸
	 * 
	 * @param imagePath
	 *            图片路径
	 * @param inSampleSize
	 *            像素数量将被压缩为原来1/(inSampleSize*inSampleSize)；<b>注意：
	 *            inSampleSize将被重新取值，值为不小于2^n,n=0,1,2...</b>
	 * @param 是否需要摆正处理
	 * @return bitmap软引用对象
	 */
	public static SoftReference<Bitmap> compressSize(String imagePath,
			int inSampleSize, boolean rotate) {
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath,
				getCompressOptions(inSampleSize));
		if (rotate) {
			bitmap = rotateBitmap(imagePath, bitmap);
		}
		return new SoftReference<Bitmap>(bitmap);
	}

	/**
	 * 按倍数压缩图片的尺寸
	 * 
	 * @param is
	 *            图片的输入流
	 * @param inSampleSize
	 *            像素数量将被压缩为原来1/(inSampleSize*inSampleSize)；<b>注意：
	 *            inSampleSize将被重新取值，值为不小于2^n,n=0,1,2...</b>
	 * @return bitmap软引用对象
	 */
	public static SoftReference<Bitmap> compressSize(InputStream is,
			int inSampleSize) {
		Bitmap bitmap = BitmapFactory.decodeStream(is, null,
				getCompressOptions(inSampleSize));
		return new SoftReference<Bitmap>(bitmap);
	}

	private static Options getCompressOptions(int inSampleSize) {
		// 取不小于inSampleSize的2的i次幂来做缩放比例
		for (int i = 0;; i++) {
			int pow = (int) Math.pow(2, i);
			if (inSampleSize <= pow) {
				inSampleSize = pow;
				break;
			}
		}
		Log.d(TAG, "compressSize()...inSampleSize=" + inSampleSize);
		Options opts = new Options();
		opts.inSampleSize = inSampleSize;
		return opts;
	}

	/**
	 * 旋转摆正照片
	 * 
	 * @param imagePath
	 *            照片文件路径
	 * @param bitmap
	 *            照片位图对象
	 * @return 如果需要摆正，返回摆正后的照片位图对象，此时原位图对象将回收处理；否则返回原位图对象
	 */
	public static Bitmap rotateBitmap(String imagePath, Bitmap bitmap) {
		Bitmap newBitmap = null;
		try {
			ExifInterface exif = new ExifInterface(imagePath);
			if (exif != null) { // 读取图片中相机方向信息
				int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
						ExifInterface.ORIENTATION_NORMAL);
				int digree = 0;
				switch (ori) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					digree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					digree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					digree = 270;
					break;
				}
				if (digree != 0) {
					Matrix m = new Matrix();
					m.postRotate(digree);
					newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), m, true);
					if (newBitmap != bitmap) {
						bitmap.recycle();
					}
					return newBitmap;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * 拷贝图片文件
	 * 
	 * @param sourceImagePath
	 *            原文件路径 如：c:/fqf.txt
	 * @param copyImagePath
	 *            复制后路径 如：f:/fqf.txt
	 * @return 拷贝成功，返回File；拷贝失败，返回null
	 * @throws IllegalArgumentException
	 *             sourceImagePath指向的不是文件或不存在
	 */
	public static File copyImage(String sourceImagePath, String copyImagePath)
			throws IllegalArgumentException {
		File sourceFile = new File(sourceImagePath);
		File copyFile = new File(copyImagePath);
		return copyImage(sourceFile, copyFile);
	}

	/**
	 * 拷贝图片文件
	 * 
	 * @param sourceImage
	 *            源图片文件对象
	 * @param copyImage
	 *            拷贝图片文件对象
	 * @return 拷贝的图片文件,拷贝失败时为null
	 * @throws IllegalArgumentException
	 *             源图片文件对象为null，或不存在，或非文件，或拷贝图片文件对象为null时抛出异常
	 */
	public static File copyImage(File sourceImage, File copyImage)
			throws IllegalArgumentException {
		if (sourceImage == null || !sourceImage.exists()
				|| !sourceImage.isFile() || copyImage == null) {
			throw new IllegalArgumentException(
					"sourceFile or copyFile is null,or sourceFile doesn't exist,or sourceFile isn't a file");
		}
		InputStream in = null;
		FileOutputStream fos = null;
		try {
			in = new FileInputStream(sourceImage); // 读入原文件
			fos = new FileOutputStream(copyImage);
			byte[] buffer = new byte[8 * 1024];
			int byteread = 0;
			while ((byteread = in.read(buffer)) != -1) {
				fos.write(buffer, 0, byteread);
			}
			return copyImage;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 把位图写到指定文件
	 * 
	 * @param bitmap
	 *            要保存的位图
	 * @param format
	 *            保存的格式，传null表示PNG
	 * @param filePath
	 *            目标文件完整路径
	 * @return 成功返回true；否则返回false
	 */
	public static boolean writeBitmap(Bitmap bitmap, CompressFormat format,
			String filePath) {
		return writeBitmap(bitmap, format, new File(filePath));
	}

	/**
	 * 把位图写到指定文件中
	 * 
	 * @param bitmap
	 *            要保存的位图
	 * @param format
	 *            保存的格式，传null表示PNG
	 * @param targetFile
	 *            目标File对象
	 * @return 成功返回true；否则返回false
	 */
	public static boolean writeBitmap(Bitmap bitmap, CompressFormat format,
			File targetFile) {
		if (format == null) {
			format = CompressFormat.PNG;
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(targetFile);
			if (bitmap.compress(format, 100, out)) {
				out.flush();
				return true;
			} else {
				return false;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out = null;
			}
		}
		return false;
	}

	public static boolean writeBitmap(Bitmap bitmap, CompressFormat format,
			File targetFile, int limitBytes, boolean recycle)
			throws IllegalArgumentException {
		if (bitmap == null) {
			throw new IllegalArgumentException("bitmap must not be null");
		}
		if (limitBytes <= 0) {
			throw new IllegalArgumentException("limitBytes must >0");
		}
		if (format == null) {
			format = CompressFormat.PNG;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int quality = 100;
		bitmap.compress(format, quality, baos);
		Log.d(TAG, "original baos.size()=" + baos.size());
		while (baos.size() > limitBytes) {
			quality -= 1;
			// quality = (int) Math.floor(limitBytes * 100.0 / baos.size());
			baos.reset();
			bitmap.compress(format, quality, baos);
			Log.d(TAG, "compress quality to " + quality + ",baos.size()="
					+ baos.size());
		}
		if (recycle) {
			recycleBitmap(bitmap);
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFile);
			baos.writeTo(fos);
			baos.flush();
			fos.flush();
			return true;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 回收bitmap对象，不使用的bitmap对象请及时回收
	 * 
	 * @param bitmap
	 *            要回收的位图对象
	 */
	public static void recycleBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
			Runtime.getRuntime().gc();
		}
	}

}
