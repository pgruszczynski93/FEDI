#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;
uint32_t width, height;
float intensity;
float mix_1, mix_2;


uchar4 __attribute__((kernel)) soft_glow(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float4 curr_pix;
    float3 rgb;

    if(x < (width-1) && y < (height-1)){
        curr_pix = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, x, y));
        rgb = 1.0f - (1.0f - full_pix.rgb) * (1.0f - curr_pix.rgb);
    }
    else{
        rgb = full_pix.rgb;
    }
    return rsPackColorTo8888(rgb);
}
