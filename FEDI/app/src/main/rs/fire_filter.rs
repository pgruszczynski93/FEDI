#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed


uchar4 __attribute__((kernel)) fire_filter(uchar4 pixel_in, uint32_t x, uint32_t y){

    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pixel.rgb, new_rgb;
    float gray_factor = dot(rgb, yuv_vector);
    new_rgb.r = gray_factor * 3;
    new_rgb.g = gray_factor;
    new_rgb.b = gray_factor/3;
    return rsPackColorTo8888(new_rgb);
}