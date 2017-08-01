#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;
int32_t width, height;
int direction;

void setup(){
    width = rsAllocationGetDimX(img_in);
    height = rsAllocationGetDimY(img_in);
}

uchar4 __attribute__((kernel)) flip_filter(uchar4 in, uint32_t x, uint32_t y){
    float4 rgba;
    if(!direction)
        rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, width-x, y));
    else if(direction == 1)
        rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, x, height - y));
    else if(direction == 2)
        rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, width-y, x));
    else if(direction == 3)
        rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, y, height - x));

    return rsPackColorTo8888(rgba.rgb);
}