#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float treshold;

uchar4 __attribute__((kernel)) quantization(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pix.rgb;
    float R = (((int) (rgb.r * 255.0f * 0.003921569f * treshold)) / treshold) * 255.0f;
   	float G = (((int) (rgb.g * 255.0f * 0.003921569f * treshold)) / treshold) * 255.0f;
   	float B = (((int) (rgb.b * 255.0f * 0.003921569f * treshold)) / treshold) * 255.0f;
   	rgb.r = R/255.0f;
   	rgb.g = G/255.0f;
   	rgb.b = B/255.0f;

    return rsPackColorTo8888(rgb);
}

