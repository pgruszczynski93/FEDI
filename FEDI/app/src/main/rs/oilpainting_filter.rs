#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;
uint32_t width, height, size;


uchar4 __attribute__((kernel)) oilpainting_filter(uchar4 pixel_in, uint32_t x, uint32_t y){
    float3 rgb;
    uint32_t new_pos, new_x, new_y;
    new_pos = rsRand(1, 10e4) % size;
    if((x+new_pos) < width)
        new_x = x+new_pos;
    else{
        new_x = ((int)x - new_pos) >= 0 ? (x-new_pos) : x;
    }
    if((y+new_pos) < height)
        new_y = y+new_pos;
    else{
        new_y = ((int)y - new_pos) >= 0 ? (y-new_pos) : y;
    }
    float4 full_pix = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, new_x, new_y));
    return rsPackColorTo8888(full_pix.rgb);
}

