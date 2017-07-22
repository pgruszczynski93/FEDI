#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;
uint32_t width, height;

uchar4 __attribute__((kernel)) sharp(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    float3 rgb = full_pix.rgb;
    return rsPackColorTo8888(rgb);
}
