#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

// wspolczynnik korekcji
float gamma;

// wzor przekształcenia, color - skladowa piksela
static float convert_to_gamma(float color){
    return pow(((float)(color)/1.0f), 1.0f/(gamma+0.01));
}

// kernel przekształcający piksel według korekcji gamma
uchar4 __attribute__((kernel)) color_shift(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pix.rgb;
    float3 new_rgb;
    new_rgb.r = 1.0f * convert_to_gamma(rgb.r);
    new_rgb.g = 1.0f * convert_to_gamma(rgb.g);
    new_rgb.b = 1.0f * convert_to_gamma(rgb.b);
    return rsPackColorTo8888(new_rgb);
}
