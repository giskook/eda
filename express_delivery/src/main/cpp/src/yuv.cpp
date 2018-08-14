//
// Created by zhangkai on 2018/6/7.
//
#include <jni.h>

#include <android/log.h>
static const char* kTAG = "__YUV";

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, kTAG, __VA_ARGS__))
#define LOGW(...) \
  ((void)__android_log_print(ANDROID_LOG_WARN, kTAG, __VA_ARGS__))
#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, kTAG, __VA_ARGS__))

extern "C" JNIEXPORT jstring JNICALL Java_com_giskook_express_1delivery_MainActivity_yuv_1string(JNIEnv * env, jobject thiz){
    LOGI("%s", "hello jni");
    return env->NewStringUTF("Hello from JNI LIBS!");
}


//crop yuv data
int yuv_crop(char *data, char *dst, int width, int height,
        int goalwidth, int goalheight) {

    int i, j;
    int h_div = 0, w_div = 0;
    w_div= (width - goalwidth) / 2;
    if (w_div % 2)
        w_div--;
    h_div= (height - goalheight) / 2;
    if (h_div % 2)
        h_div--;
    //u_div = (height-goalheight)/4;
    int src_y_length = width *height;
    int dst_y_length =goalwidth * goalheight;
    for (i = 0; i <goalheight; i++)
        for (j = 0; j <goalwidth; j++) {
            dst[i* goalwidth + j] = data[(i + h_div) * width + j + w_div];
        }
    int index = dst_y_length;
    int src_begin =src_y_length + h_div * width / 4;
    int src_u_length =src_y_length / 4;
    int dst_u_length =dst_y_length / 4;
    for (i = 0; i <goalheight / 2; i++)
        for (j = 0; j <goalwidth / 2; j++) {
            int p = src_begin + i *(width >> 1) + (w_div >> 1) + j;
            dst[index]= data[p];
            dst[dst_u_length+ index++] = data[p + src_u_length];
        }

    return 0;
}