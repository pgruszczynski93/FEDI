#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;
uint32_t width, height;
float a,b,c;


uchar4 __attribute__((kernel)) equalize_histogram(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in), new_fullpix;
    float3 rgb;

    if(x < 100 && y < 100){
        rgb.r = a;
        rgb.g = b;
        rgb.b = c;
    }
    else{
        rgb = full_pix.rgb;
    }
    return rsPackColorTo8888(rgb);
}

void init(){

    a = 0.44;
    b = 0.21;
    c = 0.84;
}
