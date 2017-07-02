#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float brightness_value;

uchar4 __attribute__((kernel)) brightness(uchar4 pixel_in, uint32_t x, uint32_t y){
    uchar4 pixel_out;
    float4 pixel_float = rsUnpackColor8888(pixel_in);

// zoptymalizować clampem
    pixel_float.r += brightness_value;
    pixel_float.g += brightness_value;
    pixel_float.b += brightness_value;

    pixel_float.r = (pixel_float.r > 1.0) ? 1.0 : pixel_float.r;
    pixel_float.r = (pixel_float.r < 0.0) ? 0.0 : pixel_float.r;
    pixel_float.g = (pixel_float.g > 1.0) ? 1.0 : pixel_float.g;
    pixel_float.g = (pixel_float.g < 0.0) ? 0.0 : pixel_float.g;
    pixel_float.b = (pixel_float.b > 1.0) ? 1.0 : pixel_float.b;
    pixel_float.b = (pixel_float.b < 0.0) ? 0.0 : pixel_float.b;

    pixel_out = rsPackColorTo8888(pixel_float.rgb);

    return pixel_out;
}