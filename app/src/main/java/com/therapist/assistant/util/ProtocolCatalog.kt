package com.therapist.assistant.util

/**
 * کاتالوگ پروتکل‌های جلسه با سوالات راهنما.
 * استفاده: ProtocolCatalog.all() / find() / formatChecklistForNotes()
 */

data class ProtocolStep(
    val title: String,
    val guideQuestions: List<String>,
    val note: String = ""
)

data class SessionProtocol(
    val title: String,
    val category: String,
    val durationHint: String,
    val linkedAssessments: String,
    val steps: List<ProtocolStep>
)

val allProtocols: List<SessionProtocol> = listOf(

    // ─── CBT ───
    SessionProtocol(
        title = "اضطراب (CBT)",
        category = "CBT",
        durationHint = "۴۵–۶۰ دقیقه",
        linkedAssessments = "GAD-7 · K6 · PSS-10",
        steps = listOf(
            ProtocolStep(
                title = "بررسی علائم و محرک‌های هفته",
                guideQuestions = listOf(
                    "این هفته میانگین اضطراب از ۱ تا ۱۰ چند بود؟ در اوج چقدر؟",
                    "در چه موقعیت‌ها، مکان‌ها یا ساعات شدت بیشتر شد؟",
                    "بدن چه نشانه‌هایی داشت؟ (تپش، تنگی نفس، لرزش، تعریق، درد قفسه)",
                    "از چه موقعیت‌ها یا افراد اجتناب کردید؟",
                    "اضطراب چه تأثیری روی کار، خواب یا روابط گذاشت؟"
                ),
                note = "فهرست محرک‌ها را برای سلسله‌مراتب مواجهه نگه دارید."
            ),
            ProtocolStep(
                title = "تنظیم برانگیختگی (۵–۱۰ دقیقه)",
                guideQuestions = listOf(
                    "الان تنش بدنی‌تان از ۱ تا ۱۰ چند است؟",
                    "کدام برایتان راحت‌تر است: تنفس دیافراگمی، جعبه‌ای، یا آرام‌سازی عضلانی؟",
                    "بعد از تمرین، عدد تنش چقدر کم شد؟"
                ),
                note = "هدف: کاهش برانگیختگی قبل از کار شناختی؛ نه سرکوب کامل اضطراب."
            ),
            ProtocolStep(
                title = "شناسایی فکر خودکار",
                guideQuestions = listOf(
                    "در اوج اضطراب دقیقاً چه جمله‌ای از ذهن گذشت؟",
                    "اگر آن فکر درست باشد، بدترین پیامد از نظر شما چیست؟",
                    "این فکر دربارهٔ خودتان / دیگران / آینده چه می‌گوید؟",
                    "الان چقدر به این فکر باور دارید؟ (۰ تا ۱۰۰٪)"
                ),
                note = "فکر را عین عبارت مراجع بنویسید."
            ),
            ProtocolStep(
                title = "بررسی شواهد له و علیه",
                guideQuestions = listOf(
                    "چه شواهدی به نفع این فکر است؟",
                    "چه شواهدی علیه آن وجود دارد؟",
                    "اگر دوست صمیمی‌تان همین فکر را داشت، چه می‌گفتید؟",
                    "آیا در گذشته موقعیت مشابهی بهتر از پیش‌بینی‌تان تمام شده؟",
                    "تفسیر جایگزین واقع‌بینانه‌تر چیست؟ درصد باور به آن چقدر است؟"
                ),
                note = "پس از بازسازی، درصد باور فکر اولیه را دوباره بپرسید."
            ),
            ProtocolStep(
                title = "سلسله‌مراتب و مواجهه کوچک",
                guideQuestions = listOf(
                    "کدام موقعیت اجتنابی است اما هنوز کمی قابل تحمل؟",
                    "ترس از آن موقعیت از ۰ تا ۱۰۰ چند است؟",
                    "یک گام پایین‌تر از «غیرممکن» که این هفته انجام‌شدنی باشد چیست؟",
                    "مدت مواجهه چقدر باشد و چگونه متوجه می‌شوید تمام شده؟"
                ),
                note = "مواجهه باید مشخص، زمان‌دار و قابل اندازه‌گیری باشد."
            ),
            ProtocolStep(
                title = "تکلیف خانه و جمع‌بندی",
                guideQuestions = listOf(
                    "تا جلسه بعد دقیقاً چه کاری، چند بار، در چه روزهایی؟",
                    "اگر اضطراب بالا رفت از کدام مهارت استفاده می‌کنید؟",
                    "نشانهٔ موفقیت کوچک این هفته چیست؟",
                    "چه چیزی ممکن است مانع تکلیف شود و برنامهٔ جایگزین چیست؟"
                ),
                note = "در صورت امکان GAD-7 را جلسه بعد تکرار کنید."
            )
        )
    ),

    SessionProtocol(
        title = "افسردگی (فعال‌سازی رفتاری)",
        category = "CBT",
        durationHint = "۴۵–۶۰ دقیقه",
        linkedAssessments = "PHQ-9 · WHO-5 · DASS-21",
        steps = listOf(
            ProtocolStep(
                title = "پایش خلق و انرژی",
                guideQuestions = listOf(
                    "خلق امروز از ۱ تا ۱۰ چند است؟ میانگین هفته چطور بود؟",
                    "سطح انرژی و انگیزه از ۱ تا ۱۰؟",
                    "خواب، اشتها و تمرکز نسبت به هفته قبل چگونه بود؟",
                    "آیا افکار ناامیدی، بی‌ارزشی یا خستگی از زندگی پررنگ‌تر شده؟",
                    "آیا افکار آسیب به خود مطرح است؟ (در صورت بله، اولویت ایمنی)"
                ),
                note = "در خطر خودکشی، پروتکل ایمنی بر هر مداخلهٔ دیگری مقدم است."
            ),
            ProtocolStep(
                title = "مرور فعالیت‌های هفته",
                guideQuestions = listOf(
                    "کدام کارها را حتی با حال بد انجام دادید؟",
                    "کدام فعالیت‌ها کمی حال را بهتر کرد؟",
                    "کجا بیشتر در رختخواب، انزوا یا صفحه نمایش ماندید؟",
                    "کدام کارها را به تعویق انداختید و چه حسی بعدش آمد؟"
                ),
                note = "حلقه خلق پایین ← کاهش فعالیت ← خلق پایین‌تر را برجسته کنید."
            ),
            ProtocolStep(
                title = "تفکیک لذت و تسلط",
                guideQuestions = listOf(
                    "کدام فعالیت حتی کمی لذت‌بخش است؟",
                    "کدام کار حس «از پسش برآمدم» می‌دهد؟",
                    "کدام فعالیت هم لذت و هم تسلط دارد؟",
                    "از ۱ تا ۱۰، لذت و تسلط هر فعالیت پیشنهادی چقدر است؟"
                ),
                note = "جدول دو ستونی لذت / تسلط در یادداشت جلسه مفید است."
            ),
            ProtocolStep(
                title = "برنامه فعالیت کوچک",
                guideQuestions = listOf(
                    "یک فعالیت ۱۵–۳۰ دقیقه‌ای کاملاً انجام‌شدنی چیست؟",
                    "چه روز و ساعتی؟ کجا؟ با چه کسی یا تنها؟",
                    "چه چیزی ممکن است مانع شود؟",
                    "اگر مانع آمد، نسخهٔ ساده‌تر ۵ دقیقه‌ای چیست؟"
                ),
                note = "حداکثر ۲ فعالیت مشخص تا جلسه بعد."
            ),
            ProtocolStep(
                title = "افکار بازدارنده",
                guideQuestions = listOf(
                    "قبل از شروع فعالیت معمولاً چه فکری می‌آید؟",
                    "«فایده‌ای ندارد» یا «حالم بهتر نمی‌شود» چقدر باور دارید؟ (۰–۱۰۰٪)",
                    "شواهد هفته که نشان دهد حرکت کوچک مفید بوده چیست؟",
                    "جملهٔ کمکی کوتاه قبل از عمل چه می‌تواند باشد؟"
                ),
                note = "هدف کاهش مانع عمل است، نه حذف کامل فکر منفی."
            ),
            ProtocolStep(
                title = "تکلیف و پیگیری",
                guideQuestions = listOf(
                    "دو فعالیت توافق‌شده دقیقاً کدام‌اند؟",
                    "چطور یادآوری می‌کنید؟ (زنگ، پیام، تقویم)",
                    "در جلسه بعد خلق و درصد انجام تکلیف را چطور گزارش می‌دهید؟",
                    "چه زمانی PHQ-9 را تکرار کنیم؟ (معمولاً هر ۲–۴ هفته)"
                )
            )
        )
    ),

    SessionProtocol(
        title = "بازبینی شناختی (CBT کلاسیک)",
        category = "CBT",
        durationHint = "۴۵ دقیقه",
        linkedAssessments = "متناسب با شکایت اصلی",
        steps = listOf(
            ProtocolStep(
                title = "موقعیت و هیجان",
                guideQuestions = listOf(
                    "چه اتفاقی افتاد؟ کجا، کی، با چه کسی؟",
                    "چه هیجانی بود و شدت آن از ۰ تا ۱۰۰؟",
                    "آیا هیجان‌های دیگری هم همزمان بود؟"
                )
            ),
            ProtocolStep(
                title = "فکر خودکار",
                guideQuestions = listOf(
                    "در آن لحظه دقیقاً چه فکری از ذهن گذشت؟",
                    "اگر این فکر درست باشد دربارهٔ شما / دیگران / آینده چه می‌گوید؟",
                    "چقدر به آن باور دارید؟ (۰–۱۰۰٪)"
                )
            ),
            ProtocolStep(
                title = "شناسایی تحریف شناختی",
                guideQuestions = listOf(
                    "آیا همه یا هیچ است؟",
                    "آیا فاجعه‌سازی یا پیش‌گویی منفی است؟",
                    "آیا ذهن‌خوانی، شخصی‌سازی یا برچسب‌زنی است؟",
                    "آیا فقط روی جنبه منفی تمرکز کرده‌اید (فیلتر ذهنی)؟"
                ),
                note = "نام تحریف را با زبان ساده برای مراجع توضیح دهید."
            ),
            ProtocolStep(
                title = "شواهد و پاسخ جایگزین",
                guideQuestions = listOf(
                    "شواهد له و علیه فکر چیست؟",
                    "جملهٔ متعادل‌تر و واقع‌بینانه‌تر چیست؟",
                    "با این جمله جدید شدت هیجان چقدر می‌شود؟"
                )
            ),
            ProtocolStep(
                title = "آزمایش رفتاری",
                guideQuestions = listOf(
                    "تا هفته بعد چه آزمایش کوچکی این فکر را می‌سنجد؟",
                    "نتیجهٔ مورد انتظار چیست و چگونه ثبت می‌کنید؟",
                    "اگر نتیجه خلاف پیش‌بینی بود، چه نتیجه‌ای می‌گیرید؟"
                )
            )
        )
    ),

    SessionProtocol(
        title = "مواجهه و پیشگیری از پاسخ (ERP سبک)",
        category = "CBT",
        durationHint = "۴۵–۶۰ دقیقه",
        linkedAssessments = "GAD-7 · متناسب با وسواس",
        steps = listOf(
            ProtocolStep(
                title = "شناسایی وسواس و اجبار",
                guideQuestions = listOf(
                    "فکر یا تصویر مزاحم چیست؟",
                    "چه کارهایی برای کم کردن اضطراب انجام می‌دهید؟ (اجبار/اجتناب)",
                    "اگر اجبار را انجام ندهید چه می‌ترسید رخ دهد؟"
                )
            ),
            ProtocolStep(
                title = "سلسله‌مراتب اضطراب",
                guideQuestions = listOf(
                    "موقعیت‌ها را از ۰ تا ۱۰۰ مرتب کنید",
                    "کدام مورد بین ۴۰ تا ۶۰ است و این هفته قابل تمرین؟"
                ),
                note = "از موارد بسیار شدید شروع نکنید."
            ),
            ProtocolStep(
                title = "طراحی مواجهه بدون پاسخ",
                guideQuestions = listOf(
                    "در مواجهه دقیقاً چه می‌کنید و چه کاری را عمداً انجام نمی‌دهید؟",
                    "چقدر باید در موقعیت بمانید تا اضطراب کمی فروکش کند؟",
                    "نشانهٔ تمام شدن تمرین چیست؟"
                )
            ),
            ProtocolStep(
                title = "تکلیف و ثبت",
                guideQuestions = listOf(
                    "چند بار در هفته این تمرین را تکرار می‌کنید؟",
                    "اضطراب قبل، حین و بعد را چگونه نمره می‌دهید؟"
                )
            )
        )
    ),

    // ─── ACT ───
    SessionProtocol(
        title = "پروتکل ACT خلاصه",
        category = "ACT",
        durationHint = "۴۵–۶۰ دقیقه",
        linkedAssessments = "—",
        steps = listOf(
            ProtocolStep(
                title = "حضور و نام‌گذاری تجربه",
                guideQuestions = listOf(
                    "الان در بدن چه حس‌هایی هست؟",
                    "در ذهن چه فکر یا تصویری می‌چرخد؟",
                    "اگر فقط نام بگذاریم (نه جنگیدن) چه برچسبی دارد؟",
                    "آیا می‌توانید یک دقیقه فقط مشاهده کنید بدون تغییر دادن؟"
                ),
                note = "پذیرش ≠ تسلیم؛ یعنی جنگیدن کمتر با تجربه درونی."
            ),
            ProtocolStep(
                title = "گسلش شناختی",
                guideQuestions = listOf(
                    "این یک فکر است یا حقیقت قطعی؟",
                    "می‌توانید بگویید: «دارم این فکر را می‌کنم که…»؟",
                    "اگر فکر را روی ابر یا برگ شناور تصور کنید چه می‌شود؟",
                    "آیا لازم است همین الان به فکر جواب بدهید؟"
                )
            ),
            ProtocolStep(
                title = "خودِ مشاهده‌گر",
                guideQuestions = listOf(
                    "چه کسی این فکر و حس را می‌بیند؟",
                    "آیا شما برابر با این فکر هستید یا بزرگ‌تر از آن؟"
                )
            ),
            ProtocolStep(
                title = "اتصال به ارزش‌ها",
                guideQuestions = listOf(
                    "می‌خواهید در زندگی نمایندهٔ چه چیزی باشید؟",
                    "در رابطه / کار / سلامت کدام ارزش برایتان مهم است؟",
                    "کدام ارزش این هفته نادیده گرفته شد؟",
                    "اگر اضطراب نبود، چه کاری همسو با ارزش انجام می‌دادید؟"
                )
            ),
            ProtocolStep(
                title = "تعهد رفتاری",
                guideQuestions = listOf(
                    "یک رفتار کوچک همسو با ارزش تا جلسه بعد چیست؟",
                    "کی و کجا انجام می‌شود؟",
                    "اگر مانع درونی آمد، همان رفتار را چگونه ۵ دقیقه انجام می‌دهید؟",
                    "موفقیت را چطور تشخیص می‌دهید؟"
                )
            )
        )
    ),

    // ─── زوج ───
    SessionProtocol(
        title = "جلسه زوج‌درمانی",
        category = "زوج",
        durationHint = "۶۰–۷۵ دقیقه",
        linkedAssessments = "—",
        steps = listOf(
            ProtocolStep(
                title = "قرارداد جلسه امن",
                guideQuestions = listOf(
                    "آیا موافقید حرف یکدیگر را قطع نکنید؟",
                    "اگر تنش بالا رفت، چه علامتی برای توقف کوتاه داریم؟",
                    "هدف مشترک این جلسه از نظر هر دو نفر چیست؟",
                    "چه موضوعی امروز خارج از دستور جلسه بماند؟"
                )
            ),
            ProtocolStep(
                title = "بیان نیاز با پیام من",
                guideQuestions = listOf(
                    "وقتی … می‌شود، من احساس … می‌کنم، چون نیاز دارم …",
                    "بدون سرزنش، یک درخواست مشخص این هفته چیست؟",
                    "آیا طرف مقابل درست متوجه نیاز شد؟"
                )
            ),
            ProtocolStep(
                title = "بازگویی همدلانه",
                guideQuestions = listOf(
                    "آنچه شنیدید را با کلمات خود بگویید",
                    "کدام بخش برای همسرتان مهم‌ترین بود؟",
                    "آیا چیزی برای اصلاح یا تکمیل هست؟"
                ),
                note = "هدف فهمیدن است، نه موافقت اجباری."
            ),
            ProtocolStep(
                title = "چرخه تعارض",
                guideQuestions = listOf(
                    "معمولاً چه کسی شروع می‌کند و چه کسی کنار می‌کشد؟",
                    "در آخرین جر و بحث بدن و فکر هر کدام چه بود؟",
                    "نقطهٔ قابل تغییر کوچک در این چرخه کجاست؟"
                )
            ),
            ProtocolStep(
                title = "تکلیف ارتباطی هفتگی",
                guideQuestions = listOf(
                    "یک گفتگوی ۱۵ دقیقه‌ای بدون موبایل در چه روز و ساعتی؟",
                    "موضوع امن برای تمرین چیست؟",
                    "اگر بحث تند شد، از کدام قاعده جلسه استفاده می‌کنید؟"
                )
            )
        )
    ),

    SessionProtocol(
        title = "پیش از ازدواج",
        category = "زوج",
        durationHint = "۶۰ دقیقه",
        linkedAssessments = "—",
        steps = listOf(
            ProtocolStep(
                title = "انگیزه و انتظار از ازدواج",
                guideQuestions = listOf(
                    "چرا می‌خواهید با هم ازدواج کنید؟",
                    "تصویر زندگی مشترک ایده‌آل هر کدام چیست؟",
                    "کدام ترس‌ها دربارهٔ ازدواج وجود دارد؟"
                )
            ),
            ProtocolStep(
                title = "خانواده مبدأ و نقش‌ها",
                guideQuestions = listOf(
                    "در خانواده اصلی نقش زن/مرد و تصمیم‌گیری چگونه بود؟",
                    "کدام الگو را می‌خواهید تکرار یا تغییر دهید؟",
                    "مرز با خانواده‌های مبدأ بعد از ازدواج کجا باشد؟"
                )
            ),
            ProtocolStep(
                title = "الگوی حل تعارض",
                guideQuestions = listOf(
                    "در آخرین اختلاف هر کدام چه کردید؟",
                    "آیا یکی کنار می‌کشد و دیگری پیگیری می‌کند؟",
                    "چگونه به مصالحه رسیده‌اید؟ آیا یکی بیشتر کوتاه آمده؟"
                )
            ),
            ProtocolStep(
                title = "مالی، فرزند و مرزها",
                guideQuestions = listOf(
                    "تصمیم‌های مالی مشترک چگونه گرفته می‌شود؟",
                    "نگاه به فرزندآوری و تربیت چیست؟",
                    "دوستی با جنس مخالف / حریم خصوصی گوشی چگونه است؟"
                )
            ),
            ProtocolStep(
                title = "جمع‌بندی",
                guideQuestions = listOf(
                    "سه نقطه قوت رابطه چیست؟",
                    "یک حوزه که نیاز به گفتگوی بیشتر دارد چیست؟",
                    "آیا هر دو برای ادامه مسیر آمادگی دارند؟"
                )
            )
        )
    ),

    // ─── بالینی ───
    SessionProtocol(
        title = "خواب / بهداشت خواب",
        category = "بالینی",
        durationHint = "۳۰–۴۵ دقیقه",
        linkedAssessments = "ISI",
        steps = listOf(
            ProtocolStep(
                title = "جدول خواب هفته",
                guideQuestions = listOf(
                    "ساعت خواب و بیداری واقعی چه بوده است؟",
                    "چقدر طول کشیده تا به خواب بروید؟",
                    "چند بار وسط شب بیدار شده‌اید و چرا؟",
                    "کیفیت خواب از ۱ تا ۱۰؟ خواب‌آلودگی روزانه چطور؟"
                )
            ),
            ProtocolStep(
                title = "محدودیت زمان بستر",
                guideQuestions = listOf(
                    "تقریباً چند ساعت واقعاً می‌خوابید؟",
                    "می‌توانید زمان حضور در بستر را به همان نزدیک کنید؟",
                    "ساعت ثابت بیدار شدن حتی در تعطیلات ممکن است؟"
                ),
                note = "محرومیت شدید خواب بدون آمادگی توصیه نمی‌شود."
            ),
            ProtocolStep(
                title = "کاهش محرک شبانه",
                guideQuestions = listOf(
                    "کافئین، نیکوتین و چرت روزانه چگونه است؟",
                    "صفحه نمایش از چند ساعت قبل خواب قطع می‌شود؟",
                    "اتاق خواب فقط برای خواب است یا کار/موبایل هم هست؟",
                    "اگر خواب نبردید چه می‌کنید؟ (ماندن در بستر یا بلند شدن)"
                )
            ),
            ProtocolStep(
                title = "نگرانی شبانه و فکر",
                guideQuestions = listOf(
                    "در تخت چه فکرهایی می‌چرخد؟",
                    "می‌توانید «زمان نگرانی» عصرگاهی ۱۵ دقیقه‌ای بگذارید؟"
                )
            ),
            ProtocolStep(
                title = "پیگیری",
                guideQuestions = listOf(
                    "تا جلسه بعد جدول خواب را پر می‌کنید؟",
                    "ISI را چه زمانی تکرار کنیم؟",
                    "یک تغییر کوچک قطعی این هفته چیست؟"
                )
            )
        )
    ),

    SessionProtocol(
        title = "مدیریت خشم",
        category = "بالینی",
        durationHint = "۴۵ دقیقه",
        linkedAssessments = "—",
        steps = listOf(
            ProtocolStep(
                title = "الگوی خشم",
                guideQuestions = listOf(
                    "آخرین بار خشم شدید چه موقع بود؟",
                    "محرک بیرونی و فکر همراه آن چه بود؟",
                    "بدن چه نشانه‌های هشدار زودرسی دارد؟",
                    "پیامد رفتار خشمگین چه بود؟"
                )
            ),
            ProtocolStep(
                title = "توقف و تنظیم",
                guideQuestions = listOf(
                    "می‌توانید علامت توقف (نفس، ترک کوتاه موقعیت) تعریف کنید؟",
                    "کدام مهارت آرام‌سازی برایتان عملی است؟",
                    "حداقل زمان لازم برای فروکش نسبی چیست؟"
                )
            ),
            ProtocolStep(
                title = "بازسازی شناختی خشم",
                guideQuestions = listOf(
                    "آیا نیت طرف مقابل را قطعی فرض کردید؟",
                    "تفسیر جایگزین کمتر خصمانه چیست؟",
                    "آیا این موقعیت واقعاً به اندازه واکنش‌تان مهم است؟"
                )
            ),
            ProtocolStep(
                title = "مهارت ابراز قاطعانه",
                guideQuestions = listOf(
                    "پیام من بدون توهین چگونه گفته می‌شود؟",
                    "درخواست مشخص شما چیست؟",
                    "تمرین نقش‌آفرینی کوتاه در جلسه ممکن است؟"
                )
            ),
            ProtocolStep(
                title = "تکلیف",
                guideQuestions = listOf(
                    "ثبت ۲ موقعیت خشم با محرک، فکر، رفتار و پیامد",
                    "یک بار استفاده آگاهانه از توقف و نفس عمیق"
                )
            )
        )
    ),

    // ─── عمومی ───
    SessionProtocol(
        title = "جلسه اول پذیرش",
        category = "عمومی",
        durationHint = "۴۵–۶۰ دقیقه",
        linkedAssessments = "غربالگری متناسب با شکایت",
        steps = listOf(
            ProtocolStep(
                title = "شکایت اصلی و اهداف",
                guideQuestions = listOf(
                    "چه چیزی شما را الان به اینجا آورده؟",
                    "مشکل از کی شروع شد و چه چیزی اخیراً بدتر کرد؟",
                    "اگر درمان موفق باشد، سه ماه دیگر چه تغییری می‌بینید؟",
                    "هدف شما از این جلسات به زبان خودتان چیست؟"
                )
            ),
            ProtocolStep(
                title = "سابقه و زمینه",
                guideQuestions = listOf(
                    "سابقه درمان روان‌شناختی یا دارو؟",
                    "حمایت خانواده و دوستان چگونه است؟",
                    "استرس‌های فعلی زندگی (کار، مالی، رابطه) کدام‌اند؟",
                    "نقاط قوت و منابع مقابله‌ای شما چیست؟"
                )
            ),
            ProtocolStep(
                title = "ایمنی",
                guideQuestions = listOf(
                    "آیا افکار آسیب به خود یا دیگران مطرح است؟",
                    "آیا برنامه‌ای یا اقدام قبلی بوده؟",
                    "در بحران به چه کسی می‌توانید زنگ بزنید؟"
                ),
                note = "در صورت خطر، پروتکل ایمنی و ارجاع فوری اولویت دارد."
            ),
            ProtocolStep(
                title = "چارچوب درمان و محرمانگی",
                guideQuestions = listOf(
                    "حدود محرمانگی و استثناهای قانونی روشن است؟",
                    "تعداد، فاصله و هزینه جلسات توافق شد؟",
                    "نقش تکلیف بین جلسات را می‌پذیرید؟"
                )
            ),
            ProtocolStep(
                title = "تکلیف اولیه",
                guideQuestions = listOf(
                    "یک مشاهده ساده تا جلسه بعد چیست؟ (مثلاً ثبت خلق روزانه)",
                    "آیا آزمون غربالگری امروز یا جلسه بعد انجام شود؟"
                )
            )
        )
    ),

    SessionProtocol(
        title = "جمع‌بندی و ترخیص",
        category = "عمومی",
        durationHint = "۳۰–۴۵ دقیقه",
        linkedAssessments = "مقایسه با نمرات اولیه",
        steps = listOf(
            ProtocolStep(
                title = "مرور اهداف و پیشرفت",
                guideQuestions = listOf(
                    "کدام اهداف محقق شد؟ کدام نیمه‌کاره ماند؟",
                    "چه مهارتی را یاد گرفتید که قبلاً نداشتید؟",
                    "نمرات آزمون‌ها نسبت به ابتدا چگونه است؟"
                )
            ),
            ProtocolStep(
                title = "پیشگیری از عود",
                guideQuestions = listOf(
                    "نشانه‌های هشدار زودرس شما چیست؟",
                    "در صورت برگشت علائم اولین اقدام چیست؟",
                    "کدام مهارت را باید هفته‌ای یک بار تمرین کنید؟"
                )
            ),
            ProtocolStep(
                title = "منابع حمایتی",
                guideQuestions = listOf(
                    "چه کسانی یا چه منابعی در دسترس‌اند؟",
                    "در بحران به کجا مراجعه می‌کنید؟"
                )
            ),
            ProtocolStep(
                title = "برنامه پیگیری",
                guideQuestions = listOf(
                    "پیگیری اختیاری چه زمانی باشد؟",
                    "آیا نیاز به ارجاع دارویی یا تخصصی دیگر هست؟",
                    "جمع‌بندی یک جمله‌ای مراجع از مسیر درمان چیست؟"
                )
            )
        )
    )
)


object ProtocolCatalog {
    fun all(): List<SessionProtocol> = allProtocols

    fun byCategory(cat: String?): List<SessionProtocol> {
        return if (cat.isNullOrBlank()) allProtocols
        else allProtocols.filter { it.category == cat }
    }

    fun find(title: String): SessionProtocol? {
        return allProtocols.find { it.title == title }
    }

    fun suggestForAssessment(type: String, score: Int): SessionProtocol? {
        val t = type.uppercase()
        return when {
            t in setOf("PHQ9", "BDI2", "WHO5") && score >= 5 ->
                find("افسردگی (فعال‌سازی رفتاری)")
            t in setOf("GAD7", "BAI", "K6", "PSS10") && score >= 5 ->
                find("اضطراب (CBT)")
            t in setOf("ISI", "PSQI") ->
                find("خواب / بهداشت خواب")
            else -> null
        }
    }

    data class SoapDraft(
        val subjective: String,
        val objective: String,
        val assessment: String,
        val plan: String
    )

    fun formatSoapDraft(protocol: SessionProtocol, checked: Set<Int>): SoapDraft {
        val done = protocol.steps.mapIndexedNotNull { i, s -> if (i in checked) s else null }
        val pending = protocol.steps.mapIndexedNotNull { i, s -> if (i !in checked) s else null }
        val s = buildString {
            appendLine("پروتکل: ${protocol.title}")
            append("محور گفتگو / شکایت مطرح‌شده در جلسه (تکمیل شود): …")
        }.trim()
        val o = buildString {
            appendLine("گام‌های پوشش‌داده‌شده:")
            if (done.isEmpty()) append("— هنوز گامی تیک نخورده")
            else done.forEach { appendLine("• ${it.title}") }
        }.trim()
        val a = buildString {
            appendLine("ارزیابی اولیه بر اساس مسیر ${protocol.category}:")
            appendLine("آزمون پیشنهادی: ${protocol.linkedAssessments}")
            if (pending.isNotEmpty()) {
                append("گام‌های باقی‌مانده: " + pending.joinToString("؛ ") { it.title })
            }
        }.trim()
        val p = buildString {
            appendLine("برنامه تا جلسه بعد:")
            done.lastOrNull()?.guideQuestions?.take(2)?.forEach { appendLine("• $it") }
            appendLine("تکلیف خانه: (در صورت توافق ثبت شود)")
            append("پیگیری: ${protocol.durationHint}")
        }.trim()
        return SoapDraft(s, o, a, p)
    }

    fun formatChecklistForNotes(protocol: SessionProtocol, checked: Set<Int>): String {
        val sb = StringBuilder()
        sb.appendLine("── پروتکل: ${protocol.title} (${protocol.category}) ──")
        protocol.steps.forEachIndexed { i, step ->
            val mark = if (i in checked) "✓" else "○"
            sb.appendLine("$mark گام ${i + 1}: ${step.title}")
            step.guideQuestions.take(2).forEach { q ->
                sb.appendLine("   • $q")
            }
        }
        sb.appendLine("── پایان پروتکل ──")
        return sb.toString()
    }
}
