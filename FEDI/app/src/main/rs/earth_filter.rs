#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

// kernel reprezentujący przekształcenie odpowiedzialne za wygenerowanie efektu ziemia
uchar4 __attribute__((kernel)) earth_filter(uchar4 pixel_in, uint32_t x, uint32_t y){

    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pixel.rgb, new_rgb;
    float gray_factor = dot(rgb, yuv_vector);
    new_rgb.r = (gray_factor + (70.0f/255.0f)) +  (gray_factor + (70.0f/255.0f) - 0.5f);
    new_rgb.g = (gray_factor + (65.0f/255.0f)) +  (gray_factor + (65.0f/255.0f) - 0.5f);
    new_rgb.b = (gray_factor + (75.0f/255.0f)) +  (gray_factor + (75.0f/255.0f) - 0.5f);
    return rsPackColorTo8888(new_rgb);
}
