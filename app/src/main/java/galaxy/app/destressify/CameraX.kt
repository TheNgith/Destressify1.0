package galaxy.app.destressify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import galaxy.app.destressify.databinding.ActivityCameraXBinding
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

typealias RGBListener = (rgb: Int) -> Unit

class CameraX : AppCompatActivity() {
    // Late initialize variable
    private lateinit var binding: ActivityCameraXBinding
    private lateinit var cameraExecutor: ExecutorService
    private var activityStartTime = 0L
    private lateinit var bpmText : TextView
    private lateinit var progressBar : CircularProgressBar

    class ColorAnalyzer(private val listener: RGBListener) : ImageAnalysis.Analyzer {

        private var lastAnalyzedTimestamp = 0L


        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()
            val data = ByteArray(remaining())
            get(data)
            return data
        }
        

        private fun getRGBfromYUV(image: ImageProxy): Int {
            val planes = image.planes

            val height = image.height
            val width = image.width

            // Y
            val yArr = planes[0].buffer
            val yArrByteArray = yArr.toByteArray()

            // U
            val uArr = planes[1].buffer
            val uArrByteArray = uArr.toByteArray()

            // V
            val vArr = planes[2].buffer
            val vArrByteArray = vArr.toByteArray()
            val byteArray = yArrByteArray + uArrByteArray + vArrByteArray
            var rSum = 0
            val size = height * width
            var yp = 0
            var u: Int
            var y: Int
            var uvp: Int
            for (j in 0 until height) {
                uvp = size + (j shr 1) * width
                u = 0
                for (i in 0 until width) {

                    y = ((0xff and byteArray[yp].toInt()) - 16)
                    if (y < 0) y = 0
                    if ((i and 1) == 0) {
                        u = ((0xff and byteArray[uvp++].toInt()) - 128)
                    }
                    val rTemp = (y + 1.370705 * u).toInt()
                    rSum += rTemp
                    yp++
                }
            }

            return rSum
        }


        override fun analyze(image: ImageProxy) {
            val currentTimestamp = System.currentTimeMillis()
            if (currentTimestamp - lastAnalyzedTimestamp > 4000) {
                val rSum = getRGBfromYUV(image)
                val rAvg = rSum / (image.width * image.height)
                listener(rAvg)
            }
            image.close()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraXBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        bpmText = binding.bpmValue
        progressBar = binding.circularProgressBar
        progressBar.apply {
            progressMax = maxSecs.toFloat()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activityStartTime = System.currentTimeMillis()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                //
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        startTime = System.currentTimeMillis()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val colorAnalyzer = createColorAnalyzer()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, colorAnalyzer
                )
                camera.cameraControl.enableTorch(true)


            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun createColorAnalyzer(): ImageAnalysis {
        return ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, ColorAnalyzer { gSum ->

                    getBPM(gSum)
                })
            }
    }

    // Here is the code for heart beat detection

    private var processing = AtomicBoolean(false)

    private var avgArrIndex = 0
    private var avgArrSize = 4
    private var avgArr = IntArray(avgArrSize)

    enum class TYPE {
        GREEN, RED
    }

    private var currentType = TYPE.GREEN

    private var beats = 0

    private var beatIndex = 0
    private val beatArrSize = 3
    private val beatArr = IntArray(beatArrSize)
    private var startTime = 0L

    private var totalBeats = 0


    private fun getBPM(imgAvg: Int) {
        runOnUiThread(
            object : Runnable {
                override fun run() {
                    progressBar.apply {
                        progress = (System.currentTimeMillis() - activityStartTime).toFloat()
                        setProgressWithAnimation(progress)
                    }
                }
            }
        )
        if (!processing.compareAndSet(false, true)) return
        if (imgAvg == 0 || imgAvg == 255) {
            processing.set(false)
            return
        }
        var avgArrAvg = 0
        var avgArrCnt = 0
        for (i in avgArr.indices) {
            if (avgArr[i] > 0) {
                avgArrAvg += avgArr[i]
                avgArrCnt++
            }
        }

        val rollingAvg = if (avgArrCnt > 0) (avgArrAvg / avgArrCnt) else 0
        var newType: TYPE = currentType
        if (imgAvg < rollingAvg) {
            newType = TYPE.RED
            if (newType != currentType) {
                beats++
                totalBeats++
                peakArray.add(System.currentTimeMillis().toDouble() / 1000.0)
                Log.d("", "Beat!")
            }
        } else if (imgAvg > rollingAvg) {
            newType = TYPE.GREEN
        }

        if (avgArrIndex == avgArrSize) avgArrIndex = 0
        avgArr[avgArrIndex] = imgAvg
        avgArrIndex++

        if (newType != currentType) {
            currentType = newType
        }
        val endTime = System.currentTimeMillis()
        val totalTimeInSecs = (endTime - startTime).toDouble() / 1000.0
        if (totalTimeInSecs >= timeInterval.toDouble()) {
            Log.d("Beats in 9s:", "$beats")
            val bps = (beats.toDouble() / totalTimeInSecs)
            val dpm = (bps * 60.0).toInt()
            if (dpm < 30 || dpm > 180) {
                startTime = System.currentTimeMillis()
                beats = 0
                processing.set(false)
                return
            }
            if (beatIndex == beatArrSize) beatIndex = 0
            beatArr[beatIndex] = dpm
            beatIndex++

            var beatAvgAvg = 0
            var beatAvgCnt = 0

            for (i in beatArr.indices) {
                if (beatArr[i] > 0) {
                    beatAvgAvg += beatArr[i]
                    beatAvgCnt++
                }
            }

            val beatAvg = beatAvgAvg / beatAvgCnt
            val newText = "BPM: $beatAvg"
            runOnUiThread(
                object : Runnable {
                    override fun run() {
                        bpmText.text = newText
                    }
                }
            )

            //Log.d("BPM", "$beatAvg")
            startTime = System.currentTimeMillis()
            beats = 0
            if (System.currentTimeMillis() - activityStartTime >= maxSecs) {
                averageBPM = beatAvg
                Log.d("Beats in 50s", "${peakArray.size}")
                endActivity()
            }
        }

        processing.set(false)
    }



    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun endActivity() {
        this.setResult(RESULT_OK)
        this.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private var peakArray = arrayListOf<Double>()
        private var averageBPM = 0
        fun getPeaksArray():ArrayList<Double>{
            return peakArray
        }
        fun getAverageBPM():Int{
            return averageBPM
        }
        private val maxSecs = 30000 + 4000
        private val timeInterval = 5
    }
}