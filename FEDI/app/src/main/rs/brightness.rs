#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float brightness_value;

uchar4 __attribute__((kernel)) brightness(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 pixel_float = rsUnpackColor8888(pixel_in);
    float3 out = pixel_float.rgb;
    out += brightness_value;
    out = Clamp01Float3(out);
    return rsPackColorTo8888(out);
}