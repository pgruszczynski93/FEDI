#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float treshold;
// progowanie po sredniej
uchar4 __attribute__((kernel)) brick(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pix.rgb;
    float avg = (rgb.r + rgb.g + rgb.b)/3.0f;
    avg = (avg >= treshold ? 1.0f : 0.0f);
    rgb.r = rgb.g = rgb.b = avg;
    return rsPackColorTo8888(rgb);
}
