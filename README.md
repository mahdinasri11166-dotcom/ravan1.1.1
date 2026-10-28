# درمانگرنو (DarmanagarNo) — v1.1.1

سامانه مدیریت مطب روان‌شناسی (نوبت، پرونده سبک، آزمون، مالی، کارگاه).

**پشتیبانی:** کانال تیم توسعهٔ نو — [@new_development_ir](https://t.me/new_development_ir)

## آپلود در GitHub (مهم)

1. فایل زیپ را باز کنید → پوشه `TherapistAssistant`
2. **محتویات داخل** آن را در **root** ریپازیتوری بگذارید:
   - باید در root دیده شود: `app/`, `gradle/`, `gradlew`, `.github/`, `settings.gradle.kts`
3. Push روی `main`
4. تب **Actions** → Workflow **Build Release APK** → Artifact را دانلود کنید

یا محلی:

```bash
chmod +x gradlew
./gradlew clean testDebugUnitTest assembleRelease
# خروجی: app/build/outputs/apk/release/*.apk
```

## نیازمندی‌ها

- JDK **17**
- Android SDK (compileSdk مطابق `app/build.gradle.kts`)

## نسخه

- `versionName` = **1.1.1**
- `versionCode` = **111**

## نکات

- داده محلی + SQLCipher؛ پشتیبان SAF / اشتراک اختیاری
- امضای Release از keystore داخل `app/` یا GitHub Secrets (`KEYSTORE_BASE64`, `DMN_*`)
