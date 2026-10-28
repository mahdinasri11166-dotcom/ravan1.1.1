package com.therapist.assistant.util

/**
 * قالب‌های آماده SOAP برای تسریع یادداشت جلسه.
 */
object SoapTemplates {

    data class SoapTemplate(
        val id: String,
        val titleFa: String,
        val category: String,
        val subjective: String,
        val objective: String,
        val assessment: String,
        val plan: String
    )

    val all: List<SoapTemplate> = listOf(
        SoapTemplate(
            id = "anxiety_cbt",
            titleFa = "اضطراب — CBT",
            category = "اضطراب",
            subjective = "مراجع از نگرانی مداوم، تنش بدنی و افکار فاجعه‌ساز گزارش می‌دهد. محرک‌های اصلی: …",
            objective = "خلق مضطرب، تماس چشمی مناسب، گفتار منسجم. علائم جسمی گزارش‌شده: تپش قلب / تعریق / بی‌خوابی.",
            assessment = "علائم همسو با اختلال اضطراب فراگیر/موقعیتی. شدت تقریبی: خفیف/متوسط/شدید. بینش خوب.",
            plan = "۱) ثبت افکار خودکار ۲) آزمایش رفتاری ۳) تنفس دیافراگمی ۴) تکلیف خانگی: … ۵) جلسه بعد: …"
        ),
        SoapTemplate(
            id = "depression_cbt",
            titleFa = "افسردگی — CBT",
            category = "خلق",
            subjective = "کاهش انرژی، بی‌علاقگی، نشخوار فکری و احساس ناامیدی. خواب/اشتها: …",
            objective = "خلق پایین، کندی روانی-حرکتی خفیف/بدون. همکاری خوب در جلسه.",
            assessment = "علائم افسردگی اساسی/خفیف. خطر خودآسیبی: بررسی شد — منفی/نیاز به پیگیری. کارکرد روزانه مختل.",
            plan = "۱) فعال‌سازی رفتاری ۲) چالش افکار منفی ۳) برنامه فعالیت روزانه ۴) پیگیری خلق ۵) جلسه بعد: …"
        ),
        SoapTemplate(
            id = "couple_session",
            titleFa = "زوج‌درمانی — جلسه مشترک",
            category = "زوج",
            subjective = "هر دو طرف مسائل ارتباطی، تعارض و الگوهای تکراری را مطرح کردند. موضوع اصلی جلسه: …",
            objective = "تعامل در جلسه: دفاعی / همدلانه. نوبت‌گیری گفتگو رعایت شد/نشد.",
            assessment = "الگوی تعارض: تعقیب-فاصله / انتقاد-دفاع. انگیزه تغییر نسبی. مرزهای رابطه نیاز به کار دارد.",
            plan = "۱) تمرین گوش دادن فعال ۲) بیان نیاز بدون سرزنش ۳) تکلیف زوجی مشترک ۴) قوانین تعارض ۵) جلسه بعد: …"
        ),
        SoapTemplate(
            id = "intake",
            titleFa = "جلسه ارزیابی اولیه",
            category = "ارزیابی",
            subjective = "شکایت اصلی: … شروع و سیر علائم: … سابقه درمان قبلی: … حمایت اجتماعی: …",
            objective = "ظاهر متناسب، جهت‌یابی کامل، همکاری در مصاحبه. علائم روان‌پریشی مشهود نیست/هست.",
            assessment = "تشخیص افتراقی اولیه: … شدت و فوریت: … نیاز به ارجاع پزشکی/روان‌پزشکی: بله/خیر.",
            plan = "۱) تکمیل آزمون‌های غربالگری ۲) قرارداد درمانی ۳) اهداف کوتاه‌مدت ۴) تعیین تعداد جلسات آزمایشی ۵) پیگیری: …"
        ),
        SoapTemplate(
            id = "relapse_prevention",
            titleFa = "پیشگیری از عود",
            category = "نگهداری",
            subjective = "مراجع نشانه‌های هشدار اولیه و موقعیت‌های پرخطر را مرور کرد. محرک‌های اخیر: …",
            objective = "انگیزه حفظ دستاوردها مشهود. مهارت‌های آموخته‌شده را بازگو می‌کند.",
            assessment = "ریسک عود: پایین/متوسط/بالا. شبکه حمایتی کافی/ناکافی. پایبندی به برنامه خوب است.",
            plan = "۱) فهرست نشانه‌های هشدار ۲) نقشه اقدام اضطراری ۳) تقویت مهارت‌های مقابله ۴) برنامه خودمراقبتی ۵) زمان پیگیری: …"
        ),
        SoapTemplate(
            id = "trauma_stabilize",
            titleFa = "تروما — پایدارسازی",
            category = "تروما",
            subjective = "مراجع از intrusions / اجتناب / بیش‌برانگیختگی گزارش می‌دهد. احساس ایمنی فعلی: …",
            objective = "توان تنظیم هیجان در جلسه متوسط. grounding مؤثر بود/نیاز به تکرار.",
            assessment = "فاز پایدارسازی اولویت دارد. پردازش خاطره هنوز مناسب نیست/آماده است. منابع درونی در حال تقویت.",
            plan = "۱) تمرین grounding روزانه ۲) بهداشت خواب ۳) کاهش محرک‌های overload ۴) تقویت منابع ۵) جلسه بعد: …"
        )
    )

    fun byId(id: String): SoapTemplate? = all.find { it.id == id }

    fun formatForNotes(t: SoapTemplate): String = buildString {
        appendLine("── قالب SOAP: ${t.titleFa} ──")
        appendLine("S: ${t.subjective}")
        appendLine("O: ${t.objective}")
        appendLine("A: ${t.assessment}")
        appendLine("P: ${t.plan}")
    }
}
