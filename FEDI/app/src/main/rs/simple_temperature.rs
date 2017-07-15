#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float temperature_value;

uchar4 __attribute__((kernel)) simple_temperature(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 rgb_pix = full_pixel.rgb;
    rgb_pix.r += temperature_value;
    rgb_pix.b -= temperature_value;
    rgb_pix = Clamp01Float3(rgb_pix);
    return rsPackColorTo8888(rgb_pix);
}