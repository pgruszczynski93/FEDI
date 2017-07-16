#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

uchar4 __attribute__((kernel)) grayscale_desaturation(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pix.rgb;
    float grayscale = (max(max(rgb.r, rgb.g), rgb.b) + min(min(rgb.r, rgb.g), rgb.b))/2.0f;
    rgb.r = rgb.g = rgb.b = grayscale;
    return rsPackColorTo8888(rgb);
}