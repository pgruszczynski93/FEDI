#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float filter_strenght;

uchar4 __attribute__((kernel)) blacklight_filter(uchar4 pixel_in, uint32_t x, uint32_t y){

    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pixel.rgb, new_rgb;
    float gray_factor = dot(rgb, yuv_vector);
    new_rgb.r = fabs(rgb.r - gray_factor) * (filter_strenght+1);
    new_rgb.g = fabs(rgb.g - gray_factor) * (filter_strenght+1);
    new_rgb.b = fabs(rgb.b - gray_factor) * (filter_strenght+1);
    new_rgb = Clamp01Float3(new_rgb);
    return rsPackColorTo8888(new_rgb);
}

