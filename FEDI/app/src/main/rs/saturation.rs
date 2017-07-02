#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float saturation_value;

uchar4 __attribute__((kernel)) saturation(uchar4 pixel_in, uint32_t x, uint32_t y){
    uchar4 pixel_out;
    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 saturated_pixel = dot(full_pixel.rgb, yuv_vector);
    saturated_pixel = mix(saturated_pixel, full_pixel.rgb, saturation_value);
    pixel_out = rsPackColorTo8888(saturated_pixel);
    return pixel_out;
}
