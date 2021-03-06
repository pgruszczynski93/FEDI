#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

uchar4 __attribute__((kernel)) invert(uchar4 pixel_in, int32_t x, int32_t y){
    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 rgb_pix = full_pixel.rgb;
    rgb_pix.r = 1.0f - rgb_pix.r;
    rgb_pix.g = 1.0f - rgb_pix.g;
    rgb_pix.b = 1.0f - rgb_pix.b;
    return rsPackColorTo8888(rgb_pix);
}