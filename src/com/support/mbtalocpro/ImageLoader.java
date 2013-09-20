package com.support.mbtalocpro;

import java.lang.ref.WeakReference;

import com.transport.mbtalocpro.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageButton;
import android.widget.ImageView;

public class ImageLoader {
	
	//Add the image into a memory cache so that it does not load everytime the user scrolls
	private LruCache<String, Bitmap> mMemoryCache;
	
	/*
	 * Add bitmap to memory
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }
	}

	/*
	 * Get bitmap from memory
	 */
	public Bitmap getBitmapFromMemCache(String key) {
	    return mMemoryCache.get(key);
	}
	
		
	/*
	 * loading the images in imagebutton in the list view
	 */
	public void loadImage(String filePath, ImageButton imgButton, Resources resources, int index) {
		final String imageKey = String.valueOf(index);
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	    // Use 1/8th of the available memory for this memory cache.
	    final int cacheSize = maxMemory / 8;

	    mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
		
	    final Bitmap image = getBitmapFromMemCache(imageKey);
	    if(image != null) {
	    	imgButton.setImageBitmap(image);
	    }
	    else {
			if (cancelPotentialWork(index, imgButton)) {
		        final AsynchronousImageLoader task = new AsynchronousImageLoader(imgButton);
		        Bitmap placeHolderImage = BitmapFactory.decodeResource(resources , R.drawable.ic_device_access_camera);
		        final AsyncDrawable asyncDrawable = new AsyncDrawable(resources,placeHolderImage, task);
		        imgButton.setImageDrawable(asyncDrawable);
		        task.execute(filePath, String.valueOf(index));
		    }
	    }
	}
	 
	/*
	 * Async Worker image loader class 
	 */
	class AsynchronousImageLoader extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageButton> imageViewReference;	
		String filePath;
		int index;
		
		public AsynchronousImageLoader(ImageButton imgButton) { 
			imageViewReference = new WeakReference<ImageButton>(imgButton);
		}
		
		@Override
		protected Bitmap doInBackground(String... params) {
			filePath = params[0];
			index = Integer.parseInt(params[1]);
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(params[0], bitmapOptions);
			
			bitmapOptions.inSampleSize = calculateInSampleSize(bitmapOptions, 70,70);
			bitmapOptions.inJustDecodeBounds = false;
			Bitmap image = BitmapFactory.decodeFile(params[0], bitmapOptions);
			
			Bitmap resizedImage = Bitmap.createScaledBitmap(image, image.getHeight(), image.getWidth(), true);
			
			addBitmapToMemoryCache(params[1], resizedImage); 
			return resizedImage;
		}
		
		@Override
		protected void onPostExecute(Bitmap image) {
			 if (imageViewReference != null && image != null) {
				 	final ImageButton imageButton = (ImageButton) imageViewReference.get();
		            if (imageButton != null) {
		            	imageButton.setImageBitmap(image);
		            }
			 }
		}
		
	}
	
	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);
	
	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	
	    return inSampleSize;
	}
	
	static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<AsynchronousImageLoader> imageLoaderReference;
		
		public AsyncDrawable(Resources resource, Bitmap placeHolderImage, AsynchronousImageLoader imageLoader) {
			super(resource, placeHolderImage);			        
	        imageLoaderReference = new WeakReference<AsynchronousImageLoader>(imageLoader);
	    }

	    public AsynchronousImageLoader getBitmapWorkerTask() {
	        return imageLoaderReference.get();
	    }		
	}
	
	public static boolean cancelPotentialWork(int index, ImageButton imageButton) {
	    final AsynchronousImageLoader imageLoader = getBitmapWorkerTask(imageButton);

	    if (imageLoader != null) {
	        final int bitmapIndex = imageLoader.index;
	        if (bitmapIndex != index) {
	            // Cancel previous task
	        	imageLoader.cancel(true);
	        } else {
	            // The same work is already in progress
	            return false;
	        }
	    }
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
	}
	
	private static AsynchronousImageLoader getBitmapWorkerTask(ImageButton imageButton) {
	   if (imageButton != null) {
	       final Drawable drawable = imageButton.getDrawable();
	       if (drawable instanceof AsyncDrawable) {
	           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
	           return asyncDrawable.getBitmapWorkerTask();
	       }
	    }
	    return null;
	}
	
	
	
}
