//
// Created by zhangkai on 2018/6/7.
//
#include <jni.h>
#include <malloc.h>
#include <string.h>
#include <android/log.h>
#include "com_giskook_express_delivery_MainActivity.h"

static const char* kTAG = "__YUV";

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, kTAG, __VA_ARGS__))
#define LOGW(...) \
  ((void)__android_log_print(ANDROID_LOG_WARN, kTAG, __VA_ARGS__))
#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, kTAG, __VA_ARGS__))

unsigned char * CutBandYUV(unsigned char *pInBuf, uint32_t width, uint32_t height, int new_x, int new_y, int new_width, int new_height);

JNIEXPORT jstring JNICALL Java_com_giskook_express_1delivery_MainActivity_yuv_1string
  (JNIEnv *env , jobject thiz){
    LOGE("%s", "____MainActivity hello jni");
    return env->NewStringUTF("Hello from JNI LIBS! :)");
}

jbyteArray as_byte_array(JNIEnv * env, unsigned char* buf, int len) {
    jbyteArray array = env->NewByteArray (len);
    env->SetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));
    return array;
}

unsigned char* as_unsigned_char_array(JNIEnv * env, jbyteArray array) {
    int len = env->GetArrayLength (array);
    unsigned char* buf = new unsigned char[len];
    env->GetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));
    return buf;
}

JNIEXPORT jbyteArray JNICALL Java_com_giskook_express_1delivery_MainActivity_yuv_1crop
  (JNIEnv * env, jobject thiz, jbyteArray src, jint height, jint width, jint left, jint top, jint dheight, jint dwidth){

    jsize num_bytes = env->GetArrayLength(src);
    LOGE("%d ____MainActivity hello jni", num_bytes);

    //unsigned char* buf = (unsigned char *)malloc(num_bytes);
    unsigned char* buf = as_unsigned_char_array(env, src);
    unsigned char* dst = CutBandYUV(buf, width, height, 0, 0, 200, 200);

    return as_byte_array(env, dst, 60000);
 }
  /*
JNIEXPORT void JNICALL Java_com_giskook_express_1delivery_MainActivity_yuv_1crop
  (JNIEnv *env, jobject thiz, jbyteArray _src ,  jbyteArray _dst, jint _srch, jint _srcw, jint _dsth, jint _dstw){
        int isCopy = 0;
        //char * src = env->GetByteArrayElements(env, _src,&isCopy);
        //char * src = env->GetByteArrayElements(env, _src,&isCopy);

        //env->ReleaseByteArrayElements(env, _src, src, JNI_ABORT);
        //env->ReleaseByteArrayElements(env, _src, src, JNI_ABORT);
}*/

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

unsigned char * CutBandYUV(unsigned char *pInBuf, uint32_t width, uint32_t height, int new_x, int new_y, int new_width, int new_height)
{
    if(NULL == pInBuf || 0 == width || 0 == height)
        return 0;

    int length = new_width* new_height* 3 / 2;
    unsigned char *pOutBuf = (unsigned char *)malloc(length);
    if (NULL == pOutBuf)
    {
        printf("malloc new size memory failed! size=%d\n", length);
        return 0;
    }
    memset(pOutBuf , 0, length);

    unsigned char *pUBuf = pOutBuf + new_width * new_height;
    unsigned char *pVBuf = pOutBuf +new_width * new_height * 5 / 4;
    for(int x = 0; x < new_width; x++)
    {
        for (int y = 0; y < new_height; y++)  //每个循环写一列
        {
            *(pOutBuf + y * new_width + x) = *(pInBuf + (x + new_x) + width * (y + new_y));  //cope Y
            int ret = (y + new_y)%2;
            if (1 == (x + new_x)%2 && 1 == (y + new_y)%2)
            {
                long pix = width * height + (width>>1) * ((y + new_y)>>1) + (((x + new_x))>>1);
                *(pUBuf + (new_width/2)*(y/2) + x/2) = *(pInBuf + pix); //cope U

                pix += width * height / 4;
                *(pVBuf + (new_width/2)*(y/2) + x/2) = *(pInBuf + pix); //cope V
            }
        }
    }
    return pOutBuf;
}