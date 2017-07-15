#ifndef __RS_UTILS__
#define __RS_UTILS__
#pragma rs_fp_relaxed

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

#endif