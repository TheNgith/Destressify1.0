@file:Suppress("DEPRECATION")

package galaxy.app.stressdetector

import android.Manifest
import android.Manifest.permission
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraFragment : Fragment() {
    // TODO: Rename and change types of parameters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        colorValues.clear()
        startTime = System.currentTimeMillis()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_camera, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mTextureView = view.findViewById(R.id.texture)
        if (progressBar!=null) {
            setProgressMax(progressBar, 100)
            setProgressAnimate(progressBar, 100)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mFile = File(activity!!.getExternalFilesDir(null), "pic.jpg")
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (mTextureView!!.isAvailable) {
            openCameraOrRequestPermission(mTextureView!!.width, mTextureView!!.height)
        } else {
            mTextureView!!.surfaceTextureListener = mSurfaceTextureListener
        }
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.getLooper())
    }

    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    class ErrorDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity: FragmentActivity? = getActivity()
            return AlertDialog.Builder(activity)
                .setMessage(getArguments()?.getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            if (activity != null) {
                                activity.finish()
                            }
                        })
                .create()
        }

        companion object {
            private const val ARG_MESSAGE = "message"
            fun newInstance(message: String?): ErrorDialog {
                val dialog = ErrorDialog()
                val args = Bundle()
                args.putString(ARG_MESSAGE, message)
                dialog.setArguments(args)
                return dialog
            }
        }
    }

    class ConfirmationDialog : DialogFragment() {
        @NonNull
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val parent = parentFragment
            return AlertDialog.Builder(activity)
                .setMessage(R.string.request_permission)
                .setPositiveButton(
                        android.R.string.ok
                ) { dialog, which ->
                    ActivityCompat.requestPermissions(
                            parent!!.activity!!,
                            arrayOf(permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION
                    )
                }
                .setNegativeButton(
                        android.R.string.cancel
                ) { dialog, which ->
                    val activity: Activity? = parent!!.activity
                    activity?.finish()
                }
                .create()
        }
    }

    private fun openCameraOrRequestPermission(width: Int, height: Int){
        if (context?.let { ActivityCompat.checkSelfPermission(it, permission.CAMERA) } != PackageManager.PERMISSION_GRANTED){
            if (activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) }!!){
                val confimationDialog = ConfirmationDialog()
                confimationDialog.show(childFragmentManager, FRAGMENT_DIALOG)
            }
            else {
                val manifestString = permission.CAMERA.toString()
                ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(manifestString), REQUEST_CAMERA_PERMISSION)
            }
        }
        else {
            openCamera(width, height)
        }
    }

    @Throws(SecurityException::class)
    private fun openCamera(width: Int, height: Int) {
        setUpCameraOutput(width, height)
        configureTransform(width, height)
        val activity: Activity? = activity
        val manager =
            activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!mCameraOpenCloseLock.tryAcquire(
                            2500,
                            TimeUnit.MILLISECONDS
                    )
            ) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(mCameraId!!, mStateCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private fun closeCamera() {
        try {
            mCameraOpenCloseLock.acquire()
            if (null != mCaptureSession) {
                mCaptureSession!!.close()
                mCaptureSession = null
            }
            if (null != mCameraDevice) {
                mCameraDevice!!.close()
                mCameraDevice = null
            }
            if (null != mImageReader) {
                mImageReader!!.close()
                mImageReader = null
            }
        } catch (e: InterruptedException) {
            throw java.lang.RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            mCameraOpenCloseLock.release()
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    internal class CompareSizesByArea : Comparator<Size?> {
        override fun compare(lhs: Size?, rhs: Size?): Int {
            var result = -1
            if (lhs != null && rhs != null) {
                if (lhs.width * lhs.height - rhs.width * rhs.height < 0){
                    result = -1
                } else {
                    if (lhs.width * lhs.height - rhs.width * rhs.height == 0) {
                        result = 0
                    }
                    else {
                        result = 1
                    }
                }
            }
            return result
        }
    }

    private fun chooseOptimalSize(
            choices: Array<Size>,
            width: Int,
            height: Int,
            aspectRatio: Size
    ): Size? {
        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough: MutableList<Size> =
            ArrayList()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.height == option.width * h / w && option.width >= width && option.height >= height
            ) {
                bigEnough.add(option)
            }
        }

        // Pick the smallest of those, assuming we found any
        return if (bigEnough.size > 0) {
            Collections.min(
                    bigEnough,
                    galaxy.app.stressdetector.CameraFragment.CompareSizesByArea()
            )
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size")
            choices[0]
        }
    }

    private fun setUpCameraOutput(width: Int, height: Int){
        val atvt = activity
        val manager : CameraManager = atvt?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics =
                    manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }
                val map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                )
                    ?: continue


                // For still image captures, we use the largest available size.
                val largest = Collections.max(
                        Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)),
                        CompareSizesByArea()
                )

                // For still image captures, we use the largest available size.
                var smallest = Collections.min(
                        Arrays.asList(*map.getOutputSizes(ImageFormat.YUV_420_888)),
                        CompareSizesByArea()
                )
                mImageReader = ImageReader.newInstance(
                        smallest.getWidth(), smallest.getHeight(),
                        ImageFormat.YUV_420_888,  /*maxImages*/2
                )
                mImageReader!!.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler
                )
                mPreviewSize = chooseOptimalSize(
                        map.getOutputSizes(
                                SurfaceTexture::class.java
                        ),
                        width, height, smallest
                )

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                val orientation = resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView!!.setAspectRatio(
                            mPreviewSize!!.width, mPreviewSize!!.height
                    )
                } else {
                    mTextureView!!.setAspectRatio(
                            mPreviewSize!!.height, mPreviewSize!!.width
                    )
                }

                // Check if the flash is supported.
                val available =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                mFlashSupported = available ?: false
                mCameraId = cameraId
                return
            }
        }
        catch (e: CameraAccessException) {
            e.printStackTrace();
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                    .show(childFragmentManager, FRAGMENT_DIALOG)
            }
        } else {
            openCameraOrRequestPermission(mTextureView!!.width, mTextureView!!.height)
        }
    }

    private val mSurfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(
                texture: SurfaceTexture,
                width: Int,
                height: Int
        ) {
            openCameraOrRequestPermission(width, height)
        }

        override fun onSurfaceTextureSizeChanged(
                texture: SurfaceTexture,
                width: Int,
                height: Int
        ) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val activity: Activity? = activity
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return
        }
        val rotation = activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0F, 0F, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(
                0F, 0F,
                mPreviewSize!!.height.toFloat(),
                mPreviewSize!!.width.toFloat()
        )
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                    viewHeight.toFloat() / mPreviewSize!!.height,
                    viewWidth.toFloat() / mPreviewSize!!.width
            )
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate(90 * (rotation - 2).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        mTextureView!!.setTransform(matrix)
    }

    private val mCaptureCallback: CaptureCallback = object : CaptureCallback() {
        private fun process(result: CaptureResult) {
            when (mState) {
                STATE_PREVIEW -> {
                }
                STATE_WAITING_LOCK -> {
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null) {
                        // captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                    ) {
                        // CONTROL_AE_STATE can be null on some devices
                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                        ) {
                            mState = STATE_PICTURE_TAKEN
                            // captureStillPicture();
                        } else {
                            runPrecaptureSequence()
                        }
                    }
                }
                STATE_WAITING_PRECAPTURE -> {

                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED
                    ) {
                        mState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {

                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN
                        //  captureStillPicture();
                    }
                }
            }
        }

        private fun runPrecaptureSequence() {
            try {
                // This is how to tell the camera to trigger.
                mPreviewRequestBuilder!!.set(
                        CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                        CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
                )
                // Tell #mCaptureCallback to wait for the precapture sequence to be set.
                mState = STATE_WAITING_PRECAPTURE
                mCaptureSession!!.capture(
                        mPreviewRequestBuilder!!.build(), this,
                        mBackgroundHandler
                )
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        private fun unlockFocus() {
            try {
                // Reset the auto-focus trigger
                mPreviewRequestBuilder!!.set(
                        CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
                )
                setAutoFlash(mPreviewRequestBuilder!!)
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                mCaptureSession!!.capture(
                        mPreviewRequestBuilder!!.build(), this,
                        mBackgroundHandler
                )
                // After this, the camera will go back to the normal state of preview.
                mState = STATE_PREVIEW
                mCaptureSession!!.setRepeatingRequest(
                        mPreviewRequest!!, this,
                        mBackgroundHandler
                )
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onCaptureProgressed(
                @NonNull session: CameraCaptureSession,
                @NonNull request: CaptureRequest,
                @NonNull partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
                @NonNull session: CameraCaptureSession,
                @NonNull request: CaptureRequest,
                @NonNull result: TotalCaptureResult
        ) {
            process(result)
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val texture = mTextureView!!.surfaceTexture!!

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)
            val mImageSurface = mImageReader!!.surface


            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder =
                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder!!.addTarget(surface)
            mPreviewRequestBuilder!!.addTarget(mImageSurface)


            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice!!.createCaptureSession(
                    Arrays.asList(surface, mImageReader!!.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return
                            }
                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder!!.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                                )
                                setAutoFlash(mPreviewRequestBuilder!!)
                                mPreviewRequest = mPreviewRequestBuilder!!.build()
                                mCaptureSession!!.setRepeatingRequest(
                                        mPreviewRequest!!,
                                        mCaptureCallback, mBackgroundHandler
                                )
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(
                                @NonNull cameraCaptureSession: CameraCaptureSession
                        ) {
                            showToast("Failed")
                        }
                    }, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun takePicture() {
        lockFocus()
    }

    private fun lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START
            )
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK
            mCaptureSession!!.capture(
                    mPreviewRequestBuilder!!.build(), mCaptureCallback,
                    mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun showToast(text: String) {
        val activity: Activity? = activity
        activity?.runOnUiThread { Toast.makeText(activity, text, Toast.LENGTH_SHORT).show() }
    }

    private val mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(@NonNull cameraDevice: CameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release()
            mCameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(@NonNull cameraDevice: CameraDevice) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(@NonNull cameraDevice: CameraDevice, error: Int) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
            val activity: Activity? = activity
            activity?.finish()
        }
    }

    private val mOnImageAvailableListener =
        ImageReader.OnImageAvailableListener { reader ->
            var inputImage: Image? = null
            try {
                if (reader != null) {
                    inputImage = reader.acquireLatestImage()
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            if (inputImage != null) {
                val imageBytes: ByteArray = ImageProcessing.getBytes(inputImage)
                val mBitmap =
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                Log.d(
                        "Length",
                        Integer.toString(imageBytes.size) + " " + Integer.toString(
                                inputImage.width
                        ) + " " + Integer.toString(inputImage.height)
                )
                mBackgroundHandler!!.post(
                        galaxy.app.stressdetector.CameraFragment.GetHeartRate(
                                mBitmap
                        )
                )
                inputImage.close()
            }
            curTime = System.currentTimeMillis()
            val totalTimeInSecs =
                (curTime - startTime) / 1000.0

            //            progressBar.setProgress((int)(totalTimeInSecs * 10));
            Log.d("Time", java.lang.Double.toString(totalTimeInSecs))
            var percentage = totalTimeInSecs / MainActivity.RECORDING_TIME * 100
            if (totalTimeInSecs >= MainActivity.RECORDING_TIME) {
                endActivity()
            }
        }
    private fun endActivity() {
        val cameraActivity: Activity? = activity
        cameraActivity!!.setResult(Activity.RESULT_OK)
        cameraActivity.finish()
    }

    private class GetHeartRate(private val mBitmap: Bitmap) : Runnable {
        override fun run() {
            var red: Long = 0
            var green: Long = 0
            var blue: Long = 0
            val width = mBitmap.width
            val height = mBitmap.height
            for (i in 0 until width) {
                for (j in 0 until height) {
                    val color = mBitmap.getPixel(i, j)
                    red += Color.red(color).toLong()
                    green += Color.green(color).toLong()
                    blue += Color.blue(color).toLong()
                }
            }
            val redAverage = (red / (width * height)).toInt()
            val greenAverage = (green / (width * height)).toInt()
            val blueAverage = (blue / (width * height)).toInt()
            val colorAverage =
                Color.rgb(redAverage, greenAverage, blueAverage)
            colorValues.add(colorAverage)
            Log.d("Red", java.lang.Double.toString(redAverage.toDouble()))
        }
    }

    fun getActiveArray(buffer: ByteBuffer): ByteArray? {
        val ret = ByteArray(buffer.remaining())
        if (buffer.hasArray()) {
            val array = buffer.array()
            System.arraycopy(
                    array,
                    buffer.arrayOffset() + buffer.position(),
                    ret,
                    0,
                    ret.size
            )
        } else {
            buffer.slice()[ret]
        }
        return ret
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (mFlashSupported) {
            try {
                requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
                mCaptureSession!!.setRepeatingRequest(requestBuilder.build(), null, null)
                //            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setProgressMax(pb: ProgressBar, max: Int) {
        pb.max = max * 100
    }

    private fun setProgressAnimate(pb: ProgressBar, progressTo: Int) {
        val animation = ObjectAnimator.ofInt(pb, "progress", pb.progress, progressTo * 100)
        animation.duration = MainActivity.RECORDING_TIME.toLong()*1000
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CameraFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(){
            colorValues.clear()
            startTime = System.currentTimeMillis()
        }

        private var param1: String? = null
        private var param2: String? = null

        private val ORIENTATIONS = SparseIntArray()
        private val REQUEST_CAMERA_PERMISSION = 1
        private val FRAGMENT_DIALOG = "dialog"

        private val TAG = "CameraFragment"
        private val STATE_PREVIEW = 0
        private val STATE_WAITING_LOCK = 1
        private val STATE_WAITING_PRECAPTURE = 2
        private val STATE_WAITING_NON_PRECAPTURE = 3
        private val STATE_PICTURE_TAKEN = 4

        private var mCameraId: String? = null
        private var mTextureView: AutoFitTextureView? = null
        private var mCaptureSession: CameraCaptureSession? = null
        private var mCameraDevice: CameraDevice? = null
        private var mPreviewSize: Size? = null

        private var mBackgroundThread: HandlerThread? = null
        private var mBackgroundHandler: Handler? = null
        private var mImageReader: ImageReader? = null
        private var mFile: File? = null

        private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
        private var mPreviewRequest: CaptureRequest? = null
        private var mState = STATE_PREVIEW
        private val mCameraOpenCloseLock =
            Semaphore(1)

        private val colorValues = ArrayList<Int>()
        private var mFlashSupported = false

        private var startTime: Long = 0
        private var curTime: Long = 0

        private val mTransitionsContainer: ViewGroup? = null
        private val progressBar: ProgressBar? = null



        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        private val resultRGB = ByteArray(0)

        private val mBitmap: Bitmap? = null
        var width_ima = 0
        var height_ima = 0

        fun getBitmap(): Bitmap? {
            return mBitmap
        }

        fun getColorValues(): ArrayList<Int>? {
            val result = ArrayList<Int>()
            for (i in colorValues) {
                result.add(i)
            }
            return result
        }
    }
}