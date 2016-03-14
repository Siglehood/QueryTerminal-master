#include <jni.h>
#include<led_dev.h>
#include <fcntl.h>
#include<android/log.h>

#define IOCTL_LED_ON    1
#define IOCTL_LED_OFF   0

#define TAG "led_dev"
#define LOGD(msg,args...)__android_log_print(ANDROID_LOG_DEBUG,TAG,msg,##args)
#define LOGE(msg,args...)__android_log_print(ANDROID_LOG_ERROR,TAG,msg,##args)

int fd = -1;

JNIEXPORT void JNICALL Java_com_gec_queryterminal_library_LedDev_openLedDev
(JNIEnv *env, jobject obj) {
//打开设备
	fd = open("/dev/leds", O_RDWR);
		if (fd < 0) {
			LOGE("Open led device unsuccessfully.\n");
return;
		}
		LOGD( "Open led device fd=%d successfully.\n", fd);
}

JNIEXPORT void JNICALL Java_com_gec_queryterminal_library_LedDev_opLedDev
(JNIEnv *env, jobject obj, jint pos, jstring op) {
const char* cstrop = (*env)->GetStringUTFChars(env, op, 0);
int led_no = pos - 1;
//开灯
if (!strcmp(cstrop, "on")) {
	ioctl(fd, IOCTL_LED_ON, led_no);
//关灯
} else if (!strcmp(cstrop, "off")) {
	ioctl(fd, IOCTL_LED_OFF, led_no);
}
}

JNIEXPORT void JNICALL Java_com_gec_queryterminal_library_LedDev_closeLedDev
(JNIEnv *env, jobject obj) {
close(fd);
LOGD("Close led device successfully.\n");
}
