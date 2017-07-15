#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed


uchar4 __attribute__((kernel)) ice_filter(uchar4 pixel_in, uint32_t x, uint32_t y){

    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pixel.rgb, new_rgb;

    new_rgb.r = fabs(rgb.r-rgb.g-rgb.b) * 1.5f;
    new_rgb.g = fabs(rgb.g - rgb.b - new_rgb.r) * 1.5;
    new_rgb.b = fabs(rgb.b - new_rgb.r - new_rgb.g) * 1.5;

    return rsPackColorTo8888(new_rgb);
}