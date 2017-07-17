#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float treshold;

uchar4 __attribute__((kernel)) solarization(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pix.rgb;
    rgb.r = (rgb.r < treshold) ? (1.0 - rgb.r) : rgb.r;
    rgb.g = (rgb.g < treshold) ? (1.0 - rgb.g) : rgb.g;
    rgb.b = (rgb.b < treshold) ? (1.0 - rgb.b) : rgb.b;
    return rsPackColorTo8888(rgb);
}
