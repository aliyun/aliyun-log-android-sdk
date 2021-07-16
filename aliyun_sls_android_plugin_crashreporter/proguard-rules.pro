-optimizationpasses 5       # 指定代码的压缩级别
-dontusemixedcaseclassnames     # 是否使用大小写混合
-dontskipnonpubliclibraryclasses        # 指定不去忽略非公共的库类
-dontskipnonpubliclibraryclassmembers       # 指定不去忽略包可见的库类的成员
-dontpreverify      # 混淆时是否做预校验
-verbose        # 混淆时是否记录日志
-dontoptimize
-dontshrink
#-applymapping mapping.txt
-printmapping mapping.txt
#-optimizations  code/removal/simple,code/removal/advanced,code/removal/variable,code/removal/exception,code/simplification/branch,code/simplification/field,code/simplification/cast,code/simplification/arithmetic,code/simplification/variable
-optimizations !code/simplification/cast,!field/*,!class/merging/*      # 混淆时所采用的算法
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
#----------------------------------------------------------------------------
-ignorewarnings     # 是否忽略检测，（是）
#---------------------------------默认保留区---------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
#-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}
#-ignorewarnings -keep class * { public private *; }

#如果有引用v4包可以添加下面这行
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#-keep public class * extends android.view.View{
#    *** get*();
#    void set*(***);
#    public <init>(android.content.Context);
#    public <init>(android.content.Context, android.util.AttributeSet);
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet);
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#表示不混淆R文件中的所有静态字段
-keep class **.R$* {
    public static <fields>;
}
#-keepclassmembers class * {
#    void *(**On*Event);
#}

#----------------------------------------------------------------------------
-keeppackagenames com.aliyun.sls.android.plugin.**
-keep class com.aliyun.sls.android.plugin.crashreporter.BuildConfig { *; }
#-keep class com.aliyun.sls.android.SLSLog {
#    public <methods>;
#}
#-keep class com.aliyun.sls.android.SLSAdapter {
#    public <methods>;
#}
#-keep class com.aliyun.sls.android.SLSConfig { *;}
-keep class com.aliyun.sls.android.plugin.crashreporter.SLSCrashReporterPlugin {
    public void init(com.aliyun.sls.android.SLSConfig);
}

# 不混淆 common
#-keep class com.aliyun.sls.android.scheme.** { *; }
#-keep class com.aliyun.sls.android.utdid.** { *; }
#-keep class com.aliyun.sls.android.JsonUtil { *; }
#-keep class com.aliyun.sls.android.SLSConfig { *; }
#-keep class com.aliyun.sls.android.SLSLog { *; }

-keep class com.uc.crashsdk.** { *; }
-keep interface com.uc.crashsdk.** { *; }