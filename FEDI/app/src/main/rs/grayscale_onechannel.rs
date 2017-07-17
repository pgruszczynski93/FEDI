#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

int channel;

uchar4 __attribute__((kernel)) grayscale_desaturation(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pix.rgb;
    float gray;
    if(!channel){
        gray = rgb.r;
    }
    else if(channel==1){
        gray = rgb.g;
    }
    else if(channel==2){
        gray = rgb.b;
    }
    rgb.r = rgb.g = rgb.b = gray;
    return rsPackColorTo8888(rgb);
}