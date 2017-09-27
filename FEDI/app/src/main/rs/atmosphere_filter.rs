#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

// kernel reprezentujący przekształcenie odpowiedzialne za wygenerowanie efektu atmosfera
uchar4 __attribute__((kernel)) atmosphere_filter(uchar4 pixel_in, uint32_t x, uint32_t y){

    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pixel.rgb, new_rgb;
    new_rgb.r = (rgb.g + rgb.b)/2;
    new_rgb.g = (rgb.r + rgb.b)/2;
    new_rgb.b = (rgb.r + rgb.g)/2;
    return rsPackColorTo8888(new_rgb);
}