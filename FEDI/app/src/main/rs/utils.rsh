#ifndef __RS_UTILS__
#define __RS_UTILS__
#pragma rs_fp_relaxed

static const int32_t MAX_CHANNEL_VALUE = 256;
const float3 yuv_vector = {0.299f, 0.587f, 0.114f};

static inline float ClampFloat(const float value, const float value_min, const float value_max) {
    if (value < value_max) {
        return ((value > value_min) ? value : value_min);
    }
    return value_max;
}

static inline float3 Clamp01Float3(const float3 pixel){
    pixel.r = ClampFloat(pixel.r, 0.0f, 1.0f);
    pixel.g = ClampFloat(pixel.g, 0.0f, 1.0f);
    pixel.b = ClampFloat(pixel.b, 0.0f, 1.0f);
    return pixel;
}

static inline float3 GrayScaleValue01(const float3 pixel){
    return dot(pixel, yuv_vector);
}

static inline uint8_t GrayScaleValue0255(const float3 pixel){
    return (uint8_t)(dot(pixel, yuv_vector) * (MAX_CHANNEL_VALUE - 1));
}

static float3 RGBtoYUV(const float3 rgb){
    float3 yuv;
    yuv.x = dot(rgb, yuv_vector);
    //wykonujac te dzialanie potem dodać
    yuv.y = ((0.492f * (rgb.b - yuv.x))+1)/2;
    yuv.z = ((0.877f * (rgb.r - yuv.x))+1)/2;
    return yuv;
}

static float3 YUVtoRGB(const float3 yuv){
    float3 rgb;
    rgb.r = yuv.x + 1.140f*yuv.z;
    rgb.g = yuv.x - 0.395f*yuv.y - 0.581f*yuv.z;
    rgb.b = yuv.x + 2.032f*yuv.y;
    return rgb;
}

static inline float GetY(const float3 rgb){
    return dot(rgb, yuv_vector);
}

static inline float GetU(const float3 rgb) {
    return  rgb.b * 0.436f - rgb.r * 0.147f - rgb.g * 0.289f;
}

static inline float GetV(const float3 rgb) {
    return rgb.r * 0.615f - rgb.g * 0.515f - rgb.b * 0.100f;
}

static void sort(float* tab, int32_t size){
    int32_t counter;
    do{
        counter = 0;
        for(int32_t i=0; i<size-1; i++){
            if(tab[i] > tab[i+1]){
                float tmp = tab[i];
                tab[i] = tab[i+1];
                tab[i+1] = tmp;
                ++counter;
            }
        }
    }
    while(counter != 0);
}

static void clear_array(float *tab, int32_t size){
    for(int32_t i=0; i<size; i++){
        tab[i] = 0.0f;
    }
}

#endif
