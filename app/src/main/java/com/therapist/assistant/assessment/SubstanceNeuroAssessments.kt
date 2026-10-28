package com.therapist.assistant.assessment

/**
 * مواد، خوردن، ADHD، اوتیسم غربال
 * افزودن آزمون جدید: یک AssessmentDef به لیست instruments اضافه کنید.
 */
object SubstanceNeuroAssessments {
    val instruments: List<AssessmentDef> = listOf(
        AssessmentDef(
            id = "AUDIT_C",
            titleFa = "AUDIT-C — مصرف الکل",
            titleEn = "Alcohol Use Disorders Identification Test-C",
            description = "۳ سؤال استاندارد WHO برای غربالگری مصرف پرخطر الکل.",
            options = listOf("۰" to 0, "۱" to 1, "۲" to 2, "۳" to 3, "۴" to 4),
            items = listOf(
                "هر چند وقت یک‌بار مشروب می‌نوشید؟",
                "در روزهای مصرف معمولاً چند واحد؟",
                "هر چند وقت ۶ واحد یا بیشتر در یک نوبت؟"
            ),
            interpret = { s ->
                if (s < 4) "خطر پایین / منفی غربالگری ($s)"
                else "مثبت غربالگری — ارزیابی کامل‌تر ($s)"
            },
            tags = "الکل مواد AUDIT WHO"
        ),

        AssessmentDef(
            id = "CAGE",
            titleFa = "CAGE — غربالگری الکل",
            titleEn = "CAGE Questionnaire",
            description = "۴ سؤال کلاسیک و معتبر غربالگری مشکل الکل.",
            options = listOf("خیر" to 0, "بله" to 1),
            items = listOf(
                "آیا احساس کرده‌اید باید مصرف را کم کنید؟ (Cut down)",
                "آیا از انتقاد دیگران آزرده شده‌اید؟ (Annoyed)",
                "آیا احساس گناه کرده‌اید؟ (Guilty)",
                "آیا صبح‌ها برای آرام شدن مشروب خورده‌اید؟ (Eye-opener)"
            ),
            interpret = { s ->
                if (s >= 2) "مثبت غربالگری ($s)" else "منفی معمول ($s)"
            },
            tags = "الکل CAGE"
        ),

        AssessmentDef(
            id = "SCOFF",
            titleFa = "SCOFF — اختلالات خوردن",
            titleEn = "SCOFF Questionnaire",
            description = "۵ سؤال غربالگری معتبر اختلالات خوردن.",
            options = listOf("خیر" to 0, "بله" to 1),
            items = listOf(
                "آیا به‌خاطر احساس ناخوشی از پرخوری، خود را وادار به استفراغ می‌کنید؟",
                "آیا نگران از دست دادن کنترل روی مقدار غذا هستید؟",
                "آیا اخیراً بیش از ۶ کیلو در ۳ ماه وزن کم کرده‌اید؟",
                "آیا فکر می‌کنید چاق هستید وقتی دیگران می‌گویند لاغرید؟",
                "آیا غذا بر زندگی شما مسلط است؟"
            ),
            interpret = { s ->
                if (s >= 2) "مثبت غربالگری ($s)" else "منفی معمول ($s)"
            },
            tags = "اختلال خوردن SCOFF"
        ),

        AssessmentDef(
            id = "ASRS6",
            titleFa = "ASRS-A — ADHD بزرگسالان",
            titleEn = "Adult ADHD Self-Report Scale Part A",
            description = "۶ سؤال بخش A مقیاس WHO برای ADHD بزرگسالان.",
            options = listOf("هرگز" to 0, "به‌ندرت" to 1, "گاهی" to 2, "اغلب" to 3, "خیلی اغلب" to 4),
            items = listOf(
                "مشکل در جزئیات نهایی پروژه",
                "مشکل در منظم کردن کارها",
                "فراموشی قرارها یا تعهدات",
                "اجتناب از کار نیازمند فکر زیاد",
                "بی‌قراری در نشستن طولانی",
                "احساس حرکت مداوم مثل موتور"
            ),
            interpret = { s ->
                when {
                    s >= 14 -> "احتمال بالاتر علائم ($s)"
                    s >= 10 -> "علائم متوسط ($s)"
                    else -> "زیر آستانه مجموع ($s)"
                }
            },
            tags = "ADHD تمرکز ASRS WHO"
        ),

        AssessmentDef(
            id = "AQ10",
            titleFa = "AQ-10 — طیف اوتیسم",
            titleEn = "Autism Spectrum Quotient-10",
            description = "۱۰ سؤال غربالگری صفات طیف اوتیسم در بزرگسالان (NICE).",
            options = listOf(
                "کاملاً موافقم" to 1, "کمی موافقم" to 1, "کمی مخالفم" to 0, "کاملاً مخالفم" to 0
            ),
            items = listOf(
                "اغلب جزئیات را بیشتر از کل متوجه می‌شوم",
                "تمرکزم روی کل تصویر بیشتر از جزئیات است",
                "انجام هم‌زمان چند کار آسان است",
                "پس از وقفه سریع به کار برمی‌گردم",
                "خواندن بین خطوط حرف دیگران آسان است",
                "ورود به گفتگو و ادامه آن آسان است",
                "تصور شخصیت‌های داستان هنگام خواندن سخت است",
                "مجذوب اعداد/تاریخ/کلکسیون می‌شوم",
                "درک نیت از چهره آسان است",
                "در موقعیت اجتماعی جدید نمی‌دانم چه کنم"
            ),
            interpret = { s ->
                if (s >= 6) "امتیاز غربالگری بالا ($s)" else "زیر آستانه ($s)"
            },
            tags = "اوتیسم طیف AQ"
        )
    )
}
