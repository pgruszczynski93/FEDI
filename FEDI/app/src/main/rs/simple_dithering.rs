#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

static const uint32_t DOTS = 10;
static const uint32_t dither_factors[100] = {
    167,200,230,216,181,94,72,193,242,232,
    36,52,222,167,200,181,126,210,94,72,
    232,153,111,36,52,167,200,230,216,181,
    94,72,193,242,232,36,52,222,167,200,
    181,126,210,94,72,232,153,111,36,52,
    167,200,230,216,181,94,72,193,242,232,
    36,52,222,167,200,181,126,210,94,72,
    232,153,111,36,52,167,200,230,216,181,
    94,72,193,242,232,36,52,222,167,200,
    181,126,210,94,72,232,153,111,36,52
};

uchar4 __attribute__((kernel)) simple_dithering(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pix.rgb;
    uint32_t space_x = x % DOTS;
    uint32_t space_y = y % DOTS;
    uint32_t index = space_y * DOTS + space_x;
    int32_t light_gray = (1.0f - rgb.b)*255;
    if (light_gray > dither_factors[index])
        rgb.r = rgb.g = rgb.b = 0;
    else
        rgb.r = rgb.g = rgb.b = 1;
    return rsPackColorTo8888(rgb);
}
