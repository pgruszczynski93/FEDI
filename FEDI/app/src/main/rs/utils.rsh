#ifndef __RS_UTILS__
#define __RS_UTILS__

static inline float ClampFloat(const float pixel, const float pixel_min, const float pixel_max) {
    if (pixel < pixel_max) {
        return ((pixel > pixel_min) ? pixel : pixel_min);
    }
    return pixel_max;
}

static inline float3 Clamp01Float3(const float3 pixel){
    pixel.r = ClampFloat(pixel.r, 0.0f, 1.0f);
    pixel.g = ClampFloat(pixel.g, 0.0f, 1.0f);
    pixel.b = ClampFloat(pixel.b, 0.0f, 1.0f);
    return pixel;
}

#endif