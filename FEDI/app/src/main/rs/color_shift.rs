#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

int shift;

uchar4 __attribute__((kernel)) color_shift(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pix.rgb;
    if(!shift){
        rgb = rgb;
    }
    else if(shift==1){
        rgb.r = rgb.b;
        rgb.g = rgb.g;
        rgb.b = rgb.r;
    }
    else if(shift==2){
        rgb.r = rgb.g;
        rgb.g = rgb.b;
        rgb.b = rgb.r;
    }
    return rsPackColorTo8888(rgb);
}
