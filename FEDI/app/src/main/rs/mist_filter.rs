#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;
uint32_t width, height;


uchar4 __attribute__((kernel)) mist_filter(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix;
    uint32_t new_x, new_y, rand;
    rand = rsRand(1,150000);
    new_x = x + rand % 19;
    new_y = y + rand % 19;
    new_x = (new_x >= width) ? width - 1 : new_x;
    new_y = (new_y >= height) ? height - 1 : new_y;
    full_pix = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, new_x, new_y));
    return rsPackColorTo8888(full_pix);
}
