package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.audio.FeedbackHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class MagnetBackgroundService : Service(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var magnetometer: Sensor? = null
    private var feedbackHelper: FeedbackHelper? = null
    private var threshold = 120.0f
    private var lastTriggerTime = 0L
    private val triggerCooldownMs = 2000L
    private val scope = CoroutineScope(Dispatchers.Default)

    companion object {
        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val EXTRA_THRESHOLD = "EXTRA_THRESHOLD"

        const val CHANNEL_ID = "magnet_radar_background_channel"
        const val NOTIFICATION_ID = 1001

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun startService(context: Context, threshold: Float = 120f) {
            val intent = Intent(context, MagnetBackgroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_THRESHOLD, threshold)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MagnetBackgroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        feedbackHelper = FeedbackHelper(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopMonitoring()
                stopForeground(true)
                stopSelf()
                _isRunning.value = false
                return START_NOT_STICKY
            }
            ACTION_START -> {
                threshold = intent.getFloatExtra(EXTRA_THRESHOLD, 120f)
                startForegroundWithNotification()
                startMonitoring()
                _isRunning.value = true
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification("المراقبة نشطة حتى أثناء فتح تطبيقات أخرى")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startMonitoring() {
        if (magnetometer != null) {
            sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun stopMonitoring() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_MAGNETIC_FIELD) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)

        val now = System.currentTimeMillis()
        if (magnitude > threshold && (now - lastTriggerTime > triggerCooldownMs)) {
            lastTriggerTime = now
            // Trigger the crackle firecracker sound and wave vibration!
            feedbackHelper?.playCrackleWaveBurst()

            // Update notification with alert
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val alertNotification = buildNotification("⚠️ تم رصد مغناطيس! القوة: ${magnitude.toInt()} µT")
            notificationManager?.notify(NOTIFICATION_ID, alertNotification)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun buildNotification(contentText: String): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MagnetBackgroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("رادار المغناطيس نشط بالخلفية")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إيقاف", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "مراقبة المغناطيس بالخلفية",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار استمرار رصد المجال المغناطيسي بالخلفية وإصدار صوت الفرقعة والموجات"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopMonitoring()
        feedbackHelper?.release()
        _isRunning.value = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
