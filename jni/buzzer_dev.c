#include <jni.h>
#include<buzzer_dev.h>
#include <fcntl.h>
#include<android/log.h>

#define TAG "buzzer_dev"
#define LOGD(msg,args...)__android_log_print(ANDROID_LOG_DEBUG,TAG,msg,##args)
#define LOGE(msg,args...)__android_log_print(ANDROID_LOG_ERROR,TAG,msg,##args)

int fd = -1;

JNIEXPORT void JNICALL Java_com_gec_queryterminal_library_BuzzerDev_openBuzzerDev
(JNIEnv *env, jobject obj) {
	fd = open("/dev/pwm",O_RDWR);
	if(fd < 0) {
		LOGE("Open buzzer device unsuccessfully.\n");
return;
	}
	LOGD( "Open buzzer device fd=%d successfully.\n", fd);
}
JNIEXPORT void JNICALL Java_com_gec_queryterminal_library_BuzzerDev_opBuzzerDev
(JNIEnv *env, jobject obj, jint op, jlong freq) {
ioctl(fd,op,freq);
}
JNIEXPORT void JNICALL Java_com_gec_queryterminal_library_BuzzerDev_closeBuzzerDev
(JNIEnv *env, jobject obj) {
close(fd);
LOGD("Close buzzer device successfully.\n");
}
