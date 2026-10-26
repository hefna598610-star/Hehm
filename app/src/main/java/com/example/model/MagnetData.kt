package com.example.model

enum class TriggerAction(val id: String, val titleAr: String, val descriptionAr: String) {
    FIRECRACKER_POP(
        id = "firecracker_pop",
        titleAr = "صوت فرقعة وموجات اهتزازية",
        descriptionAr = "إصدار طلقات فرقعة متتالية وموجات نبضية ترج التليفون وتروح"
    ),
    MOVE_TO_BACK(
        id = "move_to_back",
        titleAr = "إخفاء فوري (للشاشة الرئيسية)",
        descriptionAr = "يرسل التطبيق للخلفية فوراً لحماية الخصوصية"
    ),
    HAPTIC_ALERT(
        id = "haptic_alert",
        titleAr = "اهتزاز تنبيهي نبضي",
        descriptionAr = "اهتزاز لمسي متدرج ينبض عند الاقتراب"
    ),
    AUDIO_RADAR(
        id = "audio_radar",
        titleAr = "تنبيه صوتي (رادار كهرومغناطيسي)",
        descriptionAr = "إصدار تكتكات ونغمات صوتية تزداد عند اقتراب المغناطيس"
    ),
    VISUAL_FLASH(
        id = "visual_flash",
        titleAr = "إنذار بصري وتوهج",
        descriptionAr = "توهج شاشة الرادار ونبض العداد بالأحمر"
    )
}

enum class FieldStatus(val titleAr: String, val descriptionAr: String) {
    CALIBRATING("جاري المعايرة...", "قراءة مستقرة"),
    NORMAL("طبيعي (المجال الأرضي)", "شدة المجال المحيط الطبيعية"),
    ELEVATED("مجال مرتفع", "اقتراب من جهاز إلكتروني أو أسلاك"),
    TRIGGERED("مغناطيس مكتشف! فرقعة!", "تم تجاوز الحد المسموح به")
}

data class MagnetReading(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val magnitude: Float = 45f,
    val rawMagnitude: Float = 45f,
    val timestamp: Long = System.currentTimeMillis()
)
